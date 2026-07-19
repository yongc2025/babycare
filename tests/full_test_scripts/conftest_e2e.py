"""
好芽儿端到端全链路测试基础框架

提供：
- E2E 测试专用的 ApiClient 扩展，含数据库断言辅助
- 全局变量池管理（变量令牌化）
- 测试结果统计

用法：
    from conftest_e2e import E2EContext, E2ETestResults
    ctx = E2EContext()
    ctx.login("admin", "admin123")
    resp = ctx.api.post("/organization/create", json={...})
    ctx.set("orgId", resp["data"]["id"])
"""

import sys
import os
import time
import json
from urllib.parse import urljoin

sys.path.insert(0, os.path.join(os.path.dirname(__file__), ".."))

try:
    import requests
except ImportError:
    print("ERROR: 请先安装依赖: pip install -r tests/requirements.txt", file=sys.stderr)
    sys.exit(1)

# ─── 配置 ───────────────────────────────────────────────────────────────
BASE_URL = "http://localhost:8080/api"
REQUEST_TIMEOUT = 15

# ─── 测试账号 ────────────────────────────────────────────────────────────
TEST_ACCOUNTS = {
    "admin":     {"username": "admin",         "password": "admin123",   "role": "ADMIN"},
    "director":  {"username": "test_director",  "password": "TestPass1", "role": "ADMIN",  "staffRole": "DIRECTOR"},
    "teacher":   {"username": "test_teacher",   "password": "TestPass1", "role": "ADMIN",  "staffRole": "TEACHER"},
    "caregiver": {"username": "test_caregiver", "password": "TestPass1", "role": "ADMIN",  "staffRole": "CAREGIVER"},
    "health":    {"username": "test_health",    "password": "TestPass1", "role": "ADMIN",  "staffRole": "HEALTH_WORKER"},
    "finance":   {"username": "test_finance",   "password": "TestPass1", "role": "ADMIN",  "staffRole": "FINANCE"},
    "safety":    {"username": "test_safety",    "password": "TestPass1", "role": "ADMIN",  "staffRole": "SAFETY_OFFICER"},
    "ops":       {"username": "test_ops",       "password": "TestPass1", "role": "ADMIN",  "staffRole": "OPERATIONS_STAFF"},
    "adm":       {"username": "test_adm",       "password": "TestPass1", "role": "ADMIN",  "staffRole": "ADMISSIONS_OFFICER"},
    "parent":    {"username": "test_parent",    "password": "TestPass1", "role": "PARENT"},
    "elder":     {"username": "test_elder",     "password": "TestPass1", "role": "ELDER"},
    "unbound_parent": {"username": "test_parent2", "password": "TestPass1", "role": "PARENT"},
}


# ═══════════════════════════════════════════════════════════════════════
# API 客户端（带全局变量池）
# ═══════════════════════════════════════════════════════════════════════

