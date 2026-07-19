# API 与方法清单

本文档记录后端路由、前端 API service、关键 Service 方法和 Store action。新增或修改接口时必须同步更新。

后端全局 context path：

```text
/api
```

前端 API baseURL：

```text
/api
```

## 1. 当前后端接口

### AuthController `/auth`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/auth/register` | 注册（支持手机号为主注册，username可选） | `authAPI.register` |
| POST | `/auth/login` | 登录 | `authAPI.login` |
| POST | `/auth/refresh` | 刷新 token | `authAPI.refreshToken` |
| GET | `/auth/me` | 当前用户 | 待补充 |
| PUT | `/auth/profile` | 更新资料 | `authAPI.updateProfile` |
| PUT | `/auth/change-password` | 修改密码 | `authAPI.changePassword` |
| POST | `/auth/logout` | 登出 | `authAPI.logout` |
| GET | `/auth/check-username` | 检查用户名 | 待补充 |
| GET | `/auth/check-email` | 检查邮箱 | 待补充 |
| POST | `/auth/send-code` | 发送手机验证码 | `authAPI.sendVerificationCode` |
| POST | `/auth/verify-phone` | 验证手机验证码 | `authAPI.verifyPhone` |
| POST | `/auth/phone-login` | 手机号登录（密码或验证码） | `authAPI.phoneLogin` |

> T078 新增：`send-code`、`verify-phone`、`phone-login` 三个端点。`register` 的 username 改为可选，支持手机号为主注册。`phone-login` 支持密码登录和验证码登录两种模式。

### WeChatController `/wechat`（T080 预留）

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/wechat/mini-program/login` | 微信小程序登录（占位） | `authAPI.weChatLogin` |
| POST | `/wechat/mini-program/phone` | 微信手机号授权（占位） | `authAPI.weChatPhone` |

说明：T080 预留端点，当前返回"功能待接入"。正式接入时需对接微信服务端。

### FamilyController `/family`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/family/create` | 创建家庭 | `familyAPI.createFamily` |
| POST | `/family/join/{inviteCode}` | 加入家庭 | `familyAPI.joinFamily` |
| GET | `/family/my-families` | 我的家庭 | `familyAPI.getMyFamilies` |
| GET | `/family/{familyId}` | 家庭详情 | `familyAPI.getFamilyDetail` |
| POST | `/family/{familyId}/babies` | 添加宝宝 | `familyAPI.addBaby` |
| GET | `/family/{familyId}/babies` | 家庭宝宝列表 | `familyAPI.getFamilyBabies` |
| PUT | `/family/{familyId}/members/{memberId}` | 更新家庭成员权限（昵称、接送确认、通知确认） | `familyAPI.updateMemberPermissions` |

说明：`familyStore.ts` 已在 T003 统一到以上真实接口。T073 新增 `updateMemberPermissions` 端点，仅 CREATOR/PARENT 角色可管理。

### GrowthRecordController `/growth-record`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/growth-record/create` | 创建成长记录 | `growthRecordAPI.createRecord` |
| PUT | `/growth-record/{recordId}` | 更新成长记录 | `growthRecordAPI.updateRecord` |
| DELETE | `/growth-record/{recordId}` | 删除成长记录 | `growthRecordAPI.deleteRecord` |
| GET | `/growth-record/baby/{babyId}` | 宝宝成长记录分页 | `growthRecordAPI.getBabyRecords` |
| GET | `/growth-record/baby/{babyId}/type/{type}` | 按类型查询 | `growthRecordAPI.getRecordsByType` |
| GET | `/growth-record/baby/{babyId}/search` | 搜索记录 | 待补充 |
| GET | `/growth-record/recent` | 最近记录 | 待补充 |
| POST | `/growth-record/{recordId}/view` | 查看计数 | 待补充 |

### EducationPlanController `/education-plan`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/education-plan/create` | 创建计划 | `educationPlanAPI.createPlan` |
| PUT | `/education-plan/{planId}` | 更新计划 | `educationPlanAPI.updatePlan` |
| DELETE | `/education-plan/{planId}` | 删除计划 | `educationPlanAPI.deletePlan` |
| GET | `/education-plan/baby/{babyId}` | 宝宝计划列表 | `educationPlanAPI.getBabyPlans` |
| GET | `/education-plan/baby/{babyId}/active` | 活跃计划 | `educationPlanAPI.getActivePlans` |
| POST | `/education-plan/{planId}/start` | 启动计划 | `educationPlanAPI.startPlan` |
| POST | `/education-plan/{planId}/complete` | 完成计划 | `educationPlanAPI.completePlan` |
| POST | `/education-plan/activity/create` | 创建活动 | `educationPlanAPI.createActivity` |
| GET | `/education-plan/{planId}/activities` | 计划活动 | `educationPlanAPI.getPlanActivities` |
| POST | `/education-plan/activity/{activityId}/complete` | 完成活动 | `educationPlanAPI.completeActivity` |

