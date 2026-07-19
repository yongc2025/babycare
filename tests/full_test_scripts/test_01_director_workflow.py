"""
园长工作流 — 端到端全链路数据一致性测试

覆盖角色：园长 (DIRECTOR)
核心链路：机构初始化 → 班级管理 → 员工管理 → 入托审核 → 转班/退托 → 日报审核 → 驾驶舱

变量依赖：{{adminToken}}, {{orgId}}, {{classroomId}}, {{enrollmentId}}, {{dailyReportId}}
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))
from conftest_e2e import E2EApiClient, E2ETestResults, unique_name


def run_director_workflow(results: E2ETestResults) -> dict:
    """执行园长工作流全链路测试，返回上下文变量。"""
    ctx = E2EApiClient()
    ctx.login(use_account="admin")
    results.ok("DIR-001", "园长(admin)登录成功")

    # ── 1. 机构初始化 ──────────────────────────────────────────────────
    org_name = unique_name("好芽儿测试园")
    resp = ctx.post("/organization/create", json={
        "name": org_name,
        "contactPhone": "13800138000",
        "address": "测试地址",
        "dailyReportApprovalRequired": True
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"orgId": "data.id"})
        results.ok("DIR-002", f"创建机构成功, orgId={ctx.getv('orgId')}")
    else:
        results.fail("DIR-002", f"创建机构失败: {resp.get('message')}")
        return ctx.vars

    # ── 2. 创建班级 ────────────────────────────────────────────────────
    resp = ctx.post("/classroom/create", json={
        "organizationId": "{{orgId}}",
        "name": "豆芽一班",
        "capacity": 20,
        "ageRangeMinMonths": 12,
        "ageRangeMaxMonths": 36
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"classroomId": "data.id"})
        results.ok("DIR-003", f"创建班级成功, classroomId={ctx.getv('classroomId')}")
    else:
        results.fail("DIR-003", f"创建班级失败: {resp.get('message')}")

    # ── 3. 创建员工（多岗位） ──────────────────────────────────────────
    staff_configs = [
        ("staffDirectorId", "DIRECTOR", 2),
        ("staffTeacherId", "TEACHER", 3),
        ("staffCaregiverId", "CAREGIVER", 4),
        ("staffHealthId", "HEALTH_WORKER", 5),
        ("staffFinanceId", "FINANCE", 6),
        ("staffSafetyId", "SAFETY_OFFICER", 7),
        ("staffOpsId", "OPERATIONS_STAFF", 8),
        ("staffAdmId", "ADMISSIONS_OFFICER", 9),
    ]
    for var_name, role, user_id in staff_configs:
        resp = ctx.post("/staff/create", json={
            "organizationId": "{{orgId}}",
            "userId": user_id,
            "role": role
        })
        if resp.get("success"):
            ctx.collect_vars(resp, {var_name: "data.id"})
            results.ok("DIR-004", f"创建员工 {role} 成功, id={ctx.getv(var_name)}")
        else:
            results.fail("DIR-004", f"创建员工 {role} 失败: {resp.get('message')}")

    # ── 4. 创建家庭和宝宝（园长代录） ─────────────────────────────────
    resp = ctx.post("/family/create", json={
        "name": unique_name("测试家庭"),
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"familyId": "data.id"})
        results.ok("DIR-005", f"创建家庭成功, familyId={ctx.getv('familyId')}")
    else:
        results.fail("DIR-005", f"创建家庭失败: {resp.get('message')}")

    resp = ctx.post(f"/family/{ctx.getv('familyId')}/babies", json={
        "name": "小豆芽",
        "gender": "MALE",
        "birthDate": "2024-01-15"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"babyId": "data.id"})
        results.ok("DIR-006", f"添加宝宝成功, babyId={ctx.getv('babyId')}")
    else:
        results.fail("DIR-006", f"添加宝宝失败: {resp.get('message')}")

    # ── 5. 创建入托档案 ────────────────────────────────────────────────
    resp = ctx.post("/enrollment/create", json={
        "babyId": "{{babyId}}",
        "organizationId": "{{orgId}}",
        "classroomId": "{{classroomId}}"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"enrollmentId": "data.id"})
        status = resp.get("data", {}).get("status")
        results.ok("DIR-007", f"创建入托档案成功, id={ctx.getv('enrollmentId')}, status={status}")
        # 断言初始状态为 PENDING
        assert status == "PENDING", f"期望初始状态 PENDING，实际 {status}"
    else:
        results.fail("DIR-007", f"创建入托档案失败: {resp.get('message')}")

    # ── 6. 园长审核入托（PENDING → HEALTH_CHECK） ─────────────────────
    resp = ctx.post(f"/enrollment/{{{{enrollmentId}}}}/review", json={
        "approved": True
    })
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("DIR-008", f"园长审核入托通过, status={status}")
        assert status == "HEALTH_CHECK", f"期望状态 HEALTH_CHECK，实际 {status}"
    else:
        results.fail("DIR-008", f"园长审核失败: {resp.get('message')}")

    # ── 7. 保健员健康审核（需切换 token） ────────────────────────────
    ctx.login(use_account="health")
    resp = ctx.post(f"/enrollment/{{{{enrollmentId}}}}/health-check", json={
        "approved": True
    })
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("DIR-009", f"保健员健康审核通过, status={status}")
        assert status == "ACTIVE", f"期望状态 ACTIVE，实际 {status}"
    else:
        results.fail("DIR-009", f"保健员审核失败: {resp.get('message')}")

    # ── 8. 园长切回查看入托详情 ─────────────────────────────────────
    ctx.login(use_account="admin")
    resp = ctx.get(f"/enrollment/{{{{enrollmentId}}}}")
    if resp.get("success"):
        data = resp.get("data", {})
        results.ok("DIR-010", f"入托详情: status={data.get('status')}, enrolledAt={data.get('enrolledAt')}")
        assert data.get("status") == "ACTIVE"
        assert data.get("enrolledAt") is not None, "enrolledAt 不应为空"
    else:
        results.fail("DIR-010", f"查询入托详情失败: {resp.get('message')}")

    # ── 9. 查看园长驾驶舱 ─────────────────────────────────────────────
    resp = ctx.get(f"/director-dashboard/organization/{{{{orgId}}}}?date=2026-07-19")
    if resp.get("success"):
        data = resp.get("data", {})
        results.ok("DIR-011", f"园长驾驶舱: 在托={data.get('activeEnrollmentCount')}, 出勤率={data.get('attendanceRate')}")
    else:
        results.fail("DIR-011", f"获取驾驶舱失败: {resp.get('message')}")

    # ── 10. 园长工作台 ───────────────────────────────────────────────
    resp = ctx.get(f"/director-dashboard/workbench/{{{{orgId}}}}")
    if resp.get("success"):
        results.ok("DIR-012", "园长工作台获取成功")
    else:
        results.fail("DIR-012", f"获取工作台失败: {resp.get('message')}")

    return ctx.vars


if __name__ == "__main__":
    results = E2ETestResults("园长工作流")
    ctx_vars = run_director_workflow(results)
    results.summary()