class E2EApiClient:
    """带变量令牌化和数据库断言辅助的 API 客户端。"""

    def __init__(self, base_url=BASE_URL):
        self.base_url = base_url.rstrip("/")
        self.token = None
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})
        self.last_response = None
        self.vars = {}  # 全局变量池 {变量名: 值}

    # ── 变量池管理 ────────────────────────────────────────────────────

    def set(self, name, value):
        """设置全局变量。"""
        self.vars[name] = value
        return value

    def getv(self, name, default=None):
        """获取全局变量（注意与 HTTP GET 方法区分）。"""
        return self.vars.get(name, default)

    def resolve(self, data):
        """递归替换数据中所有 {{变量名}} 占位符。"""
        if isinstance(data, str):
            for k, v in self.vars.items():
                placeholder = "{{" + k + "}}"
                if placeholder in data:
                    data = data.replace(placeholder, str(v))
            return data
        elif isinstance(data, dict):
            return {k: self.resolve(v) for k, v in data.items()}
        elif isinstance(data, list):
            return [self.resolve(v) for v in data]
        return data

    def collect_vars(self, resp, mappings):
        """从响应中提取变量到变量池。
        
        Args:
            resp: API 响应 dict
            mappings: dict {变量名: json_path}，如 {"orgId": "data.id"}
        """
        for var_name, json_path in mappings.items():
            val = resp
            for key in json_path.split("."):
                if isinstance(val, dict):
                    val = val.get(key)
                else:
                    val = None
                    break
            if val is not None:
                self.set(var_name, val)
        return self

    # ── Token 管理 ──────────────────────────────────────────────────────

    def login(self, username=None, password=None, use_account=None):
        """登录并保存 token。"""
        if use_account:
            acct = TEST_ACCOUNTS[use_account]
            data = {"emailOrUsername": acct["username"], "password": acct["password"]}
        else:
            data = {"emailOrUsername": username, "password": password}

        resp = self.post("/auth/login", json=data, _no_auth=True)
        assert resp.get("success"), f"登录失败 ({use_account or username}): {resp.get('message', resp)}"
        self.token = resp["data"]["token"]
        self.session.headers.update({"Authorization": f"Bearer {self.token}"})
        self.set(f"{use_account or username}Token", self.token)
        return resp

    def set_token(self, token):
        self.token = token
        self.session.headers.update({"Authorization": f"Bearer {token}"})

    def clear_token(self):
        self.token = None
        self.session.headers.pop("Authorization", None)

    # ── HTTP 方法 ───────────────────────────────────────────────────────

    def _request(self, method, path, _no_auth=False, **kwargs):
        # 递归替换 URL path 中的变量占位符（如 {{orgId}} → 实际 ID）
        path = self.resolve(path)

        if path.startswith("/"):
            url = self.base_url + path
        else:
            url = urljoin(self.base_url + "/", path)

        # 递归替换 body 中的变量占位符
        if "json" in kwargs:
            kwargs["json"] = self.resolve(kwargs["json"])

        kwargs.setdefault("timeout", REQUEST_TIMEOUT)

        old_auth = None
        if _no_auth and "Authorization" in self.session.headers:
            old_auth = self.session.headers.pop("Authorization")

        try:
            response = self.session.request(method, url, **kwargs)
            self.last_response = response
            response.raise_for_status()
            if response.content:
                return response.json()
            return {"success": True, "message": "ok"}
        except requests.exceptions.HTTPError as e:
            resp_obj = e.response
            body = ""
            try:
                body = resp_obj.json() if resp_obj is not None else ""
            except Exception:
                body = resp_obj.text if resp_obj is not None else ""
            return {"success": False,
                    "status": resp_obj.status_code if resp_obj is not None else 0,
                    "message": str(e), "body": body}
        except requests.exceptions.ConnectionError:
            return {"success": False, "message": f"连接失败: {url} — 请确认后端已启动"}
        except requests.exceptions.Timeout:
            return {"success": False, "message": f"请求超时: {method} {path}"}
        finally:
            if old_auth:
                self.session.headers["Authorization"] = old_auth

    def get(self, path, **kwargs):
        return self._request("GET", path, **kwargs)

    def post(self, path, **kwargs):
        return self._request("POST", path, **kwargs)

    def put(self, path, **kwargs):
        return self._request("PUT", path, **kwargs)

    def delete(self, path, **kwargs):
        return self._request("DELETE", path, **kwargs)

    # ── 辅助断言 ──────────────────────────────────────────────────────

    def assert_ok(self, resp, msg=None):
        assert resp.get("success"), msg or f"请求失败: {resp.get('message', resp)}"
        return resp

    def assert_fail(self, resp, expected_status=None, msg=None):
        assert not resp.get("success"), msg or "期望失败但成功了"
        if expected_status:
            actual = resp.get("status", 0)
            assert actual == expected_status, \
                f"期望状态码 {expected_status}，实际 {actual}"
        return resp

    def health_check(self):
        try:
            resp = self.get("/public/health", _no_auth=True)
            return resp.get("success", False)
        except Exception:
            return False


# ═══════════════════════════════════════════════════════════════════════
# 测试结果管理
# ═══════════════════════════════════════════════════════════════════════

class E2ETestResults:
    """端到端测试结果收集器。"""

    def __init__(self, suite_name=""):
        self.suite_name = suite_name
        self.passed = []
        self.failed = []
        self.skipped = []
        self.start_time = time.time()

    def ok(self, case_id, detail=""):
        self.passed.append((case_id, detail))
        print(f"  ✅ [{case_id}] {detail}")

    def fail(self, case_id, detail=""):
        self.failed.append((case_id, detail))
        print(f"  ❌ [{case_id}] {detail}")

    def skip(self, case_id, reason=""):
        self.skipped.append((case_id, reason))
        print(f"  ⏭️  [{case_id}] {reason}")

    def summary(self):
        elapsed = time.time() - self.start_time
        total = len(self.passed) + len(self.failed) + len(self.skipped)
        print(f"\n{'='*60}")
        print(f"  套件: {self.suite_name}")
        print(f"  总计: {total} | ✅ 通过: {len(self.passed)} | ❌ 失败: {len(self.failed)} | ⏭️ 跳过: {len(self.skipped)}")
        print(f"  耗时: {elapsed:.1f}s")
        print(f"{'='*60}")
        return len(self.failed) == 0


# ═══════════════════════════════════════════════════════════════════════
# 数据库断言辅助（伪代码占位 — 实际需对接 MySQL 连接）
# ═══════════════════════════════════════════════════════════════════════

class DBAssert:
    """数据库字段断言辅助。

    生产运行时需要配置 MySQL 连接。
    当前提供占位方法，可被 mock 替换。
    """

    @staticmethod
    def query(sql: str) -> list:
        """执行 SQL 查询（需实现真实连接）。"""
        # TODO: 接入 MySQL 连接
        # import pymysql
        # conn = pymysql.connect(...)
        raise NotImplementedError("DBAssert.query: 需要配置 MySQL 连接后使用")

    @staticmethod
    def assert_field(table: str, where: dict, field: str, expected):
        """断言表中某字段值符合预期。"""
        # TODO: 实现真实断言
        pass


# ─── 工具函数 ────────────────────────────────────────────────────────────

def random_suffix():
    return str(int(time.time() * 1000))[-6:]


def unique_name(prefix="test"):
    return f"{prefix}_{random_suffix()}"