### FamilyPostController `/family-post`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/family-post/create` | 创建动态 | `familyPostAPI.createPost` |
| PUT | `/family-post/{postId}` | 更新动态 | `familyPostAPI.updatePost` |
| GET | `/family-post/family/{familyId}` | 家庭动态 | `familyPostAPI.getFamilyPosts` |
| POST | `/family-post/{postId}/like` | 点赞 | `familyPostAPI.likePost` |
| DELETE | `/family-post/{postId}/like` | 取消点赞 | `familyPostAPI.unlikePost` |
| DELETE | `/family-post/{postId}` | 删除动态 | `familyPostAPI.deletePost` |

### FamilyTaskController `/family-task`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/family-task/create` | 创建任务 | `familyTaskAPI.createTask` |
| PUT | `/family-task/{taskId}` | 更新任务 | `familyTaskAPI.updateTask` |
| GET | `/family-task/family/{familyId}` | 家庭任务 | `familyTaskAPI.getFamilyTasks` |
| GET | `/family-task/my-tasks` | 我的任务 | `familyTaskAPI.getMyTasks` |
| POST | `/family-task/{taskId}/start` | 开始任务 | `familyTaskAPI.startTask` |
| POST | `/family-task/{taskId}/complete` | 完成任务 | `familyTaskAPI.completeTask` |
| POST | `/family-task/{taskId}/cancel` | 取消任务 | `familyTaskAPI.cancelTask` |
| DELETE | `/family-task/{taskId}` | 删除任务 | `familyTaskAPI.deleteTask` |

### AIParentingController `/ai-parenting`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/ai-parenting/session/create` | 创建 AI 会话 | 待补充 |
| POST | `/ai-parenting/session/{sessionId}/message` | 发送消息 | 待补充 |
| GET | `/ai-parenting/sessions` | 会话列表 | 待补充 |
| GET | `/ai-parenting/session/{sessionId}/messages` | 会话消息 | 待补充 |
| POST | `/ai-parenting/message/{messageId}/feedback` | 消息反馈 | 待补充 |
| POST | `/ai-parenting/session/{sessionId}/complete` | 完成会话 | 待补充 |

### HealthController `/public`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/public/health` | 健康检查 | 手动访问 |
| GET | `/public/info` | 系统信息 | 手动访问 |

### OrganizationController `/organization`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/organization/create` | 创建机构 | `organizationAPI.createOrganization` |
| GET | `/organization/my-organizations` | 我的机构 | `organizationAPI.getMyOrganizations` |
| GET | `/organization/{organizationId}` | 机构详情 | `organizationAPI.getOrganizationDetail` |
| PUT | `/organization/{organizationId}` | 更新机构（含 dailyReportApprovalRequired） | `organizationAPI.updateOrganization` |

### ClassroomController `/classroom`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/classroom/create` | 创建班级 | `classroomAPI.createClassroom` |
| GET | `/classroom/organization/{organizationId}` | 机构班级列表 | `classroomAPI.getOrganizationClassrooms` |
| GET | `/classroom/{classroomId}` | 班级详情 | `classroomAPI.getClassroomDetail` |
| PUT | `/classroom/{classroomId}` | 更新班级 | `classroomAPI.updateClassroom` |

### StaffController `/staff`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/staff/create` | 创建机构员工 | `staffAPI.createStaff` |
| GET | `/staff/organization/{organizationId}` | 机构员工列表 | `staffAPI.getOrganizationStaff` |
| GET | `/staff/{staffId}` | 员工详情 | `staffAPI.getStaffDetail` |
| PUT | `/staff/{staffId}` | 更新员工角色或状态 | `staffAPI.updateStaff` |

