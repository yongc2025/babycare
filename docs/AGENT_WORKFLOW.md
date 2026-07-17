# AI 长期开发工作流入口

本文件是 Codex/AI 每次重新进入 BabyCare/HuiGrowth 项目时必须首先阅读的入口文件。

目标：确保长期开发即使中断，也能按固定流程恢复，避免遗忘上下文、重复造轮子、超出产品边界或继续堆叠 demo 功能。

## 1. 固定恢复顺序

每次开始工作前，必须按顺序阅读：

1. `docs/AGENT_WORKFLOW.md`
2. `docs/PROJECT_STATUS.md`
3. `docs/DEVELOPMENT_TASKS.md`
4. `docs/CODING_STANDARDS.md`
5. `docs/DATA_MODEL.md`
6. `docs/API_DESIGN.md`
7. `docs/ARCHITECTURE_DECISIONS.md`
8. `docs/需求差异与采纳矩阵.md`
9. `docs/托育保育产品规划.md`

阅读完成后，再根据当前任务读取相关代码。

## 2. 编码优先规则

如果发现源码或文档出现乱码、字符串断裂、注释异常：

1. 先暂停功能开发。
2. 确认乱码范围。
3. 使用 `node`/`rg` 验证文件真实 UTF-8 内容，不只相信 PowerShell `Get-Content` 的显示结果。
4. 先修复乱码并保证文件可读。
5. 运行必要验证，例如前端 `npm run build`。
6. 再继续 UI 或业务改动。

不得在乱码文件上继续叠加功能。

## 3. 固定执行流程

任何开发任务都必须按以下流程执行：

```text
读取入口文档
-> 查看任务清单
-> 确认当前任务
-> 扫描乱码和构建状态
-> 阅读相关代码
-> 做最小设计
-> 实现后端
-> 实现前端
-> 验证
-> 更新文档
-> 输出本轮总结
-> 进入下一任务
```

## 4. 当前任务选择规则

1. 优先查找 `docs/DEVELOPMENT_TASKS.md` 中状态为 `In Progress` 的任务。
2. 如果存在 `In Progress`，继续该任务。
3. 如果没有 `In Progress`，选择优先级最高的 `Pending` 任务。
4. 如果任务依赖未完成，不得跳过依赖直接开发。
5. 如果任务范围不清，先补充任务说明和验收标准。

## 5. 开发边界

必须围绕托育 + 保育产品主链推进：

```text
机构 -> 班级 -> 宝宝 -> 考勤 -> 保育记录 -> 日报 -> 家长同步
```

短期不优先做：

- 非核心 AI 包装
- 纯展示型 Dashboard
- 泛社区功能
- 与托育主链无关的大型重构
- 只增加页面但不接真实 API 的功能

## 6. 禁止事项

- 不得新增 mock/demo 数据作为正式功能。
- 不得在接口失败时 fallback 到假数据。
- 不得复制一套已有能力并换名字使用。
- 不得绕过现有 `request`/API service 自行散落写请求。
- 不得把复杂业务逻辑堆进 React 页面组件。
- 不得在 Controller 中写复杂业务逻辑。
- 不得新增未记录的数据表、接口或核心方法。
- 不得引入新框架或大依赖，除非先记录原因并确认必要性。
- 不得改动无关文件制造大面积 diff。

## 7. 每轮交付要求

每完成一个任务，必须更新：

- `docs/PROJECT_STATUS.md`
- `docs/DEVELOPMENT_TASKS.md`
- `docs/DATA_MODEL.md`，如果涉及实体、表、字段
- `docs/API_DESIGN.md`，如果涉及接口
- `docs/CHANGELOG.md`

最终回复必须包含：

- 本轮完成内容
- 修改文件
- 验证结果
- 未验证原因或遗留风险
- 下一步建议

## 8. 验证命令

前端：

```bash
cd frontend
npm run build
```

后端：

```bash
cd backend
mvn test
```

如果 Maven 不可用，至少记录：

```text
后端未验证：当前环境缺少 mvn 命令或依赖不可用。
```

## 9. 文档角色

| 文档 | 用途 |
|---|---|
| `AGENT_WORKFLOW.md` | AI 每次恢复任务的入口 |
| `PROJECT_STATUS.md` | 当前项目整体状态 |
| `DEVELOPMENT_TASKS.md` | 任务板和验收标准 |
| `CODING_STANDARDS.md` | 编码规范、边界和长度限制 |
| `DATA_MODEL.md` | 数据库实体和字段清单 |
| `API_DESIGN.md` | 接口和方法清单 |
| `CHANGELOG.md` | 重要变更记录 |
| `ARCHITECTURE_DECISIONS.md` | 架构决策记录 |
| `需求差异与采纳矩阵.md` | 客户正式需求与当前路线的覆盖关系 |
| `托育保育产品规划.md` | 产品长期蓝图 |

## 10. 长期记忆原则

聊天记录不是长期记忆来源。长期记忆只来自仓库内文档。

如果文档和代码冲突：

1. 先读代码确认事实。
2. 更新文档。
3. 再继续开发。
