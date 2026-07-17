# 数据模型清单

本文档记录当前已有数据模型和后续托育保育产品需要新增的数据模型。新增或修改 Entity、字段、枚举、关系时必须同步更新。

## 1. 当前已有模型

| Entity | 当前用途 | 状态 | 说明 |
|---|---|---|---|
| `User` | 用户账号 | 已有 | 实现 `UserDetails`，用于登录认证 |
| `Family` | 家庭 | 已有 | 当前产品中心对象之一 |
| `FamilyMember` | 家庭成员 | 已有 | 用户与家庭关系 |
| `Baby` | 宝宝 | 已有 | 当前归属于家庭，后续需扩展入托归属 |
| `GrowthRecord` | 成长记录 | 已有 | 照片、日记、里程碑等 |
| `Milestone` | 里程碑 | 已有 | 成长里程碑 |
| `FamilyPost` | 家庭动态 | 已有 | 家庭协作动态 |
| `FamilyTask` | 家庭任务 | 已有 | 家庭成员任务 |
| `EducationPlan` | 教育计划 | 已有 | 泛教育计划 |
| `EducationActivity` | 教育活动 | 已有 | 计划下活动 |
| `DevelopmentAssessment` | 发展评估 | 已有 | AI/发育评估相关 |
| `AIChatSession` | AI 会话 | 已有 | 后端已有会话模型 |
| `AIChatMessage` | AI 消息 | 已有 | 后端已有消息模型 |
| `AIChat` | AI 聊天 | 已有 | 需确认是否与 Session/Message 重复 |
| `BaseEntity` | 基础实体 | 已有 | 通用 id、创建/更新时间 |
| `Organization` | 托育机构 | 已有 | T004 新增，当前按创建人拥有机构 |
| `Classroom` | 托育班级 | 已有 | T005 新增，归属于 Organization |
| `Staff` | 机构员工 | 已有 | T006 新增，归属于 Organization 并关联 User |
| `Enrollment` | 宝宝入托档案 | 已有 | T007 新增，连接 Baby、Organization、Classroom |
| `AttendanceRecord` | 考勤记录 | 已有 | T008 新增，基于 Enrollment 记录到园、离园、缺勤、请假 |
| `LeaveRequest` | 请假申请 | 已有 | T008 新增，家长或园所提交，园所审核后生成请假考勤 |
| `CareRecord` | 一日照护记录 | 已有 | T009 新增，基于 Enrollment 记录喂养、饮水、睡眠、如厕、体温、情绪、活动 |
| `DailyReport` | 结构化家长日报 | 已有 | T010 新增，汇总考勤和一日照护记录，支持草稿和发布 |
| `HealthObservation` | 晨午检与全日观察 | 已有 | T012 新增，支持一摸二看三问四查、体温、异常和跟踪 |
| `AuthorizedPickupPerson` | 授权接送人 | 已有 | T013 新增，记录宝宝长期授权接送人 |
| `PickupDelegation` | 临时委托接送 | 已有 | T013 新增，支持临时委托申请、审核和接送码 |
| `AllergyTag` | 过敏标签 | 已有 | T014 新增，记录宝宝过敏源、反应、严重程度和状态 |
| `MedicationRequest` | 用药委托 | 已有 | T014 新增，家长提交，园所审核 |
| `MedicationAdministration` | 用药执行记录 | 已有 | T014 新增，记录实际用药时间、剂量和反应 |
| `IncidentReport` | 异常/事故上报 | 已有 | T015 新增，记录异常事故、处理过程、家长通知和确认 |
| `Announcement` | 通知公告 | 已有 | T016 新增，支持机构/班级通知草稿、发布 |
| `AnnouncementReceipt` | 通知已读回执 | 已有 | T016 新增，记录用户已读状态 |
| `FeeItem` | 收费项目 | 已有 | T017 新增，记录机构收费项目 |
| `BillingStatement` | 收费账单 | 已有 | T017 新增，记录宝宝账单和支付状态 |
| `DirectorDashboardResponse` | 园长驾驶舱聚合 DTO | 已有 | T018 新增，不落库，聚合机构运营指标 |
| `AdmissionLead` | 招生线索 | 已有 | T019 新增，承载意向登记、报名审核和试托状态 |
| `MealPlan` | 食谱 | 已有 | T020 新增，记录机构每日餐次、过敏提示和营养说明 |
| `MealIntakeRecord` | 进食记录 | 已有 | T020 新增，记录宝宝实际进食量和过敏反应 |
| `SafetyLedger` | 安全卫生台账 | 已有 | T021 新增，记录消毒、食品留样、设施巡检、消防和事故跟进 |
| `ChildDevelopmentAssessment` | 儿童发展评估 | 已有 | T022 新增，基于入托档案记录月龄里程碑和五大领域评估 |
| `RegulatoryReportResponse` | 监管报表聚合 DTO | 已有 | T024 新增，不落库，聚合备案、规模、人员、健康和安全卫生导出行 |
| `HardwareDevice` | 硬件设备档案 | 已有 | T025 新增，抽象考勤机、晨检机器人、摄像头、门禁和电子班牌 |
| `HardwareEvent` | 硬件原始事件 | 已有 | T025 新增，只沉淀设备事件，不直接绕过考勤/健康/接送业务校验 |

