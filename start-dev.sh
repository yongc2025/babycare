#!/bin/bash

# 好芽儿育儿平台 - 开发环境启动脚本

echo "🍼 好芽儿育儿平台 - 开发环境启动脚本 🍼"
echo "========================================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查环境要求
check_requirements() {
    echo -e "${BLUE}检查环境要求...${NC}"
    
    # 检查 Node.js
    if ! command -v node &> /dev/null; then
        echo -e "${RED}❌ Node.js 未安装，请先安装 Node.js 18.0+${NC}"
        exit 1
    fi
    
    NODE_VERSION=$(node -v | cut -d'v' -f2 | cut -d'.' -f1)
    if [ $NODE_VERSION -lt 18 ]; then
        echo -e "${RED}❌ Node.js 版本过低，需要 18.0+，当前版本：$(node -v)${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Node.js 版本检查通过：$(node -v)${NC}"
    
    # 检查 Java
    if ! command -v java &> /dev/null; then
        echo -e "${RED}❌ Java 未安装，请先安装 Java 17+${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Java 版本检查通过：$(java -version 2>&1 | head -n 1)${NC}"
    
    # 检查 Maven
    if ! command -v mvn &> /dev/null; then
        echo -e "${RED}❌ Maven 未安装，请先安装 Maven 3.8+${NC}"
        exit 1
    fi
    echo -e "${GREEN}✅ Maven 版本检查通过：$(mvn -v | head -n 1)${NC}"
}

# 启动数据库服务
start_database() {
    echo -e "${BLUE}启动数据库服务...${NC}"
    
    # 检查 Docker 是否可用
    if command -v docker &> /dev/null; then
        echo -e "${YELLOW}使用 Docker 启动 MySQL...${NC}"
        
        # 启动 MySQL
        docker run -d --name babycare-mysql \
            -e MYSQL_ROOT_PASSWORD=123456 \
            -e MYSQL_DATABASE=huigrowth_dev \
            -p 3306:3306 \
            mysql:8.0
            
        echo -e "${GREEN}✅ 数据库服务启动成功${NC}"
    else
        echo -e "${YELLOW}⚠️  请确保 MySQL 服务已启动${NC}"
        echo -e "${YELLOW}   MySQL: localhost:3306 (数据库: huigrowth_dev)${NC}"
    fi
}

# 安装前端依赖
install_frontend_deps() {
    echo -e "${BLUE}安装前端依赖...${NC}"
    cd frontend
    
    if [ ! -d "node_modules" ]; then
        echo -e "${YELLOW}首次安装前端依赖，可能需要几分钟...${NC}"
        npm install
    else
        echo -e "${GREEN}✅ 前端依赖已安装${NC}"
    fi
    
    cd ..
}

# 编译后端项目
build_backend() {
    echo -e "${BLUE}编译后端项目...${NC}"
    cd backend
    
    echo -e "${YELLOW}编译 Spring Boot 项目...${NC}"
    mvn clean compile -DskipTests
    
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✅ 后端项目编译成功${NC}"
    else
        echo -e "${RED}❌ 后端项目编译失败${NC}"
        exit 1
    fi
    
    cd ..
}

# 启动后端服务
start_backend() {
    echo -e "${BLUE}启动后端服务...${NC}"
    cd backend
    
    # 在后台启动 Spring Boot
    nohup mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/backend.log 2>&1 &
    BACKEND_PID=$!
    
    echo -e "${YELLOW}等待后端服务启动...${NC}"
    sleep 10
    
    # 检查服务是否启动成功
    if curl -s http://localhost:8080/api/public/health > /dev/null; then
        echo -e "${GREEN}✅ 后端服务启动成功！${NC}"
        echo -e "${GREEN}   API地址: http://localhost:8080${NC}"
        echo -e "${GREEN}   API文档: http://localhost:8080/swagger-ui.html${NC}"
    else
        echo -e "${RED}❌ 后端服务启动失败，请检查日志：logs/backend.log${NC}"
        exit 1
    fi
    
    cd ..
    echo $BACKEND_PID > .backend.pid
}

# 启动前端服务
start_frontend() {
    echo -e "${BLUE}启动前端服务...${NC}"
    cd frontend
    
    # 在后台启动 Vite
    nohup npm run dev > ../logs/frontend.log 2>&1 &
    FRONTEND_PID=$!
    
    echo -e "${YELLOW}等待前端服务启动...${NC}"
    sleep 5
    
    echo -e "${GREEN}✅ 前端服务启动成功！${NC}"
    echo -e "${GREEN}   应用地址: http://localhost:3000${NC}"
    
    cd ..
    echo $FRONTEND_PID > .frontend.pid
}

# 显示服务状态
show_status() {
    echo ""
    echo -e "${GREEN}========================================${NC}"
    echo -e "${GREEN}🎉 好芽儿育儿平台开发环境启动完成！${NC}"
    echo -e "${GREEN}========================================${NC}"
    echo ""
    echo -e "${BLUE}📱 前端应用: ${NC}http://localhost:3000"
    echo -e "${BLUE}🔧 后端API: ${NC}http://localhost:8080"
    echo -e "${BLUE}📖 API文档: ${NC}http://localhost:8080/swagger-ui.html"
    echo -e "${BLUE}💾 数据库:  ${NC}MySQL@localhost:3306"
    echo ""
    echo -e "${YELLOW}📋 使用说明:${NC}"
    echo -e "   • 访问前端应用开始使用"
    echo -e "   • 使用 API 文档测试接口"
    echo -e "   • 查看日志: logs/backend.log, logs/frontend.log"
    echo -e "   • 停止服务: ./stop-dev.sh"
    echo ""
}

# 创建日志目录
mkdir -p logs

# 主流程
main() {
    check_requirements
    start_database
    install_frontend_deps
    build_backend
    start_backend
    start_frontend
    show_status
}

# 执行主流程
main