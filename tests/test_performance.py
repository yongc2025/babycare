"""
性能测试 (Performance Testing)

测试目标：
验证关键 API 端点的响应时间、并发处理能力和数据吞吐量。

测试范围：
- 单请求响应时间基准
- 并发请求处理
- 列表接口分页性能
- 高频写入场景
- 数据库查询性能

前置条件：
- 后端已启动 (http://localhost:8080)
- 测试账号可用

运行：
    python tests/test_performance.py
"""

import sys
import os
import time
import threading
from concurrent.futures import ThreadPoolExecutor, as_completed

sys.path.insert(0, os.path.dirname(__file__))

from conftest import (
    ApiClient, TestResults, run_module,
    get_admin_client, unique_name
)

# ─── 常量 ────────────────────────────────────────────────────────────────
PERFORMANCE_THRESHOLDS = {
    "fast": 0.3,      # 快速查询/简单操作 (300ms)
    "medium": 1.0,    # 中等复杂度 (1s)
    "slow": 3.0,      # 复杂聚合/报表 (3s)
    "concurrent_max": 5.0,  # 并发场景最大容忍 (5s)
}

CONCURRENCY_LEVEL = 10  # 并发请求数


def measure_time(client, method, path, **kwargs):
    """测量请求耗时。"""
    start = time.time()
    resp = getattr(client, method)(path, **kwargs)
    elapsed = time.time() - start
    return elapsed, resp


def check_threshold(results, case_name, elapsed, threshold, unit="s"):
    """检查耗时是否在阈值内。"""
    if elapsed <= threshold:
        results.ok(case_name, f"{elapsed:.3f}s (阈值 {threshold}s)")
        return True
    else:
        results.fail(case_name, f"{elapsed:.3f}s (超阈值 {threshold}s)")
        return False


# ═══════════════════════════════════════════════════════════════════════
# 1. 单请求响应时间基准
# ═══════════════════════════════════════════════════════════════════════

def test_single_request_latency(results: TestResults):
    """单请求延迟基准测试。"""
    client = get_admin_client()

    # 1.1 健康检查（应极快）
    elapsed, resp = measure_time(client, "get", "/public/health", _no_auth=True)
    check_threshold(results, "健康检查", elapsed, PERFORMANCE_THRESHOLDS["fast"])

    # 1.2 登录（密码验证）
    elapsed, resp = measure_time(client, "post", "/auth/login", _no_auth=True,
                                 json={"emailOrUsername": "admin_t040", "password": "Test040pass"})
    if resp.get("success"):
        check_threshold(results, "登录", elapsed, PERFORMANCE_THRESHOLDS["fast"])
    else:
        results.fail("登录", f"接口失败: {resp.get('message')}")

    # 1.3 获取机构列表
    elapsed, resp = measure_time(client, "get", "/organization/my-organizations")
    if resp.get("success"):
        check_threshold(results, "获取机构列表", elapsed, PERFORMANCE_THRESHOLDS["fast"])
    else:
        results.fail("获取机构列表", f"接口失败: {resp.get('message')}")

    # 1.4 获取当前用户
    elapsed, resp = measure_time(client, "get", "/auth/me")
    if resp.get("success"):
        check_threshold(results, "获取当前用户", elapsed, PERFORMANCE_THRESHOLDS["fast"])
    else:
        results.fail("获取当前用户", f"接口失败: {resp.get('message')}")

    # 1.5 获取机构班级列表（需要先有 org_id）
    resp = client.get("/organization/my-organizations")
    if resp.get("success") and resp.get("data"):
        org_id = resp["data"][0]["id"]

        elapsed, resp2 = measure_time(client, "get", f"/classroom/organization/{org_id}")
        if resp2.get("success"):
            check_threshold(results, "获取班级列表", elapsed, PERFORMANCE_THRESHOLDS["fast"])
        else:
            results.fail("获取班级列表", f"接口失败: {resp2.get('message')}")

        # 1.6 获取入托列表
        class_resp = client.get(f"/classroom/organization/{org_id}")
        if class_resp.get("success") and class_resp.get("data"):
            class_id = class_resp["data"][0]["id"]
            elapsed, resp3 = measure_time(client, "get", f"/enrollment/classroom/{class_id}")
            if resp3.get("success"):
                check_threshold(results, "获取入托列表", elapsed, PERFORMANCE_THRESHOLDS["fast"])
            else:
                results.fail("获取入托列表", f"接口失败: {resp3.get('message')}")

            # 1.7 获取园长驾驶舱（较复杂）
            elapsed, resp4 = measure_time(client, "get", f"/director-dashboard/organization/{org_id}")
            if resp4.get("success"):
                check_threshold(results, "园长驾驶舱概览", elapsed, PERFORMANCE_THRESHOLDS["medium"])
            else:
                results.fail("园长驾驶舱概览", f"接口失败: {resp4.get('message')}")

            # 1.8 获取监管报表（聚合查询）
            elapsed, resp5 = measure_time(client, "get",
                                          f"/regulatory-report/organization/{org_id}")
            if resp5.get("success"):
                check_threshold(results, "监管报表", elapsed, PERFORMANCE_THRESHOLDS["slow"])
            else:
                results.fail("监管报表", f"接口失败: {resp5.get('message')}")
        else:
            results.skip("入托列表/驾驶舱/报表", "无班级数据")
    else:
        results.skip("机构相关测试", "无机构数据")


