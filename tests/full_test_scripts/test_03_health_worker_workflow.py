"""
保健员工作流 — 端到端全链路数据一致性测试

覆盖角色：保健员/保健医 (HEALTH_WORKER, HEALTH_DOCTOR)
核心链路：用药委托审核 → 用药执行 → 过敏标签管理 → 传染病全流程 → 食谱管理

变量依赖：{{enrollmentId}}, {{babyId}}, {{orgId}}
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))
from conftest_e2e import E2EApiClient, E2ETestResults, unique_name


def run_health_workflow(results: E2ETestResults, env: dict) -> dict:
    """执行保健员工作流全链路测试。"""
    ctx = E2EApiClient()
    ctx.vars.update(env)
    ctx.login(use_account="health")
    results.ok("HLTH-001", "保健员登录成功")

    today = "2026-07-19"

    # ── 1. 保健员：健康审核入托（如 enrollment 仍在 HEALTH_CHECK） ──
    resp = ctx.get(f"/enrollment/{{{{enrollmentId}}}}")
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        if status == "HEALTH_CHECK":
            resp2 = ctx.post(f"/enrollment/{{{{enrollmentId}}}}/health-check", json={"approved": True})
            if resp2.get("success"):
                results.ok("HLTH-002", "保健员健康审核通过")
                ctx.set("enrollmentStatus", "ACTIVE")
            else:
                results.fail("HLTH-002", f"健康审核失败: {resp2.get('message')}")
        elif status == "ACTIVE":
            results.ok("HLTH-002", "入托已是 ACTIVE 状态，跳过健康审核")
            ctx.set("enrollmentStatus", "ACTIVE")
        else:
            results.skip("HLTH-002", f"入托状态 {status}，跳过健康审核")
    else:
        results.fail("HLTH-002", f"查询入托失败: {resp.get('message')}")

    # ── 2. 保健员：过敏标签管理 ─────────────────────────────────────
    # 创建过敏标签
    resp = ctx.post("/medication-care/allergy/create", json={
        "enrollmentId": "{{enrollmentId}}",
        "allergen": "花生",
        "reaction": "皮肤红疹",
        "severity": "MODERATE"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"allergyTagId": "data.id"})
        results.ok("HLTH-003", f"创建过敏标签成功, id={ctx.getv('allergyTagId')}")
    else:
        results.fail("HLTH-003", f"创建过敏标签失败: {resp.get('message')}")

    # 查询宝宝过敏列表
    resp = ctx.get(f"/medication-care/allergy/baby/{{{{babyId}}}}")
    if resp.get("success"):
        allergies = resp.get("data", [])
        found = any(a.get("allergen") == "花生" for a in allergies)
        if found:
            results.ok("HLTH-004", "宝宝过敏列表包含花生标签")
        else:
            results.fail("HLTH-004", "未找到花生过敏标签")
    else:
        results.fail("HLTH-004", f"查询过敏列表失败: {resp.get('message')}")

    # ── 3. 保健员：用药委托审批 ─────────────────────────────────────
    # 先用家长 token 创建用药委托
    parent_ctx = E2EApiClient()
    parent_ctx.vars.update(env)
    parent_ctx.login(use_account="parent")

    resp = parent_ctx.post("/medication-care/request/create", json={
        "enrollmentId": "{{enrollmentId}}",
        "medicineName": "小儿感冒冲剂",
        "dosage": "5ml",
        "frequency": "每日3次",
        "startDate": today,
        "endDate": "2026-07-21"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"medicationRequestId": "data.id"})
        results.ok("HLTH-005", f"家长提交用药委托成功, id={ctx.getv('medicationRequestId')}")
    else:
        results.fail("HLTH-005", f"提交用药委托失败: {resp.get('message')}")

    # 保健员审核通过
    resp = ctx.post(f"/medication-care/request/{{{{medicationRequestId}}}}/approve")
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("HLTH-006", f"保健员审核用药通过, status={status}")
        assert status == "APPROVED", f"期望 APPROVED，实际 {status}"
    else:
        results.fail("HLTH-006", f"审核用药失败: {resp.get('message')}")

    # ── 4. 记录用药执行 ─────────────────────────────────────────────
    resp = ctx.post("/medication-care/administration/create", json={
        "medicationRequestId": "{{medicationRequestId}}",
        "administeredAt": f"{today}T08:30:00",
        "actualDosage": "5ml",
        "reactionObserved": False
    })
    if resp.get("success"):
        results.ok("HLTH-007", "记录用药执行成功")
    else:
        results.fail("HLTH-007", f"记录用药执行失败: {resp.get('message')}")

    # ── 5. 传染病全流程 ─────────────────────────────────────────────
    resp = ctx.post("/infectious-disease/create", json={
        "enrollmentId": "{{enrollmentId}}",
        "diseaseName": "手足口病（疑似）",
        "status": "SUSPECTED",
        "symptoms": "发热、皮疹",
        "onsetDate": today
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"diseaseRecordId": "data.id"})
        results.ok("HLTH-008", f"创建传染病记录成功, id={ctx.getv('diseaseRecordId')}")

        # 确诊
        resp = ctx.put(f"/infectious-disease/{{{{diseaseRecordId}}}}", json={
            "status": "CONFIRMED",
            "diagnosis": "手足口病"
        })
        if resp.get("success"):
            results.ok("HLTH-009", "传染病确诊成功")

            # 隔离
            resp = ctx.put(f"/infectious-disease/{{{{diseaseRecordId}}}}", json={
                "status": "ISOLATED",
                "isolationStartDate": today,
                "isolationEndDate": "2026-07-25"
            })
            if resp.get("success"):
                results.ok("HLTH-010", "传染病隔离成功")

                # 康复
                resp = ctx.put(f"/infectious-disease/{{{{diseaseRecordId}}}}", json={
                    "status": "RECOVERED",
                    "recoveryDate": "2026-07-25"
                })
                if resp.get("success"):
                    results.ok("HLTH-011", "传染病康复成功")

                    # 复园
                    resp = ctx.put(f"/infectious-disease/{{{{diseaseRecordId}}}}", json={
                        "status": "RETURNED",
                        "returnDate": "2026-07-26"
                    })
                    if resp.get("success"):
                        results.ok("HLTH-012", "传染病复园成功")
                    else:
                        results.fail("HLTH-012", f"复园失败: {resp.get('message')}")
                else:
                    results.fail("HLTH-011", f"康复失败: {resp.get('message')}")
            else:
                results.fail("HLTH-010", f"隔离失败: {resp.get('message')}")
        else:
            results.fail("HLTH-009", f"确诊失败: {resp.get('message')}")
    else:
        results.fail("HLTH-008", f"创建传染病记录失败: {resp.get('message')}")

    # ── 6. 查询机构活跃传染病数 ────────────────────────────────────
    resp = ctx.get(f"/infectious-disease/organization/{{{{orgId}}}}/active-count")
    if resp.get("success"):
        count = resp.get("data", {}).get("activeCount", -1)
        results.ok("HLTH-013", f"机构活跃传染病数={count}")
    else:
        results.fail("HLTH-013", f"查询活跃数失败: {resp.get('message')}")

    # ── 7. 食谱管理 ─────────────────────────────────────────────────
    resp = ctx.post("/meal-plan/create", json={
        "organizationId": "{{orgId}}",
        "date": today,
        "mealType": "LUNCH",
        "menu": "胡萝卜炒肉、清炒小白菜、米饭",
        "nutritionInfo": "蛋白质15g、碳水30g"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"mealPlanId": "data.id"})
        results.ok("HLTH-014", f"创建食谱成功, id={ctx.getv('mealPlanId')}")

        # 发布食谱
        resp = ctx.post(f"/meal-plan/{{{{mealPlanId}}}}/publish")
        if resp.get("success"):
            results.ok("HLTH-015", "食谱发布成功")
        else:
            results.fail("HLTH-015", f"食谱发布失败: {resp.get('message')}")

        # 记录进食
        resp = ctx.post("/meal-plan/intake/record", json={
            "mealPlanId": "{{mealPlanId}}",
            "enrollmentId": "{{enrollmentId}}",
            "intakePercentage": 80,
            "allergyReaction": False
        })
        if resp.get("success"):
            results.ok("HLTH-016", "记录宝宝进食成功")
        else:
            results.fail("HLTH-016", f"记录进食失败: {resp.get('message')}")

        # 营养分析
        resp = ctx.get(f"/meal-plan/analysis/organization/{{{{orgId}}}}")
        if resp.get("success"):
            results.ok("HLTH-017", "营养分析报表获取成功")
        else:
            results.fail("HLTH-017", f"营养分析失败: {resp.get('message')}")
    else:
        results.fail("HLTH-014", f"创建食谱失败: {resp.get('message')}")

    return ctx.vars


if __name__ == "__main__":
    results = E2ETestResults("保健员工作流")
    from test_01_director_workflow import run_director_workflow
    env = run_director_workflow(results)
    run_health_workflow(results, env)
    results.summary()