## 2. 当前模型风险

- `Baby` 本体仍偏家庭场景；入托、班级和基础健康资料已通过 `Enrollment` 承载，接送授权待补充。
- `AIChat` 与 `AIChatSession`/`AIChatMessage` 可能存在职责重复，新增 AI 能力前需先确认边界。
- 家庭协作模型不能直接替代机构/班级体系。

## 3. 第一阶段拟新增模型

### Organization

用途：托育机构主体。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `name` | String | 是 | 机构名称 |
| `description` | String | 否 | 机构简介 |
| `contactPhone` | String | 否 | 联系电话 |
| `address` | String | 否 | 地址 |
| `registrationNo` | String | 否 | 监管备案编号 |
| `licenseNo` | String | 否 | 办学/托育许可证号 |
| `legalRepresentative` | String | 否 | 法定代表人 |
| `supervisorDepartment` | String | 否 | 主管部门 |
| `organizationLevel` | String | 否 | 机构等级或评定等级 |
| `operationType` | String | 否 | 运营类型 |
| `status` | Enum | 是 | ACTIVE, DISABLED |
| `createdBy` | Long | 是 | 创建人用户 ID |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

边界：不替代 `Family`，用于机构运营和班级管理。当前先用 `createdBy` 做最小权限边界，T006 已新增员工关系，后续权限体系会继续扩展。T024 新增的监管字段只承接备案和导出所需的机构基础信息，不代表已完成外部监管平台直连。

### Classroom

用途：机构下的班级。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `organizationId` | Long | 是 | 所属机构 |
| `name` | String | 是 | 班级名称 |
| `ageRangeMinMonths` | Integer | 否 | 最小月龄 |
| `ageRangeMaxMonths` | Integer | 否 | 最大月龄 |
| `capacity` | Integer | 否 | 托位容量 |
| `status` | Enum | 是 | ACTIVE, DISABLED |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### Staff

用途：机构员工、老师、保育员。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `organizationId` | Long | 是 | 所属机构 |
| `userId` | Long | 是 | 关联用户 |
| `role` | Enum | 是 | DIRECTOR, TEACHER, CAREGIVER, FINANCE |
| `status` | Enum | 是 | ACTIVE, DISABLED |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

约束：同一机构内同一用户只能绑定一个员工记录。

### Enrollment

用途：宝宝入托关系。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `babyId` | Long | 是 | 宝宝 |
| `organizationId` | Long | 是 | 机构 |
| `classroomId` | Long | 是 | 班级 |
| `status` | Enum | 是 | PENDING, ACTIVE, SUSPENDED, WITHDRAWN |
| `enrolledAt` | LocalDate | 否 | 入托日期 |
| `allergyNotes` | String | 否 | 过敏信息 |
| `medicalNotes` | String | 否 | 健康/病史备注 |
| `specialCareNotes` | String | 否 | 特殊照护要求 |
| `emergencyContactName` | String | 否 | 紧急联系人 |
| `emergencyContactPhone` | String | 否 | 紧急联系电话 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