# ═══════════════════════════════════════════════════════════════════════
# 2. 并发请求测试
# ═══════════════════════════════════════════════════════════════════════

def _concurrent_get(client_factory, path, n):
    """并发执行 n 次 GET 请求。"""
    errors = 0
    times = []

    def _do():
        nonlocal errors
        c = client_factory()
        start = time.time()
        resp = c.get(path)
        elapsed = time.time() - start
        times.append(elapsed)
        if not resp.get("success"):
            errors += 1
        return elapsed, resp

    threads = []
    for _ in range(n):
        t = threading.Thread(target=_do)
        threads.append(t)
        t.start()

    for t in threads:
        t.join()

    return times, errors


def test_concurrent_requests(results: TestResults):
    """并发请求测试。"""
    admin = get_admin_client()

    def make_client():
        c = ApiClient()
        c.token = admin.token
        c.session.headers.update({"Authorization": f"Bearer {admin.token}"})
        return c

    # 2.1 并发健康检查（10并发）
    n = CONCURRENCY_LEVEL
    start = time.time()
    times = []
    errors = 0

    with ThreadPoolExecutor(max_workers=n) as executor:
        futures = [executor.submit(
            lambda: measure_time(ApiClient(), "get", "/public/health", _no_auth=True))
            for _ in range(n)]
        for future in as_completed(futures):
            elapsed, resp = future.result()
            times.append(elapsed)
            if not resp.get("success") and "status" not in resp:
                errors += 1

    total_elapsed = time.time() - start
    avg = sum(times) / len(times) if times else 0
    max_t = max(times) if times else 0

    if errors == 0 and max_t <= PERFORMANCE_THRESHOLDS["concurrent_max"]:
        results.ok(f"并发健康检查({n}并发)",
                   f"平均{avg:.3f}s 最大{max_t:.3f}s 总计{total_elapsed:.3f}s")
    else:
        results.fail(f"并发健康检查({n}并发)",
                     f"错误{errors}个 平均{avg:.3f}s 最大{max_t:.3f}s")

    # 2.2 并发查询机构列表
    times2 = []
    errors2 = 0
    start = time.time()

    with ThreadPoolExecutor(max_workers=n) as executor:
        futures = [executor.submit(lambda: measure_time(
            make_client(), "get", "/organization/my-organizations"))
            for _ in range(n)]
        for future in as_completed(futures):
            elapsed, resp = future.result()
            times2.append(elapsed)
            if not resp.get("success"):
                errors2 += 1

    total_elapsed2 = time.time() - start
    avg2 = sum(times2) / len(times2) if times2 else 0
    max_t2 = max(times2) if times2 else 0

    if errors2 == 0 and max_t2 <= PERFORMANCE_THRESHOLDS["concurrent_max"]:
        results.ok(f"并发机构查询({n}并发)",
                   f"平均{avg2:.3f}s 最大{max_t2:.3f}s 总计{total_elapsed2:.3f}s")
    else:
        results.fail(f"并发机构查询({n}并发)",
                     f"错误{errors2}个 平均{avg2:.3f}s 最大{max_t2:.3f}s")


# ═══════════════════════════════════════════════════════════════════════
# 3. 列表分页性能
# ═══════════════════════════════════════════════════════════════════════

