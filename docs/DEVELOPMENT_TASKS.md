# 开发任务清单

## 执行规则

- 每次开始开发前，先阅读 `docs/AGENT_WORKFLOW.md`、本文件和 `docs/PROJECT_STATUS.md`。
- 如果发现源码或文档乱码，必须先修复乱码，再继续功能开发。
- 必须使用 `node`/`rg` 验证文件真实 UTF-8 内容，不只相信 PowerShell 显示。
- 新功能不得新增正式 mock fallback；暂未接真实接口时，页面必须明确展示空状态、错误状态或“待接入”状态。
- 每完成一个任务，必须更新本文件、`docs/PROJECT_STATUS.md` 和 `docs/CHANGELOG.md`。

## 当前迭代

当前任务：`T048 系统管理后台规划与入口`

目标：

- T040 真实数据完整试跑已完成，详见 `docs/TEST_REPORT_T040.md`。
- 主链 TC-L1-001 至 TC-L1-011 全部通过，L2/L3 补充用例通过。
- 已完成角色工作内容、权限边界和责任边界审计，详见 `docs/ROLE_PERMISSION_AUDIT.md`。
- 已完成系统端到端运转流程串联，详见 `docs/OPERATION_WORKFLOW.md`。
- 下一步优先建立系统管理后台入口和 RBAC 设计，避免后续功能继续堆到 `ADMIN` 一个角色上。

验收标准：

- 系统管理员、老板/机构管理员、园长、教师、保育员、保健员、安全后勤、财务运营、家长、长辈的边界已形成文档。
- 任务板已新增 T048-T077，覆盖系统后台、RBAC、多园区、园长、教师、保育员、保健、安全、财务、家长、长辈和入托流程。
- 后续开发必须按 `docs/ROLE_PERMISSION_AUDIT.md` 的边界和 `docs/OPERATION_WORKFLOW.md` 的流程执行。

## 任务板

