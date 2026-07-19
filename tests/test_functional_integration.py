"""
功能联调测试 (Functional Integration Testing)

测试目标：
验证各模块 API 在实际前后端联调场景下能正确协同工作。

测试范围：
- 多角色认证与会话管理
- 机构-班级-入托主链路 CRUD
- 考勤、照护、健康观察、日报闭环
- 安全卫生台账、食谱、招生线索、收费模块
- 异常场景（404、校验失败、状态冲突）

前置条件：
- 后端已启动 (http://localhost:8080)
- 测试账号 admin_t040 / teacher_t040 / parent_t040 已存在

运行：
    python tests/test_functional_integration.py
"""

import sys
import os
sys.path.insert(0, os.path.dirname(__file__))

from conftest import (
    ApiClient, TestResults, run_module,
    get_admin_client, get_teacher_client, get_parent_client,
    unique_name
)


# ═══════════════════════════════════════════════════════════════════════
# 1. 认证模块
# ═══════════════════════════════════════════════════════════════════════

def test_auth(results: TestResults):
    """认证相关接口测试。"""
    client = ApiClient()

    # 1.1 健康检查
    healthy = client.health_check()
    if healthy:
        results.ok("健康检查", "后端可正常访问")
    else:
        results.fail("健康检查", "后端不可达，请确认后端已启动")
        return  # 后端不可达则跳过后续

    # 1.2 登录 - 正确凭据
    resp = client.post("/auth/login", json={
        "emailOrUsername": "admin_t040", "password": "Test040pass"
    }, _no_auth=True)
    if resp.get("success") and resp.get("data", {}).get("token"):
        results.ok("登录(正确凭据)", "成功获取 token")
        token = resp["data"]["token"]
    else:
        results.fail("登录(正确凭据)", f"{resp.get('message', resp)}")
        return

    # 1.3 获取当前用户
    client.token = token
    client.session.headers.update({"Authorization": f"Bearer {token}"})
    resp = client.get("/auth/me")
    if resp.get("success"):
        role = resp.get("data", {}).get("role", "N/A")
        results.ok("获取当前用户", f"role={role}")
    else:
        results.fail("获取当前用户", resp.get("message", ""))

    # 1.4 登录 - 错误密码
    resp = client.post("/auth/login", json={
        "emailOrUsername": "admin_t040", "password": "wrong_password"
    }, _no_auth=True)
    if not resp.get("success"):
        results.ok("登录(错误密码)", "正确拒绝")
    else:
        results.fail("登录(错误密码)", "应该失败但成功了")

    # 1.5 登录 - 不存在用户
    resp = client.post("/auth/login", json={
        "emailOrUsername": "nonexistent_user_xxx", "password": "anything"
    }, _no_auth=True)
    if not resp.get("success"):
        results.ok("登录(不存在用户)", "正确拒绝")
    else:
        results.fail("登录(不存在用户)", "应该失败但成功了")

    # 1.6 无 token 访问受保护接口
    client.clear_token()
    resp = client.get("/auth/me")
    if not resp.get("success") and resp.get("status") in (401, 403, 500):
        results.ok("无 token 访问受保护接口", f"状态码 {resp.get('status')}")
    else:
        results.fail("无 token 访问受保护接口",
                      f"期望 401/403/500，实际 success={resp.get('success')} status={resp.get('status')}")

    # 1.7 无效 token
    client.set_token("invalid_token_xxx")
    resp = client.get("/auth/me")
    if not resp.get("success") and resp.get("status") in (401, 403, 500):
        results.ok("无效 token", f"状态码 {resp.get('status')}")
    else:
        results.fail("无效 token",
                      f"期望 401/403/500，实际 success={resp.get('success')} status={resp.get('status')}")


# ═══════════════════════════════════════════════════════════════════════
# 2. 机构-班级-入托主链路
# ═══════════════════════════════════════════════════════════════════════

