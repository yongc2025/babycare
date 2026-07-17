# 项目状态总览

最近更新：2026-07-16

## 当前定位

BabyCare/HuiGrowth 当前代码基础已从“家庭育儿记录 + 家庭协作”雏形，开始向“托育 + 保育机构运营平台”迁移。

2026-07-15 已纳入正式用户需求《托育+保育平台需求（用户版-供参考）》的评估结论。后续开发策略调整为：

```text
模块化单体后端
单 Web 多角色前端
先跑通托育主链
再逐步扩展健康、安全、运营、监管、多端和硬件
```

## 当前技术栈

- 前端：React 18、TypeScript、Vite、Ant Design、Zustand、Axios
- 后端：Spring Boot 3.2、Java 17、Spring Security、JWT、Spring Data JPA、MySQL
- 文档入口：`docs/AGENT_WORKFLOW.md`
- 架构决策：`docs/ARCHITECTURE_DECISIONS.md`
- 需求矩阵：`docs/需求差异与采纳矩阵.md`
- 任务清单：`docs/DEVELOPMENT_TASKS.md`
- T040 测试方案：`docs/TEST_PLAN_T040.md`
- T040 测试报告：`docs/TEST_REPORT_T040.md`
- 角色权限审计：`docs/ROLE_PERMISSION_AUDIT.md`
- 系统运转流程：`docs/OPERATION_WORKFLOW.md`
- 产品蓝图：`docs/托育保育产品规划.md`
- 用户需求参考：`docs/托育+保育平台需求（用户版-供参考）.md`

## 已完成