约束：同一机构内同一宝宝只能有一个入托档案；有效状态包含 PENDING 和 ACTIVE。

### AttendanceRecord

用途：考勤接送。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `attendanceDate` | LocalDate | 是 | 日期 |
| `status` | Enum | 是 | ABSENT, CHECKED_IN, CHECKED_OUT, LEAVE |
| `checkInAt` | LocalDateTime | 否 | 到园时间 |
| `checkOutAt` | LocalDateTime | 否 | 离园时间 |
| `temperature` | BigDecimal | 否 | 到园体温 |
| `pickupPersonName` | String | 否 | 接送人姓名 |
| `pickupRelationship` | String | 否 | 接送人与宝宝关系 |
| `pickupPhone` | String | 否 | 接送人电话 |
| `source` | String | 是 | 数据来源，当前默认 MANUAL |
| `remark` | String | 否 | 备注 |
| `recordedBy` | Long | 否 | 最近记录人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

约束：同一入托档案同一天只能有一条考勤记录。授权接送人、委托接送码、误操作标记后续由 T013 扩展，不塞入本模型。

### LeaveRequest

用途：请假申请与审核。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `startDate` | LocalDate | 是 | 请假开始日期 |
| `endDate` | LocalDate | 是 | 请假结束日期 |
| `type` | Enum | 是 | SICK, PERSONAL, OTHER |
| `status` | Enum | 是 | PENDING, APPROVED, REJECTED, CANCELLED |
| `reason` | String | 否 | 请假原因 |
| `requestedBy` | Long | 是 | 申请人 |
| `reviewedBy` | Long | 否 | 审核人 |
| `reviewedAt` | LocalDateTime | 否 | 审核时间 |
| `reviewRemark` | String | 否 | 审核备注 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

规则：审核通过后，为请假区间内每天创建或更新 `AttendanceRecord`，状态为 `LEAVE`。

### CareRecord

用途：每日保育记录。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `recordDate` | LocalDate | 是 | 日期 |
| `recordTime` | LocalDateTime | 是 | 记录时间 |
| `type` | Enum | 是 | FEEDING, WATER, SLEEP, TOILET, TEMPERATURE, MOOD, ACTIVITY |
| `valueText` | String | 否 | 文本值，例如情绪、活动名称、餐食说明 |
| `amount` | Double | 否 | 数值，例如饮水量、体温、奶量 |
| `unit` | String | 否 | 单位，例如 ml、℃、min |
| `startedAt` | LocalDateTime | 否 | 开始时间，适合睡眠、活动等持续记录 |
| `endedAt` | LocalDateTime | 否 | 结束时间，适合睡眠、活动等持续记录 |
| `remark` | String | 否 | 备注 |
| `source` | String | 是 | 数据来源，当前默认 MANUAL |
| `recordedBy` | Long | 否 | 记录人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

边界：本模型只记录日常照护事实。异常、事故、用药、晨午检、传染病等高风险数据后续使用独立模型，不混入普通照护记录。

### DailyReport

用途：家长每日托育日报。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `reportDate` | LocalDate | 是 | 日报日期 |
| `status` | Enum | 是 | DRAFT, PUBLISHED |
| `summary` | String | 否 | 日报摘要 |
| `attendanceSummary` | String | 否 | 考勤摘要 |
| `careSummary` | String | 否 | 照护摘要 |
| `healthSummary` | String | 否 | 健康摘要 |
| `activitySummary` | String | 否 | 活动摘要 |
| `teacherComment` | String | 否 | 老师评语 |
| `aiDraftContent` | String | 否 | AI 日报辅助草稿预留 |
| `publishedAt` | LocalDateTime | 否 | 发布时间 |
| `publishedBy` | Long | 否 | 发布人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

