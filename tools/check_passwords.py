import urllib.request, json

BASE = 'http://localhost:8080/api'
def login(user, pwd):
    try:
        req = urllib.request.Request(f'{BASE}/auth/login',
            data=json.dumps({'emailOrUsername': user, 'password': pwd}).encode(),
            headers={'Content-Type': 'application/json'})
        resp = json.loads(urllib.request.urlopen(req).read())
        return resp.get('success'), resp.get('data', {}).get('user', {})
    except Exception as e:
        return False, str(e)

for acct in ['admin_t040', 'parent_t040', 'teacher_li']:
    ok, info = login(acct, 'Test040pass')
    if ok:
        print(f'  {acct} / Test040pass -> OK  role={info.get("role")}  nickname={info.get("nickname")}')
    else:
        print(f'  {acct} / Test040pass -> FAIL')

# Also try with email login
print()
for acct in ['admin040@test.com', 'parent040@test.com', 'teacherli@test.com']:
    ok, info = login(acct, 'Test040pass')
    if ok:
        print(f'  {acct} / Test040pass -> OK  username={info.get("username")}  role={info.get("role")}')
    else:
        print(f'  {acct} / Test040pass -> FAIL')
