#!/usr/bin/env python
"""
好芽儿 BabyCare/HuiGrowth 全链路测试执行器

功能：
1. 清空业务数据（保留管理员账户 + 系统配置、角色、菜单、权限）
2. 执行 E2E 全链路测试套件（8 个阶段）
3. 收集测试结果，生成 Markdown 测试报告

用法：
    cd d:/workspace/babycare
    python tests/full_test_scripts/run_clean_test_report.py

依赖：
    pip install pymysql requests
"""

import sys
import os
import time
import json
import subprocess
import traceback
from datetime import datetime

# ─── 确保能找到其他模块 ───────────────────────────────────────────────
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))
sys.path.insert(0, SCRIPT_DIR)
sys.path.insert(0, os.path.join(SCRIPT_DIR, ".."))

# ─── 数据库配置（从 application.properties 同步） ──────────────────────
DB_CONFIG = {
    "host": "localhost",
    "port": 3306,
    "user": "root",
    "password": "yongc20",
    "database": "huigrowth_dev",
    "charset": "utf8mb4",
}

MYSQL_CLI = r'E:\app\MySQL80\bin\mysql'

# ─── 需要保留的表（系统基础数据） ─────────────────────────────────────
SYSTEM_TABLES = {
    "users",             # 所有用户（含 admin + 测试账号）
    "sys_user_role",     # 用户角色关联
    "sys_role",          # 角色定义
    "sys_role_menu",     # 角色菜单关联
    "sys_role_permission",  # 角色权限关联
    "sys_menu",          # 菜单
    "sys_permission",    # 权限
    "sys_config",        # 系统配置
    "sys_data_dict",     # 数据字典
}

# ─── 清空顺序（按外键依赖排序，防止 FK 冲突） ────────────────────────
CLEAR_TABLES_ORDER = [
    # 聊天/AI
    "ai_chat_messages",
    "ai_chat_sessions",
    "ai_chats",
    # 通知公告
    "announcement_receipts",
    "announcements",
    # 用药
    "medication_administrations",
    "medication_requests",
    # 传染病
    "infectious_diseases",
    # 事故/异常
    "incident_reports",
    # 食谱/膳食
    "meal_intake_records",
    "meal_plans",
    # 安全台账
    "safety_ledgers",
    "safety_ledger_templates",
    # 请假
    "leave_requests",
    # 健康观察
    "health_observations",
    # 日报
    "daily_reports",
    # 照护记录
    "care_records",
    # 考勤
    "attendance_records",
    # 发展评估 / 成长记录
    "child_development_assessments",
    "development_assessments",
    "education_activities",
    "education_plans",
    "growth_records",
    "milestones",
    # 过敏标签（多对多关联表）
    "allergy_tags",
    # 招生线索
    "follow_up_records",
    "admission_leads",
    # 账单/费用
    "billing_statements",
    "fee_items",
    # 硬件
    "hardware_events",
    "hardware_devices",
    # 入托相关
    "enrollment_status_history",
    "enrollment_guardians",
    "pickup_delegations",
    "authorized_pickup_persons",
    "enrollments",
    # 家庭
    "family_tasks",
    "family_posts",
    "family_members",
    "families",
    # 宝宝
    "babies",
    # 员工/班级/机构
    "org_classroom_staff",
    "staff_classroom",
    "staff",
    "classrooms",
    "org_group",
    "organizations",
    # 审计日志（可选清理，保留也可）
    "sys_audit_log",
]


# ═══════════════════════════════════════════════════════════════════════
# 数据库清理
# ═══════════════════════════════════════════════════════════════════════

