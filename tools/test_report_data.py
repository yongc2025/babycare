"""收集测试统计数据"""
import urllib.request, json

BASE = 'http://localhost:8080/api'

def api_call(method, path, token=None, data=None):
    headers = {'Content-Type': 'application/json'}
    if token: headers['Authorization'] = f'Bearer {token}'
    body = json.dumps(data).encode() if data else None
    req = urllib.request.Request(f'{BASE}{path}', data=body, headers=headers, method=method)
    try:
        resp = urllib.request.urlopen(req)
        return json.loads(resp.read())
    except urllib.error.HTTPError as e:
        return json.loads(e.read())

login_resp = api_call('POST', '/auth/login', data={'emailOrUsername': 'admin_t040', 'password': 'Test040pass'})
token = login_resp['data']['token']

org_resp = api_call('GET', '/organization/my-organizations', token=token)
org = org_resp['data'][0]
org_id = org['id']

class_resp = api_call('GET', f'/classroom/organization/{org_id}', token=token)
class_id = class_resp['data'][0]['id']

enroll_resp = api_call('GET', f'/enrollment/classroom/{class_id}', token=token)
health_resp = api_call('GET', f'/health-observation/classroom/{class_id}', token=token)
care_resp = api_call('GET', f'/care-record/classroom/{class_id}', token=token)
med_resp = api_call('GET', f'/medication-care/request/classroom/{class_id}', token=token)
incident_resp = api_call('GET', f'/incident-report/classroom/{class_id}', token=token)
ledger_resp = api_call('GET', f'/safety-ledger/organization/{org_id}?startDate=2026-07-16&endDate=2026-07-16', token=token)
lead_resp = api_call('GET', f'/admission-lead/organization/{org_id}', token=token)
bill_resp = api_call('GET', f'/billing/bill/organization/{org_id}', token=token)
meal_resp = api_call('GET', f'/meal-plan/organization/{org_id}?date=2026-07-16', token=token)
staff_resp = api_call('GET', f'/staff/organization/{org_id}', token=token)

print('=== DATA COUNTS ===')
print(f'Organization: {org["name"]}')
print(f'Classes: {len(class_resp["data"])}')
print(f'Enrollments: {len(enroll_resp["data"])}')
print(f'Staff: {len(staff_resp.get("data", []))}')
print(f'Health observations: {len(health_resp.get("data", []))}')
print(f'Care records: {len(care_resp.get("data", []))}')
print(f'Medication requests: {len(med_resp.get("data", []))}')
print(f'Incident reports: {len(incident_resp.get("data", []))}')
print(f'Safety ledgers: {len(ledger_resp.get("data", []))}')
print(f'Admission leads: {len(lead_resp.get("data", []))}')
print(f'Billing statements: {len(bill_resp.get("data", []))}')
print(f'Meal plans: {len(meal_resp.get("data", []))}')