规则：同一入托档案同一天只能有一份日报。园所侧可生成草稿、编辑和发布；家长侧只能查看已发布日报。

### HealthObservation

用途：晨检、午检和全日观察。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `observationDate` | LocalDate | 是 | 观察日期 |
| `observationTime` | LocalDateTime | 是 | 观察时间 |
| `type` | Enum | 是 | MORNING_CHECK, NOON_CHECK, FULL_DAY_OBSERVATION |
| `temperature` | Double | 否 | 体温 |
| `touchStatus` | String | 否 | 一摸结果 |
| `lookStatus` | String | 否 | 二看结果 |
| `askStatus` | String | 否 | 三问结果 |
| `checkStatus` | String | 否 | 四查结果 |
| `symptoms` | String | 否 | 症状描述 |
| `actionTaken` | String | 否 | 处理建议或处理动作 |
| `abnormal` | Boolean | 是 | 是否异常 |
| `followUpRequired` | Boolean | 是 | 是否需要继续观察 |
| `source` | String | 是 | 数据来源，当前默认 MANUAL |
| `recordedBy` | Long | 否 | 记录人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

边界：本模型只承载健康观察事实和初步处置。用药、事故、传染病预警后续由独立任务建模。

### AuthorizedPickupPerson

用途：宝宝长期授权接送人。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `name` | String | 是 | 接送人姓名 |
| `relationship` | String | 否 | 与宝宝关系 |
| `phone` | String | 否 | 电话 |
| `identityNo` | String | 否 | 证件号 |
| `photoUrl` | String | 否 | 照片地址 |
| `status` | Enum | 是 | ACTIVE, DISABLED |
| `remark` | String | 否 | 备注 |
| `createdBy` | Long | 否 | 创建人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### PickupDelegation

用途：临时委托接送申请和审核。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `pickupDate` | LocalDate | 是 | 委托接送日期 |
| `pickupPersonName` | String | 是 | 临时接送人姓名 |
| `pickupRelationship` | String | 否 | 与宝宝关系 |
| `pickupPhone` | String | 否 | 电话 |
| `reason` | String | 否 | 委托原因 |
| `status` | Enum | 是 | PENDING, APPROVED, REJECTED, CANCELLED |
| `pickupCode` | String | 否 | 审核通过后生成的接送码 |
| `requestedBy` | Long | 是 | 申请人 |
| `reviewedBy` | Long | 否 | 审核人 |
| `reviewedAt` | LocalDateTime | 否 | 审核时间 |
| `reviewRemark` | String | 否 | 审核备注 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### AllergyTag

用途：宝宝过敏源标签。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `allergen` | String | 是 | 过敏源 |
| `reaction` | String | 否 | 过敏反应 |
| `severity` | Enum | 是 | MILD, MODERATE, SEVERE |
| `status` | Enum | 是 | ACTIVE, INACTIVE |
| `remark` | String | 否 | 备注 |
| `createdBy` | Long | 否 | 创建人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### MedicationRequest

用途：家长提交的用药委托。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `medicineName` | String | 是 | 药品名称 |
| `dosage` | String | 否 | 剂量 |
| `frequency` | String | 否 | 频次 |
| `startDate` | LocalDate | 是 | 开始日期 |
| `endDate` | LocalDate | 是 | 结束日期 |
| `instructions` | String | 否 | 用药说明 |
| `status` | Enum | 是 | PENDING, APPROVED, REJECTED, CANCELLED |
| `requestedBy` | Long | 是 | 申请人 |
| `reviewedBy` | Long | 否 | 审核人 |
| `reviewedAt` | LocalDateTime | 否 | 审核时间 |
| `reviewRemark` | String | 否 | 审核备注 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### MedicationAdministration

用途：用药执行记录。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `medicationRequestId` | Long | 是 | 用药委托 |
| `administeredAt` | LocalDateTime | 是 | 实际用药时间 |
| `actualDosage` | String | 否 | 实际剂量 |
| `reactionObserved` | Boolean | 是 | 是否观察到反应 |
| `remark` | String | 否 | 执行备注 |
| `administeredBy` | Long | 是 | 执行人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### IncidentReport

