"""
教师与保育员工作流 — 端到端全链路数据一致性测试

覆盖角色：教师 (TEACHER)、保育员 (CAREGIVER)
核心链路：考勤签到 → 照护记录 → 健康观察 → 日报生成 → 日报发布 → 请假审批

变量依赖：{{enrollmentId}}, {{classroomId}}, {{babyId}}, {{orgId}}
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))
from conftest_e2e import E2EApiClient, E2ETestResults, unique_name


def run_teacher_workflow(results: E2ETestResults, env: dict) -> dict:
    """执行教师/保育员工作流全链路测试。
    
    Args:
        results: 测试结果收集器
        env: 上级测试传递的上下文变量（含 enrollmentId, orgId, classroomId, babyId）
    """
    ctx = E2EApiClient()
    ctx.vars.update(env)
    ctx.login(use_account="teacher")
    results.ok("TCH-001", "教师登录成功")

    today = "2026-07-19"
    ctx.set("attendanceDate", today)

    # ── 1. 教师：考勤签到 ─────────────────────────────────────────────
    resp = ctx.post("/attendance/check-in", json={
        "enrollmentId": "{{enrollmentId}}",
        "attendanceDate": "{{attendanceDate}}"
    })
    if resp.get("success"):
        results.ok("TCH-002", f"考勤签到成功, status={resp.get('data', {}).get('status')}")
    else:
        results.fail("TCH-002", f"考勤签到失败: {resp.get('message')}")

    # ── 2. 验证班级考勤列表 ───────────────────────────────────────────
    resp = ctx.get(f"/attendance/classroom/{{{{classroomId}}}}?date={today}")
    if resp.get("success"):
        enrollments = resp.get("data", [])
        found = any(e.get("enrollmentId") == ctx.getv("enrollmentId")
                     and e.get("status") == "CHECKED_IN" for e in enrollments)
        if found:
            results.ok("TCH-003", "班级考勤列表包含签到记录")
        else:
            results.fail("TCH-003", "班级考勤列表中未找到签到记录")
    else:
        results.fail("TCH-003", f"查询班级考勤失败: {resp.get('message')}")

    # ── 3. 保育员：创建照护记录（喂养 + 睡眠 + 饮水） ──────────────
    ctx.login(use_account="caregiver")
    results.ok("TCH-004", "保育员登录成功")

    care_records = [
        {"type": "FEEDING", "recordTime": f"{today}T08:30:00", "amount": 200, "unit": "ml"},
        {"type": "SLEEP", "recordTime": f"{today}T12:30:00",
         "startedAt": f"{today}T12:00:00", "endedAt": f"{today}T14:30:00"},
        {"type": "WATER", "recordTime": f"{today}T10:00:00", "amount": 100, "unit": "ml"},
        {"type": "TOILET", "recordTime": f"{today}T09:30:00", "valueText": "正常"},
    ]

    for i, rec in enumerate(care_records):
        body = {
            "enrollmentId": "{{enrollmentId}}",
            "recordDate": today,
            **rec
        }
        resp = ctx.post("/care-record/create", json=body)
        if resp.get("success"):
            if i == 0:
                ctx.collect_vars(resp, {"careRecordId": "data.id"})
            results.ok("TCH-005", f"创建照护记录 {rec['type']} 成功")
        else:
            results.fail("TCH-005", f"创建照护记录 {rec['type']} 失败: {resp.get('message')}")

    # ── 4. 查询班级照护记录 ───────────────────────────────────────────
    resp = ctx.get(f"/care-record/classroom/{{{{classroomId}}}}?date={today}")
    if resp.get("success"):
        records = resp.get("data", [])
        record_types = [r.get("type") for r in records]
        for t in ["FEEDING", "SLEEP", "WATER", "TOILET"]:
            if t in record_types:
                results.ok("TCH-006", f"班级照护列表包含 {t}")
            else:
                results.fail("TCH-006", f"班级照护列表缺少 {t}")
    else:
        results.fail("TCH-006", f"查询班级照护失败: {resp.get('message')}")

    # ── 5. 教师：健康观察（晨检） ─────────────────────────────────────
    ctx.login(use_account="teacher")
    resp = ctx.post("/health-observation/create", json={
        "enrollmentId": "{{enrollmentId}}",
        "observationDate": today,
        "observationTime": f"{today}T08:30:00",
        "type": "MORNING_CHECK",
        "temperature": 36.5,
        "touchStatus": "正常",
        "lookStatus": "正常",
        "askStatus": "正常",
        "checkStatus": "正常",
        "abnormal": False,
        "followUpRequired": False
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"healthObsId": "data.id"})
        results.ok("TCH-007", "创建晨检记录成功")
    else:
        results.fail("TCH-007", f"创建晨检失败: {resp.get('message')}")

    # ── 6. 教师：生成日报草稿 ─────────────────────────────────────────
    resp = ctx.post("/daily-report/generate", json={
        "enrollmentId": "{{enrollmentId}}",
        "reportDate": today
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"dailyReportId": "data.id"})
        status = resp.get("data", {}).get("status")
        results.ok("TCH-008", f"生成日报草稿成功, status={status}")
        assert status == "DRAFT", f"期望 DRAFT，实际 {status}"
    else:
        results.fail("TCH-008", f"生成日报失败: {resp.get('message')}")

    # ── 7. 教师：编辑日报 ─────────────────────────────────────────────
    resp = ctx.put(f"/daily-report/{{{{dailyReportId}}}}", json={
        "teacherComment": "宝宝今天在户外活动中表现积极，能够主动与小朋友分享玩具。午餐吃了大半碗，午睡1.5小时。"
    })
    if resp.get("success"):
        results.ok("TCH-009", "编辑日报草稿成功")
    else:
        results.fail("TCH-009", f"编辑日报失败: {resp.get('message')}")

    # ── 8. 教师：提交审核（因机构开启审批） ──────────────────────────
    resp = ctx.post(f"/daily-report/{{{{dailyReportId}}}}/submit")
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("TCH-010", f"提交日报审核成功, status={status}")
        assert status == "PENDING_APPROVAL", f"期望 PENDING_APPROVAL，实际 {status}"
    else:
        results.fail("TCH-010", f"提交审核失败: {resp.get('message')}")

    # ── 9. 园长审核通过 ──────────────────────────────────────────────
    ctx.login(use_account="admin")
    resp = ctx.post(f"/daily-report/{{{{dailyReportId}}}}/approve")
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("TCH-011", f"园长审核日报通过, status={status}")
        assert status == "PUBLISHED", f"期望 PUBLISHED，实际 {status}"
    else:
        results.fail("TCH-011", f"园长审核失败: {resp.get('message')}")

    # ── 10. 教师：查看已发布日报 ────────────────────────────────────
    ctx.login(use_account="teacher")
    resp = ctx.get(f"/daily-report/{{{{dailyReportId}}}}")
    if resp.get("success"):
        data = resp.get("data", {})
        results.ok("TCH-012", f"日报详情: status={data.get('status')}, publishedAt={data.get('publishedAt')}")
        assert data.get("status") == "PUBLISHED"
    else:
        results.fail("TCH-012", f"查询日报失败: {resp.get('message')}")

    # ── 11. 教师：创建请假申请并审批 ────────────────────────────────
    resp = ctx.post("/attendance/leave/request", json={
        "enrollmentId": "{{enrollmentId}}",
        "startDate": "2026-07-21",
        "endDate": "2026-07-22",
        "type": "SICK",
        "reason": "宝宝感冒"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"leaveRequestId": "data.id"})
        results.ok("TCH-013", f"创建请假申请成功, id={ctx.getv('leaveRequestId')}")
    else:
        results.fail("TCH-013", f"创建请假失败: {resp.get('message')}")

    # 审批通过
    resp = ctx.post(f"/attendance/leave/{{{{leaveRequestId}}}}/approve")
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("TCH-014", f"请假审批通过, status={status}")
        assert status == "APPROVED", f"期望 APPROVED，实际 {status}"
    else:
        results.fail("TCH-014", f"审批请假失败: {resp.get('message')}")

    # ── 12. 验证请假后考勤自动变为 LEAVE ─────────────────────────────
    resp = ctx.get(f"/attendance/classroom/{{{{classroomId}}}}?date=2026-07-21")
    if resp.get("success"):
        enrollments = resp.get("data", [])
        found_leave = any(
            e.get("enrollmentId") == ctx.getv("enrollmentId") and e.get("status") == "LEAVE"
            for e in enrollments
        )
        if found_leave:
            results.ok("TCH-015", "请假后考勤自动变为 LEAVE (7月21日)")
        else:
            results.fail("TCH-015", "未找到 LEAVE 考勤记录")
    else:
        results.fail("TCH-015", f"查询考勤失败: {resp.get('message')}")

    return ctx.vars


if __name__ == "__main__":
    results = E2ETestResults("教师/保育员工作流")
    # 先运行园长工作流获取上下文
    from test_01_director_workflow import run_director_workflow
    env = run_director_workflow(results)
    run_teacher_workflow(results, env)
    results.summary()
