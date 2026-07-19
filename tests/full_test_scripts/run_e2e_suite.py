#!/usr/bin/env python
"""
好芽儿端到端全链路数据一致性测试套件 — 运行入口

按依赖顺序依次执行：
  1. 园长工作流  (test_01_director_workflow)
  2. 教师/保育员  (test_02_teacher_caregiver_workflow)
  3. 保健员工作流  (test_03_health_worker_workflow)
  4. 财务/运营工作流 (test_04_finance_operations_workflow)
  5. 家长工作流    (test_05_parent_workflow)
  6. RBAC权限穿透  (test_06_rbac_penetration)
  7. 补偿流程      (test_07_compensation_flow)
  8. 数据一致性验证 (test_08_data_consistency)

运行：
    python tests/full_test_scripts/run_e2e_suite.py
"""

import sys
import os
import time

sys.path.insert(0, os.path.dirname(__file__))
sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

from conftest_e2e import E2ETestResults, E2EApiClient


def main():
    print("=" * 70)
    print("  好芽儿 端到端全链路数据一致性测试套件")
    print("  BabyCare/HuiGrowth E2E Data Consistency Test Suite")
    print("=" * 70)
    print()
    print("  前置条件:")
    print("  - 后端已启动 (http://localhost:8080)")
    print("  - 测试账号已存在 (参见 test_*.py 中的 TEST_ACCOUNTS)")
    print("  - MySQL 已初始化 (huigrowth_dev)")
    print()

    # 检查后端健康
    health_ctx = E2EApiClient()
    if not health_ctx.health_check():
        print("  ❌ 后端不可达！请先启动后端服务。")
        print("     启动方式: .\\start-dev.bat")
        sys.exit(1)
    print("  ✅ 后端健康检查通过")
    print()

    start_time = time.time()
    all_results = []
    suite_env = {}

    # ════════════════════════════════════════════════════════════════
    # 阶段 1: 园长工作流
    # ════════════════════════════════════════════════════════════════
    print(f"\n{'─'*70}")
    print("  阶段 1/8: 园长工作流 — 机构/班级/员工/入托")
    print(f"{'─'*70}")
    from test_01_director_workflow import run_director_workflow
    r1 = E2ETestResults("园长工作流")
    suite_env = run_director_workflow(r1)
    r1.summary()
    all_results.append(r1)
    if r1.failed:
        print("  ⚠️  园长工作流存在失败用例，继续执行后续阶段")

    # ════════════════════════════════════════════════════════════════
    # 阶段 2: 教师/保育员工作流（暂禁）
    # ════════════════════════════════════════════════════════════════
    if False:  # TODO: 阶段1通过后放开
        print(f"\n{'─'*70}")
        print("  阶段 2/8: 教师/保育员工作流 — 考勤/照护/日报/请假")
        print(f"{'─'*70}")
        from test_02_teacher_caregiver_workflow import run_teacher_workflow
        r2 = E2ETestResults("教师/保育员工作流")
        suite_env = run_teacher_workflow(r2, suite_env)
        r2.summary()
        all_results.append(r2)

    # ════════════════════════════════════════════════════════════════
    # 阶段 3: 保健员工作流（暂禁）
    # ════════════════════════════════════════════════════════════════
    if False:
        print(f"\n{'─'*70}")
        print("  阶段 3/8: 保健员工作流 — 用药/过敏/传染病/食谱")
        print(f"{'─'*70}")
        from test_03_health_worker_workflow import run_health_workflow
        r3 = E2ETestResults("保健员工作流")
        suite_env = run_health_workflow(r3, suite_env)
        r3.summary()
        all_results.append(r3)

    # ════════════════════════════════════════════════════════════════
    # 阶段 4: 财务/运营工作流（暂禁）
    # ════════════════════════════════════════════════════════════════
    if False:
        print(f"\n{'─'*70}")
        print("  阶段 4/8: 财务/运营工作流 — 账单/招生/台账")
        print(f"{'─'*70}")
        from test_04_finance_operations_workflow import run_finance_workflow
        r4 = E2ETestResults("财务/运营工作流")
        suite_env = run_finance_workflow(r4, suite_env)
        r4.summary()
        all_results.append(r4)

    # ════════════════════════════════════════════════════════════════
    # 阶段 5: 家长工作流（暂禁）
    # ════════════════════════════════════════════════════════════════
    if False:
        print(f"\n{'─'*70}")
        print("  阶段 5/8: 家长工作流 — 查看/接送/成长记录/长辈模式")
        print(f"{'─'*70}")
        from test_05_parent_workflow import run_parent_workflow
        r5 = E2ETestResults("家长工作流")
        suite_env = run_parent_workflow(r5, suite_env)
        r5.summary()
        all_results.append(r5)

    # ════════════════════════════════════════════════════════════════
    # 阶段 6: RBAC 权限穿透（暂禁）
    # ════════════════════════════════════════════════════════════════
    if False:
        print(f"\n{'─'*70}")
        print("  阶段 6/8: RBAC 权限穿透 — 跨角色权限边界校验")
        print(f"{'─'*70}")
        from test_06_rbac_penetration import run_rbac_penetration
        r6 = E2ETestResults("RBAC权限穿透")
        suite_env = run_rbac_penetration(r6, suite_env)
        r6.summary()
        all_results.append(r6)

    # ════════════════════════════════════════════════════════════════
    # 阶段 7: 补偿流程（暂禁）
    # ════════════════════════════════════════════════════════════════
    if False:
        print(f"\n{'─'*70}")
        print("  阶段 7/8: 补偿流程 — 暂停/复托/退托/转班 + 考勤联动")
        print(f"{'─'*70}")
        from test_07_compensation_flow import run_compensation_flow
        r7 = E2ETestResults("补偿流程")
        suite_env = run_compensation_flow(r7, suite_env)
        r7.summary()
        all_results.append(r7)

    # ════════════════════════════════════════════════════════════════
    # 阶段 8: 数据一致性（暂禁）
    # ════════════════════════════════════════════════════════════════
    if False:
        print(f"\n{'─'*70}")
        print("  阶段 8/8: 数据一致性验证 — 聚合/约束/补录/事故")
        print(f"{'─'*70}")
        from test_08_data_consistency import run_data_consistency
        r8 = E2ETestResults("数据一致性验证")
        suite_env = run_data_consistency(r8, suite_env)
        r8.summary()
        all_results.append(r8)

    # ════════════════════════════════════════════════════════════════
    # 汇总报告
    # ════════════════════════════════════════════════════════════════
    total_elapsed = time.time() - start_time
    total_passed = sum(len(r.passed) for r in all_results)
    total_failed = sum(len(r.failed) for r in all_results)
    total_skipped = sum(len(r.skipped) for r in all_results)
    total_all = total_passed + total_failed + total_skipped

    print(f"\n{'='*70}")
    print(f"  📊 端到端全链路测试汇总报告")
    print(f"{'='*70}")
    print(f"  执行时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"  总耗时:   {total_elapsed:.1f}s")
    print(f"  ───────────────────────────────────────────")
    print(f"  总用例数: {total_all}")
    print(f"  ✅ 通过:   {total_passed}")
    print(f"  ❌ 失败:   {total_failed}")
    print(f"  ⏭️ 跳过:   {total_skipped}")
    if total_all > 0:
        pass_rate = total_passed / total_all * 100
        print(f"  通过率:   {pass_rate:.1f}%")
    print(f"{'='*70}")

    # 铁律验证清单
    print(f"\n  4 大铁律验证状态:")
    print(f"  ───────────────────────────────────────────")
    print(f"  [铁律1] 状态机强制验证 (数据库字段断言)")

    # 统计各阶段是否包含铁律验证
    iron_law_checks = {
        "铁律1-状态机": False,
        "铁律2-变量令牌化": False,
        "铁律3-RBAC穿透": False,
        "铁律4-补偿一致性": False,
    }

    # 检查铁律2: 变量令牌化 (所有脚本使用 {{变量}})
    script_files = [
        "test_01_director_workflow.py",
        "test_02_teacher_caregiver_workflow.py",
        "test_03_health_worker_workflow.py",
        "test_04_finance_operations_workflow.py",
        "test_05_parent_workflow.py",
        "test_06_rbac_penetration.py",
        "test_07_compensation_flow.py",
        "test_08_data_consistency.py",
    ]
    import re
    all_use_var_refs = True
    for script in script_files:
        script_path = os.path.join(os.path.dirname(__file__), script)
        if os.path.exists(script_path):
            with open(script_path, "r", encoding="utf-8") as f:
                content = f.read()
                if "{{" in content and "}}" in content:
                    pass  # uses variable references
                else:
                    all_use_var_refs = False
                    print(f"  ⚠️  {script} 未使用变量引用 {{变量}}")
    if all_use_var_refs:
        iron_law_checks["铁律2-变量令牌化"] = True

    for r in all_results:
        for case_id, _ in r.passed:
            if "状态机" in str(case_id) or "status=" in str(_) or "状态" in str(_):
                iron_law_checks["铁律1-状态机"] = True
            if "RBAC" in str(case_id) or "权限" in str(case_id) or "403" in str(_):
                iron_law_checks["铁律3-RBAC穿透"] = True
            if "SUSPEND" in str(case_id) or "暂停" in str(case_id) or "复托" in str(case_id) or "补偿" in str(case_id):
                iron_law_checks["铁律4-补偿一致性"] = True

    # 检查 test_07 中的铁律4
    if any("CMP-005" in str(case_id) or "CMP-008" in str(case_id) for r in all_results for case_id, _ in r.passed):
        iron_law_checks["铁律4-补偿一致性"] = True

    for law, status in iron_law_checks.items():
        print(f"  {'✅' if status else '❌'} {law}")

    print(f"\n  退出代码: {0 if total_failed == 0 else 1}")
    print()
    return total_failed == 0


if __name__ == "__main__":
    success = main()
    sys.exit(0 if success else 1)