用途：异常事件和事故上报。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `type` | Enum | 是 | HEALTH_ABNORMAL, INJURY, SAFETY_EVENT, BEHAVIOR, OTHER |
| `severity` | Enum | 是 | LOW, MEDIUM, HIGH, CRITICAL |
| `status` | Enum | 是 | OPEN, PROCESSING, CLOSED |
| `occurredAt` | LocalDateTime | 是 | 发生时间 |
| `location` | String | 否 | 发生地点 |
| `title` | String | 是 | 标题 |
| `description` | String | 否 | 描述 |
| `handlingProcess` | String | 否 | 处理过程 |
| `followUpPlan` | String | 否 | 后续跟进 |
| `parentNotified` | Boolean | 是 | 是否已通知家长 |
| `parentNotifiedAt` | LocalDateTime | 否 | 通知时间 |
| `parentConfirmed` | Boolean | 是 | 家长是否确认 |
| `parentConfirmedAt` | LocalDateTime | 否 | 家长确认时间 |
| `reportedBy` | Long | 是 | 上报人 |
| `handledBy` | Long | 否 | 处理人 |
| `confirmedBy` | Long | 否 | 确认人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### Announcement

用途：机构/班级通知公告。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `organizationId` | Long | 是 | 所属机构 |
| `classroomId` | Long | 否 | 所属班级，机构通知为空 |
| `scope` | Enum | 是 | ORGANIZATION, CLASSROOM |
| `status` | Enum | 是 | DRAFT, PUBLISHED |
| `title` | String | 是 | 标题 |
| `content` | String | 是 | 内容 |
| `requireReceipt` | Boolean | 是 | 是否要求回执 |
| `publishedAt` | LocalDateTime | 否 | 发布时间 |
| `publishedBy` | Long | 否 | 发布人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### AnnouncementReceipt

用途：通知已读回执。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `announcementId` | Long | 是 | 通知 |
| `userId` | Long | 是 | 已读用户 |
| `readAt` | LocalDateTime | 是 | 已读时间 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### FeeItem

用途：机构收费项目。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `organizationId` | Long | 是 | 所属机构 |
| `name` | String | 是 | 项目名称 |
| `description` | String | 否 | 项目说明 |
| `amount` | BigDecimal | 是 | 默认金额 |
| `status` | Enum | 是 | ACTIVE, DISABLED |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### BillingStatement

用途：收费账单。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `organizationId` | Long | 是 | 所属机构 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `feeItemId` | Long | 否 | 收费项目 |
| `title` | String | 是 | 账单标题 |
| `amount` | BigDecimal | 是 | 金额 |
| `dueDate` | LocalDate | 否 | 截止日期 |
| `status` | Enum | 是 | UNPAID, PAID, CANCELLED |
| `paidAt` | LocalDateTime | 否 | 支付时间 |
| `paymentMethod` | String | 否 | 支付方式 |
| `remark` | String | 否 | 备注 |
| `createdBy` | Long | 否 | 创建人 |
| `paidBy` | Long | 否 | 标记支付人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### DirectorDashboardResponse

用途：园长驾驶舱机构运营概览聚合 DTO。

状态：已实现。

说明：T018 不新增持久化 Entity，基于 `Organization`、`Classroom`、`Enrollment`、`AttendanceRecord`、`IncidentReport`、`BillingStatement`、`Announcement` 聚合生成。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `organizationId` | Long | 是 | 机构 ID |
| `organizationName` | String | 是 | 机构名称 |
| `date` | LocalDate | 是 | 统计日期 |
| `classroomCount` | Integer | 是 | 班级数 |
| `activeEnrollmentCount` | Integer | 是 | 当前在托人数 |
| `expectedAttendanceCount` | Integer | 是 | 应到人数，当前等于在托人数 |
| `checkedInCount` | Integer | 是 | 已到人数 |
| `leaveCount` | Integer | 是 | 请假人数 |
| `attendanceRate` | Double | 是 | 出勤率百分比 |
| `openIncidentCount` | Integer | 是 | 未关闭异常/事故数 |
| `unpaidBillCount` | Integer | 是 | 未支付账单数 |
| `unpaidBillAmount` | BigDecimal | 是 | 未支付账单金额 |
| `publishedAnnouncementCount` | Integer | 是 | 已发布通知数 |