### EnrollmentController `/enrollment`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/enrollment/create` | 创建入托档案 | `enrollmentAPI.createEnrollment` |
| GET | `/enrollment/classroom/{classroomId}` | 班级宝宝列表 | `enrollmentAPI.getClassroomEnrollments` |
| GET | `/enrollment/baby/{babyId}` | 宝宝入托信息 | `enrollmentAPI.getBabyEnrollments` |
| GET | `/enrollment/{enrollmentId}` | 入托档案详情 | `enrollmentAPI.getEnrollmentDetail` |
| PUT | `/enrollment/{enrollmentId}` | 更新入托档案 | `enrollmentAPI.updateEnrollment` |
| POST | `/enrollment/{enrollmentId}/review` | 园长审核入托（PENDING→HEALTH_CHECK/REJECTED） | `enrollmentAPI.reviewEnrollment` |
| POST | `/enrollment/{enrollmentId}/health-check` | 保健员健康审核（HEALTH_CHECK→ACTIVE/REJECTED） | `enrollmentAPI.healthCheckEnrollment` |
| GET | `/enrollment/health-check-pending/{organizationId}` | 待保健审核列表 | `enrollmentAPI.getHealthCheckPendingEnrollments` |
| POST | `/enrollment/{enrollmentId}/transfer` | 转班 | `enrollmentAPI.transferClassroom` |
| POST | `/enrollment/{enrollmentId}/withdraw` | 退托 | `enrollmentAPI.withdrawEnrollment` |
| POST | `/enrollment/{enrollmentId}/suspend` | 暂停入托（ACTIVE→SUSPENDED） | `enrollmentAPI.suspendEnrollment` |
| POST | `/enrollment/{enrollmentId}/reactivate` | 复托（SUSPENDED→ACTIVE） | `enrollmentAPI.reactivateEnrollment` |
| GET | `/enrollment/{enrollmentId}/history` | 状态变更历史 | `enrollmentAPI.getEnrollmentHistory` |
| POST | `/enrollment/{enrollmentId}/guardians` | 添加入托监护人 | `enrollmentAPI.addGuardian` |
| DELETE | `/enrollment/{enrollmentId}/guardians/{guardianId}` | 移除入托监护人 | `enrollmentAPI.removeGuardian` |
| GET | `/enrollment/{enrollmentId}/guardians` | 入托监护人列表 | `enrollmentAPI.getEnrollmentGuardians` |
| GET | `/enrollment/my-enrollments` | 我的入托档案（家长视角） | `enrollmentAPI.getMyEnrollments` |
| POST | `/enrollment/{enrollmentId}/invite-code` | 生成家长绑定邀请码 | `enrollmentAPI.generateInviteCode` |
| POST | `/enrollment/bind-by-code` | 通过邀请码绑定入托 | `enrollmentAPI.bindByInviteCode` |

### 家长资料补充与确认（T076，在 EnrollmentController 中）

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/enrollment/{enrollmentId}/supplement` | 获取家长补充资料状态 | `enrollmentAPI.getSupplementStatus` |
| PUT | `/enrollment/{enrollmentId}/supplement` | 家长补充入托资料（宝宝身份、监护人身份、健康与紧急联系） | `enrollmentAPI.saveSupplement` |
| POST | `/enrollment/{enrollmentId}/confirm` | 家长确认资料完整 | `enrollmentAPI.confirmSupplement` |

### AttendanceController `/attendance`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/attendance/check-in` | 到园签到 | `attendanceAPI.checkIn` |
| POST | `/attendance/check-out` | 离园签退 | `attendanceAPI.checkOut` |
| POST | `/attendance/absent` | 标记缺勤 | `attendanceAPI.markAbsent` |
| GET | `/attendance/classroom/{classroomId}` | 按班级和日期查询考勤 | `attendanceAPI.getClassroomAttendance` |
| GET | `/attendance/baby/{babyId}` | 按宝宝和日期范围查询考勤 | `attendanceAPI.getBabyAttendance` |
| POST | `/attendance/leave/request` | 创建请假申请 | `attendanceAPI.createLeaveRequest` |
| POST | `/attendance/leave/{leaveRequestId}/approve` | 审批通过请假 | `attendanceAPI.approveLeave` |
| POST | `/attendance/leave/{leaveRequestId}/reject` | 驳回请假 | `attendanceAPI.rejectLeave` |
| GET | `/attendance/leave/classroom/{classroomId}` | 班级请假申请列表 | `attendanceAPI.getClassroomLeaveRequests` |
| GET | `/attendance/leave/baby/{babyId}` | 宝宝请假申请列表 | `attendanceAPI.getBabyLeaveRequests` |

### CareRecordController `/care-record`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/care-record/create` | 创建照护记录（支持 backfillReason 补录） | `careRecordAPI.createRecord` |
| PUT | `/care-record/{recordId}` | 更新照护记录 | `careRecordAPI.updateRecord` |
| DELETE | `/care-record/{recordId}` | 删除照护记录 | `careRecordAPI.deleteRecord` |
| GET | `/care-record/classroom/{classroomId}` | 班级某日照护记录 | `careRecordAPI.getClassroomRecords` |
| GET | `/care-record/baby/{babyId}` | 宝宝某日照护记录 | `careRecordAPI.getBabyRecords` |
| GET | `/care-record/enrollment/{enrollmentId}` | 入托档案某日照护记录 | `careRecordAPI.getEnrollmentRecords` |

