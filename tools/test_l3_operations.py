"""
TC-L3-001 招生线索, TC-L3-002 收费账单, TC-L3-003 食谱, TC-L3-004 监管导出
通过API创建数据（因为OperationsRegulatory页面没有创建按钮）
"""
import urllib.request, json, sys

BASE = 'http://localhost:8080/api'

def api_call(method, path, token=None, data=None):
    headers = {'Content-Type': 'application/json'}
    if token: headers['Authorization'] = f'Bearer {token}'
    body = json.dumps(data, ensure_ascii=False).encode('utf-8') if data else None
    req = urllib.request.Request(f'{BASE}{path}', data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        resp_body = resp.read().decode('utf-8')
        if resp_body.strip():
            return json.loads(resp_body)
        return {"success": True, "message": "ok"}
    except urllib.error.HTTPError as e:
        error_body = e.read().decode('utf-8')
        print(f"  HTTP {e.code}: {error_body}", file=sys.stderr)
        return json.loads(error_body) if error_body.strip() else {"success": False, "message": f"HTTP {e.code}"}
    except Exception as e:
        print(f"  Error: {e}", file=sys.stderr)
        return {"success": False, "message": str(e)}

# 1. Login as ADMIN
print("=== Login ===")
login_resp = api_call('POST', '/auth/login', data={'emailOrUsername': 'admin_t040', 'password': 'Test040pass'})
if not login_resp.get('success'):
    print(f"FAIL: Login failed: {login_resp.get('message')}")
    sys.exit(1)
token = login_resp['data']['token']
print("OK: admin_t040 logged in")

# 2. Get organization
print("\n=== Get Organization ===")
org_resp = api_call('GET', '/organization/my-organizations', token=token)
if not org_resp.get('success'):
    print(f"FAIL: {org_resp.get('message')}")
    sys.exit(1)
org_id = org_resp['data'][0]['id']
print(f"OK: org_id={org_id}")

# 3. Get classroom
print("\n=== Get Classroom ===")
class_resp = api_call('GET', f'/classroom/organization/{org_id}', token=token)
if not class_resp.get('success'):
    print(f"FAIL: {class_resp.get('message')}")
    sys.exit(1)
class_id = class_resp['data'][0]['id']
print(f"OK: class_id={class_id}")

# 4. Get enrollment
print("\n=== Get Enrollment ===")
enroll_resp = api_call('GET', f'/enrollment/classroom/{class_id}', token=token)
if not enroll_resp.get('success'):
    print(f"FAIL: {enroll_resp.get('message')}")
    sys.exit(1)
enrollments = enroll_resp['data']
enroll = next((e for e in enrollments if e.get('babyName') == '小芽芽' or e.get('baby', {}).get('name') == '小芽芽'), None)
if not enroll:
    print(f"FAIL: No enrollment found: {json.dumps(enrollments, ensure_ascii=False)}")
    sys.exit(1)
enrollment_id = enroll['id']
baby_id = enroll.get('babyId') or enroll.get('baby', {}).get('id')
print(f"OK: enrollment_id={enrollment_id}, baby_id={baby_id}")

# TC-L3-001: Create admission lead (招生线索)
print("\n=== TC-L3-001: Create Admission Lead ===")
lead_data = {
    "organizationId": org_id,
    "intendedClassroomId": class_id,
    "childName": "小花花",
    "childGender": "FEMALE",
    "childBirthday": "2023-06-15",
    "guardianName": "花花妈妈",
    "guardianPhone": "13800004201",
    "source": "REFERRAL",
    "intentionLevel": "HIGH",
    "remark": "朋友推荐，意向很高"
}
lead_resp = api_call('POST', '/admission-lead/create', token=token, data=lead_data)
if lead_resp.get('success'):
    print(f"PASS: Admission lead created: id={lead_resp.get('data', {}).get('id', '?')}")
else:
    print(f"FAIL: {lead_resp.get('message')} - {json.dumps(lead_resp, ensure_ascii=False)}")

# TC-L3-002: Create billing statement (收费账单)
print("\n=== TC-L3-002: Create Billing Statement ===")
bill_data = {
    "enrollmentId": enrollment_id,
    "title": "2026年7月托育费",
    "amount": 3200.00,
    "dueDate": "2026-07-31",
    "remark": "含保教费+伙食费"
}
bill_resp = api_call('POST', '/billing/bill/create', token=token, data=bill_data)
if bill_resp.get('success'):
    print(f"PASS: Billing statement created: id={bill_resp.get('data', {}).get('id', '?')}")
else:
    print(f"FAIL: {bill_resp.get('message')} - {json.dumps(bill_resp, ensure_ascii=False)}")

# TC-L3-003: Create meal plan (食谱)
print("\n=== TC-L3-003: Create Meal Plan ===")
meal_data = {
    "organizationId": org_id,
    "mealDate": "2026-07-16",
    "mealType": "BREAKFAST",
    "title": "早餐",
    "foodItems": "小米粥、鸡蛋、全麦面包",
    "allergenNotes": "含鸡蛋、麸质",
    "nutritionNotes": "蛋白质15g，碳水35g"
}
meal_resp = api_call('POST', '/meal-plan/create', token=token, data=meal_data)
if meal_resp.get('success'):
    print(f"PASS: Meal plan created: id={meal_resp.get('data', {}).get('id', '?')}")
else:
    print(f"FAIL: {meal_resp.get('message')} - {json.dumps(meal_resp, ensure_ascii=False)}")

# Also create LUNCH meal
meal_data2 = {
    "organizationId": org_id,
    "mealDate": "2026-07-16",
    "mealType": "LUNCH",
    "title": "午餐",
    "foodItems": "米饭、西红柿炒蛋、清炒西兰花、紫菜汤",
    "allergenNotes": "含鸡蛋",
    "nutritionNotes": "蛋白质20g，碳水45g"
}
meal_resp2 = api_call('POST', '/meal-plan/create', token=token, data=meal_data2)
if meal_resp2.get('success'):
    print(f"PASS: Lunch meal plan created: id={meal_resp2.get('data', {}).get('id', '?')}")
else:
    print(f"FAIL: {meal_resp2.get('message')} - {json.dumps(meal_resp2, ensure_ascii=False)}")

# TC-L3-004: Check regulatory report (监管导出)
print("\n=== TC-L3-004: Check Regulatory Report ===")
report_resp = api_call('GET', f'/regulatory-report/organization/{org_id}', token=token)
if report_resp.get('success'):
    print(f"PASS: Regulatory report available: {json.dumps(report_resp.get('data', {}), ensure_ascii=False)[:300]}")
else:
    print(f"Result: {report_resp.get('message')} - {json.dumps(report_resp, ensure_ascii=False)[:200]}")

export_resp = api_call('GET', f'/regulatory-report/organization/{org_id}/export-rows', token=token)
if export_resp.get('success'):
    rows = export_resp.get('data', [])
    print(f"PASS: Export rows count: {len(rows)}")
    if rows:
        print(f"  First row: {json.dumps(rows[0], ensure_ascii=False)[:200]}")
else:
    print(f"Result: {export_resp.get('message')}")

print("\n=== L3 运营监管补充测试完成 ===")