def clear_business_data():
    """清空所有业务表，保留系统基础表。"""
    import pymysql

    conn = pymysql.connect(**DB_CONFIG)
    cur = conn.cursor()

    try:
        # 1. 禁用外键检查
        cur.execute("SET FOREIGN_KEY_CHECKS = 0")

        # 2. 清空业务表
        cleared = []
        skipped = []
        for table in CLEAR_TABLES_ORDER:
            try:
                cur.execute(f"DELETE FROM `{table}`")
                cleared.append((table, cur.rowcount))
                print(f"  ✅ 清空 {table}: 删除 {cur.rowcount} 行")
            except Exception as e:
                err_msg = str(e)
                if "doesn't exist" in err_msg or "Unknown table" in err_msg:
                    skipped.append(table)
                else:
                    print(f"  ⚠️  清空 {table} 失败: {err_msg}")
                    skipped.append(table)

        # 3. 重置 AUTO_INCREMENT（除了系统表）
        system_tables_lower = {t.lower() for t in SYSTEM_TABLES}
        for table in CLEAR_TABLES_ORDER:
            if table.lower() not in system_tables_lower:
                try:
                    cur.execute(f"ALTER TABLE `{table}` AUTO_INCREMENT = 1")
                except Exception:
                    pass  # 忽略自增重置失败

        # 4. 重新启用外键检查
        cur.execute("SET FOREIGN_KEY_CHECKS = 1")
        conn.commit()

        print(f"\n  📊 清空完成: {len(cleared)} 张表已清空, {len(skipped)} 张跳过")
        return True, cleared, skipped

    except Exception as e:
        conn.rollback()
        print(f"\n  ❌ 数据库清空异常: {e}")
        traceback.print_exc()
        return False, [], []

    finally:
        cur.close()
        conn.close()


# ═══════════════════════════════════════════════════════════════════════
# 运行测试套件并收集结果
# ═══════════════════════════════════════════════════════════════════════

def run_test_suite():
    """运行 E2E 测试套件，捕获 stdout/stderr 输出。"""
    suite_script = os.path.join(SCRIPT_DIR, "run_e2e_suite.py")
    
    env = os.environ.copy()
    env["PYTHONUNBUFFERED"] = "1"
    env["PYTHONIOENCODING"] = "utf-8"
    
    print("\n  🚀 启动 E2E 测试套件...\n")
    sys.stdout.flush()
    
    # 使用临时文件捕获输出，避免编码和管道缓冲问题
    import tempfile
    tmp_stdout = tempfile.NamedTemporaryFile(mode="w+", encoding="utf-8", delete=False, suffix=".log")
    tmp_stdout.close()
    
    try:
        with open(tmp_stdout.name, "w", encoding="utf-8") as f:
            process = subprocess.run(
                [sys.executable, suite_script],
                cwd=PROJECT_ROOT,
                env=env,
                stdout=f,
                stderr=subprocess.STDOUT,
                timeout=1800,  # 30 分钟超时
            )
        
        with open(tmp_stdout.name, "r", encoding="utf-8") as f:
            full_output = f.read()
        
        # 也打印一些输出到控制台
        lines = full_output.split("\n")
        for line in lines[-50:]:
            print(f"  | {line}")
        
        return process.returncode, full_output, ""
    except subprocess.TimeoutExpired:
        print("  ⚠️  测试套件执行超时（>30分钟）")
        with open(tmp_stdout.name, "r", encoding="utf-8") as f:
            full_output = f.read()
        return -1, full_output, "TIMEOUT"
    finally:
        try:
            os.unlink(tmp_stdout.name)
        except:
            pass


# ═══════════════════════════════════════════════════════════════════════
# 解析测试结果
# ═══════════════════════════════════════════════════════════════════════