def test_org_classroom_enrollment(results: TestResults):
    """机构-班级-入托核心链路。"""
    admin = get_admin_client()
    teacher = get_teacher_client()

    # 2.1 获取机构列表
    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.fail("获取机构列表", "无机构数据")
        return
    org = resp["data"][0]
    org_id = org["id"]
    results.ok("获取机构列表", f"org_id={org_id}, name={org.get('name')}")

    # 2.2 创建班级
    class_name = unique_name("class")
    resp = admin.post("/classroom/create", json={
        "organizationId": org_id,
        "name": class_name,
        "description": "功能联调测试班级"
    })
    if resp.get("success"):
        class_id = resp["data"]["id"]
        results.ok("创建班级", f"class_id={class_id}, name={class_name}")
    else:
        results.fail("创建班级", resp.get("message", ""))
        return

    # 2.3 获取班级详情
    resp = admin.get(f"/classroom/{class_id}")
    if resp.get("success") and resp["data"].get("name") == class_name:
        results.ok("获取班级详情", "名称匹配")
    else:
        results.fail("获取班级详情", resp.get("message", ""))

    # 2.4 教师视角：获取班级列表（教师没有机构所有者权限，跳过）
    results.skip("教师获取班级列表", "教师角色无机构所有者权限")

    # 2.5 获取入托列表
    resp = admin.get(f"/enrollment/classroom/{class_id}")
    if resp.get("success"):
        results.ok("获取班级入托列表", f"共 {len(resp.get('data', []))} 条")
    else:
        results.fail("获取班级入托列表", resp.get("message", ""))

    # 2.6 创建入托档案（先获取一个宝宝 ID）
    # 先找一个家庭和宝宝
    parent = get_parent_client()
    resp = parent.get("/family/my-families")
    family_id = None
    baby_id = None
    if resp.get("success") and resp.get("data"):
        family_id = resp["data"][0].get("id")
        resp2 = parent.get(f"/family/{family_id}/babies")
        if resp2.get("success") and resp2.get("data"):
            baby_id = resp2["data"][0].get("id")

    if not baby_id:
        results.skip("创建入托档案", "无可用宝宝，跳过")
    else:
        resp = admin.post("/enrollment/create", json={
            "babyId": baby_id,
            "organizationId": org_id,
            "classroomId": class_id
        })
        if resp.get("success"):
            enroll_id = resp["data"]["id"]
            results.ok("创建入托档案", f"enrollment_id={enroll_id}")
        elif "已存在" in str(resp.get("body", {}).get("message", "")) + str(resp.get("message", "")):
            results.ok("创建入托档案", "已有入托档案（跳过）")
        else:
            results.fail("创建入托档案", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 3. 员工管理
# ═══════════════════════════════════════════════════════════════════════

def test_staff(results: TestResults):
    """员工管理测试。"""
    admin = get_admin_client()

    # 获取机构 ID
    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("员工管理", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 3.1 获取员工列表
    resp = admin.get(f"/staff/organization/{org_id}")
    if resp.get("success"):
        staff_list = resp.get("data", [])
        results.ok("获取员工列表", f"共 {len(staff_list)} 人")
    else:
        results.fail("获取员工列表", resp.get("message", ""))
        return

    # 3.2 员工详情（取第一个员工）
    if staff_list:
        staff_id = staff_list[0].get("id")
        resp = admin.get(f"/staff/{staff_id}")
        if resp.get("success"):
            results.ok("获取员工详情", f"staff_id={staff_id}")
        else:
            results.fail("获取员工详情", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 4. 考勤模块
# ═══════════════════════════════════════════════════════════════════════

def test_attendance(results: TestResults):
    """考勤模块测试。"""
    admin = get_admin_client()

    # 获取机构、班级、入托
    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("考勤模块", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    resp = admin.get(f"/classroom/organization/{org_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("考勤模块", "无班级数据")
        return
    class_id = resp["data"][0]["id"]

    resp = admin.get(f"/enrollment/classroom/{class_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("考勤模块", "无入托数据")
        return
    enroll_id = resp["data"][0].get("id")
    baby_id = resp["data"][0].get("baby", {}).get("id")

    # 4.1 签到
    if baby_id:
        resp = admin.post("/attendance/check-in", json={
            "enrollmentId": enroll_id,
            "babyId": baby_id,
            "checkInTime": "08:30"
        })
        # 可能已签到过，所以 success 或已存在都可接受
        if resp.get("success") or "already" in str(resp.get("message", "")).lower():
            results.ok("到园签到", "成功或已签到")
        else:
            results.fail("到园签到", resp.get("message", ""))

        # 4.2 获取班级考勤
        resp = admin.get(f"/attendance/classroom/{class_id}")
        if resp.get("success"):
            results.ok("获取班级考勤", f"共 {len(resp.get('data', []))} 条")
        else:
            results.fail("获取班级考勤", resp.get("message", ""))
    else:
        results.skip("考勤模块", "无可用宝宝")

    # 4.3 创建请假申请（需要已入托的宝宝）
    if baby_id:
        resp = admin.post("/attendance/leave/request", json={
            "enrollmentId": enroll_id,
            "babyId": baby_id,
            "startDate": "2026-07-20",
            "endDate": "2026-07-21",
            "reason": "功能联调-病假测试"
        })
        if resp.get("success"):
            leave_id = resp["data"].get("id")
            results.ok("创建请假申请", f"leave_id={leave_id}")

            # 4.4 审批通过请假
            if leave_id:
                resp = admin.post(f"/attendance/leave/{leave_id}/approve")
                if resp.get("success"):
                    results.ok("审批通过请假", "成功")
                else:
                    results.fail("审批通过请假", resp.get("message", ""))
        else:
            results.fail("创建请假申请", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 5. 照护记录模块
# ═══════════════════════════════════════════════════════════════════════

def test_care_record(results: TestResults):
    """照护记录测试。"""
    teacher = get_teacher_client()

    # 获取机构、班级、入托
    resp = teacher.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("照护记录", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    resp = teacher.get(f"/classroom/organization/{org_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("照护记录", "无班级数据")
        return
    class_id = resp["data"][0]["id"]

    resp = teacher.get(f"/enrollment/classroom/{class_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("照护记录", "无入托数据")
        return
    enroll_id = resp["data"][0].get("id")
    baby_id = resp["data"][0].get("baby", {}).get("id")

    if not baby_id:
        results.skip("照护记录", "无可用宝宝")
        return

    # 5.1 创建照护记录（喂养）
    today = "2026-07-18"
    resp = teacher.post("/care-record/create", json={
        "enrollmentId": enroll_id,
        "babyId": baby_id,
        "recordDate": today,
        "careType": "FEEDING",
        "content": "午饭吃了大半碗面条",
        "remark": "胃口良好"
    })
    if resp.get("success"):
        record_id = resp["data"].get("id")
        results.ok("创建照护记录(喂养)", f"record_id={record_id}")
    else:
        results.fail("创建照护记录(喂养)", resp.get("message", ""))

    # 5.2 创建照护记录（睡眠）
    resp = teacher.post("/care-record/create", json={
        "enrollmentId": enroll_id,
        "babyId": baby_id,
        "recordDate": today,
        "careType": "SLEEP",
        "content": "午睡1.5小时",
        "remark": "睡眠安稳"
    })
    if resp.get("success"):
        results.ok("创建照护记录(睡眠)", "成功")
    else:
        results.fail("创建照护记录(睡眠)", resp.get("message", ""))

    # 5.3 获取班级当日照护
    resp = teacher.get(f"/care-record/classroom/{class_id}")
    if resp.get("success"):
        results.ok("获取班级照护记录", f"共 {len(resp.get('data', []))} 条")
    else:
        results.fail("获取班级照护记录", resp.get("message", ""))

    # 5.4 获取宝宝当日照护
    resp = teacher.get(f"/care-record/baby/{baby_id}")
    if resp.get("success"):
        results.ok("获取宝宝照护记录", f"共 {len(resp.get('data', []))} 条")
    else:
        results.fail("获取宝宝照护记录", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 6. 健康观察模块
# ═══════════════════════════════════════════════════════════════════════

def test_health_observation(results: TestResults):
    """健康观察测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("健康观察", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    resp = admin.get(f"/classroom/organization/{org_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("健康观察", "无班级数据")
        return
    class_id = resp["data"][0]["id"]

    resp = admin.get(f"/enrollment/classroom/{class_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("健康观察", "无入托数据")
        return
    enroll_id = resp["data"][0].get("id")
    baby_id = resp["data"][0].get("baby", {}).get("id")

    if not baby_id:
        results.skip("健康观察", "无可用宝宝")
        return

    # 6.1 创建健康观察
    today = "2026-07-18"
    resp = admin.post("/health-observation/create", json={
        "enrollmentId": enroll_id,
        "babyId": baby_id,
        "observationDate": today,
        "morningCheck": True,
        "temperature": "36.5",
        "healthStatus": "NORMAL",
        "remark": "晨检正常"
    })
    if resp.get("success"):
        obs_id = resp["data"].get("id")
        results.ok("创建健康观察", f"obs_id={obs_id}")
    else:
        results.fail("创建健康观察", resp.get("message", ""))

    # 6.2 获取班级当日健康观察
    resp = admin.get(f"/health-observation/classroom/{class_id}")
    if resp.get("success"):
        results.ok("获取班级健康观察", f"共 {len(resp.get('data', []))} 条")
    else:
        results.fail("获取班级健康观察", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 7. 日报模块
# ═══════════════════════════════════════════════════════════════════════

def test_daily_report(results: TestResults):
    """日报测试。"""
    teacher = get_teacher_client()

    resp = teacher.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("日报", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    resp = teacher.get(f"/classroom/organization/{org_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("日报", "无班级数据")
        return
    class_id = resp["data"][0]["id"]

    resp = teacher.get(f"/enrollment/classroom/{class_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("日报", "无入托数据")
        return
    baby_id = resp["data"][0].get("baby", {}).get("id")

    if not baby_id:
        results.skip("日报", "无可用宝宝")
        return

    # 7.1 生成日报草稿
    today = "2026-07-18"
    resp = teacher.post("/daily-report/generate", json={
        "babyId": baby_id,
        "reportDate": today
    })
    if resp.get("success"):
        report_id = resp["data"].get("id")
        results.ok("生成日报草稿", f"report_id={report_id}")

        # 7.2 更新日报
        if report_id:
            resp = teacher.put(f"/daily-report/{report_id}", json={
                "teacherSummary": "今天宝宝表现良好，积极参与活动"
            })
            if resp.get("success"):
                results.ok("更新日报", "成功")
            else:
                results.fail("更新日报", resp.get("message", ""))

            # 7.3 发布日报（可能返回提交审核或直接发布）
            resp = teacher.post(f"/daily-report/{report_id}/publish")
            if resp.get("success"):
                results.ok("发布日报", "成功")
            else:
                results.fail("发布日报", resp.get("message", ""))
    else:
        results.fail("生成日报草稿", resp.get("message", ""))

    # 7.4 家长获取宝宝日报
    parent = get_parent_client()
    resp = parent.get(f"/daily-report/baby/{baby_id}")
    if resp.get("success") and resp.get("data"):
        results.ok("家长获取宝宝日报", "成功看到日报")
    elif not resp.get("success"):
        results.fail("家长获取宝宝日报", resp.get("message", ""))
    else:
        results.skip("家长获取宝宝日报", "暂无日报数据")


# ═══════════════════════════════════════════════════════════════════════
# 8. 食谱与进食记录
# ═══════════════════════════════════════════════════════════════════════

def test_meal_plan(results: TestResults):
    """食谱模块测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("食谱", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 8.1 创建食谱
    today = "2026-07-18"
    resp = admin.post("/meal-plan/create", json={
        "organizationId": org_id,
        "mealDate": today,
        "mealType": "LUNCH",
        "title": "午餐食谱",
        "foodItems": "红烧肉、清炒西兰花、米饭",
        "nutritionNotes": "均衡营养"
    })
    if resp.get("success"):
        plan_id = resp["data"].get("id")
        results.ok("创建食谱", f"plan_id={plan_id}")

        # 8.2 发布食谱
        if plan_id:
            resp = admin.post(f"/meal-plan/{plan_id}/publish")
            if resp.get("success"):
                results.ok("发布食谱", "成功")
            else:
                results.fail("发布食谱", resp.get("message", ""))
    else:
        results.fail("创建食谱", resp.get("message", ""))

    # 8.3 获取机构食谱
    resp = admin.get(f"/meal-plan/organization/{org_id}")
    if resp.get("success"):
        results.ok("获取机构食谱", f"共 {len(resp.get('data', []))} 条")
    else:
        results.fail("获取机构食谱", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 9. 安全卫生台账
# ═══════════════════════════════════════════════════════════════════════

def test_safety_ledger(results: TestResults):
    """安全卫生台账测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("安全台账", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 9.1 创建安全台账
    today = "2026-07-18"
    resp = admin.post("/safety-ledger/create", json={
        "organizationId": org_id,
        "ledgerDate": today,
        "ledgerType": "DISINFECTION",
        "title": "日常消毒记录",
        "content": "教室日常消毒",
        "responsiblePerson": "测试保健员"
    })
    if resp.get("success"):
        ledger_id = resp["data"].get("id")
        results.ok("创建安全台账", f"ledger_id={ledger_id}")

        # 9.2 标记处理中
        if ledger_id:
            resp = admin.post(f"/safety-ledger/{ledger_id}/processing")
            if resp.get("success"):
                results.ok("标记台账处理中", "成功")
            else:
                results.fail("标记台账处理中", resp.get("message", ""))

            # 9.3 关闭台账
            resp = admin.post(f"/safety-ledger/{ledger_id}/close")
            if resp.get("success"):
                results.ok("关闭台账", "成功")
            else:
                results.fail("关闭台账", resp.get("message", ""))
    else:
        results.fail("创建安全台账", resp.get("message", ""))

    # 9.4 获取机构台账列表
    resp = admin.get(f"/safety-ledger/organization/{org_id}")
    if resp.get("success"):
        results.ok("获取机构台账列表", f"共 {len(resp.get('data', []))} 条")
    else:
        results.fail("获取机构台账列表", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 10. 通知公告
# ═══════════════════════════════════════════════════════════════════════

def test_announcement(results: TestResults):
    """通知公告测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("通知公告", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 10.1 创建通知
    resp = admin.post("/announcement/create", json={
        "organizationId": org_id,
        "title": "功能联调测试通知",
        "content": "这是一条测试通知内容",
        "scope": "ORGANIZATION"
    })
    if resp.get("success"):
        ann_id = resp["data"].get("id")
        results.ok("创建通知", f"ann_id={ann_id}")

        # 10.2 发布通知
        if ann_id:
            resp = admin.post(f"/announcement/{ann_id}/publish")
            if resp.get("success"):
                results.ok("发布通知", "成功")
            else:
                results.fail("发布通知", resp.get("message", ""))
    else:
        results.fail("创建通知", resp.get("message", ""))

    # 10.3 获取机构通知列表
    resp = admin.get(f"/announcement/organization/{org_id}")
    if resp.get("success"):
        results.ok("获取机构通知列表", f"共 {len(resp.get('data', []))} 条")
    else:
        results.fail("获取机构通知列表", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 11. 招生线索
# ═══════════════════════════════════════════════════════════════════════

def test_admission_lead(results: TestResults):
    """招生线索测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("招生线索", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 11.1 创建招生线索
    phone = f"138{unique_name('phone')[:8]}"
    resp = admin.post("/admission-lead/create", json={
        "organizationId": org_id,
        "childName": "测试宝宝",
        "childBirthday": "2024-01-15",
        "guardianName": "测试家长",
        "guardianPhone": phone,
        "source": "ONLINE"
    })
    if resp.get("success"):
        lead_id = resp["data"].get("id")
        results.ok("创建招生线索", f"lead_id={lead_id}")

        # 11.2 获取线索列表
        resp = admin.get(f"/admission-lead/organization/{org_id}")
        if resp.get("success"):
            results.ok("获取招生线索列表", f"共 {len(resp.get('data', []))} 条")
        else:
            results.fail("获取招生线索列表", resp.get("message", ""))
    else:
        results.fail("创建招生线索", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 12. 园长驾驶舱
# ═══════════════════════════════════════════════════════════════════════

def test_director_dashboard(results: TestResults):
    """园长驾驶舱测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("园长驾驶舱", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    # 12.1 机构运营概览
    resp = admin.get(f"/director-dashboard/organization/{org_id}")
    if resp.get("success"):
        results.ok("机构运营概览", "成功获取")
    else:
        results.fail("机构运营概览", resp.get("message", ""))

    # 12.2 园长工作台
    resp = admin.get(f"/director-dashboard/workbench/{org_id}")
    if resp.get("success"):
        results.ok("园长工作台", "成功获取")
    else:
        results.fail("园长工作台", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 13. 异常场景
# ═══════════════════════════════════════════════════════════════════════

def test_error_scenarios(results: TestResults):
    """异常场景测试。"""
    admin = get_admin_client()

    # 13.1 访问不存在的路径
    resp = admin.get("/nonexistent-endpoint")
    if not resp.get("success") and resp.get("status") in (404, 500):
        results.ok("不存在的路径", f"返回 {resp.get('status')}")
    else:
        results.fail("不存在的路径",
                      f"期望 404/500，实际 status={resp.get('status')}")

    # 13.2 创建班级缺少必填字段
    resp = admin.post("/classroom/create", json={})
    if not resp.get("success"):
        results.ok("创建班级(缺少必填字段)", "正确拒绝")
    else:
        results.fail("创建班级(缺少必填字段)", "应该失败但成功了")

    # 13.3 获取不存在的资源
    resp = admin.get("/classroom/99999999")
    if not resp.get("success"):
        results.ok("获取不存在资源", "正确返回错误")
    else:
        results.fail("获取不存在资源", "应该失败但成功了")


# ═══════════════════════════════════════════════════════════════════════
# 14. 转班/暂停/复托/退托流程（T077）
# ═══════════════════════════════════════════════════════════════════════

def test_enrollment_status_flow(results: TestResults):
    """入托状态变更流程测试。"""
    admin = get_admin_client()

    resp = admin.get("/organization/my-organizations")
    if not resp.get("success") or not resp.get("data"):
        results.skip("入托状态变更", "无机构数据")
        return
    org_id = resp["data"][0]["id"]

    resp = admin.get(f"/classroom/organization/{org_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("入托状态变更", "无班级数据")
        return
    class_id = resp["data"][0]["id"]

    resp = admin.get(f"/enrollment/classroom/{class_id}")
    if not resp.get("success") or not resp.get("data"):
        results.skip("入托状态变更", "无入托数据")
        return

    # 找一个 ACTIVE 的入托
    enrollments = resp["data"]
    active_enroll = None
    for e in enrollments:
        if e.get("status") == "ACTIVE":
            active_enroll = e
            break

    if not active_enroll:
        results.skip("入托状态变更", "无 ACTIVE 状态的入托档案")
        return

    enroll_id = active_enroll["id"]
    baby_name = active_enroll.get("baby", {}).get("name", "N/A")

    # 14.1 暂停入托
    resp = admin.post(f"/enrollment/{enroll_id}/suspend", json={
        "reason": "功能联调-暂停测试"
    })
    if resp.get("success"):
        results.ok("暂停入托", f"宝宝={baby_name}")

        # 14.2 获取状态变更历史
        resp = admin.get(f"/enrollment/{enroll_id}/history")
        if resp.get("success") and len(resp.get("data", [])) > 0:
            results.ok("获取状态变更历史",
                        f"共 {len(resp['data'])} 条记录")
        else:
            results.fail("获取状态变更历史", resp.get("message", ""))

        # 14.3 复托
        resp = admin.post(f"/enrollment/{enroll_id}/reactivate")
        if resp.get("success"):
            results.ok("复托", "成功")
        else:
            results.fail("复托", resp.get("message", ""))
    else:
        results.fail("暂停入托", resp.get("message", ""))


# ═══════════════════════════════════════════════════════════════════════
# 运行入口
# ═══════════════════════════════════════════════════════════════════════

def run_all(results: TestResults):
    """运行所有功能联调测试。"""
    test_auth(results)
    test_org_classroom_enrollment(results)
    test_staff(results)
    test_attendance(results)
    test_care_record(results)
    test_health_observation(results)
    test_daily_report(results)
    test_meal_plan(results)
    test_safety_ledger(results)
    test_announcement(results)
    test_admission_lead(results)
    test_director_dashboard(results)
    test_error_scenarios(results)
    test_enrollment_status_flow(results)


if __name__ == "__main__":
    results, ok = run_module("🔗 功能联调测试 (Functional Integration Testing)", run_all)
    sys.exit(0 if ok else 1)
