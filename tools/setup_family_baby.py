import requests
import json

BASE = "http://localhost:8080/api"

# Login
resp = requests.post(f"{BASE}/auth/login", json={"emailOrUsername": "admin_t040", "password": "Test040pass"})
data = resp.json()
token = data["data"]["token"]
headers = {"Authorization": f"Bearer {token}", "Content-Type": "application/json"}
print(f"Login: {data['message']}, Role: {data['data']['user']['role']}")

# Check existing families
resp = requests.get(f"{BASE}/family/my-families", headers=headers)
families = resp.json()
print(f"Existing families: {families}")

# Create family if none
if not families.get("data") or len(families["data"]) == 0:
    resp = requests.post(f"{BASE}/family/create", headers=headers, json={"name": "小芽芽家庭"})
    print(f"Create family: {resp.status_code} - {resp.json()}")
    family_resp = resp.json()
    family_id = family_resp["data"]["id"]
else:
    family_id = families["data"][0]["id"]
    print(f"Using existing family id={family_id}")

# Create baby
from datetime import date, timedelta
today = date.today()
birthday = today - timedelta(days=30*30)  # ~30 months ago
resp = requests.post(f"{BASE}/family/{family_id}/babies", headers=headers, json={
    "name": "小芽芽",
    "gender": "FEMALE",
    "birthday": birthday.isoformat()
})
print(f"Create baby: {resp.status_code} - {resp.json()}")
baby_data = resp.json()
if baby_data.get("success"):
    baby_id = baby_data["data"]["id"]
    print(f"Baby ID: {baby_id}")
else:
    print("Failed to create baby")
    # Check babies in family
    resp = requests.get(f"{BASE}/family/{family_id}/babies", headers=headers)
    print(f"Babies in family: {resp.json()}")