def parse_test_results(stdout: str):
    """从测试输出中解析通过/失败/跳过的用例。"""
    import re
    
    results = {
        "total": 0, "passed": 0, "failed": 0, "skipped": 0,
        "pass_rate": 0.0, "elapsed": 0.0,
        "phases": [],
        "failed_cases": [],
        "iron_law_checks": {},
    }
    
    # 提取汇总信息（只在最终汇总部分搜索）
    # 先找到 "📊 端到端全链路测试汇总报告" 之后的内容
    summary_section = stdout
    idx = stdout.rfind("端到端全链路测试汇总报告")
    if idx >= 0:
        summary_section = stdout[idx:]
    
    summary_patterns = {
        "total": [r"总用例数\s*[:：]\s*(\d+)"],
        "passed": [r"通过\s*[:：]\s*(\d+)"],
        "failed": [r"失败\s*[:：]\s*(\d+)"],
        "skipped": [r"跳过\s*[:：]\s*(\d+)"],
        "pass_rate": [r"通过率\s*[:：]\s*([\d.]+)\s*%"],
        "elapsed": [r"总耗时\s*[:：]\s*([\d.]+)s"],
    }
    
    for key, patterns in summary_patterns.items():
        for pattern in patterns:
            m = re.search(pattern, summary_section)
            if m:
                if key in ("pass_rate", "elapsed"):
                    results[key] = float(m.group(1))
                else:
                    results[key] = int(m.group(1))
                break
    
    if results["total"] > 0 and results["pass_rate"] == 0.0:
        results["pass_rate"] = results["passed"] / results["total"] * 100
    
    # 提取各阶段结果（按 === 分隔的区块解析）
    sections = re.split(r"={2,}", stdout)
    for sec in sections:
        m = re.search(r"套件:\s*(.+?)\n", sec)
        s = re.search(r"总计\s*[:：]\s*(\d+)", sec)
        p = re.search(r"通过\s*[:：]\s*(\d+)", sec)
        f = re.search(r"失败\s*[:：]\s*(\d+)", sec)
        sk = re.search(r"跳过\s*[:：]\s*(\d+)", sec)
        el = re.search(r"耗时\s*[:：]\s*([\d.]+)s", sec)
        if m and s:
            results["phases"].append({
                "name": m.group(1).strip(),
                "total": int(s.group(1)),
                "passed": int(p.group(1)) if p else 0,
                "failed": int(f.group(1)) if f else 0,
                "skipped": int(sk.group(1)) if sk else 0,
                "elapsed": float(el.group(1)) if el else 0.0,
            })
    
    # 提取失败的用例
    # 格式: "  ❌ [DIR-006] 添加宝宝失败: ..."
    for line in stdout.split("\n"):
        line = line.strip()
        m = re.match(r"❌\s*\[(\S+)\]\s*(.*)", line)
        if m:
            results["failed_cases"].append({
                "case_id": m.group(1),
                "detail": m.group(2).strip()[:200],  # 截断过长详情
            })
    
    # 提取铁律检查结果
    iron_pattern = re.compile(r"([✅❌])\s*(铁律[\d]-[\w]+)")
    for m in iron_pattern.finditer(stdout):
        results["iron_law_checks"][m.group(2)] = m.group(1) == "✅"
    
    return results


# ═══════════════════════════════════════════════════════════════════════
# 生成测试报告
# ═══════════════════════════════════════════════════════════════════════