### DailyReportController `/daily-report`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/daily-report/generate` | 生成日报草稿 | `dailyReportAPI.generateReport` |
| POST | `/daily-report/ai-draft/generate` | 基于真实记录生成 AI 日报辅助草稿并写入 `aiDraftContent` | `dailyReportAPI.generateAiDraft` |
| PUT | `/daily-report/{reportId}` | 更新日报草稿 | `dailyReportAPI.updateReport` |
| POST | `/daily-report/{reportId}/publish` | 发布日报（如机构开启审核则变为提交审核） | `dailyReportAPI.publishReport` |
| POST | `/daily-report/{reportId}/submit` | 提交审核（直转待审核） | `dailyReportAPI.submitForReview` |
| POST | `/daily-report/{reportId}/approve` | 审核通过 | `dailyReportAPI.approveReport` |
| POST | `/daily-report/{reportId}/reject` | 驳回审核（需 reason） | `dailyReportAPI.rejectReport` |
| GET | `/daily-report/baby/{babyId}` | 宝宝某日日报 | `dailyReportAPI.getBabyReport` |
| GET | `/daily-report/baby/{babyId}/list` | 宝宝日报列表 | `dailyReportAPI.getBabyReports` |
| GET | `/daily-report/classroom/{classroomId}` | 班级某日日报列表 | `dailyReportAPI.getClassroomReports` |

> T058 新增：`submitForReview`、`approveReport`、`rejectReport` 三个审核端点。`publishReport` 根据 Organization.`dailyReportApprovalRequired` 决定是直接发布还是转为待审核。

> T060 新增：创建照护记录时，若 `recordDate` 早于今天或提供 `backfillReason`，后端自动标记为补录记录（`isBackfill=true`），并记录补录人和补录时间。

### HealthObservationController `/health-observation`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/health-observation/create` | 创建健康观察记录 | `healthObservationAPI.createObservation` |
| PUT | `/health-observation/{observationId}` | 更新健康观察记录 | `healthObservationAPI.updateObservation` |
| DELETE | `/health-observation/{observationId}` | 删除健康观察记录 | `healthObservationAPI.deleteObservation` |
| GET | `/health-observation/classroom/{classroomId}` | 班级某日健康观察记录 | `healthObservationAPI.getClassroomObservations` |
| GET | `/health-observation/baby/{babyId}` | 宝宝某日健康观察记录 | `healthObservationAPI.getBabyObservations` |
| GET | `/health-observation/enrollment/{enrollmentId}` | 入托档案某日健康观察记录 | `healthObservationAPI.getEnrollmentObservations` |

### PickupController `/pickup`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/pickup/person/create` | 新增授权接送人 | `pickupAPI.createPickupPerson` |
| PUT | `/pickup/person/{personId}` | 更新授权接送人 | `pickupAPI.updatePickupPerson` |
| GET | `/pickup/person/baby/{babyId}` | 宝宝授权接送人列表 | `pickupAPI.getBabyPickupPersons` |
| GET | `/pickup/person/classroom/{classroomId}` | 班级授权接送人列表 | `pickupAPI.getClassroomPickupPersons` |
| POST | `/pickup/delegation/create` | 创建临时委托接送 | `pickupAPI.createDelegation` |
| POST | `/pickup/delegation/{delegationId}/approve` | 审核通过委托接送 | `pickupAPI.approveDelegation` |
| POST | `/pickup/delegation/{delegationId}/reject` | 审核拒绝委托接送 | `pickupAPI.rejectDelegation` |
| GET | `/pickup/delegation/baby/{babyId}` | 宝宝委托接送列表 | `pickupAPI.getBabyDelegations` |
| GET | `/pickup/delegation/classroom/{classroomId}` | 班级某日委托接送列表 | `pickupAPI.getClassroomDelegations` |
| POST | `/pickup/delegation/{delegationId}/elder-confirm` | 长辈确认接送委托（需 canConfirmPickup 权限） | `pickupAPI.elderConfirmDelegation` |

### MedicationCareController `/medication-care`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/medication-care/allergy/create` | 创建过敏标签 | `medicationCareAPI.createAllergyTag` |
| PUT | `/medication-care/allergy/{allergyId}` | 更新过敏标签 | `medicationCareAPI.updateAllergyTag` |
| GET | `/medication-care/allergy/baby/{babyId}` | 宝宝过敏标签列表 | `medicationCareAPI.getBabyAllergies` |
| POST | `/medication-care/request/create` | 创建用药委托 | `medicationCareAPI.createMedicationRequest` |
| POST | `/medication-care/request/{medicationRequestId}/approve` | 审核通过用药委托 | `medicationCareAPI.approveMedication` |
| POST | `/medication-care/request/{medicationRequestId}/reject` | 审核拒绝用药委托 | `medicationCareAPI.rejectMedication` |
| POST | `/medication-care/administration/create` | 记录用药执行 | `medicationCareAPI.recordAdministration` |
| GET | `/medication-care/request/baby/{babyId}` | 宝宝用药委托列表 | `medicationCareAPI.getBabyMedications` |
| GET | `/medication-care/request/classroom/{classroomId}` | 班级用药委托列表 | `medicationCareAPI.getClassroomMedications` |