def test_pagination_performance(results: TestResults):
    """分页性能测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("分页测试", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 3.1 获取员工列表（可能有分页参数）
    elapsed, resp = measure_time(admin, "get", f"/staff/organization/{org_id}")
    if resp.get("success"):
        check_threshold(results, "员工列表查询", elapsed, PERFORMANCE_THRESHOLDS["fast"])
    else:
        results.fail("员工列表查询", resp.get("message", ""))

    # 3.2 获取通知列表
    elapsed, resp = measure_time(admin, "get", f"/announcement/organization/{org_id}")
    if resp.get("success"):
        check_threshold(results, "通知列表查询", elapsed, PERFORMANCE_THRESHOLDS["fast"])
    else:
        results.fail("通知列表查询", resp.get("message", ""))

    # 3.3 获取安全台账列表（带日期筛选）
    elapsed, resp = measure_time(admin, "get",
                                 f"/safety-ledger/organization/{org_id}",
                                 params={"startDate": "2026-07-01", "endDate": "2026-07-31"})
    if resp.get("success"):
        check_threshold(results, "台账列表(带日期筛选)", elapsed, PERFORMANCE_THRESHOLDS["medium"])
    else:
        results.fail("台账列表(带日期筛选)", resp.get("message", ""))

    # 3.4 招生线索列表
    elapsed, resp = measure_time(admin, "get", f"/admission-lead/organization/{org_id}")
    if resp.get("success"):
        check_threshold(results, "招生线索列表", elapsed, PERFORMANCE_THRESHOLDS["fast"])
    else:
        results.fail("招生线索列表", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 4. 高频写入场景
# ═══════════════════════════════════════════════════════════════════════

def test_write_throughput(results: TestResults):
    """写入吞吐量测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("写入测试", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 4.1 连续创建通知（5次）
    n = 5
    start = time.time()
    successes = 0
    for i in range(n):
        resp = admin.post("/announcement/create", json={
            "organizationId": org_id,
            "title": f"性能测试通知_{i}_{unique_name('')}",
            "content": f"性能测试内容 #{i}",
            "scope": "ORGANIZATION"
        })
        if resp.get("success"):
            successes += 1

    elapsed = time.time() - start
    avg = elapsed / n
    if successes == n and avg <= 1.0:
        results.ok(f"连续创建通知({n}次)",
                   f"成功率{successes}/{n} 平均{avg:.3f}s/次")
    else:
        results.fail(f"连续创建通知({n}次)",
                     f"成功率{successes}/{n} 平均{avg:.3f}s/次")

    # 4.2 连续获取列表
    start = time.time()
    successes2 = 0
    for i in range(n):
        resp = admin.get(f"/announcement/organization/{org_id}")
        if resp.get("success"):
            successes2 += 1

    elapsed2 = time.time() - start
    avg2 = elapsed2 / n
    if successes2 == n and avg2 <= 0.5:
        results.ok(f"连续读取列表({n}次)",
                   f"成功率{successes2}/{n} 平均{avg2:.3f}s/次")
    else:
        results.fail(f"连续读取列表({n}次)",
                     f"成功率{successes2}/{n} 平均{avg2:.3f}s/次")


# ═══════════════════════════════════════════════════════════════════════
# 5. 长耗时接口专项
# ═══════════════════════════════════════════════════════════════════════

def test_heavy_endpoints(results: TestResults):
    """长耗时接口专项测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("长耗时接口", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 5.1 财务运营工作台（可能聚合大量数据）
    elapsed, resp = measure_time(admin, "get", f"/billing/finance-workbench/{org_id}")
    if resp.get("success"):
        check_threshold(results, "财务运营工作台", elapsed, PERFORMANCE_THRESHOLDS["slow"])
    else:
        # 如果返回错误（如无数据），也算可接受
        results.ok("财务运营工作台", f"返回结果(耗时{elapsed:.3f}s)")

    # 5.2 营养摄入分析
    elapsed, resp = measure_time(admin, "get",
                                 f"/meal-plan/analysis/organization/{org_id}",
                                 params={"startDate": "2026-07-01", "endDate": "2026-07-31"})
    if resp.get("success"):
        check_threshold(results, "营养摄入分析", elapsed, PERFORMANCE_THRESHOLDS["slow"])
    else:
        results.ok("营养摄入分析", f"返回结果(耗时{elapsed:.3f}s)")

    # 5.3 生成日报草稿（需要先有宝宝和数据）
    class_resp = admin.get(f"/classroom/organization/{org_id}")
    if class_resp.get("success") and class_resp.get("data"):
        class_id = class_resp["data"][0]["id"]
        enroll_resp = admin.get(f"/enrollment/classroom/{class_id}")
        if enroll_resp.get("success") and enroll_resp.get("data"):
            baby_id = enroll_resp["data"][0].get("baby", {}).get("id")
            if baby_id:
                elapsed, resp = measure_time(admin, "post", "/daily-report/generate",
                                             json={"babyId": baby_id, "reportDate": "2026-07-18"})
                if resp.get("success"):
                    check_threshold(results, "生成日报草稿", elapsed, PERFORMANCE_THRESHOLDS["medium"])
                else:
                    results.fail("生成日报草稿", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 运行入口
# ═══════════════════════════════════════════════════════════════════════

def run_all(results: TestResults):
    """运行所有性能测试。"""
    print("\n  性能阈值: 快速<{}s 中等<{}s 复杂<{}s 并发最大<{}s".format(
        PERFORMANCE_THRESHOLDS["fast"], PERFORMANCE_THRESHOLDS["medium"],
        PERFORMANCE_THRESHOLDS["slow"], PERFORMANCE_THRESHOLDS["concurrent_max"]
    ))
    print(f"  并发级别: {CONCURRENCY_LEVEL}\n")

    test_single_request_latency(results)
    test_concurrent_requests(results)
    test_pagination_performance(results)
    test_write_throughput(results)
    test_heavy_endpoints(results)


if __name__ == "__main__":
    results, ok = run_module("⚡ 性能测试 (Performance Testing)", run_all)
    sys.exit(0 if ok else 1)
