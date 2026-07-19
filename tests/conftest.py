"""
BabyCare/HuiGrowth 测试基础设施。

提供：
- ApiClient: 基于 requests 的 API 客户端，自动管理 token
- 测试账号常量
- 辅助函数

用法：
    from conftest import ApiClient
    client = ApiClient()
    client.login('admin_t040', 'Test040pass')
    resp = client.get('/auth/me')
"""

import json
import sys
import time
from urllib.parse import urljoin

try:
    import requests
except ImportError:
    print("ERROR: 请先安装依赖: pip install -r tests/requirements.txt", file=sys.stderr)
    sys.exit(1)

# ─── 配置 ───────────────────────────────────────────────────────────────
BASE_URL = "http://localhost:8080/api"
REQUEST_TIMEOUT = 15  # 秒

# ─── 测试账号 ────────────────────────────────────────────────────────────
TEST_ACCOUNTS = {
    "admin": {"username": "admin", "password": "admin123"},
    "teacher": {"username": "teacher_t040", "password": "Test040pass"},
    "parent": {"username": "parent_t040", "password": "Test040pass"},
}

# ─── API 客户端 ──────────────────────────────────────────────────────────

class ApiClient:
    """带 token 管理的 API 客户端。"""

    def __init__(self, base_url=BASE_URL, timeout=REQUEST_TIMEOUT):
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self.token = None
        self.session = requests.Session()
        self.session.headers.update({"Content-Type": "application/json"})
        self.last_response = None  # 保留用于调试

    # ── Token 管理 ──────────────────────────────────────────────────────

    def login(self, username=None, password=None, email_or_username=None,
              phone=None, code=None, use_account=None):
        """登录并保存 token。

        支持多种登录方式:
        1) use_account='admin' 等 -> 从 TEST_ACCOUNTS 取
        2) username/password -> 传统账号密码
        3) phone/code -> 手机验证码
        4) email_or_username -> emailOrUsername 字段
        """
        if use_account:
            acct = TEST_ACCOUNTS[use_account]
            data = {"emailOrUsername": acct["username"], "password": acct["password"]}
        elif email_or_username:
            data = {"emailOrUsername": email_or_username, "password": password}
        elif phone:
            data = {"phone": phone, "code": code} if code else {"phone": phone, "password": password}
        else:
            data = {"emailOrUsername": username, "password": password}

        resp = self.post("/auth/login", json=data, _no_auth=True)
        assert resp.get("success"), f"登录失败: {resp.get('message', resp)}"
        self.token = resp["data"]["token"]
        self.session.headers.update({"Authorization": f"Bearer {self.token}"})
        return resp

    def set_token(self, token, raw=False):
        """直接设置 token（用于测试异常场景）。
        如果 raw=True，则直接使用 token 作为 Authorization 头值（不加 Bearer 前缀）。
        """
        self.token = token
        if raw:
            self.session.headers.update({"Authorization": token})
        else:
            self.session.headers.update({"Authorization": f"Bearer {token}"})

    def clear_token(self):
        """清除 token（用于测试未认证场景）。"""
        self.token = None
        self.session.headers.pop("Authorization", None)

    # ── HTTP 方法 ───────────────────────────────────────────────────────

    def _request(self, method, path, _no_auth=False, **kwargs):
        """底层请求方法。"""
        if path.startswith("/"):
            url = self.base_url + path
        else:
            url = urljoin(self.base_url + "/", path)
        kwargs.setdefault("timeout", self.timeout)

        # 如 _no_auth=True，临时移除 Authorization header
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
            body = ""
            resp_obj = e.response
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

    # ── 健康检查 ────────────────────────────────────────────────────────

    def health_check(self):
        """检查后端是否可访问。"""
        try:
            resp = self.get("/public/health", _no_auth=True)
            return resp.get("success", False) or "status" in resp
        except Exception:
            return False

    # ── 通用辅助 ────────────────────────────────────────────────────────

    def assert_ok(self, resp, msg=None):
        """断言响应成功。"""
        assert resp.get("success"), msg or f"请求失败: {resp.get('message', resp)}"
        return resp

    def assert_fail(self, resp, expected_status=None, msg=None):
        """断言响应失败。"""
        assert not resp.get("success"), msg or "期望失败但成功了"
        if expected_status:
            assert resp.get("status") == expected_status, \
                f"期望状态码 {expected_status}，实际 {resp.get('status')}"
        return resp


# ─── 数据生成辅助 ────────────────────────────────────────────────────────

def random_suffix():
    """生成随机后缀（基于时间戳）。"""
    return str(int(time.time() * 1000))[-6:]


def unique_name(prefix="test"):
    """生成唯一名称。"""
    return f"{prefix}_{random_suffix()}"


# ─── 会话级账号管理 ──────────────────────────────────────────────────────

_admin_client = None
_teacher_client = None
_parent_client = None


def get_admin_client():
    """获取 ADMIN 客户端（单例）。"""
    global _admin_client
    if _admin_client is None:
        _admin_client = ApiClient()
        _admin_client.login(use_account="admin")
    return _admin_client


def get_teacher_client():
    """获取 TEACHER 客户端（单例）。"""
    global _teacher_client
    if _teacher_client is None:
        _teacher_client = ApiClient()
        _teacher_client.login(use_account="teacher")
    return _teacher_client


def get_parent_client():
    """获取 PARENT 客户端（单例）。"""
    global _parent_client
    if _parent_client is None:
        _parent_client = ApiClient()
        _parent_client.login(use_account="parent")
    return _parent_client


# ─── Result 收集器 ───────────────────────────────────────────────────────

class TestResults:
    """轻量级测试结果收集。"""

    def __init__(self, suite_name):
        self.suite_name = suite_name
        self.passed = []
        self.failed = []
        self.skipped = []

    def ok(self, case_name, detail=""):
        self.passed.append((case_name, detail))
        print(f"  ✅ {case_name}")

    def fail(self, case_name, detail=""):
        self.failed.append((case_name, detail))
        print(f"  ❌ {case_name}: {detail}")

    def skip(self, case_name, reason=""):
        self.skipped.append((case_name, reason))
        print(f"  ⏭️  {case_name}: {reason}")

    def summary(self):
        total = len(self.passed) + len(self.failed) + len(self.skipped)
        print(f"\n{'='*50}")
        print(f"📊 {self.suite_name} 汇总")
        print(f"   总计: {total}  |  ✅ 通过: {len(self.passed)}  "
              f"|  ❌ 失败: {len(self.failed)}  |  ⏭️ 跳过: {len(self.skipped)}")
        if self.failed:
            print(f"\n  失败的案例:")
            for name, detail in self.failed:
                print(f"    - {name}: {detail}")
        return len(self.failed) == 0


def run_module(suite_name, test_fn):
    """便捷运行函数。"""
    results = TestResults(suite_name)
    print(f"\n{'='*50}")
    print(f"🔍 {suite_name}")
    print(f"{'='*50}")
    try:
        test_fn(results)
    except Exception as e:
        print(f"\n  💥 测试执行异常: {e}")
        import traceback
        traceback.print_exc()
        results.fail("__执行异常__", str(e))
    ok = results.summary()
    return results, ok
