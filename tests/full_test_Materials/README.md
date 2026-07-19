# 好芽儿端到端全链路数据一致性测试套件

## 概述

本套件聚焦 **数据状态流转** 与 **跨模块数据关联**，不写孤立的单接口用例。
每个用例强制覆盖：**数据库字段断言 + 全链路变量令牌化 + 跨域RBAC校验 + 逆向补偿一致性**。

## 全局变量池（变量令牌化定义）

所有用例引用变量必须使用 `{{变量名}}` 格式，严禁硬编码 ID。

| 变量名 | 来源接口 | 传递下游 |
|---|---|---|
| `{{adminToken}}` | `POST /auth/login` (admin) | 所有需管理员权限的接口 |
| `{{directorToken}}` | `POST /auth/login` (test_director) | 园长操作 |
| `{{teacherToken}}` | `POST /auth/login` (test_teacher) | 教师操作 |
| `{{caregiverToken}}` | `POST /auth/login` (test_caregiver) | 保育员操作 |
| `{{healthToken}}` | `POST /auth/login` (test_health) | 保健员操作 |
| `{{financeToken}}` | `POST /auth/login` (test_finance) | 财务操作 |
| `{{safetyToken}}` | `POST /auth/login` (test_safety) | 安全员操作 |
| `{{opsToken}}` | `POST /auth/login` (test_ops) | 运营操作 |
| `{{admToken}}` | `POST /auth/login` (test_adm) | 招生操作 |
| `{{parentToken}}` | `POST /auth/login` (test_parent) | 家长操作 |
| `{{elderToken}}` | `POST /auth/login` (test_elder) | 长辈操作 |
| `{{orgId}}` | `POST /organization/create` → `data.id` | classroom, staff, enrollment, billing... |
| `{{classroomId}}` | `POST /classroom/create` → `data.id` | enrollment, attendance, care record, daily report |
| `{{staffDirectorId}}` | `POST /staff/create` (role=DIRECTOR) → `data.id` | 员工管理 |
| `{{staffTeacherId}}` | `POST /staff/create` (role=TEACHER) → `data.id` | 班级分配 |
| `{{staffCaregiverId}}` | `POST /staff/create` (role=CAREGIVER) → `data.id` | 班级分配 |
| `{{staffHealthId}}` | `POST /staff/create` (role=HEALTH_WORKER) → `data.id` | 保健操作 |
| `{{staffFinanceId}}` | `POST /staff/create` (role=FINANCE) → `data.id` | 财务操作 |
| `{{familyId}}` | `POST /family/create` → `data.id` | baby, member |
| `{{babyId}}` | `POST /family/{familyId}/babies` → `data.id` | enrollment, growth record |
| `{{enrollmentId}}` | `POST /enrollment/create` → `data.id` | attendance, care record, daily report, leave, medication... |
| `{{attendanceDate}}` | 动态入参 | attendance check-in/out |
| `{{careRecordId}}` | `POST /care-record/create` → `data.id` | 照护记录管理 |
| `{{healthObsId}}` | `POST /health-observation/create` → `data.id` | 健康观察管理 |
| `{{dailyReportId}}` | `POST /daily-report/generate` → `data.id` | 日报审核/发布 |
| `{{leaveRequestId}}` | `POST /attendance/leave/request` → `data.id` | 请假审批 |
| `{{medicationRequestId}}` | `POST /medication-care/request/create` → `data.id` | 用药审批 |
| `{{allergyTagId}}` | `POST /medication-care/allergy/create` → `data.id` | 过敏标签管理 |
| `{{incidentReportId}}` | `POST /incident-report/create` → `data.id` | 事故管理 |
| `{{announcementId}}` | `POST /announcement/create` → `data.id` | 通知管理 |
| `{{feeItemId}}` | `POST /billing/fee-item/create` → `data.id` | 收费项目 |
| `{{billId}}` | `POST /billing/bill/create` → `data.id` | 账单管理 |
| `{{leadId}}` | `POST /admission-lead/create` → `data.id` | 招生线索 |
| `{{safetyLedgerId}}` | `POST /safety-ledger/create` → `data.id` | 台账管理 |
| `{{mealPlanId}}` | `POST /meal-plan/create` → `data.id` | 食谱管理 |
| `{{pickupPersonId}}` | `POST /pickup/person/create` → `data.id` | 授权接送人 |
| `{{delegationId}}` | `POST /pickup/delegation/create` → `data.id` | 委托接送 |
| `{{enrollmentHistoryId}}` | `POST /enrollment/{enrollmentId}/history` → `data[0].id` | 状态变更历史断言 |

## 例分类

| 分组 | 说明 | 文件 |
|---|---|---|
| [主流程组](./01-主流程组.md) | 核心业务主链路端到端状态机验证 | `01-主流程组.md` |
| [异常补偿组](./02-异常补偿组.md) | 事务回滚、状态逆袭、最终一致性 | `02-异常补偿组.md` |
| [权限穿透组](./03-权限穿透组.md) | 跨角色RBAC穿透、数据隔离 | `03-权限穿透组.md` |

## 4 大铁律检查清单

每个用例必须满足：

- [ ] **状态机强制验证**：预期结果包含数据库字段断言（`表.字段 = 预期值`）
- [ ] **全链路数据令牌化**：入参引用 `{{变量}}`，无硬编码 ID
- [ ] **跨域 RBAC 穿透校验**（如适用）：多角色 Token 调用同一接口
- [ ] **逆向补偿与最终一致性**（如适用）：状态逆转后关联数据自动同步

## 测试账号

| 角色 | 岗位 | 用户名 | 密码 |
|---|---|---|---|
| 系统管理员 | — | admin | admin123 |
| 园长 | DIRECTOR | test_director | TestPass1 |
| 教师 | TEACHER | test_teacher | TestPass1 |
| 保育员 | CAREGIVER | test_caregiver | TestPass1 |
| 保健员 | HEALTH_WORKER | test_health | TestPass1 |
| 安全员 | SAFETY_OFFICER | test_safety | TestPass1 |
| 财务 | FINANCE | test_finance | TestPass1 |
| 运营 | OPERATIONS_STAFF | test_ops | TestPass1 |
| 招生 | ADMISSIONS_OFFICER | test_adm | TestPass1 |
| 家长 | — | test_parent | TestPass1 |
| 长辈 | — | test_elder | TestPass1 |

## 数据库断言语法约定

```
Enrollment.status = 'HEALTH_CHECK' AND updated_at IS NOT NULL
AttendanceRecord[enrollmentId={{enrollmentId}}][date=today].status = 'ABSENT'
DailyReport[enrollmentId={{enrollmentId}}][date=today].status = 'PUBLISHED'
```
