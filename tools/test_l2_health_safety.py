"""
TC-L2-001 用药委托, TC-L2-002 事故上报, TC-L2-003 安全卫生台账
通过API创建数据（因为HealthSafety页面没有创建按钮）
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

# TC-L2-001: Create medication request (用药委托)
print("\n=== TC-L2-001: Create Medication Request ===")
med_data = {
    "enrollmentId": enrollment_id,
    "medicineName": "小儿氨酚黄那敏颗粒",
    "dosage": "每次1袋，每日3次",
    "frequency": "每日3次",
    "startDate": "2026-07-16",
    "endDate": "2026-07-18",
    "instructions": "饭后服用"
}
med_resp = api_call('POST', '/medication-care/request/create', token=token, data=med_data)
if med_resp.get('success'):
    print(f"PASS: Medication request created: id={med_resp.get('data', {}).get('id', '?')}")
    med_id = med_resp.get('data', {}).get('id')
else:
    print(f"FAIL: {med_resp.get('message')} - {json.dumps(med_resp, ensure_ascii=False)}")

# TC-L2-002: Create incident report (事故上报)
print("\n=== TC-L2-002: Create Incident Report ===")
import datetime
incident_data = {
    "enrollmentId": enrollment_id,
    "type": "INJURY",
    "severity": "LOW",
    "occurredAt": datetime.datetime.now().isoformat(),
    "location": "户外活动区",
    "title": "小芽芽滑倒",
    "description": "小芽芽在户外活动时不小心滑倒，膝盖轻微擦伤。老师已进行消毒处理。",
    "handlingProcess": "消毒处理，通知家长",
    "followUpPlan": "持续观察伤口情况"
}
incident_resp = api_call('POST', '/incident-report/create', token=token, data=incident_data)
if incident_resp.get('success'):
    print(f"PASS: Incident report created: id={incident_resp.get('data', {}).get('id', '?')}")
    incident_id = incident_resp.get('data', {}).get('id')
else:
    print(f"FAIL: {incident_resp.get('message')} - {json.dumps(incident_resp, ensure_ascii=False)}")

# TC-L2-003: Create safety ledger (安全卫生台账)
print("\n=== TC-L2-003: Create Safety Ledger ===")
ledger_data = {
    "organizationId": org_id,
    "ledgerDate": "2026-07-16",
    "ledgerType": "FACILITY_INSPECTION",
    "title": "2026-07-16 日常安全巡查",
    "content": "对户外活动区进行安全巡查，发现滑梯螺丝松动，已报修。",
    "location": "户外活动区"
}
ledger_resp = api_call('POST', '/safety-ledger/create', token=token, data=ledger_data)
if ledger_resp.get('success'):
    print(f"PASS: Safety ledger created: id={ledger_resp.get('data', {}).get('id', '?')}")
    ledger_id = ledger_resp.get('data', {}).get('id')
else:
    print(f"FAIL: {ledger_resp.get('message')} - {json.dumps(ledger_resp, ensure_ascii=False)}")

print("\n=== L2 安全健康补充测试完成 ===")
