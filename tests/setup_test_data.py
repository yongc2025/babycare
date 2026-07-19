"""
测试数据初始化脚本
在运行测试前执行，创建测试账号和基础数据。

用法：
    python tests/setup_test_data.py
"""

import sys
import os
import time

sys.path.insert(0, os.path.dirname(__file__))
from conftest import ApiClient, unique_name, BASE_URL


def setup_admin_account(client):
    """创建或确认管理员账号。"""
    # 先用系统管理员登录
    resp = client.post("/auth/login", json={
        "emailOrUsername": "admin", "password": "admin123"
    }, _no_auth=True)
    if resp.get("success"):
        token = resp["data"]["token"]
        client.token = token
        client.session.headers.update({"Authorization": f"Bearer {token}"})
        print("  ✅ 系统管理员 admin 登录成功")
        return True
    else:
        print(f"  ❌ 系统管理员登录失败: {resp.get('message', '')}")
        return False


def create_test_accounts(client):
    """创建测试账号 admin_t040 / teacher_t040 / parent_t040。"""
    accounts = {
        "admin_t040": {"password": "Test040pass", "nickname": "管理员T040", "role": "ADMIN"},
        "teacher_t040": {"password": "Test040pass", "nickname": "教师T040", "role": "TEACHER"},
        "parent_t040": {"password": "Test040pass", "nickname": "家长T040", "role": "PARENT"},
    }

    created = []
    for username, info in accounts.items():
        # 检查是否已存在
        resp = client.post("/auth/login", json={
            "emailOrUsername": username, "password": info["password"]
        }, _no_auth=True)
        if resp.get("success"):
            print(f"  ⏭️  {username} 已存在，跳过创建")
            created.append(username)
            continue

        # 注册新账号
        resp = client.post("/auth/register", json={
            "username": username,
            "password": info["password"],
            "email": f"{username}@test.com",
            "nickname": info["nickname"],
            "role": info["role"]
        }, _no_auth=True)
        if resp.get("success"):
            print(f"  ✅ 创建测试账号: {username} ({info['nickname']})")
            created.append(username)
        else:
            print(f"  ❌ 创建 {username} 失败: {resp.get('message', '')}")

    return created


def create_test_organization(client):
    """创建测试机构。"""
    resp = client.get("/organization/my-organizations")
    if resp.get("success") and resp.get("data"):
        org = resp["data"][0]
        print(f"  ⏭️  已有机构: {org.get('name')} (ID={org['id']})")
        return org

    resp = client.post("/organization/create", json={
        "name": "好芽儿测试园",
        "description": "自动化测试用机构",
        "contactPhone": "13800000000",
        "address": "测试地址",
        "orgType": "SINGLE"
    })
    if resp.get("success"):
        org = resp["data"]
        print(f"  ✅ 创建机构: {org.get('name')} (ID={org['id']})")
        return org
    else:
        print(f"  ❌ 创建机构失败: {resp.get('message', '')}")
        return None


def create_test_classroom(client, org_id):
    """创建测试班级。"""
    resp = client.get(f"/classroom/organization/{org_id}")
    if resp.get("success") and resp.get("data"):
        cls = resp["data"][0]
        print(f"  ⏭️  已有班级: {cls.get('name')} (ID={cls['id']})")
        return cls

    class_name = f"小班1班"
    resp = client.post("/classroom/create", json={
        "organizationId": org_id,
        "name": class_name,
        "description": "自动化测试班级",
        "capacity": 20,
        "ageGroup": "24-36"
    })
    if resp.get("success"):
        cls = resp["data"]
        print(f"  ✅ 创建班级: {cls.get('name')} (ID={cls['id']})")
        return cls
    else:
        print(f"  ❌ 创建班级失败: {resp.get('message', '')}")
        return None