| 模块 | 状态 | 说明 |
|---|---|---|
| 用户认证 | 可用雏形 | 登录、注册、JWT、用户信息存储 |
| 前端基础构建 | 已恢复 | `npm run build` 已通过 |
| 全局主题 | 已重构 | Ant Design token 和 CSS 变量已改为托育系统风格 |
| 登录页 | 已重构 | 从模板登录页改为托育机构工作台入口 |
| 后台整体 UI/UX | 已重构 | T039 已完成全局 token、Ant Design 主题、主布局、左侧菜单、顶部栏和首页工作台统一 |
| 主布局 | 已重构 | 改为托育机构后台工作台侧栏、顶部栏和角色空间结构 |
| Dashboard | 已接真实数据并重构 | 接入机构、班级和园长驾驶舱 API，新增运营入口，移除首页静态 demo 统计 |
| 业务页面乱码 | 已清理一轮 | AI 育儿、保育记录、教育规划、家园协作、家庭管理已恢复清晰中文骨架 |
| 家庭状态层 | 已统一 | `familyStore` 已改为使用真实 `/family/*` API |
| 机构模型与接口 | 已实现并验证 | Organization 实体、Repository、DTO、Service、Controller 和前端 `organizationAPI` |
| 班级模型与接口 | 已实现并验证 | Classroom 实体、Repository、DTO、Service、Controller 和前端 `classroomAPI` |
| 员工模型与接口 | 已实现并验证 | Staff 实体、Repository、DTO、Service、Controller 和前端 `staffAPI` |
| 入托档案模型与接口 | 已实现并验证 | Enrollment 实体、Repository、DTO、Service、Controller 和前端 `enrollmentAPI` |
| 考勤与请假模型与接口 | 已实现并验证 | AttendanceRecord、LeaveRequest 实体、Repository、DTO、Service、Controller 和前端 `attendanceAPI` |
| 一日照护模型与接口 | 已实现并验证 | CareRecord 实体、Repository、DTO、Service、Controller 和前端 `careRecordAPI` |
| 结构化家长日报模型与接口 | 已实现并验证 | DailyReport 实体、Repository、DTO、Service、Controller 和前端 `dailyReportAPI` |
| 家长日报查看入口 | 已实现并验证 | 新增 `ParentReports` 页面，接入日报、考勤和照护记录真实 API |
| 晨午检与全日观察模型与接口 | 已实现并验证 | HealthObservation 实体、Repository、DTO、Service、Controller 和前端 `healthObservationAPI` |
| 接送授权与委托模型与接口 | 已实现并验证 | AuthorizedPickupPerson、PickupDelegation、PickupService、PickupController 和前端 `pickupAPI` |
| 用药与过敏模型与接口 | 已实现并验证 | AllergyTag、MedicationRequest、MedicationAdministration、MedicationCareService 和前端 `medicationCareAPI` |
| 异常/事故上报模型与接口 | 已实现并验证 | IncidentReport 实体、Repository、DTO、Service、Controller 和前端 `incidentReportAPI` |
| 通知公告与已读回执 | 已实现并验证 | Announcement、AnnouncementReceipt、AnnouncementService、AnnouncementController 和前端 `announcementAPI` |
| 收费项目与账单 | 已实现并验证 | FeeItem、BillingStatement、BillingService、BillingController 和前端 `billingAPI` |
| 园长驾驶舱聚合接口 | 已实现并验证 | DirectorDashboardResponse、DirectorDashboardService、DirectorDashboardController 和前端 `directorDashboardAPI` |
| 园所运营页面 | 已实现并验证 | 新增 `/organization-management`，接入机构、班级、员工和入托档案真实 API |
| 教师班级照护页 | 已实现并验证 | 新增 `/teacher-workbench`，接入机构、班级、入托、考勤、健康观察和照护记录真实 API |
| 日报闭环页面 | 已实现并验证 | 新增 `/daily-report-management`，支持日报草稿生成、AI 草稿、编辑、保存和发布 |
| 家长聚合页 | 已增强并验证 | 家长日报页聚合日报、考勤、照护、通知、接送和用药真实 API |
| 健康安全页 | 已实现并验证 | 新增 `/health-safety`，聚合晨午检、用药、事故和安全卫生台账 |
| 运营监管页 | 已实现并验证 | 新增 `/operations-regulatory`，聚合招生、收费、食谱和监管导出 |
| 前端路由分包 | 已完成 | `App.tsx` 改为懒加载路由，`vite.config.ts` 增加手动分包 |
| 前端类型/API 拆分 | 已完成 | `types/index.ts`、`services/api.ts` 已拆薄，避免继续超过硬上限 |
| Maven 警告清理 | 已完成 | 移除重复 `spring-boot-starter-web`，MySQL 驱动改为 `com.mysql:mysql-connector-j` |
| 招生报名与试托雏形 | 已实现并验证 | AdmissionLead、AdmissionLeadService、AdmissionLeadController 和前端 `admissionLeadAPI` |
| 食谱与膳食记录雏形 | 已实现并验证 | MealPlan、MealIntakeRecord、MealPlanService、MealPlanController 和前端 `mealPlanAPI` |
| 安全与卫生台账 | 已实现并验证 | SafetyLedger、SafetyLedgerService、SafetyLedgerController 和前端 `safetyLedgerAPI` |
| 月龄里程碑与五大领域评估 | 已实现并验证 | ChildDevelopmentAssessment、ChildDevelopmentAssessmentService、Controller 和前端 `childDevelopmentAssessmentAPI` |
| 祖辈模式 | 已实现并验证 | 新增 `/elder-mode` 长辈入口，复用日报、考勤、接送真实 API，支持大字、高对比和语音播报预留 |
| 监管字段与导出 | 已实现并验证 | 机构新增备案字段，新增 `RegulatoryReportController` 和前端 `regulatoryReportAPI`，提供监管聚合与导出行 |
| 硬件接入抽象 | 已实现并验证 | 新增 `HardwareDevice`、`HardwareEvent`、`HardwareIntegrationController` 和前端 `hardwareIntegrationAPI`，仅沉淀设备档案与原始事件 |
| AI 日报辅助 | 已实现并验证 | 新增 `/daily-report/ai-draft/generate`，基于真实考勤、照护和健康观察生成可编辑日报草稿 |
| 真实数据完整试跑 | 已完成 | T040 主链 TC-L1-001 至 TC-L1-011 全部通过，L2/L3 补充用例通过 |
| T040 缺陷修复 | 已完成首轮 | T041-T046 已完成代码修复，等待回归测试确认 |
| 角色权限审计 | 已完成 | 已按系统管理员、老板、园长、教师、保育员、保健员、安全后勤、财务运营、家长、长辈完成边界审计 |
| 系统运转流程 | 已完成 | 已从平台开通、机构入驻、多园区、招生、入托、照护、家园、健康安全、财务、监管串联完整流程 |
| 正式需求评估 | 已完成 | 已吸收用户正式需求中的双龄段、多角色、合规、安全健康和多端策略 |
| 需求采纳矩阵 | 已建立 | 按客户七大模块映射采纳策略、差距、阶段和任务 |
| 架构决策记录 | 已建立 | 记录当前不拆微服务、不拆多端、硬件和监管分阶段落地 |

## 主要差距