### AdmissionLead

用途：招生线索、报名审核和试托状态雏形。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `organizationId` | Long | 是 | 所属机构 |
| `intendedClassroomId` | Long | 否 | 意向班级 |
| `childName` | String | 是 | 宝宝姓名 |
| `childGender` | String | 否 | 宝宝性别 |
| `childBirthday` | LocalDate | 否 | 宝宝生日 |
| `guardianName` | String | 是 | 家长姓名 |
| `guardianPhone` | String | 是 | 家长手机号 |
| `source` | Enum | 是 | ONLINE, REFERRAL, OPEN_DAY, WALK_IN, OTHER |
| `intentionLevel` | Enum | 是 | HIGH, MEDIUM, LOW, LOST |
| `status` | Enum | 是 | NEW, FOLLOWING, APPLIED, APPROVED, REJECTED, TRIALING, TRIAL_COMPLETED, ENROLLED, LOST |
| `preferredStartDate` | LocalDate | 否 | 期望入托日期 |
| `remark` | String | 否 | 跟进备注 |
| `reviewedBy` | Long | 否 | 审核人 |
| `reviewedAt` | LocalDateTime | 否 | 审核时间 |
| `reviewRemark` | String | 否 | 审核备注 |
| `trialStartDate` | LocalDate | 否 | 试托开始日期 |
| `trialEndDate` | LocalDate | 否 | 试托结束日期 |
| `trialFeedback` | String | 否 | 试托反馈 |

### MealPlan

用途：机构食谱和餐次公示。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `organizationId` | Long | 是 | 所属机构 |
| `mealDate` | LocalDate | 是 | 食谱日期 |
| `mealType` | Enum | 是 | BREAKFAST, MORNING_SNACK, LUNCH, AFTERNOON_SNACK, DINNER |
| `title` | String | 是 | 餐次标题 |
| `foodItems` | String | 否 | 菜品/食材内容 |
| `allergenNotes` | String | 否 | 过敏提示 |
| `nutritionNotes` | String | 否 | 营养说明 |
| `status` | Enum | 是 | DRAFT, PUBLISHED |
| `createdBy` | Long | 否 | 创建人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### MealIntakeRecord

用途：宝宝实际进食记录。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `mealPlanId` | Long | 是 | 对应食谱餐次 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `intakeLevel` | Enum | 是 | ALL, MOST, HALF, LESS, NONE |
| `allergyReaction` | Boolean | 是 | 是否出现过敏反应 |
| `reactionNotes` | String | 否 | 反应说明 |
| `remark` | String | 否 | 备注 |
| `recordedBy` | Long | 否 | 记录人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### SafetyLedger

用途：安全、卫生、消毒、食品留样、设施巡检、消防和事故跟进台账。

状态：已实现。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `organizationId` | Long | 是 | 所属机构 |
| `relatedIncidentId` | Long | 否 | 关联异常事故 |
| `ledgerDate` | LocalDate | 是 | 台账日期 |
| `ledgerType` | Enum | 是 | DISINFECTION, FOOD_SAMPLE, FACILITY_INSPECTION, FIRE_SAFETY, SAFETY_EDUCATION, INCIDENT_FOLLOWUP, OTHER |
| `title` | String | 是 | 台账标题 |
| `content` | String | 否 | 台账内容 |
| `location` | String | 否 | 位置 |
| `responsiblePerson` | String | 否 | 责任人 |
| `dueAt` | LocalDateTime | 否 | 截止时间 |
| `completedAt` | LocalDateTime | 否 | 完成时间 |
| `status` | Enum | 是 | OPEN, PROCESSING, CLOSED, OVERDUE |
| `handleRemark` | String | 否 | 处理备注 |
| `createdBy` | Long | 否 | 创建人 |
| `handledBy` | Long | 否 | 处理人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### ChildDevelopmentAssessment

