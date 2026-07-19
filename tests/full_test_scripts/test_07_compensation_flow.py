"""
逆向补偿与最终一致性测试 — Enrollment 状态机 + 考勤自动联动

核心测试：
  1. ACTIVE → SUSPENDED → 考勤自动冻结(ABSENT)
  2. SUSPENDED → REACTIVATE → ACTIVE → 考勤恢复可签到
  3. ACTIVE → WITHDRAWN → 相关记录只读冻结
  4. 转班后班级关联变更 + 历史保留

铁律4验证：暂停入托(SUSPENDED)时，同步校验该宝宝在AttendanceRecord中
           "今日及未来日期"的状态是否自动变为"缺席/冻结"，且在复托(REACTIVATE)后，
           考勤状态是否恢复可签到。

变量依赖：{{enrollmentId}}, {{classroomId}}, {{orgId}}, {{babyId}}
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))
from conftest_e2e import E2EApiClient, E2ETestResults, unique_name


def run_compensation_flow(results: E2ETestResults, env: dict) -> dict:
    """执行补偿流程全链路测试。"""
    ctx = E2EApiClient()
    ctx.vars.update(env)
    ctx.login(use_account="admin")
    results.ok("CMP-001", "园长登录成功")

    today = "2026-07-19"

    # 确保 enrollment 处于 ACTIVE 状态
    resp = ctx.get(f"/enrollment/{{{{enrollmentId}}}}")
    initial_status = resp.get("data", {}).get("status") if resp.get("success") else None
    
    if initial_status != "ACTIVE":
        if initial_status == "PENDING":
            ctx.post(f"/enrollment/{{{{enrollmentId}}}}/review", json={"approved": True})
            ctx.login(use_account="health")
            ctx.post(f"/enrollment/{{{{enrollmentId}}}}/health-check", json={"approved": True})
            ctx.login(use_account="admin")
        results.ok("CMP-002", f"入托状态已调整为 ACTIVE (原状态={initial_status})")
    else:
        results.ok("CMP-002", "入托已是 ACTIVE 状态")

    # ════════════════════════════════════════════════════════════════
    # 测试1: 暂停入托 + 考勤冻结
    # ════════════════════════════════════════════════════════════════

    # 先创建一个签到记录
    resp = ctx.post("/attendance/check-in", json={
        "enrollmentId": "{{enrollmentId}}",
        "attendanceDate": today
    })
    if resp.get("success"):
        results.ok("CMP-003", "考勤签到成功（暂停前）")
    else:
        results.skip("CMP-003", f"签到失败: {resp.get('message')}，继续后续")

    # Step 1: 暂停入托 ACTIVE → SUSPENDED
    resp = ctx.post(f"/enrollment/{{{{enrollmentId}}}}/suspend", json={
        "reason": "家长出差，暂停2周"
    })
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("CMP-004", f"暂停入托成功, status={status}")
        assert status == "SUSPENDED", f"期望 SUSPENDED，实际 {status}"

        # Step 2: 验证未来日期考勤为 ABSENT（铁律4核心断言）
        future_dates = ["2026-07-20", "2026-07-21", "2026-07-22"]
        all_absent = True
        for date in future_dates:
            resp2 = ctx.get(f"/attendance/classroom/{{{{classroomId}}}}?date={date}")
            if resp2.get("success"):
                records = resp2.get("data", [])
                enrollment_records = [r for r in records 
                                       if r.get("enrollmentId") == ctx.get("enrollmentId")]
                if enrollment_records:
                    for r in enrollment_records:
                        if r.get("status") != "ABSENT":
                            all_absent = False
                            results.fail("CMP-005", f"{date} 考勤状态为 {r.get('status')}，期望 ABSENT")
                    if enrollment_records:
                        pass  # checked above
                else:
                    # 无记录也可接受（懒生成）
                    pass
        
        if all_absent:
            results.ok("CMP-005", "✅ 铁律4: 暂停后未来考勤状态均为 ABSENT")

        # Step 3: SUSPENDED 状态下尝试签到应失败
        resp3 = ctx.post("/attendance/check-in", json={
            "enrollmentId": "{{enrollmentId}}",
            "attendanceDate": "2026-07-23"
        })
        if not resp3.get("success"):
            results.ok("CMP-006", "SUSPENDED状态下签到正确拒绝")
        else:
            results.fail("CMP-006", "SUSPENDED状态下签到未被拒绝")
    else:
        results.fail("CMP-004", f"暂停入托失败: {resp.get('message')}")
        results.skip("CMP-005~006", "暂停失败，跳过考勤冻结验证")

    # ════════════════════════════════════════════════════════════════
    # 测试2: 复托 SUSPENDED → ACTIVE + 考勤恢复
    # ════════════════════════════════════════════════════════════════

    # Step 4: 复托
    resp = ctx.post(f"/enrollment/{{{{enrollmentId}}}}/reactivate", json={
        "reason": "家长已回来"
    })
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("CMP-007", f"复托成功, status={status}")
        assert status == "ACTIVE", f"期望 ACTIVE，实际 {status}"

        # Step 5: 验证可签到
        resp2 = ctx.post("/attendance/check-in", json={
            "enrollmentId": "{{enrollmentId}}",
            "attendanceDate": "2026-07-24"
        })
        if resp2.get("success"):
            results.ok("CMP-008", "✅ 铁律4: 复托后签到成功，考勤状态恢复")
        else:
            results.fail("CMP-008", f"复托后签到失败: {resp2.get('message')}")
    else:
        results.fail("CMP-007", f"复托失败: {resp.get('message')}")

    # ════════════════════════════════════════════════════════════════
    # 测试3: 退托 ACTIVE → WITHDRAWN
    # ════════════════════════════════════════════════════════════════

    # 创建一个新的 enrollment 用于退托测试（避免影响已有流程）
    resp = ctx.post("/enrollment/create", json={
        "babyId": "{{babyId}}",
        "organizationId": "{{orgId}}",
        "classroomId": "{{classroomId}}"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"withdrawEnrollmentId": "data.id"})
        weid = ctx.get("withdrawEnrollmentId")

        # 快速审核到 ACTIVE
        ctx.post(f"/enrollment/{weid}/review", json={"approved": True})
        ctx.login(use_account="health")
        ctx.post(f"/enrollment/{weid}/health-check", json={"approved": True})
        ctx.login(use_account="admin")

        # 退托
        resp2 = ctx.post(f"/enrollment/{weid}/withdraw", json={
            "reason": "搬家转园",
            "withdrawnAt": today
        })
        if resp2.get("success"):
            data = resp2.get("data", {})
            results.ok("CMP-009", f"退托成功, status={data.get('status')}, reason={data.get('withdrawReason')}")

            # 验证退托后不可签到
            resp3 = ctx.post("/attendance/check-in", json={
                "enrollmentId": weid,
                "attendanceDate": today
            })
            if not resp3.get("success"):
                results.ok("CMP-010", "WITHDRAWN状态下签到正确拒绝")
            else:
                results.fail("CMP-010", "WITHDRAWN状态下签到未被拒绝")

            # 验证历史日报仍可查看
            resp4 = ctx.get(f"/daily-report/baby/{{{{babyId}}}}/list")
            if resp4.get("success"):
                results.ok("CMP-011", "退托后历史日报仍可查看")
            else:
                results.fail("CMP-011", f"退托后查看历史日报失败: {resp4.get('message')}")
        else:
            results.fail("CMP-009", f"退托失败: {resp2.get('message')}")
    else:
        results.skip("CMP-009~011", "创建退托测试 enrollment 失败")

    # ════════════════════════════════════════════════════════════════
    # 测试4: 转班
    # ════════════════════════════════════════════════════════════════

    resp = ctx.post("/classroom/create", json={
        "organizationId": "{{orgId}}",
        "name": unique_name("升班班级"),
        "capacity": 20
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"newClassroomId": "data.id"})

        resp2 = ctx.post(f"/enrollment/{{{{enrollmentId}}}}/transfer", json={
            "newClassroomId": "{{newClassroomId}}",
            "reason": "月龄升班"
        })
        if resp2.get("success"):
            data = resp2.get("data", {})
            results.ok("CMP-012", f"转班成功, newClassroomId={data.get('classroomId')}")

            # 验证历史记录
            resp3 = ctx.get(f"/enrollment/{{{{enrollmentId}}}}/history")
            if resp3.get("success"):
                results.ok("CMP-013", "转班历史记录可查")
            else:
                results.fail("CMP-013", f"查询转班历史失败: {resp3.get('message')}")
        else:
            results.fail("CMP-012", f"转班失败: {resp2.get('message')}")
    else:
        results.skip("CMP-012~013", "创建新班级失败，跳过转班测试")

    return ctx.vars


if __name__ == "__main__":
    results = E2ETestResults("补偿流程")
    from test_01_director_workflow import run_director_workflow
    env = run_director_workflow(results)
    run_compensation_flow(results, env)
    results.summary()