| ID | 优先级 | 阶段 | 模块 | 任务 | 状态 | 依赖 | 验收标准 |
|---|---|---|---|---|---|---|---|
| T000 | P0 | 基础 | 文档 | 建立长期开发文档体系 | Done | 无 | 入口、规范、任务、模型、接口、状态文档齐全 |
| T000A | P0 | 基础 | UI/UX | 系统样式审计与设计方向 | Done | T000 | 输出 `docs/UI_STYLE_AUDIT.md` |
| T001 | P0 | 基础 | 稳定性 | 前端构建错误清理 | Done | T000 | `npm run build` 通过 |
| T001A | P0 | 基础 | UI/UX | 建立前端设计系统 token | Done | T001 | Ant Design theme 与 CSS tokens 统一 |
| T001B | P0 | 基础 | UI/UX | 登录页视觉重构 | Done | T001A | 登录页符合托育系统入口定位，移动端可用 |
| T001C | P1 | 基础 | UI/UX | 主布局与 Dashboard 重构 | Done | T001A | 主布局和首页改为机构工作台风格 |
| T001D | P0 | 基础 | 稳定性 | 前端乱码清理与视觉骨架恢复 | Done | T001 | 核心前端页面无明显乱码，构建通过 |
| T002 | P0 | 基础 | 稳定性 | 清理前端 mock fallback | Done | T001D | 移除 AI 本地模拟回复，接口未接入时展示待接入/空状态 |
| T003 | P0 | 基础 | 状态层 | 统一 familyStore 接口 | Done | T002 | 使用现有 `/family/*` API |
| T004 | P1 | 主链 | 机构 | 机构模型与接口 | Done | T003 | Organization 后端代码和前端 `organizationAPI` 已完成 |
| T005 | P1 | 主链 | 班级 | 班级模型与接口 | Done | T004 | Classroom 后端代码和前端 `classroomAPI` 已完成 |
| T006 | P1 | 主链 | 员工 | 员工/老师角色模型 | Done | T004 | Staff 后端代码和前端 `staffAPI` 已完成 |
| T007 | P1 | 主链 | 宝宝 | 宝宝入托档案 | Done | T005,T006 | Enrollment 后端代码和前端 `enrollmentAPI` 已完成 |
| T008 | P1 | 主链 | 考勤接送 | 考勤与接送基础 | Done | T007 | AttendanceRecord、LeaveRequest 后端接口和前端 `attendanceAPI` 已完成 |
| T009 | P1 | 主链 | 保育 | 一日照护记录 | Done | T008 | CareRecord 后端接口和前端 `careRecordAPI` 已完成 |
| T010 | P1 | 主链 | 日报 | 结构化家长日报 | Done | T009 | DailyReport 后端接口和前端 `dailyReportAPI` 已完成 |
| T011 | P1 | 安全健康 | 家长端 | 家长查看日报与考勤 | Done | T010 | 家长日报页面已接入 `dailyReportAPI`、`attendanceAPI`、`careRecordAPI` |
| T012 | P1 | 安全健康 | 晨午检 | 晨午检与全日观察 | Done | T008 | HealthObservation 后端接口和前端 `healthObservationAPI` 已完成 |
| T013 | P1 | 安全健康 | 接送 | 接送人和委托接送 | Done | T008 | AuthorizedPickupPerson、PickupDelegation 和前端 `pickupAPI` 已完成 |
| T014 | P1 | 安全健康 | 用药过敏 | 用药与过敏管理 | Done | T007 | AllergyTag、MedicationRequest、MedicationAdministration 和前端 `medicationCareAPI` 已完成 |
| T015 | P1 | 安全健康 | 事故 | 异常/事故上报 | Done | T009 | IncidentReport 后端接口和前端 `incidentReportAPI` 已完成 |
| T016 | P2 | 园所运营 | 通知 | 通知公告与已读回执 | Done | T004 | Announcement、AnnouncementReceipt 和前端 `announcementAPI` 已完成 |
| T017 | P2 | 园所运营 | 财务 | 收费账单雏形 | Done | T007 | FeeItem、BillingStatement 和前端 `billingAPI` 已完成 |
| T018 | P2 | 园所运营 | 看板 | 园长驾驶舱 | Done | T008,T009,T010 | `DirectorDashboardResponse`、聚合 Service、Controller 和前端 `directorDashboardAPI` 已完成 |
| T019 | P2 | 园所运营 | 招生 | 招生/报名/试托雏形 | Done | T004 | `AdmissionLead` 和前端 `admissionLeadAPI` 已完成 |
| T020 | P2 | 专业保健 | 食谱 | 食谱与膳食记录雏形 | Done | T014 | `MealPlan`、`MealIntakeRecord` 和前端 `mealPlanAPI` 已完成 |
| T021 | P2 | 专业保健 | 台账 | 安全与卫生台账 | Done | T012,T015 | `SafetyLedger` 和前端 `safetyLedgerAPI` 已完成 |
| T022 | P2 | 成长评估 | 评估 | 月龄里程碑与五大领域评估 | Done | T009 | `ChildDevelopmentAssessment` 和前端 `childDevelopmentAssessmentAPI` 已完成 |
| T023 | P3 | 多端体验 | 祖辈 | 祖辈模式 | Done | T011,T013 | 新增 `/elder-mode`，复用日报、考勤、接送真实 API，支持大字、高对比、语音播报预留 |
| T024 | P3 | 平台能力 | 监管 | 监管字段与导出 | Done | T018,T021 | 新增机构监管备案字段、`RegulatoryReportController` 和前端 `regulatoryReportAPI` |
| T025 | P3 | 平台能力 | 硬件 | 硬件接入抽象 | Done | T008,T012,T013 | 新增 `HardwareDevice`、`HardwareEvent`、`HardwareIntegrationController` 和前端 `hardwareIntegrationAPI` |
| T026 | P3 | 智能能力 | AI | AI 日报辅助 | Done | T010 | 新增 `/daily-report/ai-draft/generate`，基于真实记录生成可编辑日报草稿 |
| T027 | P0 | 第二阶段 | 规划 | 重建产品闭环任务板 | Done | T026 | 明确第二阶段从页面串联、权限细化、测试补强和验收准备推进 |
| T028 | P0 | 第二阶段 | 工作台 | 首页真实数据工作台 | Done | T018 | Dashboard 接入 `organizationAPI`、`classroomAPI`、`directorDashboardAPI`，移除首页静态统计 |
| T029 | P0 | 第二阶段 | 机构运营 | 园长端机构/班级/员工/入托管理页面 | Done | T028 | 新增 `/organization-management`，接真实机构、班级、员工、入托 API，支持创建和列表空/错/加载状态 |
| T030 | P0 | 第二阶段 | 教师保育 | 教师今日班级工作台 | Done | T029 | 新增 `/teacher-workbench`，按班级展示宝宝、考勤、晨午检、照护，并支持真实到园/缺勤操作 |
| T031 | P0 | 第二阶段 | 日报闭环 | 日报生成、AI 草稿、编辑、发布页面 | Done | T030 | 新增 `/daily-report-management`，支持真实日报草稿生成、AI 草稿、编辑、保存和发布 |
| T032 | P1 | 第二阶段 | 家长祖辈 | 家长/祖辈端聚合页增强 | Done | T031 | 家长日报页聚合日报、考勤、照护、通知、接送、用药真实 API |
| T033 | P1 | 第二阶段 | 安全健康 | 健康、安全、用药、事故页面串联 | Done | T030 | 新增 `/health-safety`，晨午检、用药、事故、安全台账形成可操作页面 |
| T034 | P1 | 第二阶段 | 运营监管 | 招生、收费、食谱、监管页面串联 | Done | T029 | 新增 `/operations-regulatory`，招生、收费、食谱、监管导出从 API 变为可验收页面 |
| T035 | P1 | 第二阶段 | 权限 | 多角色菜单与操作权限细化 | Done | T029 | 基于现有 `User.role` 做 ADMIN/PARENT 菜单可见性边界 |
| T036 | P1 | 第二阶段 | 测试 | 主链测试补强 | Done | T031 | 前端构建通过；后端 `mvn test` 构建通过但当前无测试源，真实测试用例后续继续补 |
| T037 | P2 | 第二阶段 | 技术债 | 前端超长类型/API 文件拆分 | Done | T028 | `types/index.ts` 改为导出口，`services/api.ts` 拆出 `http.ts`、`legacyApi.ts`、`organizationApi.ts` |
| T038 | P2 | 第二阶段 | 技术债 | 配置与构建警告清理 | Done | T036 | 清理 Maven 重复依赖、MySQL 坐标警告、前端路由懒加载和分包警告 |
| T039 | P0 | 第三阶段 | UI/UX | 后台整体 UI/UX 重构 | Done | T038 | 重构全局 token、主布局、菜单、顶部栏和今日工作台，使后台风格符合托育保育 SaaS 产品 |
| T040 | P0 | 第三阶段 | 验收 | 真实数据完整试跑 | Done | T039 | 主链 TC-L1-001 至 TC-L1-011 全部通过，L2/L3 补充用例通过，详见 `docs/TEST_REPORT_T040.md` |
| T041 | P0 | 第三阶段 | 数据质量 | 修复中文昵称编码显示问题 | Done | T040 | 新增 MySQL utf8mb4 启动检查；注册字段 trim/空字符串规范化；待真实库回归确认中文昵称不再出现 `???` |
| T042 | P1 | 第三阶段 | 入托档案 | 修正入托档案入园日期展示 | Done | T040 | 入托创建默认 ACTIVE；后端兼容 `entryDate`/`admissionDate`；前端统一显示“入园日期” |
| T043 | P1 | 第三阶段 | 认证权限 | 注册接口字段校验与角色策略 | Done | T040 | 手机号空字符串按未填写处理；开发环境自动初始化 ADMIN 账号；修复启用用户查询条件 |
| T044 | P2 | 第三阶段 | 页面操作 | 补齐照护与健康安全创建入口 | Done | T040 | TeacherWorkbench/HealthSafety 已提供健康观察、照护、用药、事故、台账创建入口 |
| T045 | P3 | 第三阶段 | 开发配置 | CORS 开发端口补充 | Done | T040 | 开发环境允许 `localhost:3001`，Security 复用统一 CORS 配置 |
| T046 | P3 | 第三阶段 | 运营监管 | 修正运营监管在托幼儿数统计 | Done | T040 | 入托创建默认 ACTIVE，监管统计将有效入托纳入口径 |
| T047 | P0 | 第三阶段 | 验收 | T040 缺陷修复回归测试 | Pending | T041-T046 | 复跑 T040 缺陷点，确认 6 个问题关闭或记录剩余风险 |
| T048 | P0 | 第四阶段 | 系统管理 | 系统管理后台规划与入口 | Pending | 角色审计 | 新增系统设置/平台管理入口，明确平台配置、账号、数据字典、日志入口 |
| T049 | P0 | 第四阶段 | 权限 | RBAC 角色权限模型 | Pending | T048 | 明确登录角色、员工岗位、菜单权限、按钮权限和接口权限关系 |
| T050 | P1 | 第四阶段 | 审计 | 审计日志与关键操作留痕 | Pending | T049 | 机构、员工、入托、健康、财务、权限变更等关键操作可追踪 |
| T051 | P0 | 第四阶段 | 多园区 | 多园区组织模型设计 | Pending | T049 | 明确集团/机构主体、园区、班级、员工、宝宝的数据关系 |
| T052 | P1 | 第四阶段 | 老板端 | 老板/机构管理员多园区驾驶舱 | Pending | T051 | 可查看多园区托位、出勤、招生、收费、健康、安全和风险对比 |
| T053 | P1 | 第四阶段 | 多园区 | 园区负责人任命与数据范围控制 | Pending | T051 | 老板可分配园长；园长只能管理授权园区 |
| T054 | P0 | 第四阶段 | 园长端 | 园长工作台与园区数据范围 | Pending | T049,T051 | 园长菜单、待办、风险和数据范围清晰 |
| T055 | P1 | 第四阶段 | 员工 | 班级员工分配 | Pending | T054 | 教师/保育员可分配到班级，后续按班级授权 |
| T056 | P1 | 第四阶段 | 入托档案 | 入托审核、转班、退托流程 | Pending | T054 | 入托不再只是直接创建，支持审核、转班、退托状态流 |
| T057 | P0 | 第四阶段 | 教师端 | 教师班级授权与菜单边界 | Pending | T049,T055 | 教师只能访问授权班级和宝宝，可操作考勤、日报、教学相关内容 |
| T058 | P1 | 第四阶段 | 日报 | 日报发布审核策略 | Pending | T057 | 支持教师发布或园长审核发布的配置策略 |
| T059 | P1 | 第四阶段 | 保育员端 | 保育员工作台与照护任务清单 | Pending | T049,T055 | 保育员只看本班照护任务和漏记提醒 |
| T060 | P2 | 第四阶段 | 照护 | 照护补录与审核机制 | Pending | T059 | 补录照护记录需保留原因和审核痕迹 |
| T061 | P0 | 第四阶段 | 保健 | 扩展保健员角色 | Pending | T049 | 新增保健员/保健医岗位和菜单权限 |
| T062 | P1 | 第四阶段 | 保健 | 保健工作台 | Pending | T061 | 聚合晨午检、异常、用药、过敏、食谱、健康台账 |
| T063 | P2 | 第四阶段 | 保健 | 传染病防控流程 | Pending | T062 | 支持疑似、隔离、上报、复园、班级提醒 |
| T064 | P2 | 第四阶段 | 营养 | 营养膳食分析 | Pending | T062 | 食谱和进食记录可形成营养/摄入统计 |
| T065 | P1 | 第四阶段 | 安全后勤 | 扩展安全/后勤角色 | Pending | T049 | 新增安全后勤岗位和菜单权限 |
| T066 | P2 | 第四阶段 | 安全后勤 | 安全台账周期任务与过期提醒 | Pending | T065 | 消毒、消防、巡检、食品留样等形成周期任务和逾期提醒 |
| T067 | P1 | 第四阶段 | 运营 | 扩展运营/招生角色 | Pending | T049 | 新增招生/运营岗位并限定招生线索、试托、经营数据权限 |
| T068 | P1 | 第四阶段 | 财务运营 | 财务运营工作台 | Pending | T067 | 收费、账单、欠费、招生转化形成独立工作台 |
| T069 | P2 | 第四阶段 | 招生 | 招生漏斗与跟进提醒 | Pending | T067 | 支持线索阶段、跟进记录、试托转化和提醒 |
| T070 | P0 | 第四阶段 | 家长端 | 家长入托关系绑定 | Pending | T056 | 家长账号与宝宝入托档案建立正式授权关系 |
| T071 | P1 | 第四阶段 | 家长端 | 家长申请闭环增强 | Pending | T070 | 请假、用药、接送委托具备提交、审核、反馈闭环 |
| T072 | P2 | 第四阶段 | 家长端 | 家长账单查看与支付预留 | Pending | T070 | 家长可查看账单，支付接口先预留 |
| T073 | P2 | 第四阶段 | 长辈端 | 长辈授权与只读权限 | Pending | T070 | 长辈默认只读，可由家长授权接送/通知确认 |
| T074 | P0 | 第四阶段 | 入托流程 | 入托流程重构 | Pending | T051,T070 | 明确招生、家长、园长、保健员在入托中的责任边界 |
| T075 | P1 | 第四阶段 | 入托流程 | 招生线索转入托 | Pending | T074 | 试托/报名可转为入托档案 |
| T076 | P1 | 第四阶段 | 入托流程 | 家长补资料与确认 | Pending | T074 | 家长可补充宝宝、监护人、接送、健康资料并确认 |
| T077 | P1 | 第四阶段 | 入托流程 | 转班、退托、复托流程 | Pending | T074 | 入托档案支持完整状态流和历史留痕 |
| T078 | P0 | 第四阶段 | 家长账号 | 家长手机号注册与短信验证 | Pending | T070 | 家长公开注册以手机号为主，支持短信验证码，用户名仅作为兼容字段 |
| T079 | P0 | 第四阶段 | 家长绑定 | 家长邀请码绑定宝宝和入托档案 | Pending | T070,T074 | 园长/招生可生成邀请码，家长注册或登录后安全绑定指定宝宝和入托档案 |
| T080 | P2 | 第四阶段 | 家长账号 | 微信登录/手机号授权预留 | Pending | T078 | 预留微信登录和微信手机号授权，不影响手机号主链 |

## 阶段完成定义

### L1 主链闭环

```text
创建机构
-> 创建班级
-> 添加员工
-> 创建入托档案
-> 老师记录到园/离园/请假
-> 老师记录一日照护
-> 系统生成结构化日报
-> 家长查看日报
```

### L2 安全健康

```text
晨午检
-> 异常跟踪
-> 用药/过敏
-> 接送授权
-> 事故上报
-> 安全健康台账
```

### L3 园所运营

```text
招生/试托
-> 收费账单
-> 通知公告
-> 园长驾驶舱
-> 师生比和容量预警
```
