@echo off
chcp 65001 > nul
title 项目管理系统启动脚本

echo ===================================
echo 项目管理系统启动脚本
echo ===================================

REM ==========================================
REM 1. 环境检查
REM ==========================================

REM 检查 Java
java --version >nul 2>&1
if errorlevel 1 (
    echo [错误] Java 未安装或未配置环境变量
    echo 请安装 JDK 17+ 并配置 JAVA_HOME
    pause
    exit /b 1
)

REM 检查 Maven
mvn --version >nul 2>&1
if errorlevel 1 (
    echo [错误] Maven 未安装或未配置环境变量
    echo 请安装 Maven 3.8+ 并配置 PATH
    pause
    exit /b 1
)

REM 检查 Node.js
node --version >nul 2>&1
if errorlevel 1 (
    echo [错误] Node.js 未安装
    echo 请安装 Node.js 18+
    pause
    exit /b 1
)

for /f "tokens=*" %%i in ('java --version 2^>^&1 ^| head -1') do set JAVA_VERSION=%%i
for /f "tokens=*" %%i in ('mvn --version 2^>^&1 ^| grep "Apache Maven"') do set MAVEN_VERSION=%%i
for /f "tokens=*" %%i in ('node --version') do set NODE_VERSION=%%i

echo [环境检查通过]
echo   Java: %JAVA_VERSION%
echo   Maven: %MAVEN_VERSION%
echo   Node.js: %NODE_VERSION%

REM ==========================================
REM 2. 端口冲突检测与处理
REM ==========================================

echo.
echo [端口检查] 检查 5173 和 6200 端口...

REM 检查 5173 端口
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    echo [警告] 端口 5173 已被进程 PID=%%a 占用
    echo [信息] 正在关闭占用 5173 端口的进程...
    taskkill /f /pid %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
)

REM 检查 6200 端口
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":6200" ^| findstr "LISTENING"') do (
    echo [警告] 端口 6200 已被进程 PID=%%a 占用
    echo [信息] 正在关闭占用 6200 端口的进程...
    taskkill /f /pid %%a >nul 2>&1
    timeout /t 2 /nobreak >nul
)

echo [端口检查] 5173 和 6200 端口已就绪

REM ==========================================
REM 3. 启动后端 (Spring Boot)
REM ==========================================

echo.
echo [后端] 编译并启动 Spring Boot 服务...
cd /d "%~dp0backend-spring"

if not exist "pom.xml" (
    echo [错误] backend-spring\pom.xml 不存在
    pause
    exit /b 1
)

REM 编译（如果 target 不存在或源码有更新）
if not exist "target\classes" (
    echo [后端] 正在编译...
    call mvn clean compile -q
    if errorlevel 1 (
        echo [错误] 后端编译失败
        pause
        exit /b 1
    )
)

REM 设置默认 OAuth 回调地址（开发环境）
set WECHAT_REDIRECT_URI=http://localhost:5173/oauth/callback
set DINGTALK_REDIRECT_URI=http://localhost:5173/oauth/callback

REM 在新窗口启动后端
echo [后端] 启动服务中...
start "后端服务 (Spring Boot)" cmd /k "mvn spring-boot:run"

REM ==========================================
REM 4. 等待后端启动
REM ==========================================

echo.
echo [等待] 等待后端服务启动 (约 30 秒)...
set RETRY_COUNT=0
:CHECK_BACKEND
set /a RETRY_COUNT+=1
timeout /t 3 /nobreak >nul

REM 测试后端健康检查
curl -s --max-time 2 http://localhost:6200/actuator/health >nul 2>&1
if not errorlevel 1 (
    echo [后端] 服务已启动 ✅
    goto BACKEND_READY
)

REM 备选：检查端口是否在监听
netstat -ano | findstr ":6200" | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [后端] 服务已启动 (端口已监听) ✅
    goto BACKEND_READY
)

if %RETRY_COUNT% lss 15 goto CHECK_BACKEND

echo [警告] 后端服务启动超时，可能仍在启动中，请稍后手动检查
echo          http://localhost:6200

:BACKEND_READY

REM ==========================================
REM 5. 启动前端 (Vite)
REM ==========================================

echo.
echo [前端] 安装依赖并启动 Vite 开发服务器...
cd /d "%~dp0frontend"

if not exist "package.json" (
    echo [错误] frontend\package.json 不存在
    pause
    exit /b 1
)

REM 安装依赖（如果 node_modules 不存在）
if not exist "node_modules" (
    echo [前端] 正在安装依赖...
    call npm install
    if errorlevel 1 (
        echo [错误] 前端依赖安装失败
        pause
        exit /b 1
    )
)

REM 在新窗口启动前端
echo [前端] 启动开发服务器...
start "前端服务 (Vite)" cmd /k "npm run dev"

REM ==========================================
REM 6. 等待前端启动
REM ==========================================

echo.
echo [等待] 等待前端服务启动...
set RETRY_COUNT=0
:CHECK_FRONTEND
set /a RETRY_COUNT+=1
timeout /t 2 /nobreak >nul

curl -s --max-time 2 http://localhost:5173/ >nul 2>&1
if not errorlevel 1 (
    echo [前端] 服务已启动 ✅
    goto FRONTEND_READY
)

netstat -ano | findstr ":5173" | findstr "LISTENING" >nul
if not errorlevel 1 (
    echo [前端] 服务已启动 (端口已监听) ✅
    goto FRONTEND_READY
)

if %RETRY_COUNT% lss 15 goto CHECK_FRONTEND

echo [警告] 前端服务启动超时，可能仍在启动中

:FRONTEND_READY

REM ==========================================
REM 7. 完成提示
REM ==========================================

echo.
echo ===================================
echo    项目启动成功!
echo ===================================
echo.
echo [访问地址]
echo   前端页面: http://localhost:5173
echo   后端 API: http://localhost:6200
echo.
echo [注意事项]
echo   1. 首次启动 Windows 可能会弹出防火墙提示，请点击"允许访问"
echo   2. 如果浏览器显示"无法访问"，请尝试:
echo      - 刷新页面 (Ctrl+F5)
echo      - 使用 http://127.0.0.1:5173 访问
echo      - 暂时关闭 Windows Defender 防火墙测试
echo   3. 关闭本窗口不会停止服务，请手动关闭后端/前端窗口
echo.

REM 尝试自动打开浏览器
start http://localhost:5173/

echo 按任意键关闭本窗口 (服务仍在运行)...
pause >nul