- 缺少接送记录不可删除、滞留预警等安全接送能力。
- 缺少容量预警、招生漏斗统计等园所运营增强能力。
- 缺少传染病防控、祖辈模式细化和硬件事件到业务记录的处理流程。
- 当前部分页面仍是产品骨架，后续需要接真实 API。
- `ADMIN` 角色承担过多职责，缺少系统管理员、老板/机构管理员、园长、保健、运营、安全后勤等明确角色边界。
- 当前 `Organization` 更像单园区，尚不支持集团/多园区统筹。

## 当前风险

- 历史文件曾出现编码显示异常；后续每次开发前必须用 `node`/`rg` 验证真实 UTF-8 内容。
- `application.properties` 和 `application-dev.properties` 中存在本地明文配置，后续应迁移到环境变量。
- 前端已完成路由懒加载和分包，最近 `npm run build` 无 Vite chunk 警告。
- 后端 Maven 可通过 `C:\tools\apache-maven-3.9.16\bin\mvn.cmd` 执行；`start-dev.bat` 已内置该 Maven 路径和 JDK 17 路径。
- `backend/pom.xml` 已清理重复 `spring-boot-starter-web` 声明，MySQL 驱动坐标已迁移到 `com.mysql:mysql-connector-j`。
- 正式需求中微服务、多端、硬件和监管能力范围很大，必须按 ADR 分阶段执行，避免过早复杂化。
- T040 测试发现的 6 个缺陷已完成首轮修复；中文昵称问题仍需要在真实 MySQL 环境复测确认。

## 当前架构策略

当前采用模块化单体：

```text
organization
enrollment
attendance
care
health
safety
family
finance
report
```

当前采用单 Web 多角色：

```text
director
teacher
parent
health
finance
security
```

## 下一阶段目标

第一阶段目标：建立托育产品主链最小闭环。

优先级顺序：

1. 机构、班级、员工、入托档案。已完成基础模型。
2. 考勤与接送基础。已完成基础模型。
3. 一日照护记录。已完成基础模型。
4. 结构化家长日报。已完成基础模型。
5. 家长查看端。已完成基础入口。
6. 晨午检、用药、过敏、异常事故已完成基础模型。
7. 通知、收费、园长驾驶舱已完成基础接口。
8. 招生、报名、试托已完成基础模型。
9. 食谱与膳食记录已完成基础模型。
10. 安全与卫生台账已完成基础模型。
11. 月龄里程碑与五大领域评估已完成基础模型。
12. 祖辈模式体验增强已完成。
13. 监管字段与导出雏形已完成。
14. 硬件接入抽象已完成。
15. AI 日报辅助已完成。
16. 当前 T000-T026 任务板已完成。
17. 第二阶段已启动，进入页面串联、权限细化、测试补强和客户验收准备。

## 当前迭代

当前任务：`T048 系统管理后台规划与入口`

当前阶段：

```text
产品闭环与验收准备阶段
```

阶段目标：

1. 将已有后端 API 串到真实页面，减少 demo 感。T028-T034 已完成主要页面串联。
2. 形成园长端、教师/保育员端、家长/祖辈端的端到端使用路径。T029-T034 已完成基础路径。
3. 补齐空状态、错误状态、加载状态和权限边界。T035 已完成现有角色下的菜单边界。
4. 在页面闭环稳定后再做测试补强、文件拆分和构建警告清理。T036-T038 已完成构建验证和主要技术债清理。
5. 根据用户验证反馈统一后台视觉底座。T039 已完成。

下一轮建议：

- 优先执行 T048-T049：系统管理后台入口和 RBAC 角色权限模型。
- 随后执行 T051-T054：多园区组织模型、老板/机构管理员统筹、园长园区负责人边界。
- T047 仍需由测试侧复跑，用于确认 T040 缺陷修复是否关闭。

验证方式：

```text
cd backend
mvn test

cd frontend
npm run build
```

最近验证结果：T041-T046 首轮修复后，后端 `mvn test` 通过；前端 `npm run build` 通过。

## 状态维护规则

每完成一个开发任务，必须更新：

- `docs/PROJECT_STATUS.md`
- `docs/DEVELOPMENT_TASKS.md`
- 如涉及数据模型，更新 `docs/DATA_MODEL.md`
- 如涉及 API，更新 `docs/API_DESIGN.md`
- 如涉及架构策略，更新 `docs/ARCHITECTURE_DECISIONS.md`
- 如涉及规范变更，更新 `docs/CODING_STANDARDS.md`
- 重要变更记录到 `docs/CHANGELOG.md`