def generate_report(results, stdout, start_time, end_time, db_ok, db_cleared, db_skipped):
    """生成 Markdown 测试报告。"""
    now = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
    total_elapsed = (end_time - start_time).total_seconds()
    
    lines = []
    lines.append(f"# 好芽儿全链路自动化测试报告")
    lines.append("")
    lines.append(f"- **测试日期**: {now}")
    lines.append(f"- **测试环境**: 前端 localhost:3001 / 后端 localhost:8080 / MySQL huigrowth_dev")
    lines.append(f"- **测试套件**: 端到端全链路数据一致性测试套件（8 阶段）")
    lines.append(f"- **测试账号**: 12 种角色（admin、园长、教师、保育员、保健员、安全后勤、财务、运营、招生、家长、长辈、未绑定家长）")
    lines.append(f"- **测试方式**: 清空业务数据 → 执行全链路测试 → 记录缺陷")
    lines.append("")
    
    # ── 目录 ──
    lines.append("## 目录")
    lines.append("")
    lines.append("1. [执行摘要](#1-执行摘要)")
    lines.append("2. [数据清理结果](#2-数据清理结果)")
    lines.append("3. [测试执行结果](#3-测试执行结果)")
    lines.append("4. [各阶段明细](#4-各阶段明细)")
    lines.append("5. [失败用例清单](#5-失败用例清单)")
    lines.append("6. [铁律验证状态](#6-铁律验证状态)")
    lines.append("7. [缺陷记录](#7-缺陷记录)")
    lines.append("8. [原始测试输出](#8-原始测试输出)")
    lines.append("")
    
    # ── 1. 执行摘要 ──
    lines.append("## 1. 执行摘要")
    lines.append("")
    lines.append(f"| 指标 | 值 |")
    lines.append(f"|---|---|")
    lines.append(f"| 执行时间 | {now} |")
    lines.append(f"| 总耗时 | {total_elapsed:.1f}s |")
    lines.append(f"| 数据库清空 | {'✅ 成功' if db_ok else '❌ 失败'} |")
    lines.append(f"| 测试套件退出码 | {results.get('exit_code', 'N/A')} |")
    lines.append(f"| 总用例数 | {results['total']} |")
    lines.append(f"| ✅ 通过 | {results['passed']} |")
    lines.append(f"| ❌ 失败 | {results['failed']} |")
    lines.append(f"| ⏭️ 跳过 | {results['skipped']} |")
    lines.append(f"| 通过率 | {results['pass_rate']:.1f}% |")
    lines.append("")
    
    # ── 2. 数据清理结果 ──
    lines.append("## 2. 数据清理结果")
    lines.append("")
    if db_ok:
        lines.append(f"成功清空 {len(db_cleared)} 张业务数据表，{len(db_skipped)} 张表跳过（不存在）。")
        lines.append("")
        lines.append("### 已清空的表")
        lines.append("")
        lines.append("| 表名 | 删除行数 |")
        lines.append("|---|---|")
        for table, count in db_cleared:
            lines.append(f"| `{table}` | {count} |")
        lines.append("")
        if db_skipped:
            lines.append("### 跳过的表（不存在）")
            lines.append("")
            lines.append("| 表名 |")
            lines.append("|---|")
            for table in db_skipped:
                lines.append(f"| `{table}` |")
            lines.append("")
        lines.append("### 保留的系统表")
        lines.append("")
        for t in sorted(SYSTEM_TABLES):
            lines.append(f"- `{t}`")
        lines.append("")
    else:
        lines.append("❌ 数据库清空失败，请检查数据库连接和权限。")
        lines.append("")
    
    # ── 3. 测试执行结果 ──
    lines.append("## 3. 测试执行结果")
    lines.append("")
    lines.append(f"测试套件退出代码: `{results.get('exit_code', 'N/A')}`")
    lines.append("")
    
    pct = results['pass_rate']
    if pct == 100:
        status_icon = "✅"
    elif pct >= 90:
        status_icon = "⚠️"
    else:
        status_icon = "❌"
    
    lines.append(f"{status_icon} **总体通过率: {results['pass_rate']:.1f}%**")
    lines.append("")
    
    if results["failed"] > 0:
        lines.append("> ⚠️ 以下用例失败，缺陷已记录至第 7 节，本轮不修复。")
        lines.append("")
    
    # ── 4. 各阶段明细 ──
    lines.append("## 4. 各阶段明细")
    lines.append("")
    lines.append("| 阶段 | 总计 | ✅ 通过 | ❌ 失败 | ⏭️ 跳过 | 耗时 |")
    lines.append("|---|---|---|---|---|---|")
    for phase in results["phases"]:
        icon = "✅" if phase["failed"] == 0 else "❌"
        lines.append(f"| {icon} {phase['name']} | {phase['total']} | {phase['passed']} | {phase['failed']} | {phase['skipped']} | {phase['elapsed']}s |")
    if not results["phases"]:
        lines.append("| (未能解析阶段结果) |")
    lines.append("")
    
    # ── 5. 失败用例清单 ──
    lines.append("## 5. 失败用例清单")
    lines.append("")
    if results["failed_cases"]:
        lines.append("| 用例编号 | 失败详情 |")
        lines.append("|---|---|")
        for i, fc in enumerate(results["failed_cases"], 1):
            lines.append(f"| {fc['case_id']} | {fc['detail']} |")
        lines.append("")
    else:
        lines.append("✅ 无失败用例。")
        lines.append("")
    
    # ── 6. 铁律验证状态 ──
    lines.append("## 6. 铁律验证状态")
    lines.append("")
    checks = results.get("iron_law_checks", {})
    if checks:
        lines.append("| 铁律 | 状态 |")
        lines.append("|---|---|")
        for law, status in checks.items():
            icon = "✅" if status else "❌"
            lines.append(f"| {law} | {icon} |")
    else:
        lines.append("未能从输出中解析铁律验证状态。")
    lines.append("")
    
    # ── 7. 缺陷记录 ──
    lines.append("## 7. 缺陷记录")
    lines.append("")
    if results["failed_cases"]:
        lines.append("| 编号 | 用例 | 缺陷描述 | 级别 | 所属模块 |")
        lines.append("|---|---|---|---|---|")
        for i, fc in enumerate(results["failed_cases"], 1):
            # 根据用例编号推断严重级别和模块
            case_id = fc["case_id"]
            detail = fc["detail"]
            
            # 推断级别
            if any(kw in case_id for kw in ["AUTH", "RBAC", "SEC"]):
                level = "Major"
            elif any(kw in case_id for kw in ["CMP", "DATA", "CONSIST"]):
                level = "Major"
            elif any(kw in case_id for kw in ["UI", "DISPLAY"]):
                level = "Minor"
            else:
                level = "Major"
            
            # 推断模块
            if any(kw in case_id for kw in ["DIR"]):
                module = "机构管理 / 园长工作流"
            elif any(kw in case_id for kw in ["TCH", "CAR"]):
                module = "考勤照护 / 教师工作流"
            elif any(kw in case_id for kw in ["HLT"]):
                module = "健康保健 / 保健员工作流"
            elif any(kw in case_id for kw in ["FIN", "OPS", "ADM"]):
                module = "财务运营 / 招生工作流"
            elif any(kw in case_id for kw in ["PAR", "ELD"]):
                module = "家长端 / 长辈模式"
            elif any(kw in case_id for kw in ["RBAC"]):
                module = "权限管理 / RBAC"
            elif any(kw in case_id for kw in ["CMP"]):
                module = "补偿流程"
            elif any(kw in case_id for kw in ["CONSIST", "DATA"]):
                module = "数据一致性"
            else:
                module = "通用"
            
            lines.append(f"| BUG-{i:03d} | {case_id} | {detail} | {level} | {module} |")
        lines.append("")
        lines.append("**缺陷处理策略**: 本轮仅记录不修复，后续统一安排修复。")
    else:
        lines.append("✅ 本轮测试未发现缺陷。")
    lines.append("")
    
    # ── 8. 原始测试输出 ──
    lines.append("## 8. 原始测试输出")
    lines.append("")
    lines.append("```")
    # 限制原始输出长度，避免报告过大
    max_output_lines = 500
    output_lines = stdout.split("\n")
    if len(output_lines) > max_output_lines:
        lines.append(f"（输出过长，仅显示前 {max_output_lines} 行，完整输出见控制台日志）")
        lines.append("")
        lines.extend(output_lines[:max_output_lines])
        lines.append("")
        lines.append(f"... (剩余 {len(output_lines) - max_output_lines} 行已截断)")
    else:
        lines.extend(output_lines)
    lines.append("```")
    lines.append("")
    
    lines.append("---")
    lines.append("")
    lines.append(f"*报告生成时间: {now}*")
    lines.append("*本报告由 `run_clean_test_report.py` 自动生成*")
    
    return "\n".join(lines)


