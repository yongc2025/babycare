"""
家长工作流 — 端到端全链路数据一致性测试

覆盖角色：家长 (PARENT)、长辈 (ELDER)
核心链路：查看日报 → 查看考勤 → 查看照护 → 授权接送人 → 委托接送 → 长辈模式
         → 查看账单 → 成长记录 → 教育计划

变量依赖：{{enrollmentId}}, {{babyId}}, {{familyId}}, {{dailyReportId}}, {{billId}}
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))
from conftest_e2e import E2EApiClient, E2ETestResults, unique_name


def run_parent_workflow(results: E2ETestResults, env: dict) -> dict:
    """执行家长工作流全链路测试。"""
    ctx = E2EApiClient()
    ctx.vars.update(env)

    # ── 1. 家长登录 ─────────────────────────────────────────────────
    ctx.login(use_account="parent")
    results.ok("PRT-001", "家长登录成功")

    today = "2026-07-19"

    # ── 2. 查看已发布日报 ───────────────────────────────────────────
    resp = ctx.get(f"/daily-report/baby/{{{{babyId}}}}?date={today}")
    if resp.get("success"):
        report = resp.get("data", {})
        if report and report.get("status") == "PUBLISHED":
            results.ok("PRT-002", "家长查看已发布日报成功")
            ctx.collect_vars(resp, {"dailyReportId": "data.id"})
        else:
            results.skip("PRT-002", "日报未发布或不存在")
    else:
        results.fail("PRT-002", f"查看日报失败: {resp.get('message')}")

    # ── 3. 查看宝宝考勤 ─────────────────────────────────────────────
    resp = ctx.get(f"/attendance/baby/{{{{babyId}}}}?startDate={today}&endDate={today}")
    if resp.get("success"):
        results.ok("PRT-003", "查看宝宝考勤成功")
    else:
        results.fail("PRT-003", f"查看考勤失败: {resp.get('message')}")

    # ── 4. 查看宝宝照护记录 ─────────────────────────────────────────
    resp = ctx.get(f"/care-record/enrollment/{{{{enrollmentId}}}}?date={today}")
    if resp.get("success"):
        records = resp.get("data", [])
        results.ok("PRT-004", f"查看照护记录成功, 共{len(records)}条")
    else:
        results.fail("PRT-004", f"查看照护记录失败: {resp.get('message')}")

    # ── 5. 查看宝宝健康观察 ─────────────────────────────────────────
    resp = ctx.get(f"/health-observation/enrollment/{{{{enrollmentId}}}}?date={today}")
    if resp.get("success"):
        results.ok("PRT-005", "查看健康观察成功")
    else:
        results.fail("PRT-005", f"查看健康观察失败: {resp.get('message')}")

    # ── 6. 创建授权接送人 ───────────────────────────────────────────
    resp = ctx.post("/pickup/person/create", json={
        "enrollmentId": "{{enrollmentId}}",
        "name": "爷爷",
        "relationship": "祖父",
        "phone": "13700137000"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"pickupPersonId": "data.id"})
        results.ok("PRT-006", f"创建授权接送人成功, id={ctx.getv('pickupPersonId')}")
    else:
        results.fail("PRT-006", f"创建授权接送人失败: {resp.get('message')}")

    # ── 7. 查看授权接送人列表 ───────────────────────────────────────
    resp = ctx.get(f"/pickup/person/baby/{{{{babyId}}}}")
    if resp.get("success"):
        persons = resp.get("data", [])
        if any(p.get("id") == ctx.getv("pickupPersonId") for p in persons):
            results.ok("PRT-007", "授权接送人列表包含新创建的接送人")
        else:
            results.fail("PRT-007", "未找到新创建接送人")
    else:
        results.fail("PRT-007", f"查询接送人失败: {resp.get('message')}")

    # ── 8. 创建委托接送 ─────────────────────────────────────────────
    resp = ctx.post("/pickup/delegation/create", json={
        "enrollmentId": "{{enrollmentId}}",
        "pickupDate": today,
        "pickupPersonName": "临时阿姨",
        "pickupPhone": "13600136000",
        "reason": "加班"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"delegationId": "data.id"})
        results.ok("PRT-008", f"创建委托接送成功, id={ctx.getv('delegationId')}")
    else:
        results.fail("PRT-008", f"创建委托接送失败: {resp.get('message')}")

    # 需要教师/园长审核委托接送
    ctx.login(use_account="teacher")
    resp = ctx.post(f"/pickup/delegation/{{{{delegationId}}}}/approve")
    if resp.get("success"):
        data = resp.get("data", {})
        results.ok("PRT-009", f"委托接送审核通过, pickupCode={data.get('pickupCode')}")
    else:
        results.fail("PRT-009", f"审核委托失败: {resp.get('message')}")

    # ── 9. 家长查看自己的申请列表 ──────────────────────────────────
    ctx.login(use_account="parent")
    resp = ctx.get("/parent/my-applications")
    if resp.get("success"):
        results.ok("PRT-010", f"查看我的申请列表成功, 共{len(resp.get('data', []))}条")
    else:
        results.fail("PRT-010", f"查看申请列表失败: {resp.get('message')}")

    # ── 10. 查看我的账单 ───────────────────────────────────────────
    resp = ctx.get("/parent/my-bills")
    if resp.get("success"):
        results.ok("PRT-011", "查看我的账单成功")
    else:
        results.fail("PRT-011", f"查看账单失败: {resp.get('message')}")

    # ── 11. 查看通知公告 ───────────────────────────────────────────
    if ctx.getv("announcementId"):
        resp = ctx.post(f"/announcement/{{{{announcementId}}}}/read")
        if resp.get("success"):
            results.ok("PRT-012", "通知标记已读成功")
        else:
            results.fail("PRT-012", f"标记已读失败: {resp.get('message')}")

    # ── 12. 成长记录（家长端） ─────────────────────────────────────
    resp = ctx.post("/growth-record/create", json={
        "babyId": "{{babyId}}",
        "type": "DIARY",
        "title": "宝宝今天会叫妈妈了",
        "content": "今天宝宝第一次清晰地叫了'妈妈'，太开心了！"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"growthRecordId": "data.id"})
        results.ok("PRT-013", "创建成长记录成功")
    else:
        results.fail("PRT-013", f"创建成长记录失败: {resp.get('message')}")

    # ── 13. 长辈模式（ELDER） ──────────────────────────────────────
    ctx.login(use_account="elder")
    results.ok("PRT-014", "长辈登录成功")

    resp = ctx.get(f"/daily-report/baby/{{{{babyId}}}}?date={today}")
    if resp.get("success"):
        results.ok("PRT-015", "长辈查看日报成功")
    else:
        results.fail("PRT-015", f"长辈查看日报失败: {resp.get('message')}")

    # 验证长辈只读：尝试写操作应失败
    resp = ctx.post("/attendance/check-in", json={
        "enrollmentId": "{{enrollmentId}}",
        "attendanceDate": today
    })
    if not resp.get("success") and resp.get("status") == 403:
        results.ok("PRT-016", "长辈签到被正确拒绝(403)")
    else:
        results.fail("PRT-016", f"长辈签到未被拒绝: {resp}")

    # ── 14. 家长查看发展评估 ──────────────────────────────────────
    ctx.login(use_account="parent")
    resp = ctx.get(f"/child-development-assessment/baby/{{{{babyId}}}}")
    if resp.get("success"):
        results.ok("PRT-017", "查看发展评估列表成功")
    else:
        results.fail("PRT-017", f"查看发展评估失败: {resp.get('message')}")

    return ctx.vars


if __name__ == "__main__":
    results = E2ETestResults("家长工作流")
    from test_01_director_workflow import run_director_workflow
    from test_02_teacher_caregiver_workflow import run_teacher_workflow
    env = run_director_workflow(results)
    env = run_teacher_workflow(results, env)
    run_parent_workflow(results, env)
    results.summary()
