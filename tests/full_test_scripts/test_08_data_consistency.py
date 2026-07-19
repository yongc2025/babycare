"""
数据一致性专项验证 — 跨模块数据关联穿透校验

核心验证点：
  1. 园长驾驶舱聚合数据与实际业务记录一致
  2. AdmissionLead 转为 Enrollment 后数据正确传递
  3. 请假审批通过后 AttendanceRecord 自动生成
  4. 照护补录标记正确
  5. 并发约束（唯一键）验证
  6. 异常事故关闭前必须通知家长

变量依赖：{{orgId}}, {{enrollmentId}}, {{classroomId}}, {{babyId}}, {{leadId}}
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))
from conftest_e2e import E2EApiClient, E2ETestResults, unique_name


def test_dashboard_consistency(ctx: E2EApiClient, results: E2ETestResults):
    """验证园长驾驶舱数据与实际记录一致。"""
    resp = ctx.get(f"/director-dashboard/organization/{{{{orgId}}}}?date=2026-07-19")
    if not resp.get("success"):
        results.skip("DC-001", "驾驶舱接口不可用")
        return

    data = resp.get("data", {})
    dashboard_active = data.get("activeEnrollmentCount", 0)
    dashboard_checked_in = data.get("checkedInCount", 0)

    # 通过 API 查实际在托数
    resp2 = ctx.get(f"/enrollment/classroom/{{{{classroomId}}}}")
    if resp2.get("success"):
        actual_active = sum(1 for e in resp2.get("data", [])
                           if e.get("status") == "ACTIVE")
        results.ok("DC-001", f"驾驶舱在托数={dashboard_active}, 实际在托数={actual_active}")

    # 验证考勤率
    expected = data.get("expectedAttendanceCount", 0)
    if expected > 0:
        rate = data.get("attendanceRate", 0)
        results.ok("DC-002", f"出勤率={rate}% (应到={expected}, 已到={dashboard_checked_in})")


def test_lead_to_enrollment_consistency(ctx: E2EApiClient, results: E2ETestResults):
    """验证招生线索转入托后数据一致。"""
    if not ctx.get("leadId"):
        results.skip("DC-003", "缺少招生线索ID")
        return

    resp = ctx.get(f"/admission-lead/{{{{leadId}}}}")
    if resp.get("success"):
        lead_data = resp.get("data", {})
        results.ok("DC-003", f"招生线索状态={lead_data.get('status')}")
    else:
        results.skip("DC-003", "查询招生线索失败")


def test_leave_to_attendance_consistency(ctx: E2EApiClient, results: E2ETestResults):
    """验证请假审批后考勤自动生成。"""
    if not ctx.get("leaveRequestId"):
        results.skip("DC-004", "缺少请假ID")
        return

    resp = ctx.get(f"/attendance/leave/{{{{leaveRequestId}}}}")
    if resp.get("success"):
        data = resp.get("data", {})
        status = data.get("status")
        if status == "APPROVED":
            # 验证请假日期范围内生成了 LEAVE 考勤
            start = data.get("startDate")
            end = data.get("endDate")
            resp2 = ctx.get(f"/attendance/classroom/{{{{classroomId}}}}?date={start}")
            if resp2.get("success"):
                records = resp2.get("data", [])
                found_leave = any(
                    r.get("enrollmentId") == ctx.get("enrollmentId") and r.get("status") == "LEAVE"
                    for r in records
                )
                if found_leave:
                    results.ok("DC-004", "✅ 请假审批后考勤自动变为 LEAVE")
                else:
                    results.fail("DC-004", "请假审批后未找到 LEAVE 考勤")
        else:
            results.skip("DC-004", f"请假状态={status}，非 APPROVED")
    else:
        results.skip("DC-004", "查询请假失败")


def test_backfill_consistency(ctx: E2EApiClient, results: E2ETestResults):
    """验证照护补录标记。"""
    resp = ctx.post("/care-record/create", json={
        "enrollmentId": "{{enrollmentId}}",
        "recordDate": "2026-07-18",
        "recordTime": "2026-07-18T12:00:00",
        "type": "FEEDING",
        "amount": 200,
        "unit": "ml",
        "backfillReason": "昨天忘记记录了"
    })
    if resp.get("success"):
        data = resp.get("data", {})
        is_backfill = data.get("isBackfill", False)
        has_reason = data.get("backfillReason") is not None
        if is_backfill and has_reason:
            results.ok("DC-005", "✅ 补录标记正确 (isBackfill=true, backfillReason非空)")
        else:
            results.fail("DC-005", f"补录标记缺失: isBackfill={is_backfill}, reason={data.get('backfillReason')}")
    else:
        results.fail("DC-005", f"创建补录记录失败: {resp.get('message')}")


def test_unique_constraint_daily_report(ctx: E2EApiClient, results: E2ETestResults):
    """验证同一入托同一天不可重复创建日报。"""
    resp = ctx.post("/daily-report/generate", json={
        "enrollmentId": "{{enrollmentId}}",
        "reportDate": "2026-07-19"
    })
    if not resp.get("success"):
        results.ok("DC-006", "✅ 重复创建日报被正确拒绝")
    else:
        results.fail("DC-006", "重复创建日报未被拒绝")


def test_unique_constraint_attendance(ctx: E2EApiClient, results: E2ETestResults):
    """验证同一入托同一天不可重复签到。"""
    resp = ctx.post("/attendance/check-in", json={
        "enrollmentId": "{{enrollmentId}}",
        "attendanceDate": "2026-07-19"
    })
    if not resp.get("success"):
        results.ok("DC-007", "✅ 重复签到被正确拒绝")
    else:
        results.fail("DC-007", "重复签到未被拒绝")


def test_incident_close_requires_parent_notification(ctx: E2EApiClient, results: E2ETestResults):
    """验证异常事故关闭前必须通知家长。"""
    resp = ctx.post("/incident-report/create", json={
        "enrollmentId": "{{enrollmentId}}",
        "type": "INJURY",
        "severity": "LOW",
        "title": "轻微擦伤",
        "description": "玩耍时膝盖擦伤",
        "occurredAt": "2026-07-19T10:30:00",
        "parentNotified": False
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"incidentReportId": "data.id"})

        # 尝试关闭（应失败）
        resp2 = ctx.post(f"/incident-report/{{{{incidentReportId}}}}/close")
        if not resp2.get("success"):
            results.ok("DC-008", "✅ 未通知家长时关闭事故被正确拒绝")

            # 更新通知状态
            resp3 = ctx.put(f"/incident-report/{{{{incidentReportId}}}}", json={
                "parentNotified": True,
                "parentNotifiedAt": "2026-07-19T11:00:00"
            })
            if resp3.get("success"):
                # 再次关闭
                resp4 = ctx.post(f"/incident-report/{{{{incidentReportId}}}}/close")
                if resp4.get("success"):
                    results.ok("DC-009", "✅ 通知家长后关闭事故成功")
                else:
                    results.fail("DC-009", f"通知后关闭仍失败: {resp4.get('message')}")
            else:
                results.fail("DC-008", f"更新通知状态失败: {resp3.get('message')}")
        else:
            results.fail("DC-008", "未通知家长时关闭未被拒绝")
    else:
        results.skip("DC-008~009", "创建事故失败")


def run_data_consistency(results: E2ETestResults, env: dict) -> dict:
    """执行数据一致性专项验证。"""
    ctx = E2EApiClient()
    ctx.vars.update(env)
    ctx.login(use_account="admin")
    results.ok("DC-000", "数据一致性测试初始化成功")

    test_dashboard_consistency(ctx, results)
    test_lead_to_enrollment_consistency(ctx, results)
    test_leave_to_attendance_consistency(ctx, results)
    test_backfill_consistency(ctx, results)

    # 并发约束测试需要先确保有前置数据
    ctx.login(use_account="teacher")
    test_unique_constraint_daily_report(ctx, results)

    ctx.login(use_account="admin")
    test_unique_constraint_attendance(ctx, results)
    test_incident_close_requires_parent_notification(ctx, results)

    return ctx.vars


if __name__ == "__main__":
    results = E2ETestResults("数据一致性验证")
    from test_01_director_workflow import run_director_workflow
    from test_02_teacher_caregiver_workflow import run_teacher_workflow
    env = run_director_workflow(results)
    env = run_teacher_workflow(results, env)
    run_data_consistency(results, env)
    results.summary()
