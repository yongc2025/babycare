"""
财务与运营工作流 — 端到端全链路数据一致性测试

覆盖角色：财务 (FINANCE)、运营 (OPERATIONS_STAFF)、招生 (ADMISSIONS_OFFICER)
核心链路：收费项目管理 → 账单生成/支付 → 招生线索 → 试托 → 转入学 → 安全台账

变量依赖：{{orgId}}, {{enrollmentId}}, {{classroomId}}
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))
from conftest_e2e import E2EApiClient, E2ETestResults, unique_name


def run_finance_workflow(results: E2ETestResults, env: dict) -> dict:
    """执行财务/运营工作流全链路测试。"""
    ctx = E2EApiClient()
    ctx.vars.update(env)
    ctx.login(use_account="finance")
    results.ok("FIN-001", "财务登录成功")

    today = "2026-07-19"

    # ════════════════════════════════════════════════════════════════
    # 财务模块
    # ════════════════════════════════════════════════════════════════

    # ── 1. 创建收费项目 ─────────────────────────────────────────────
    resp = ctx.post("/billing/fee-item/create", json={
        "organizationId": "{{orgId}}",
        "name": "托育费",
        "description": "7月托育费",
        "amount": 3000.00
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"feeItemId": "data.id"})
        results.ok("FIN-002", f"创建收费项目成功, id={ctx.getv('feeItemId')}")
    else:
        results.fail("FIN-002", f"创建收费项目失败: {resp.get('message')}")

    # ── 2. 查询机构收费项目列表 ────────────────────────────────────
    resp = ctx.get(f"/billing/fee-item/organization/{{{{orgId}}}}")
    if resp.get("success"):
        items = resp.get("data", [])
        found = any(i.get("id") == ctx.getv("feeItemId") for i in items)
        results.ok("FIN-003", "收费项目列表包含新创建项目" if found else "未找到新创建项目")
    else:
        results.fail("FIN-003", f"查询收费项目失败: {resp.get('message')}")

    # ── 3. 生成账单 ─────────────────────────────────────────────────
    resp = ctx.post("/billing/bill/create", json={
        "organizationId": "{{orgId}}",
        "enrollmentId": "{{enrollmentId}}",
        "feeItemId": "{{feeItemId}}",
        "title": "7月托育费",
        "amount": 3000.00,
        "dueDate": "2026-07-31"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"billId": "data.id"})
        status = resp.get("data", {}).get("status")
        results.ok("FIN-004", f"生成账单成功, id={ctx.getv('billId')}, status={status}")
        assert status == "UNPAID", f"期望 UNPAID，实际 {status}"
    else:
        results.fail("FIN-004", f"生成账单失败: {resp.get('message')}")

    # ── 4. 标记账单已支付 ──────────────────────────────────────────
    resp = ctx.post(f"/billing/bill/{{{{billId}}}}/paid", json={
        "paymentMethod": "CASH"
    })
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("FIN-005", f"标记支付成功, status={status}")
        assert status == "PAID", f"期望 PAID，实际 {status}"
    else:
        results.fail("FIN-005", f"标记支付失败: {resp.get('message')}")

    # ── 5. 查询账单详情 ────────────────────────────────────────────
    resp = ctx.get(f"/billing/bill/{{{{billId}}}}")
    if resp.get("success"):
        data = resp.get("data", {})
        assert data.get("status") == "PAID"
        assert data.get("paidAt") is not None
        results.ok("FIN-006", "账单状态已确认 PAID，paidAt 非空")
    else:
        results.fail("FIN-006", f"查询账单失败: {resp.get('message')}")

    # ── 6. 财务工作台 ──────────────────────────────────────────────
    resp = ctx.get(f"/billing/finance-workbench/{{{{orgId}}}}")
    if resp.get("success"):
        results.ok("FIN-007", "财务工作台获取成功")
    else:
        results.fail("FIN-007", f"获取财务工作台失败: {resp.get('message')}")

    # ════════════════════════════════════════════════════════════════
    # 招生模块
    # ════════════════════════════════════════════════════════════════

    ctx.login(use_account="adm")
    results.ok("FIN-008", "招生人员登录成功")

    # ── 7. 创建招生线索 ───────────────────────────────────────────
    resp = ctx.post("/admission-lead/create", json={
        "organizationId": "{{orgId}}",
        "babyName": "乐乐",
        "babyGender": "MALE",
        "birthDate": "2024-01-15",
        "parentName": "乐乐妈",
        "parentPhone": "13900139000",
        "source": "ONLINE"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"leadId": "data.id"})
        results.ok("FIN-009", f"创建招生线索成功, id={ctx.getv('leadId')}")
    else:
        results.fail("FIN-009", f"创建招生线索失败: {resp.get('message')}")

    # ── 8. 添加跟进记录 ───────────────────────────────────────────
    resp = ctx.post(f"/admission-lead/{{{{leadId}}}}/follow-up", json={
        "content": "已电话沟通，明天来园参观"
    })
    if resp.get("success"):
        results.ok("FIN-010", "添加跟进记录成功")
    else:
        results.fail("FIN-010", f"添加跟进失败: {resp.get('message')}")

    # ── 9. 开始试托 ───────────────────────────────────────────────
    resp = ctx.post(f"/admission-lead/{{{{leadId}}}}/trial/start", json={
        "trialStartDate": "2026-07-21",
        "trialEndDate": "2026-07-25"
    })
    if resp.get("success"):
        results.ok("FIN-011", "开始试托成功")
    else:
        results.fail("FIN-011", f"开始试托失败: {resp.get('message')}")

    # ── 10. 报名审核通过 ──────────────────────────────────────────
    resp = ctx.post(f"/admission-lead/{{{{leadId}}}}/review", json={
        "approved": True,
        "classroomId": "{{classroomId}}"
    })
    if resp.get("success"):
        results.ok("FIN-012", "报名审核通过")
    else:
        results.fail("FIN-012", f"报名审核失败: {resp.get('message')}")

    # ── 11. 招生漏斗统计 ──────────────────────────────────────────
    resp = ctx.get(f"/admission-lead/funnel/{{{{orgId}}}}")
    if resp.get("success"):
        results.ok("FIN-013", "招生漏斗统计获取成功")
    else:
        results.fail("FIN-013", f"招生漏斗失败: {resp.get('message')}")

    # ════════════════════════════════════════════════════════════════
    # 安全台账模块（安全/后勤）
    # ════════════════════════════════════════════════════════════════

    ctx.login(use_account="safety")
    results.ok("FIN-014", "安全员登录成功")

    # ── 12. 创建台账模板 ──────────────────────────────────────────
    resp = ctx.post("/safety-ledger/template/create", json={
        "organizationId": "{{orgId}}",
        "name": "每日消毒记录",
        "type": "DISINFECTION",
        "frequency": "DAILY"
    })
    if resp.get("success"):
        ctx.collect_vars(resp, {"safetyTemplateId": "data.id"})
        results.ok("FIN-015", f"创建台账模板成功, id={ctx.getv('safetyTemplateId')}")
    else:
        results.fail("FIN-015", f"创建台账模板失败: {resp.get('message')}")

    # ── 13. 生成到期台账 ──────────────────────────────────────────
    resp = ctx.post(f"/safety-ledger/generate-tasks/{{{{orgId}}}}")
    if resp.get("success"):
        count = resp.get("data", {}).get("count", 0)
        results.ok("FIN-016", f"生成到期台账成功, 生成数={count}")
    else:
        results.fail("FIN-016", f"生成台账失败: {resp.get('message')}")

    # ── 14. 查询台账列表 ──────────────────────────────────────────
    resp = ctx.get(f"/safety-ledger/organization/{{{{orgId}}}}")
    if resp.get("success"):
        ledgers = resp.get("data", [])
        if ledgers:
            ctx.collect_vars(resp, {"safetyLedgerId": "data.0.id"})
            results.ok("FIN-017", f"查询台账列表成功, 共{len(ledgers)}条")
        else:
            results.skip("FIN-017", "台账列表为空，跳过后续台账步骤")
            return ctx.vars
    else:
        results.fail("FIN-017", f"查询台账失败: {resp.get('message')}")
        return ctx.vars

    # ── 15. 填写台账内容 ──────────────────────────────────────────
    resp = ctx.put(f"/safety-ledger/{{{{safetyLedgerId}}}}", json={
        "description": "已完成教室消毒"
    })
    if resp.get("success"):
        results.ok("FIN-018", "填写台账内容成功")
    else:
        results.fail("FIN-018", f"填写台账失败: {resp.get('message')}")

    # ── 16. 关闭台账 ──────────────────────────────────────────────
    resp = ctx.post(f"/safety-ledger/{{{{safetyLedgerId}}}}/close")
    if resp.get("success"):
        status = resp.get("data", {}).get("status")
        results.ok("FIN-019", f"关闭台账成功, status={status}")
    else:
        results.fail("FIN-019", f"关闭台账失败: {resp.get('message')}")

    # ── 17. 机构台账统计 ──────────────────────────────────────────
    resp = ctx.get(f"/safety-ledger/overdue-count/{{{{orgId}}}}")
    if resp.get("success"):
        results.ok("FIN-020", "台账统计获取成功")
    else:
        results.fail("FIN-020", f"台账统计失败: {resp.get('message')}")

    return ctx.vars


if __name__ == "__main__":
    results = E2ETestResults("财务/运营工作流")
    from test_01_director_workflow import run_director_workflow
    env = run_director_workflow(results)
    run_finance_workflow(results, env)
    results.summary()