### IncidentReportController `/incident-report`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/incident-report/create` | 创建异常事故记录 | `incidentReportAPI.createReport` |
| PUT | `/incident-report/{reportId}` | 更新异常事故记录 | `incidentReportAPI.updateReport` |
| POST | `/incident-report/{reportId}/close` | 关闭异常事故记录 | `incidentReportAPI.closeReport` |
| POST | `/incident-report/{reportId}/parent-confirm` | 家长确认异常事故记录 | `incidentReportAPI.confirmByParent` |
| GET | `/incident-report/baby/{babyId}` | 宝宝异常事故记录 | `incidentReportAPI.getBabyReports` |
| GET | `/incident-report/classroom/{classroomId}` | 班级异常事故记录 | `incidentReportAPI.getClassroomReports` |

### AnnouncementController `/announcement`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/announcement/create` | 创建通知草稿 | `announcementAPI.createAnnouncement` |
| PUT | `/announcement/{announcementId}` | 更新通知草稿 | `announcementAPI.updateAnnouncement` |
| POST | `/announcement/{announcementId}/publish` | 发布通知 | `announcementAPI.publishAnnouncement` |
| POST | `/announcement/{announcementId}/read` | 标记已读 | `announcementAPI.markRead` |
| GET | `/announcement/organization/{organizationId}` | 机构通知列表 | `announcementAPI.getOrganizationAnnouncements` |
| GET | `/announcement/classroom/{classroomId}` | 班级通知列表 | `announcementAPI.getClassroomAnnouncements` |

### BillingController `/billing`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/billing/fee-item/create` | 创建收费项目 | `billingAPI.createFeeItem` |
| PUT | `/billing/fee-item/{feeItemId}` | 更新收费项目 | `billingAPI.updateFeeItem` |
| GET | `/billing/fee-item/organization/{organizationId}` | 机构收费项目列表 | `billingAPI.getOrganizationFeeItems` |
| POST | `/billing/bill/create` | 生成账单 | `billingAPI.createBill` |
| POST | `/billing/bill/{billId}/paid` | 标记账单已支付 | `billingAPI.markPaid` |
| POST | `/billing/bill/{billId}/cancel` | 取消账单 | `billingAPI.cancelBill` |
| GET | `/billing/bill/organization/{organizationId}` | 机构账单列表 | `billingAPI.getOrganizationBills` |
| GET | `/billing/bill/baby/{babyId}` | 宝宝账单列表 | `billingAPI.getBabyBills` |
| GET | `/billing/finance-workbench/{organizationId}` | 财务运营工作台聚合 | `billingAPI.getFinanceWorkbench` |

### DirectorDashboardController `/director-dashboard`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/director-dashboard/organization/{organizationId}` | 机构运营概览，可选 `date` | `directorDashboardAPI.getOrganizationOverview` |
| GET | `/director-dashboard/workbench/{organizationId}` | 园长工作台（概览+待办+风险） | `directorDashboardAPI.getWorkbench` |

### BossDashboardController `/api/boss/dashboard`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/api/boss/dashboard/overview` | 多园区数据聚合概览 | `bossDashboardAPI.getOverview` |

### ParentApplicationController `/parent`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/parent/my-applications` | 我的申请列表（请假+用药+接送） | `parentApplicationAPI.getMyApplications` |
| POST | `/parent/leave-request` | 提交请假申请 | `parentApplicationAPI.createLeaveRequest` |
| POST | `/parent/medication-request` | 提交用药委托 | `parentApplicationAPI.createMedicationRequest` |
| POST | `/parent/pickup-delegation` | 提交接送委托 | `parentApplicationAPI.createPickupDelegation` |
| GET | `/parent/my-bills` | 我的账单列表 | `parentApplicationAPI.getMyBills` |
| POST | `/parent/cancel-application` | 取消待审核申请 | `parentApplicationAPI.cancelApplication` |

### AdmissionLeadController `/admission-lead`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/admission-lead/create` | 创建招生线索 | `admissionLeadAPI.createLead` |
| PUT | `/admission-lead/{leadId}` | 更新招生线索 | `admissionLeadAPI.updateLead` |
| POST | `/admission-lead/{leadId}/review` | 报名审核 | `admissionLeadAPI.reviewApplication` |
| POST | `/admission-lead/{leadId}/trial/start` | 开始试托 | `admissionLeadAPI.startTrial` |
| POST | `/admission-lead/{leadId}/trial/finish` | 结束试托 | `admissionLeadAPI.finishTrial` |
| POST | `/admission-lead/{leadId}/convert` | 转为入托档案（T075） | `admissionLeadAPI.convertToEnrollment` |
| GET | `/admission-lead/organization/{organizationId}` | 机构招生线索列表，可选 `status` | `admissionLeadAPI.getOrganizationLeads` |
| GET | `/admission-lead/{leadId}` | 招生线索详情 | `admissionLeadAPI.getLeadDetail` |
| POST | `/admission-lead/{leadId}/follow-up` | 添加跟进记录 | `admissionLeadAPI.addFollowUp` |
| GET | `/admission-lead/{leadId}/follow-ups` | 跟进记录列表 | `admissionLeadAPI.getFollowUps` |
| GET | `/admission-lead/funnel/{organizationId}` | 招生漏斗统计 | `admissionLeadAPI.getFunnelStats` |

