@echo off
chcp 65001 >nul

REM 设置 JDK 17 环境变量
set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot
set PATH=%JAVA_HOME%\bin;%PATH%

REM 慧成长育儿平台 - Windows 开发环境启动脚本

echo 🍼 慧成长育儿平台 - 开发环境启动脚本 🍼
echo ========================================

REM 检查环境要求
echo 检查环境要求...

REM 检查 Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Node.js 未安装，请先安装 Node.js 18.0+
    pause
    exit /b 1
)
echo ✅ Node.js 版本检查通过
node --version

REM 检查 Java
java -version >nul 2>&1
if errorlevel 1 (
    echo ❌ Java 未安装，请先安装 Java 17+
    pause
    exit /b 1
)
echo ✅ Java 版本检查通过

REM 检查 Maven
mvn --version >nul 2>&1
if errorlevel 1 (
    echo ❌ Maven 未安装，请先安装 Maven 3.8+
    pause
    exit /b 1
)
echo ✅ Maven 版本检查通过

REM 创建日志目录
if not exist logs mkdir logs

REM 安装前端依赖
echo 安装前端依赖...
cd frontend
if not exist node_modules (
    echo 首次安装前端依赖，可能需要几分钟...
    npm install
) else (
    echo ✅ 前端依赖已安装
)
cd ..

REM 编译后端项目
echo 编译后端项目...
cd backend
echo 编译 Spring Boot 项目...
call mvn clean compile -DskipTests
if errorlevel 1 (
    echo ❌ 后端项目编译失败
    pause
    exit /b 1
)
echo ✅ 后端项目编译成功
cd ..

REM 启动后端服务
echo 启动后端服务...
cd backend
start "BabyCare Backend" cmd /c "mvn spring-boot:run -Dspring-boot.run.profiles=dev > ../logs/backend.log 2>&1"
cd ..

REM 等待后端服务启动
echo 等待后端服务启动...
timeout /t 15 /nobreak >nul

REM 启动前端服务
echo 启动前端服务...
cd frontend
start "BabyCare Frontend" cmd /c "npm run dev > ../logs/frontend.log 2>&1"
cd ..

REM 等待前端服务启动
echo 等待前端服务启动...
timeout /t 10 /nobreak >nul

REM 显示服务状态
echo.
echo ========================================
echo 🎉 慧成长育儿平台开发环境启动完成！
echo ========================================
echo.
echo 📱 前端应用: http://localhost:3001
echo 🔧 后端API: http://localhost:8080
echo 📖 API文档: http://localhost:8080/swagger-ui.html
echo 💾 数据库:  MySQL@localhost:3306
echo.
echo 📋 使用说明:
echo    • 访问前端应用开始使用
echo    • 使用 API 文档测试接口
echo    • 查看日志: logs/backend.log, logs/frontend.log
echo    • 停止服务请关闭对应的命令行窗口
echo.

REM 自动打开浏览器
echo 正在打开浏览器...
start http://localhost:3001

pause