用途：托育月龄里程碑和保育五大领域评估。

状态：已实现。

说明：旧 `DevelopmentAssessment` 保留给历史家庭/AI 评估场景；T022 新增本模型承接机构入托后的专业评估。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `enrollmentId` | Long | 是 | 入托档案 |
| `assessmentDate` | LocalDate | 是 | 评估日期 |
| `childAgeMonths` | Integer | 是 | 评估时月龄 |
| `assessmentMode` | Enum | 是 | TODDLER_MILESTONE, PRESCHOOL_DOMAIN |
| `title` | String | 是 | 评估标题 |
| `grossMotorScore` | Integer | 否 | 大运动得分 |
| `fineMotorScore` | Integer | 否 | 精细动作得分 |
| `languageScore` | Integer | 否 | 语言得分 |
| `cognitiveScore` | Integer | 否 | 认知得分 |
| `socialEmotionalScore` | Integer | 否 | 社交情绪得分 |
| `healthScore` | Integer | 否 | 健康领域得分 |
| `scienceScore` | Integer | 否 | 科学领域得分 |
| `artScore` | Integer | 否 | 艺术领域得分 |
| `maxScore` | Integer | 是 | 单项最高分 |
| `overallLevel` | Enum | 是 | ADVANCED, AGE_APPROPRIATE, NEEDS_SUPPORT, DELAY_RISK |
| `summary` | String | 否 | 评估摘要 |
| `recommendation` | String | 否 | 发展建议 |
| `radarData` | Text | 否 | 雷达图数据 JSON |
| `assessedBy` | Long | 否 | 评估人 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### RegulatoryReportResponse

用途：监管字段与导出数据聚合响应。

状态：已实现，非持久化 DTO。

说明：T024 不新增监管报表持久化表，基于 `Organization`、`Classroom`、`Enrollment`、`Staff`、`AttendanceRecord`、`HealthObservation`、`SafetyLedger` 聚合生成。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `organizationId` | Long | 是 | 机构 ID |
| `organizationName` | String | 是 | 机构名称 |
| `registrationNo` | String | 否 | 监管备案编号 |
| `licenseNo` | String | 否 | 办学/托育许可证号 |
| `legalRepresentative` | String | 否 | 法定代表人 |
| `supervisorDepartment` | String | 否 | 主管部门 |
| `organizationLevel` | String | 否 | 机构等级 |
| `operationType` | String | 否 | 运营类型 |
| `periodStart` | LocalDate | 是 | 统计开始日期 |
| `periodEnd` | LocalDate | 是 | 统计结束日期 |
| `classroomCount` | Integer | 是 | 班级数量 |
| `totalCapacity` | Integer | 是 | 总托位容量 |
| `activeEnrollmentCount` | Integer | 是 | 在托幼儿数 |
| `capacityUsageRate` | Double | 是 | 托位使用率 |
| `staffCount` | Integer | 是 | 员工总数 |
| `directorCount` | Integer | 是 | 园长数 |
| `teacherCount` | Integer | 是 | 教师数 |
| `caregiverCount` | Integer | 是 | 保育员数 |
| `financeCount` | Integer | 是 | 财务数 |
| `attendanceRecordCount` | Integer | 是 | 考勤记录数 |
| `leaveRecordCount` | Integer | 是 | 请假记录数 |
| `healthObservationCount` | Integer | 是 | 健康观察记录数 |
| `abnormalObservationCount` | Integer | 是 | 异常观察数 |
| `followUpObservationCount` | Integer | 是 | 需跟进观察数 |
| `safetyLedgerCount` | Integer | 是 | 安全卫生台账数 |
| `openSafetyLedgerCount` | Integer | 是 | 待处理台账数 |
| `closedSafetyLedgerCount` | Integer | 是 | 已关闭台账数 |
| `overdueSafetyLedgerCount` | Integer | 是 | 逾期台账数 |
| `missingRegulatoryFields` | List<String> | 是 | 缺失的监管备案字段 |
| `exportRows` | List<RegulatoryExportRow> | 是 | 扁平化导出行 |