### MealPlanController `/meal-plan`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/meal-plan/create` | 创建食谱 | `mealPlanAPI.createMealPlan` |
| PUT | `/meal-plan/{mealPlanId}` | 更新食谱 | `mealPlanAPI.updateMealPlan` |
| POST | `/meal-plan/{mealPlanId}/publish` | 发布食谱 | `mealPlanAPI.publishMealPlan` |
| GET | `/meal-plan/organization/{organizationId}` | 机构食谱列表，可选 `date/startDate/endDate` | `mealPlanAPI.getOrganizationMeals` |
| POST | `/meal-plan/intake/record` | 记录宝宝实际进食 | `mealPlanAPI.recordIntake` |
| GET | `/meal-plan/{mealPlanId}/intakes` | 食谱进食记录 | `mealPlanAPI.getMealIntakes` |
| GET | `/meal-plan/intake/enrollment/{enrollmentId}` | 宝宝进食记录 | `mealPlanAPI.getEnrollmentIntakes` |
| GET | `/meal-plan/analysis/organization/{organizationId}` | 营养摄入分析，可指定 `startDate/endDate` | `mealPlanAPI.getNutritionAnalysis` |

### SafetyLedgerController `/safety-ledger`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/safety-ledger/create` | 创建安全卫生台账 | `safetyLedgerAPI.createLedger` |
| PUT | `/safety-ledger/{ledgerId}` | 更新安全卫生台账 | `safetyLedgerAPI.updateLedger` |
| POST | `/safety-ledger/{ledgerId}/processing` | 标记台账处理中 | `safetyLedgerAPI.markProcessing` |
| POST | `/safety-ledger/{ledgerId}/close` | 关闭安全卫生台账 | `safetyLedgerAPI.closeLedger` |
| GET | `/safety-ledger/organization/{organizationId}` | 机构台账列表，可选 `startDate/endDate/type/status` | `safetyLedgerAPI.getOrganizationLedgers` |
| GET | `/safety-ledger/{ledgerId}` | 台账详情 | `safetyLedgerAPI.getLedgerDetail` |
| POST | `/safety-ledger/template/create` | 创建安全台账模板 | `safetyLedgerAPI.createTemplate` |
| PUT | `/safety-ledger/template/{templateId}` | 更新安全台账模板 | `safetyLedgerAPI.updateTemplate` |
| POST | `/safety-ledger/template/{templateId}/delete` | 删除安全台账模板 | `safetyLedgerAPI.deleteTemplate` |
| GET | `/safety-ledger/template/organization/{organizationId}` | 机构模板列表 | `safetyLedgerAPI.getOrganizationTemplates` |
| POST | `/safety-ledger/generate-tasks/{organizationId}` | 根据模板生成到期台账 | `safetyLedgerAPI.generateTasks` |
| POST | `/safety-ledger/check-overdue/{organizationId}` | 检查并标记逾期台账 | `safetyLedgerAPI.checkOverdue` |
| GET | `/safety-ledger/overdue-count/{organizationId}` | 机构台账统计（逾期/待处理/处理中数量） | `safetyLedgerAPI.getOverdueCount` |

### RegulatoryReportController `/regulatory-report`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/regulatory-report/organization/{organizationId}` | 机构监管报表概览，可选 `startDate/endDate` | `regulatoryReportAPI.getOrganizationReport` |
| GET | `/regulatory-report/organization/{organizationId}/export-rows` | 机构监管导出行，可选 `startDate/endDate` | `regulatoryReportAPI.getOrganizationExportRows` |

### HardwareIntegrationController `/hardware-integration`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/hardware-integration/device/create` | 创建设备档案 | `hardwareIntegrationAPI.createDevice` |
| PUT | `/hardware-integration/device/{deviceId}` | 更新设备档案 | `hardwareIntegrationAPI.updateDevice` |
| GET | `/hardware-integration/device/{deviceId}` | 设备详情 | `hardwareIntegrationAPI.getDeviceDetail` |
| GET | `/hardware-integration/organization/{organizationId}/devices` | 机构设备列表，可选 `deviceType/status` | `hardwareIntegrationAPI.getOrganizationDevices` |
| POST | `/hardware-integration/event/ingest` | 接收硬件原始事件 | `hardwareIntegrationAPI.ingestEvent` |
| PUT | `/hardware-integration/event/{eventId}/status` | 标记硬件事件状态 | `hardwareIntegrationAPI.updateEventStatus` |
| GET | `/hardware-integration/device/{deviceId}/events` | 设备事件列表 | `hardwareIntegrationAPI.getDeviceEvents` |
| GET | `/hardware-integration/organization/{organizationId}/events` | 机构硬件事件列表，可选 `startDate/endDate` | `hardwareIntegrationAPI.getOrganizationEvents` |

