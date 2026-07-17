import urllib.request, json, sys

BASE = 'http://localhost:8080/api'

def api_call(method, path, token=None, data=None):
    headers = {'Content-Type': 'application/json'}
    if token:
        headers['Authorization'] = f'Bearer {token}'
    body = json.dumps(data).encode() if data else None
    req = urllib.request.Request(f'{BASE}{path}', data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        return json.loads(resp.read())
    except urllib.error.HTTPError as e:
        return json.loads(e.read())

# Login
login_resp = api_call('POST', '/auth/login', data={'emailOrUsername': 'admin_t040', 'password': 'Test040pass'})
token = login_resp['data']['token']
print(f'Login: {login_resp["data"]["user"]["role"]}')

# Get classroom ID
org_resp = api_call('GET', '/organization/my-organizations', token=token)
org_id = org_resp['data'][0]['id']
print(f'Org ID: {org_id}')

class_resp = api_call('GET', f'/classroom/organization/{org_id}', token=token)
if not class_resp.get('success'):
    print(f"FAIL: {class_resp.get('message')}")
    sys.exit(1)
classes = class_resp['data']
cls = next((c for c in classes if c['name'] == '豆芽一班'), None)
if not cls:
    print(f"FAIL: Class not found: {classes}")
    sys.exit(1)
class_id = cls['id']
print(f'Class ID: {class_id}, name={cls["name"]}')

# Get enrollment ID for baby 小芽芽
enroll_resp = api_call('GET', f'/enrollment/classroom/{class_id}', token=token)
if not enroll_resp.get('success'):
    print(f"FAIL: {enroll_resp.get('message')}")
    sys.exit(1)
enrollments = enroll_resp['data']
enroll = next((e for e in enrollments if e.get('babyName') == '小芽芽'), None)
if not enroll:
    enroll = next((e for e in enrollments if e.get('baby', {}).get('name') == '小芽芽'), None)
if not enroll:
    print(f"FAIL: No enrollment found: {json.dumps(enrollments, ensure_ascii=False)}")
    sys.exit(1)
enroll_id = enroll['id']
baby_name = enroll.get('babyName') or enroll.get('baby', {}).get('name', '?')
print(f'Enrollment ID: {enroll_id}, baby={baby_name}')

# TC-L1-007: Create health observation (晨午检)
print("\n=== TC-L1-007: Create Health Observation ===")
health_data = {
    'enrollmentId': enroll_id,
    'observationDate': '2026-07-16',
    'type': 'MORNING_CHECK',
    'temperature': 36.5,
    'touchStatus': '正常',
    'lookStatus': '正常',
    'askStatus': '正常',
    'checkStatus': '正常',
    'symptoms': '无异常',
    'actionTaken': '正常入园',
    'abnormal': False,
    'followUpRequired': False,
    'source': 'TEST'
}
health_resp = api_call('POST', '/health-observation/create', token=token, data=health_data)
if health_resp.get('success'):
    print(f"PASS: Health observation created: id={health_resp.get('data', {}).get('id', '?')}")
else:
    print(f"FAIL: {health_resp.get('message')} - {json.dumps(health_resp, ensure_ascii=False)}")

# TC-L1-008: Create care record (照护记录 - MEAL)
print("\n=== TC-L1-008: Create Care Record (MEAL) ===")
care_data = {
    'enrollmentId': enroll_id,
    'recordDate': '2026-07-16',
    'type': 'FEEDING',
    'valueText': '午餐全部吃完，吃了1碗米饭和青菜',
    'amount': 1.0,
    'unit': '碗',
    'remark': '胃口良好',
    'source': 'TEST'
}
care_resp = api_call('POST', '/care-record/create', token=token, data=care_data)
if care_resp.get('success'):
    print(f"PASS: Care record (MEAL) created: id={care_resp.get('data', {}).get('id', '?')}")
else:
    print(f"FAIL: {care_resp.get('message')} - {json.dumps(care_resp, ensure_ascii=False)}")

# Create NAP care record
print("\n--- Create Care Record (NAP) ---")
care_data2 = {
    'enrollmentId': enroll_id,
    'recordDate': '2026-07-16',
    'type': 'SLEEP',
    'valueText': '午睡1.5小时',
    'amount': 1.5,
    'unit': '小时',
    'remark': '睡眠质量良好',
    'source': 'TEST'
}
care_resp2 = api_call('POST', '/care-record/create', token=token, data=care_data2)
if care_resp2.get('success'):
    print(f"PASS: Care record (NAP) created: id={care_resp2.get('data', {}).get('id', '?')}")
else:
    print(f"FAIL: {care_resp2.get('message')} - {json.dumps(care_resp2, ensure_ascii=False)}")

print('\n=== TC-L1-007 and TC-L1-008 completed ===')
