@echo off
chcp 65001 >nul
setlocal

set "ROOT_DIR=%~dp0"
cd /d "%ROOT_DIR%"

set "JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-17.0.19.10-hotspot"
set "MAVEN_HOME=C:\tools\apache-maven-3.9.16"
set "MAVEN_OPTS=-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8"

if exist "%JAVA_HOME%\bin\java.exe" (
    set "PATH=%JAVA_HOME%\bin;%PATH%"
)

if exist "%MAVEN_HOME%\bin\mvn.cmd" (
    set "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"
    set "PATH=%MAVEN_HOME%\bin;%PATH%"
) else (
    set "MVN_CMD=mvn"
)

echo BabyCare dev environment startup
echo ========================================
echo Checking requirements...

node --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Node.js not found. Please install Node.js 18+.
    pause
    exit /b 1
)
echo OK: Node.js
node --version

java -version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Java not found. Please check JAVA_HOME.
    echo JAVA_HOME=%JAVA_HOME%
    pause
    exit /b 1
)
echo OK: Java

call "%MVN_CMD%" --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Maven not found. Please check MAVEN_HOME or PATH.
    echo MAVEN_HOME=%MAVEN_HOME%
    echo MVN_CMD=%MVN_CMD%
    pause
    exit /b 1
)
echo OK: Maven
call "%MVN_CMD%" --version

if "%~1"=="--check" (
    echo Environment check passed.
    exit /b 0
)

if not exist logs mkdir logs

set "BACKEND_RUNNER=%ROOT_DIR%logs\run-backend.bat"
set "FRONTEND_RUNNER=%ROOT_DIR%logs\run-frontend.bat"

call :write_runners
if "%~1"=="--make-runners" (
    echo Runner scripts generated.
    echo %BACKEND_RUNNER%
    echo %FRONTEND_RUNNER%
    exit /b 0
)

echo Installing frontend dependencies...
cd /d "%ROOT_DIR%frontend"
if not exist node_modules (
    echo Installing npm dependencies. This may take a few minutes...
    call npm install
    if errorlevel 1 (
        echo ERROR: npm install failed.
        pause
        exit /b 1
    )
) else (
    echo OK: frontend dependencies already installed.
)

echo Compiling backend...
cd /d "%ROOT_DIR%backend"
call "%MVN_CMD%" clean compile -DskipTests
if errorlevel 1 (
    echo ERROR: backend compile failed.
    pause
    exit /b 1
)
echo OK: backend compile succeeded.

call :is_port_busy 8080
if errorlevel 1 (
    echo Backend port 8080 is already in use. Skip starting another backend.
) else (
    echo Starting backend service...
    start "BabyCare Backend" cmd /k call "%BACKEND_RUNNER%"
)

echo Waiting for backend startup...
timeout /t 15 /nobreak >nul

call :is_port_busy 3001
if errorlevel 1 (
    echo Frontend port 3001 is already in use. Skip starting another frontend.
) else (
    echo Starting frontend service...
    start "BabyCare Frontend" cmd /k call "%FRONTEND_RUNNER%"
)

echo Waiting for frontend startup...
timeout /t 10 /nobreak >nul

echo.
echo ========================================
echo BabyCare dev environment started.
echo ========================================
echo Frontend: http://localhost:3001
echo Backend API: http://localhost:8080
echo API Docs: http://localhost:8080/swagger-ui.html
echo Logs: logs\backend.log, logs\frontend.log
echo Close the opened command windows to stop services.
echo.

start http://localhost:3001

pause
exit /b 0

:write_runners
(
    echo @echo off
    echo chcp 65001 ^>nul
    echo set "JAVA_HOME=%JAVA_HOME%"
    echo set "MAVEN_OPTS=%MAVEN_OPTS%"
    echo set "PATH=%JAVA_HOME%\bin;%MAVEN_HOME%\bin;%%PATH%%"
    echo cd /d "%ROOT_DIR%backend"
    echo echo Starting BabyCare Backend...
    echo echo Backend log: "%ROOT_DIR%logs\backend.log"
    echo call "%MVN_CMD%" spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.jvmArguments="-Dfile.encoding=UTF-8 -Dsun.stdout.encoding=UTF-8 -Dsun.stderr.encoding=UTF-8" ^> "%ROOT_DIR%logs\backend.log" 2^>^&1
    echo echo.
    echo echo Backend process exited. Check "%ROOT_DIR%logs\backend.log".
    echo pause
) > "%BACKEND_RUNNER%"

(
    echo @echo off
    echo chcp 65001 ^>nul
    echo cd /d "%ROOT_DIR%frontend"
    echo echo Starting BabyCare Frontend...
    echo echo Frontend log: "%ROOT_DIR%logs\frontend.log"
    echo call npm run dev ^> "%ROOT_DIR%logs\frontend.log" 2^>^&1
    echo echo.
    echo echo Frontend process exited. Check "%ROOT_DIR%logs\frontend.log".
    echo pause
) > "%FRONTEND_RUNNER%"
exit /b 0

:is_port_busy
netstat -ano | findstr /R /C:":%~1 .*LISTENING" >nul 2>&1
if errorlevel 1 exit /b 0
exit /b 1