### HardwareDevice

用途：硬件设备接入抽象。

状态：已实现。

说明：T025 不接真实厂商 SDK，只记录设备档案、设备类型、位置和接入模式。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `organizationId` | Long | 是 | 所属机构 |
| `classroomId` | Long | 否 | 绑定班级 |
| `deviceCode` | String | 是 | 机构内唯一设备编码 |
| `name` | String | 是 | 设备名称 |
| `deviceType` | Enum | 是 | ATTENDANCE_TERMINAL, HEALTH_CHECK_ROBOT, CAMERA, ACCESS_CONTROL, SMART_CLASS_CARD, OTHER |
| `vendor` | String | 否 | 厂商 |
| `model` | String | 否 | 型号 |
| `location` | String | 否 | 安装位置 |
| `integrationMode` | String | 否 | 接入模式，默认 MANUAL_GATEWAY |
| `status` | Enum | 是 | ACTIVE, DISABLED, MAINTENANCE |
| `lastSeenAt` | LocalDateTime | 否 | 最近事件时间 |
| `remark` | String | 否 | 备注 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

### HardwareEvent

用途：硬件原始事件流水。

状态：已实现。

说明：事件只作为设备事实入库，后续需要由明确业务流程映射到 `AttendanceRecord`、`HealthObservation` 或接送核验，避免硬件事件绕过人工/园所业务校验。

| 字段 | 类型 | 必填 | 说明 |
|---|---|---|---|
| `id` | Long | 是 | 主键 |
| `deviceId` | Long | 是 | 来源设备 |
| `organizationId` | Long | 是 | 所属机构 |
| `classroomId` | Long | 否 | 关联班级 |
| `enrollmentId` | Long | 否 | 关联入托档案 |
| `eventType` | Enum | 是 | ATTENDANCE_CHECK_IN, ATTENDANCE_CHECK_OUT, HEALTH_MEASUREMENT, FACE_CAPTURE, PICKUP_VERIFY, SAFETY_ALERT, RAW_MESSAGE |
| `eventTime` | LocalDateTime | 是 | 设备事件时间 |
| `eventKey` | String | 否 | 厂商事件键或幂等键 |
| `subjectRef` | String | 否 | 设备识别到的人员/对象引用 |
| `confidence` | Double | 否 | 识别置信度 |
| `payload` | Text | 否 | 原始载荷 JSON 或文本 |
| `status` | Enum | 是 | RECEIVED, IGNORED, PROCESSED, FAILED |
| `processedAt` | LocalDateTime | 否 | 处理时间 |
| `processRemark` | String | 否 | 处理备注 |
| `createdAt` | LocalDateTime | 是 | 创建时间 |
| `updatedAt` | LocalDateTime | 是 | 更新时间 |

## 4. 关系边界

- `Family` 负责家庭关系。
- `Organization` 负责机构关系。
- `Enrollment` 连接 `Baby`、`Organization`、`Classroom`。
- `AttendanceRecord` 和 `CareRecord` 基于入托后的宝宝记录。
- `DailyReport` 汇总考勤和保育记录，不直接替代原始记录。
- `HardwareDevice` 只描述设备归属和接入状态。
- `HardwareEvent` 只沉淀设备原始事件，不直接替代 `AttendanceRecord`、`HealthObservation`、`PickupDelegation` 等业务记录。

## 5. 新增模型前检查清单

新增 Entity 前必须确认：

- 是否已有模型可扩展
- 是否会与现有模型职责重复
- 是否需要新增枚举
- 是否需要唯一约束
- 是否需要权限校验
- 是否需要前端类型和接口
- 是否已更新本文档