# ═══════════════════════════════════════════════════════════════════════
# 主流程
# ═══════════════════════════════════════════════════════════════════════

def main():
    start_time = datetime.now()
    
    print("=" * 70)
    print("  好芽儿 BabyCare/HuiGrowth 全链路自动化测试执行器")
    print("=" * 70)
    print(f"  开始时间: {start_time.strftime('%Y-%m-%d %H:%M:%S')}")
    print(f"  数据库:   {DB_CONFIG['host']}:{DB_CONFIG['port']}/{DB_CONFIG['database']}")
    print(f"  后端:     http://localhost:8080/api")
    print()
    
    # ── 第一步: 后端健康检查 ──
    print("─" * 70)
    print("  步骤 1/4: 后端健康检查")
    print("─" * 70)
    try:
        import requests
        resp = requests.get("http://localhost:8080/api/public/health", timeout=5)
        if resp.status_code == 200:
            print("  ✅ 后端服务正常")
        else:
            print(f"  ⚠️  后端返回状态码: {resp.status_code}，继续执行")
    except Exception as e:
        print(f"  ❌ 后端不可达: {e}")
        print("     请先启动后端: .\\start-dev.bat")
        return False
    print()
    
    # ── 第二步: 清空业务数据 ──
    print("─" * 70)
    print("  步骤 2/4: 清空业务数据（保留管理员账户 + 系统配置）")
    print("─" * 70)
    db_ok, db_cleared, db_skipped = clear_business_data()
    print()
    
    if not db_ok:
        print("  ❌ 数据库清空失败，终止执行")
        return False
    
    # ── 第三步: 执行 E2E 测试 ──
    print("─" * 70)
    print("  步骤 3/4: 执行 E2E 全链路测试套件（8 阶段）")
    print("─" * 70)
    exit_code, full_output, _stderr = run_test_suite()
    results = parse_test_results(full_output)
    results["exit_code"] = exit_code
    print(f"\n  测试套件退出代码: {exit_code}")
    print()
    
    # ── 第四步: 生成测试报告 ──
    print("─" * 70)
    print("  步骤 4/4: 生成测试报告")
    print("─" * 70)
    end_time = datetime.now()
    report_content = generate_report(results, full_output, start_time, end_time, db_ok, db_cleared, db_skipped)
    
    report_dir = os.path.join(PROJECT_ROOT, "docs", "06-测试")
    os.makedirs(report_dir, exist_ok=True)
    
    report_filename = f"自动化测试报告_{start_time.strftime('%Y%m%d_%H%M%S')}.md"
    report_path = os.path.join(report_dir, report_filename)
    
    with open(report_path, "w", encoding="utf-8") as f:
        f.write(report_content)
    
    print(f"  ✅ 测试报告已生成: {report_path}")
    print()
    
    # ── 最终汇总 ──
    print("=" * 70)
    print("  📊 最终汇总")
    print("=" * 70)
    print(f"  数据库清空: {'✅' if db_ok else '❌'}")
    print(f"  总用例:     {results['total']}")
    print(f"  ✅ 通过:     {results['passed']}")
    print(f"  ❌ 失败:     {results['failed']}")
    print(f"  ⏭️ 跳过:     {results['skipped']}")
    print(f"  通过率:     {results['pass_rate']:.1f}%")
    print(f"  测试报告:   {report_path}")
    print(f"  总耗时:     {(end_time - start_time).total_seconds():.1f}s")
    print("=" * 70)
    print()
    
    if results["failed"] > 0:
        print(f"  ⚠️  {results['failed']} 个用例失败，缺陷已记录在测试报告中，本轮不修复。")
        print()
    
    return results["failed"] == 0


if __name__ == "__main__":
    try:
        success = main()
        sys.exit(0 if success else 1)
    except KeyboardInterrupt:
        print("\n\n  ⚠️  用户中断执行")
        sys.exit(130)
    except Exception as e:
        print(f"\n\n  ❌ 执行异常: {e}")
        traceback.print_exc()
        sys.exit(2)