def create_test_staff(client, org_id, classroom_id):
    """创建测试员工并分配到班级。"""
    resp = client.get(f"/staff/organization/{org_id}")
    if resp.get("success") and resp.get("data"):
        staff_list = resp["data"]
        if staff_list:
            print(f"  ⏭️  已有 {len(staff_list)} 名员工")
            return staff_list[0]

    # 先创建用户作为员工
    staff_username = f"staff_{unique_name()}"
    resp = client.post("/auth/register", json={
        "username": staff_username,
        "password": "Test123456",
        "email": f"{staff_username}@test.com",
        "nickname": "测试老师",
        "role": "TEACHER"
    }, _no_auth=True)
    if not resp.get("success"):
        print(f"  ❌ 创建员工用户失败: {resp.get('message', '')}")
        return None

    user_id = resp["data"]["user"]["id"]

    # 创建员工记录
    resp = client.post("/staff/create", json={
        "organizationId": org_id,
        "userId": user_id,
        "role": "TEACHER",
        "phone": f"138{unique_name()[:8]}",
        "title": "主班老师"
    })
    if resp.get("success"):
        staff = resp["data"]
        print(f"  ✅ 创建员工: {staff.get('nickname', '')} (ID={staff.get('id')})")

        # 分配到班级
        resp = client.post("/staff/assign-to-classroom", json={
            "staffId": staff["id"],
            "classroomId": classroom_id
        })
        if resp.get("success"):
            print(f"  ✅ 员工分配到班级成功")
        else:
            print(f"  ⚠️  员工分配班级: {resp.get('message', '')}")

        return staff
    else:
        print(f"  ❌ 创建员工失败: {resp.get('message', '')}")
        return None


def create_test_family_and_baby(client):
    """创建测试家庭和宝宝（使用家长账号）。"""
    # 尝试复用已有家庭
    family = None
    resp = client.get("/family/my-families")
    if resp.get("success") and resp.get("data"):
        family = resp["data"][0]
        print(f"  ⏭️  已有家庭: {family.get('name', '')} (ID={family['id']})")
        # 检查是否有宝宝
        resp = client.get(f"/family/{family['id']}/babies")
        if resp.get("success") and resp.get("data"):
            baby = resp["data"][0]
            print(f"  ⏭️  已有宝宝: {baby.get('name', '')} (ID={baby['id']})")
            return family, baby
    else:
        # 创建新家庭
        resp = client.post("/family/create", json={
            "name": f"测试家庭_{unique_name()}"
        })
        if not resp.get("success"):
            print(f"  ❌ 创建家庭失败: {resp.get('message', '')}")
            return None, None
        family = resp["data"]
        print(f"  ✅ 创建家庭: {family.get('name', '')} (ID={family['id']})")

    # 创建宝宝（已有家庭则追加宝宝）
    resp = client.post(f"/family/{family['id']}/babies", json={
        "name": f"测试宝宝_{unique_name()[:4]}",
        "birthday": "2025-01-15",
        "gender": "MALE"
    })
    if resp.get("success"):
        baby = resp["data"]
        print(f"  ✅ 创建宝宝: {baby.get('name', '')} (ID={baby['id']})")
        return family, baby
    else:
        print(f"  ❌ 创建宝宝失败: {resp.get('message', '')}")
        return family, None


def add_admin_to_family(admin_client, parent_client, family):
    """把管理员加入家庭（入托需要管理员同时有机构和家庭权限）。"""
    invite_code = family.get("inviteCode")
    if not invite_code:
        print(f"  ⚠️  无法获取家庭邀请码")
        return False

    # 检查管理员是否已在家庭中
    resp = admin_client.get("/family/my-families")
    if resp.get("success") and resp.get("data"):
        for f in resp["data"]:
            if f["id"] == family["id"]:
                print(f"  ⏭️  管理员已在家庭中")
                return True

    # 用管理员登录并加入家庭
    resp = admin_client.post(f"/family/join/{invite_code}")
    if resp.get("success"):
        print(f"  ✅ 管理员已加入家庭 (inviteCode={invite_code})")
        return True
    else:
        print(f"  ⚠️  管理员加入家庭失败: {resp.get('message', '')}")
        return False


