# 好芽儿（HuiGrowth）- 托育保育运营平台

## 项目概述

好芽儿是一个面向托育机构的保育照护、健康安全、家园共育和运营监管平台。

## 功能特色

### 🏠 核心功能模块

1. **成长记录** - 智能相册、成长日记、里程碑记录
2. **智能育儿** - AI育儿助手、发育测评、个性化指导  
3. **教育规划** - 能力培养、学习计划、资源推荐
4. **家庭协作** - 家庭圈、任务分配、专家咨询
5. **用户管理** - 多宝宝管理、家庭设置、会员服务

![alt text](image-1.png)

### ✨ 技术亮点

- **现代化技术栈**：React 18 + Spring Boot 3 + MySQL 8
- **响应式设计**：完美适配桌面端和移动端
- **智能化体验**：AI驱动的个性化育儿建议
- **安全可靠**：JWT认证 + 数据加密保护
- **高性能**：数据库优化

## 技术架构

### 前端技术栈
- **框架**：React 18 + TypeScript
- **构建工具**：Vite
- **UI组件库**：Ant Design 5
- **状态管理**：Zustand
- **路由管理**：React Router 6
- **HTTP客户端**：Axios

### 后端技术栈
- **框架**：Spring Boot 3.2
- **安全认证**：Spring Security + JWT
- **数据访问**：Spring Data JPA
- **数据库**：MySQL 8.0
- **API文档**：Swagger 3

### 开发工具
- **版本控制**：Git
- **项目管理**：Maven
- **代码规范**：ESLint + Prettier
- **API测试**：Swagger UI

## 快速开始

### 环境要求

- **Node.js** 18.0+
- **Java** 17+
- **Maven** 3.8+
- **MySQL** 8.0+

### 安装步骤

#### 1. 克隆项目
```bash
git clone https://github.com/your-repo/babycare.git
cd babycare
```

#### 2. 数据库设置

**创建数据库**
```sql
CREATE DATABASE huigrowth CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

**修改数据库配置**
```properties
# backend/src/main/resources/application-dev.properties
spring.datasource.username=your_username
spring.datasource.password=your_password
```

#### 3. 启动后端服务
```bash
cd backend
mvn clean install
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

后端服务将在 http://localhost:8080 启动

#### 4. 启动前端应用
```bash
cd frontend
npm install
npm run dev
```

前端应用将在 http://localhost:3000 启动

### 访问应用

- **前端应用**：http://localhost:3000
- **API文档**：http://localhost:8080/swagger-ui.html
- **健康检查**：http://localhost:8080/api/public/health

## 项目结构

```
BabyCare/
├── frontend/                 # 前端React应用
│   ├── src/
│   │   ├── components/       # 通用组件
│   │   ├── pages/           # 页面组件
│   │   ├── stores/          # 状态管理
│   │   ├── services/        # API服务
│   │   ├── types/           # TypeScript类型
│   │   └── utils/           # 工具函数
│   ├── public/              # 静态资源
│   └── package.json
├── backend/                  # 后端Spring Boot应用
│   ├── src/main/java/
│   │   └── com/huigrowth/babycare/
│   │       ├── controller/   # 控制器层
│   │       ├── service/      # 业务逻辑层
│   │       ├── repository/   # 数据访问层
│   │       ├── entity/       # 实体类
│   │       ├── dto/          # 数据传输对象
│   │       ├── config/       # 配置类
│   │       ├── security/     # 安全配置
│   │       └── util/         # 工具类
│   ├── src/main/resources/   # 配置文件
│   └── pom.xml
├── docs/                     # 项目文档
├── README.md
└── package.json             # 项目元信息
```

## 开发指南

### 代码规范

**前端规范**
- 使用 TypeScript 严格模式
- 组件使用函数式组件 + Hooks
- 遵循 ESLint + Prettier 代码规范
- 使用语义化的命名

**后端规范**
- 遵循 Spring Boot 最佳实践
- 使用 Lombok 减少样板代码
- RESTful API 设计
- 完整的异常处理

### Git 提交规范

```bash
# 功能开发
git commit -m "feat: 添加用户注册功能"

# Bug修复  
git commit -m "fix: 修复登录验证问题"

# 文档更新
git commit -m "docs: 更新API文档"

# 代码重构
git commit -m "refactor: 重构用户服务层"
```

### 数据库迁移

项目使用 JPA 自动建表，首次运行会自动创建所需表结构。

生产环境建议设置 `spring.jpa.hibernate.ddl-auto=validate`

### API测试

访问 http://localhost:8080/swagger-ui.html 进行API测试

## 部署指南

### Docker部署

```bash
# 构建镜像
docker build -t babycare-frontend ./frontend
docker build -t babycare-backend ./backend

# 运行容器
docker run -d -p 3000:3000 babycare-frontend
docker run -d -p 8080:8080 babycare-backend
```

### 生产环境配置

1. 修改生产环境配置文件
2. 配置 HTTPS 证书
3. 设置环境变量
4. 配置反向代理（Nginx）
5. 设置监控和日志

## 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/amazing-feature`)
3. 提交更改 (`git commit -m 'Add amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

## 许可证

本项目采用 MIT 许可证 - 详情请见 [LICENSE](LICENSE) 文件

## 联系我们

- **项目地址**：https://github.com/xiaoqianbaobao/babycare
- **问题反馈**：https://github.com/xiaoqianbaobao/babycare/issues
- **邮箱**：chenshengqian2020@gmail.com

## 更新日志

### v1.0.0 (2025-09-09)
- ✅ 项目初始化和基础架构搭建
- ✅ 用户认证系统（注册、登录、JWT）
- ✅ 数据库模型设计和实现
- ✅ 前端基础框架和路由配置
- ✅ API文档和开发环境配置

### 计划中功能
- ✅ 家庭管理功能
- ✅ 成长记录功能  
- ✅ AI育儿助手
- ✅ 教育规划功能
- ✅ 家庭协作功能

---

**感谢使用好芽儿托育保育平台！让我们一起为孩子的健康成长助力。** 🌟