### RbacController `/api/admin/rbac`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/api/admin/rbac/roles` | 获取角色列表 | `adminAPI.listRoles` |
| GET | `/api/admin/rbac/roles/{id}` | 获取角色详情 | `adminAPI.getRole` |
| POST | `/api/admin/rbac/roles` | 创建角色 | `adminAPI.createRole` |
| PUT | `/api/admin/rbac/roles/{id}` | 更新角色 | `adminAPI.updateRole` |
| DELETE | `/api/admin/rbac/roles/{id}` | 删除角色 | `adminAPI.deleteRole` |
| POST | `/api/admin/rbac/user-roles` | 分配用户角色 | `adminAPI.assignUserRoles` |
| GET | `/api/admin/rbac/user-roles/{userId}` | 获取用户角色 | `adminAPI.getUserRoles` |
| POST | `/api/admin/rbac/role-menus` | 分配角色菜单 | `adminAPI.assignRoleMenus` |
| GET | `/api/admin/rbac/role-menus/{roleId}` | 获取角色菜单 | `adminAPI.getRoleMenus` |
| POST | `/api/admin/rbac/role-permissions` | 分配角色权限 | `adminAPI.assignRolePermissions` |
| GET | `/api/admin/rbac/role-permissions/{roleId}` | 获取角色权限 | `adminAPI.getRolePermissions` |
| GET | `/api/admin/rbac/permissions` | 获取权限列表 | `adminAPI.listPermissions` |
| POST | `/api/admin/rbac/permissions` | 创建权限 | `adminAPI.createPermission` |
| DELETE | `/api/admin/rbac/permissions/{id}` | 删除权限 | `adminAPI.deletePermission` |
| GET | `/api/admin/rbac/menus` | 获取菜单树 | `adminAPI.listMenus` |
| GET | `/api/admin/rbac/menus/user` | 获取用户菜单树 | `adminAPI.getUserMenus` |
| POST | `/api/admin/rbac/menus` | 创建菜单 | `adminAPI.createMenu` |
| DELETE | `/api/admin/rbac/menus/{id}` | 删除菜单 | `adminAPI.deleteMenu` |
| GET | `/api/admin/rbac/users` | 获取用户列表 | `adminAPI.listUsers` |

### AuditLogController `/api/admin/audit-log`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/api/admin/audit-log` | 查询审计日志（支持 action/targetType/username/startTime/endTime 筛选） | `adminAPI.queryAuditLogs` |

### DataDictController `/api/admin/data-dict`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/api/admin/data-dict/types` | 获取字典类型列表 | `adminAPI.listDictTypes` |
| GET | `/api/admin/data-dict/type/{dictType}` | 获取指定类型字典项 | `adminAPI.listDictByType` |
| POST | `/api/admin/data-dict` | 创建字典项 | `adminAPI.createDictItem` |
| PUT | `/api/admin/data-dict/{id}` | 更新字典项 | `adminAPI.updateDictItem` |
| DELETE | `/api/admin/data-dict/{id}` | 删除字典项 | `adminAPI.deleteDictItem` |

### SystemConfigController `/api/admin/config`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/api/admin/config` | 获取所有配置 | `adminAPI.listConfigs` |
| GET | `/api/admin/config/{configKey}` | 根据键获取配置 | `adminAPI.getConfig` |
| PUT | `/api/admin/config/{configKey}` | 更新配置 | `adminAPI.updateConfig` |

### OrgGroupController `/api/admin/org-groups`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| GET | `/api/admin/org-groups` | 获取集团列表 | `orgGroupAPI.listAll` |
| GET | `/api/admin/org-groups/{id}` | 获取集团详情 | `orgGroupAPI.getById` |
| POST | `/api/admin/org-groups` | 创建集团品牌 | `orgGroupAPI.create` |
| PUT | `/api/admin/org-groups/{id}` | 更新集团品牌 | `orgGroupAPI.update` |
| DELETE | `/api/admin/org-groups/{id}` | 禁用集团品牌 | `orgGroupAPI.delete` |

### OrganizationController（新增端点）

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/organization/{organizationId}/appoint-director` | 任命园长（T053） | `organizationAPI.appointDirector` |

### ChildDevelopmentAssessmentController `/child-development-assessment`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/child-development-assessment/create` | 创建发展评估 | `childDevelopmentAssessmentAPI.createAssessment` |
| PUT | `/child-development-assessment/{assessmentId}` | 更新发展评估 | `childDevelopmentAssessmentAPI.updateAssessment` |
| GET | `/child-development-assessment/baby/{babyId}` | 宝宝评估历史 | `childDevelopmentAssessmentAPI.getBabyAssessments` |
| GET | `/child-development-assessment/enrollment/{enrollmentId}` | 入托档案评估历史，可选 `assessmentMode` | `childDevelopmentAssessmentAPI.getEnrollmentAssessments` |
| GET | `/child-development-assessment/{assessmentId}` | 评估详情 | `childDevelopmentAssessmentAPI.getAssessmentDetail` |

