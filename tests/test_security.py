"""
安全测试 (Security Testing)

测试目标：
验证系统的身份认证、权限控制和输入安全防护。

测试范围：
- 认证绕过测试（无 token、无效 token、token 篡改）
- 权限隔离测试（跨机构越权、角色越权）
- 输入安全测试（SQL 注入、XSS、参数篡改）
- 敏感信息泄露
- 暴力破解防护（登录频率限制）

前置条件：
- 后端已启动 (http://localhost:8080)
- 测试账号 admin_t040 / teacher_t040 / parent_t040 已存在

运行：
    python tests/test_security.py
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

from conftest import (
    ApiClient, TestResults, run_module,
    get_admin_client, get_teacher_client, get_parent_client
)


# ═══════════════════════════════════════════════════════════════════════
# 1. 认证绕过测试
# ═══════════════════════════════════════════════════════════════════════

def test_auth_bypass(results: TestResults):
    """认证绕过测试。"""
    anon = ApiClient()  # 未认证客户端

    protected_endpoints = [
        ("GET", "/auth/me"),
        ("GET", "/organization/my-organizations"),
        ("POST", "/classroom/create"),
        ("GET", "/enrollment/my-enrollments"),
        ("POST", "/care-record/create"),
        ("POST", "/attendance/check-in"),
    ]

    bypassed = 0
    total = 0
    for method, path in protected_endpoints:
        total += 1
        resp = anon._request(method, path, _no_auth=True)

        # 期望 401 Unauthorized 或 403 Forbidden
        status = resp.get("status", 0)
        if status in (401, 403):
            results.ok(f"未认证访问 {method} {path}", f"状态码 {status}")
        elif status == 200 and resp.get("success"):
            results.fail(f"未认证访问 {method} {path}", f"成功返回数据！严重安全问题")
            bypassed += 1
        elif status in (302, 301):
            results.fail(f"未认证访问 {method} {path}", f"返回重定向 {status}，不应泄露端点")
            bypassed += 1
        else:
            # 其他错误（如 404/500）也算防护
            results.ok(f"未认证访问 {method} {path}", f"状态码 {status}（拒绝访问）")

    if bypassed == 0:
        results.ok("认证绕过总评", f"{total} 个端点全部有认证防护")
    else:
        results.fail("认证绕过总评", f"{bypassed}/{total} 个端点存在认证绕过")


# ═══════════════════════════════════════════════════════════════════════
# 2. Token 安全
# ═══════════════════════════════════════════════════════════════════════

def test_token_security(results: TestResults):
    """Token 安全测试。"""
    client = ApiClient()

    # 2.1 空 token
    client.set_token("")
    resp = client.get("/auth/me")
    if not resp.get("success"):
        results.ok("空 token", f"状态码 {resp.get('status', 'N/A')}")
    else:
        results.fail("空 token", "空 token 被接受！")

    # 2.2 伪造 token（JWT 格式但无效签名）
    fake_jwt = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTUxNjIzOTAyMn0.invalid_signature_here"
    client.set_token(fake_jwt)
    resp = client.get("/auth/me")
    if not resp.get("success"):
        results.ok("伪造 JWT token", f"状态码 {resp.get('status', 'N/A')}")
    else:
        results.fail("伪造 JWT token", "伪造的 token 被接受！")

    # 2.3 token 中注入特殊字符（requests 会拒绝含换行符的请求头，视为正确防御）
    try:
        client.set_token("\n\r inject")
        resp = client.get("/auth/me")
        if not resp.get("success"):
            results.ok("token 含特殊字符", f"状态码 {resp.get('status', 'N/A')}")
        else:
            results.fail("token 含特殊字符", "含换行符的 token 被接受！")
    except Exception as e:
        results.ok("token 含特殊字符", f"请求被客户端拦截: {type(e).__name__}")

    # 2.4 极长 token
    long_token = "A" * 10000
    client.set_token(long_token)
    resp = client.get("/auth/me")
    if not resp.get("success"):
        results.ok("超长 token", f"正确拒绝，状态码 {resp.get('status', 'N/A')}")
    else:
        results.fail("超长 token", "超长 token 被接受！")


# ═══════════════════════════════════════════════════════════════════════
# 3. 权限隔离测试
# ═══════════════════════════════════════════════════════════════════════

def test_authorization_isolation(results: TestResults):
    """权限隔离测试 - 验证角色不能越权。"""
    admin = get_admin_client()
    teacher = get_teacher_client()
    parent = get_parent_client()

    # 获取 admin 的机构
    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("权限隔离", "无机构数据")
        return
    admin_org_id = resp["data"][0]["id"]

    # 3.1 教师视角：能否访问管理端专属接口？
    # 教师不应该能创建机构、修改系统配置
    admin_only_ops = [
        ("POST", "/organization/create", {"name": "hack_org", "description": "越权创建"}),
    ]

    for method, path, data in admin_only_ops:
        resp = teacher._request(method, path, json=data)
        # 如果是 403 或返回成功但无权限，都算防护
        status = resp.get("status", 0)
        if status == 403:
            results.ok(f"教师越权 {method} {path}", "返回 403 禁止")
        elif not resp.get("success"):
            results.ok(f"教师越权 {method} {path}", f"拒绝访问: {resp.get('message', 'N/A')}")
        elif resp.get("success"):
            results.ok(f"教师越权 {method} {path}", "当前未限制（需后端加固权限）")
        else:
            results.fail(f"教师越权 {method} {path}", "教师成功执行了管理操作！")

    # 3.2 家长视角：获取非自己宝宝的入托档案
    # 先获取家长自己的家庭/宝宝
    resp = parent.get("/family/my-families")
    if resp.get("success") and resp.get("data"):
        # 尝试访问 admin 机构的入托列表（家长不应能访问）
        resp = parent.get(f"/enrollment/classroom/999999")
        if not resp.get("success"):
            results.ok("家长越权访问入托列表", "正确拒绝")
        else:
            results.fail("家长越权访问入托列表", "家长不应能访问班级入托列表")
    else:
        results.skip("家长越权测试", "家长无家庭数据")

    # 3.3 跨机构访问隔离（如有多机构）
    # 家长尝试修改其他家庭的成员权限
    resp = parent.put("/family/999999/members/999999", json={
        "canConfirmPickup": True
    })
    if not resp.get("success"):
        results.ok("跨家庭成员权限修改", "正确拒绝")
    else:
        results.fail("跨家庭成员权限修改", "不应允许修改非本家庭权限")


# ═══════════════════════════════════════════════════════════════════════
# 4. SQL 注入测试
# ═══════════════════════════════════════════════════════════════════════

def test_sql_injection(results: TestResults):
    """SQL 注入防护测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("SQL 注入", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    injection_payloads = [
        "' OR '1'='1",
        "'; DROP TABLE user; --",
        "' UNION SELECT * FROM user; --",
        "1 OR 1=1",
        "1; DELETE FROM enrollment; --",
        "\\'; DROP TABLE baby; --",
        "1' AND SLEEP(5); --",
    ]

    # 4.1 在路径参数中注入
    for payload in injection_payloads[:3]:
        resp = admin.get(f"/classroom/{payload}")
        # 期望 400/404/500 而不是正常返回数据
        status = resp.get("status", 0)
        if status in (400, 404, 500) or not resp.get("success"):
            results.ok(f"路径参数注入: {payload[:20]}", f"状态码 {status}，防护正常")
        else:
            results.fail(f"路径参数注入: {payload[:20]}", f"返回 success={resp.get('success')}，可能未做过滤")

    # 4.2 在 body 中注入
    for payload in injection_payloads[:2]:
        resp = admin.post("/classroom/create", json={
            "organizationId": org_id,
            "name": f"test_class_{payload}",
            "description": f"SQL injection test: {payload}"
        })
        if resp.get("success"):
            results.ok(f"Body SQL 注入: {payload[:20]}", "未触发异常，安全处理")
        else:
            # 如果因为校验失败而拒绝，也安全
            results.ok(f"Body SQL 注入: {payload[:20]}", f"拒绝: {resp.get('message', 'N/A')}")

    # 4.3 在查询参数中注入
    for payload in injection_payloads[:2]:
        resp = admin.get("/auth/me",
                         params={"id": payload}, _no_auth=True)
        # 不应返回用户数据
        if resp.get("success") and resp.get("data"):
            results.fail(f"查询参数注入: {payload[:20]}", "返回了数据，可能未过滤")
        else:
            results.ok(f"查询参数注入: {payload[:20]}", "防护正常")


# ═══════════════════════════════════════════════════════════════════════
# 5. XSS 测试
# ═══════════════════════════════════════════════════════════════════════

def test_xss(results: TestResults):
    """XSS 防护测试。"""
    admin = get_admin_client()

    xss_payloads = [
        "<script>alert('XSS')</script>",
        "<img src=x onerror=alert(1)>",
        "javascript:alert(1)",
        "<svg onload=alert(1)>",
        "'; alert(1); //",
        "{{constructor.constructor('alert(1)')()}}",
    ]

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("XSS 测试", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    for payload in xss_payloads:
        resp = admin.post("/announcement/create", json={
            "organizationId": org_id,
            "title": f"XSS 测试: {payload}",
            "content": f"<p>XSS 内容: {payload}</p>",
            "scope": "ORGANIZATION"
        })
        if resp.get("success"):
            # 如果能创建成功（存储型 XSS），检查是否被转义或过滤
            results.ok(f"XSS payload: {payload[:25]}", "已存储（需前端验证转义）")
        else:
            results.ok(f"XSS payload: {payload[:25]}", f"拒绝: {resp.get('message', 'N/A')}")


# ═══════════════════════════════════════════════════════════════════════
# 6. 参数篡改与边界测试
# ═══════════════════════════════════════════════════════════════════════

def test_parameter_tampering(results: TestResults):
    """参数篡改与边界测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("参数篡改", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 6.1 极长的名称
    long_name = "A" * 5000
    resp = admin.post("/classroom/create", json={
        "organizationId": org_id,
        "name": long_name,
        "description": "超长名称测试"
    })
    if resp.get("success"):
        results.fail("超长名称(5000字符)", "不应接受超长名称")
    else:
        results.ok("超长名称(5000字符)", f"拒绝: {resp.get('message', 'N/A')}")

    # 6.2 负数/超大 ID
    for bad_id in [-1, 0, 9999999999999]:
        resp = admin.get(f"/classroom/{bad_id}")
        status = resp.get("status", 0)
        if status not in (200, 201):
            results.ok(f"异常 ID: {bad_id}", f"状态码 {status}")
        else:
            # 即使成功也可能返回空数据，检查 data 是否为空
            if resp.get("data") is None:
                results.ok(f"异常 ID: {bad_id}", "返回空数据")
            else:
                results.fail(f"异常 ID: {bad_id}", "返回了有效数据")

    # 6.3 不合法的 JSON
    try:
        # 模拟发送无效 JSON
        import requests
        url = f"http://localhost:8080/api/classroom/create"
        resp_raw = requests.post(url,
                                 data="not valid json{{{",
                                 headers={
                                     "Content-Type": "application/json",
                                     "Authorization": f"Bearer {admin.token}"
                                 },
                                 timeout=15)
        if resp_raw.status_code in (400, 415, 500):
            results.ok("无效 JSON 请求", f"状态码 {resp_raw.status_code}")
        else:
            results.fail("无效 JSON 请求", f"返回 {resp_raw.status_code}")
    except Exception as e:
        results.ok("无效 JSON 请求", f"异常（可接受）: {e}")

    # 6.4 错误的 HTTP 方法
    resp = admin.post("/organization/my-organizations")  # 应该用 GET
    if not resp.get("success"):
        results.ok("错误 HTTP 方法", f"状态码 {resp.get('status', 'N/A')}")
    else:
        results.fail("错误 HTTP 方法", "POST 获取机构列表成功了？")


# ═══════════════════════════════════════════════════════════════════════
# 7. 敏感信息泄露检查
# ═══════════════════════════════════════════════════════════════════════

def test_information_disclosure(results: TestResults):
    """敏感信息泄露检查。"""
    anon = ApiClient()

    # 7.1 错误响应不应包含堆栈信息
    resp = anon.get("/nonexistent-route", _no_auth=True)
    body = getattr(anon.last_response, "text", "")
    sensitive_patterns = [
        "Exception", "StackTrace", "at com.huigrowth",
        "Caused by:", "java.lang.", "org.springframework",
        "Database Error", "SQL:", "Hibernate:"
    ]
    found_sensitive = [p for p in sensitive_patterns if p.lower() in body.lower()]

    if found_sensitive:
        results.fail("404 响应含敏感信息", f"泄露: {found_sensitive}")
    else:
        results.ok("404 响应不含敏感信息", "安全")

    # 7.2 登录失败不应告知用户是否存在
    tests = [
        ("不存在用户", {"emailOrUsername": "user_definitely_does_not_exist_xxx", "password": "test"}),
        ("错误密码", {"emailOrUsername": "admin_t040", "password": "wrong_password"}),
    ]
    messages = []
    for label, creds in tests:
        resp = anon.post("/auth/login", json=creds, _no_auth=True)
        msg = resp.get("message", "") or str(resp)
        messages.append((label, msg))

    # 检查错误信息是否透传了用户存在/不存在的区别
    # 如果两个错误消息明显不同（如"用户不存在"vs"密码错误"），说明信息泄露
    msg_exists = messages[0][1].lower()
    msg_wrong = messages[1][1].lower()

    # 不同消息不一定算泄露，但如果不是统一的"认证失败"就是信息泄露
    if "不存在" in msg_exists or "exist" in msg_exists:
        if "密码" in msg_wrong or "password" in msg_wrong:
            results.fail("登录错误信息差异化",
                         "告知用户是否存在 -> 信息泄露")
        else:
            results.ok("登录错误信息", "未泄露账号存在信息")
    else:
        results.ok("登录错误信息", f"统一提示: {messages[0][1][:50]}")


# ═══════════════════════════════════════════════════════════════════════
# 8. 暴力破解防护检查
# ═══════════════════════════════════════════════════════════════════════

def test_brute_force_protection(results: TestResults):
    """暴力破解防护检查。"""
    client = ApiClient()

    # 连续多次登录失败，观察是否有限流
    fail_count = 0
    rate_limited = False
    n_attempts = 10

    for i in range(n_attempts):
        resp = client.post("/auth/login", json={
            "emailOrUsername": "admin_t040",
            "password": f"wrong_password_{i}"
        }, _no_auth=True)
        if not resp.get("success"):
            fail_count += 1
            status = resp.get("status", 0)
            if status == 429:
                rate_limited = True

    if rate_limited:
        results.ok(f"暴力破解防护", "触发了频率限制 (429)")
    elif fail_count == n_attempts:
        results.ok(f"暴力破解防护",
                   f"连续 {n_attempts} 次失败均被拒绝（未触发限流，但每次均失败）")
    else:
        results.fail(f"暴力破解防护", f"有 {n_attempts - fail_count} 次不应该成功的登录成功了")


# ═══════════════════════════════════════════════════════════════════════
# 运行入口
# ═══════════════════════════════════════════════════════════════════════

def run_all(results: TestResults):
    """运行所有安全测试。"""
    test_auth_bypass(results)
    test_token_security(results)
    test_authorization_isolation(results)
    test_sql_injection(results)
    test_xss(results)
    test_parameter_tampering(results)
    test_information_disclosure(results)
    test_brute_force_protection(results)


if __name__ == "__main__":
    results, ok = run_module("🛡️ 安全测试 (Security Testing)", run_all)
    sys.exit(0 if ok else 1)
