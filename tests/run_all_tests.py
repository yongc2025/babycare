"""
BabyCare/HuiGrowth 全量测试运行器。

按顺序依次运行：
1. 🔗 功能联调测试   (test_functional_integration.py)
2. ⚡ 性能测试       (test_performance.py)
3. 🛡️ 安全测试       (test_security.py)
4. 🧪 验收测试       (test_acceptance.py)

用法：
    # 运行全部测试
    python tests/run_all_tests.py

    # 运行指定模块
    python tests/test_functional_integration.py
    python tests/test_performance.py
    python tests/test_security.py
    python tests/test_acceptance.py
"""

import sys
import os
import time

sys.path.insert(0, os.path.dirname(__file__))

from conftest import TestResults, run_module


TEST_MODULES = [
    ("🔗 功能联调测试", "test_functional_integration", "Functional Integration Testing"),
    ("⚡ 性能测试",       "test_performance",           "Performance Testing"),
    ("🛡️ 安全测试",       "test_security",              "Security Testing"),
    ("🧪 验收测试",       "test_acceptance",            "Acceptance Testing"),
]


def main():
    print("=" * 60)
    print("🏥 BabyCare/HuiGrowth 全量测试套件")
    print("=" * 60)
    print(f"开始时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print()

    overall_passed = 0
    overall_failed = 0
    overall_skipped = 0
    module_results = []

    for display_name, module_name, suite_name in TEST_MODULES:
        try:
            mod = __import__(module_name)
            if hasattr(mod, "run_all"):
                results = TestResults(display_name)
                print(f"\n{'='*60}")
                print(f"▶ 运行: {display_name}")
                print(f"{'='*60}")
                mod.run_all(results)
                module_results.append((display_name, results))
                overall_passed += len(results.passed)
                overall_failed += len(results.failed)
                overall_skipped += len(results.skipped)
            else:
                print(f"\n⏭️  跳过 {display_name}: 未找到 run_all 函数")
        except Exception as e:
            print(f"\n💥 加载 {display_name} 失败: {e}")
            overall_failed += 1

    # ── 总汇总 ───────────────────────────────────────────────────────────
    print(f"\n\n{'='*60}")
    print("📊 全量测试汇总")
    print(f"{'='*60}")
    grand_total = overall_passed + overall_failed + overall_skipped
    print(f"   总用例: {grand_total}")
    print(f"   ✅ 通过: {overall_passed}")
    print(f"   ❌ 失败: {overall_failed}")
    print(f"   ⏭️ 跳过: {overall_skipped}")
    print()

    for name, results in module_results:
        status = "✅" if len(results.failed) == 0 else "❌"
        print(f"   {status} {name}: "
              f"通过 {len(results.passed)} / 失败 {len(results.failed)} / 跳过 {len(results.skipped)}")

    print(f"\n结束时间: {time.strftime('%Y-%m-%d %H:%M:%S')}")
    print()

    if overall_failed > 0:
        print("⚠️  有测试未通过，请查看各模块详细日志。")
    else:
        print("🎉 所有测试通过！")

    return 0 if overall_failed == 0 else 1


if __name__ == "__main__":
    # 检查依赖
    try:
        import requests
    except ImportError:
        print("❌ 请先安装依赖: pip install -r tests/requirements.txt", file=sys.stderr)
        sys.exit(1)

    sys.exit(main())