### InfectiousDiseaseController `/infectious-disease`

| 方法 | 路径 | 用途 | 前端调用 |
|---|---|---|---|
| POST | `/infectious-disease/create` | 创建传染病记录 | `infectiousDiseaseAPI.create` |
| PUT | `/infectious-disease/{recordId}` | 更新传染病记录 | `infectiousDiseaseAPI.update` |
| GET | `/infectious-disease/classroom/{classroomId}` | 查询班级传染病记录 | `infectiousDiseaseAPI.getClassroomRecords` |
| GET | `/infectious-disease/organization/{organizationId}` | 查询机构传染病记录 | `infectiousDiseaseAPI.getOrganizationRecords` |
| GET | `/infectious-disease/{recordId}` | 查询传染病记录详情 | `infectiousDiseaseAPI.getRecordDetail` |
| GET | `/infectious-disease/organization/{organizationId}/active-count` | 查询机构活跃传染病数 | `infectiousDiseaseAPI.getActiveCount` |

## 2. 当前前端 API service

文件：`frontend/src/services/api.ts`

| API 对象 | 用途 | 状态 |
|---|---|---|
| `request` | 通用 get/post/put/delete/patch | 已有 |
| `uploadFile` | 单文件上传 | 后端接口待确认 |
| `uploadFiles` | 批量上传 | 后端接口待确认 |
| `growthRecordAPI` | 成长记录 | 已有 |
| `educationPlanAPI` | 教育计划 | 已有 |
| `familyAPI` | 家庭 | 已有 |
| `familyPostAPI` | 家庭动态 | 已有 |
| `familyTaskAPI` | 家庭任务 | 已有 |
| `organizationAPI` | 托育机构 | 已有 |
| `classroomAPI` | 托育班级 | 已有 |
| `staffAPI` | 机构员工 | 已有 |
| `enrollmentAPI` | 入托档案 CRUD + 审核/转班/退托 + 监护人管理 + 我的入托 + 邀请码绑定 | 已有 |
| `attendanceAPI` | 考勤和请假 | 已有 |
| `careRecordAPI` | 一日照护记录 | 已有 |
| `dailyReportAPI` | 结构化家长日报 | 已有 |
| `healthObservationAPI` | 晨午检与全日观察 | 已有 |
| `pickupAPI` | 接送授权与委托 | 已有 |
| `medicationCareAPI` | 用药与过敏管理 | 已有 |
| `incidentReportAPI` | 异常/事故上报 | 已有 |
| `announcementAPI` | 通知公告与已读回执 | 已有 |
| `billingAPI` | 收费项目、账单、财务工作台 | 已有 |
| `directorDashboardAPI` | 园长驾驶舱聚合指标 | 已有 |
| `admissionLeadAPI` | 招生线索、报名审核、试托状态、跟进记录、漏斗统计 | 已有 |
| `mealPlanAPI` | 食谱与实际进食记录 | 已有 |
| `safetyLedgerAPI` | 安全卫生台账 | 已有 |
| `regulatoryReportAPI` | 监管字段聚合与导出行 | 已有 |
| `hardwareIntegrationAPI` | 硬件设备档案与原始事件 | 已有 |
| `childDevelopmentAssessmentAPI` | 月龄里程碑与五大领域评估 | 已有 |
| `infectiousDiseaseAPI` | 传染病防控记录 | T063 新增 |
| `adminAPI` | 系统管理（RBAC/审计日志/字典/配置） | T048 新增 |
| `orgGroupAPI` | 集团/品牌管理 | T051 新增 |
| `bossDashboardAPI` | 老板多园区驾驶舱 | T052 新增 |
| `directorDashboardAPI.getWorkbench` | 园长工作台（待办+风险） | T054 新增 |
| `userStaffAPI` | 当前员工信息查询 | 已有 |
| `parentApplicationAPI` | 家长申请（请假/用药/接送/账单/取消） | T071 新增 |

文件：`frontend/src/services/authService.ts`

| API 对象 | 用途 | 状态 |
|---|---|---|
| `authAPI` | 认证相关 | 已有 |

## 3. 拟新增托育主链 API

当前任务板 T000-T026 已全部完成。T026 新增 `dailyReportAPI.generateAiDraft` 和后端 `/daily-report/ai-draft/generate`，基于真实考勤、照护和健康观察记录生成规则化可编辑日报草稿，不接外部大模型、不生成随机内容。

## 4. 维护规则

新增 Controller、Service 公共方法、前端 API 方法、Store action 时，必须补充：

- 方法名称
- 路径或调用位置
- 用途
- 当前状态
- 前端/后端对应关系