def create_test_enrollment(admin_client, org_id, classroom_id, baby_id):
    """创建测试入托档案（需管理员身份，已加入家庭）。"""
    resp = admin_client.get(f"/enrollment/classroom/{classroom_id}")
    if resp.get("success") and resp.get("data"):
        enrollments = resp["data"]
        for e in enrollments:
            if e.get("baby", {}).get("id") == baby_id:
                print(f"  ⏭️  已有入托档案 (ID={e['id']})")
                return e

    resp = admin_client.post("/enrollment/create", json={
        "babyId": baby_id,
        "organizationId": org_id,
        "classroomId": classroom_id,
        "entryDate": "2026-07-01",
        "emergencyContact": "测试家长",
        "emergencyPhone": "13800000001"
    })
    if resp.get("success"):
        enrollment = resp["data"]
        print(f"  ✅ 创建入托档案 (ID={enrollment['id']}, status={enrollment.get('status', 'N/A')})")
        return enrollment
    elif "已存在" in str(resp.get("message", "")):
        # 已存在则查询返回
        resp2 = admin_client.get(f"/enrollment/classroom/{classroom_id}")
        if resp2.get("success") and resp2.get("data"):
            for e in resp2["data"]:
                if e.get("baby", {}).get("id") == baby_id:
                    print(f"  ⏭️  已有入托档案 (ID={e['id']})")
                    return e
        print(f"  ⏭️  入托档案已存在（但查询失败）")
        return None
    else:
        print(f"  ❌ 创建入托档案失败: {resp.get('message', '')}")
        return None


def main():
    print("=" * 50)
    print("📦 测试数据初始化")
    print("=" * 50)

    admin_client = ApiClient()

    # 第1步：系统管理员登录
    print("\n▶ 第1步：系统管理员登录")
    if not setup_admin_account(admin_client):
        print("❌ 管理员登录失败，请确认后端已启动")
        sys.exit(1)

    # 第2步：创建测试账号
    print("\n▶ 第2步：创建测试账号")
    accounts = create_test_accounts(admin_client)

    # 第3步：创建测试机构
    print("\n▶ 第3步：创建测试机构")
    org = create_test_organization(admin_client)
    if not org:
        print("❌ 机构创建失败，终止")
        sys.exit(1)
    org_id = org["id"]

    # 第4步：创建测试班级
    print("\n▶ 第4步：创建测试班级")
    cls = create_test_classroom(admin_client, org_id)
    if not cls:
        print("❌ 班级创建失败，终止")
        sys.exit(1)
    classroom_id = cls["id"]

    # 第5步：创建测试员工
    print("\n▶ 第5步：创建测试员工")
    staff = create_test_staff(admin_client, org_id, classroom_id)

    # 第6步：创建测试家庭和宝宝（使用家长账号）
    print("\n▶ 第6步：创建测试家庭和宝宝")
    parent_client = ApiClient()
    if "parent_t040" in accounts:
        parent_client.login(use_account="parent")
    else:
        parent_client = admin_client

    family, baby = create_test_family_and_baby(parent_client)
    if not baby:
        print("❌ 宝宝创建失败，终止")
        sys.exit(1)

    # 第7步：将管理员加入家庭（入托需要同时有机构和家庭权限）
    print("\n▶ 第7步：管理员加入家庭")
    add_admin_to_family(admin_client, parent_client, family)

    # 第8步：创建入托档案
    print("\n▶ 第8步：创建入托档案")
    enrollment = create_test_enrollment(admin_client, org_id, classroom_id, baby["id"])

    print("\n" + "=" * 50)
    print("✅ 测试数据初始化完成")
    print("=" * 50)
    print(f"  机构ID: {org_id}")
    print(f"  班级ID: {classroom_id}")
    print(f"  宝宝ID: {baby['id']}")
    if enrollment:
        print(f"  入托ID: {enrollment['id']}")
    print(f"  测试账号: {', '.join(accounts)}")
    print()


if __name__ == "__main__":
    main()
