@echo off
chcp 65001 > nul
title 项目管理系统诊断工具

echo ===================================
echo    项目管理系统 - 网络诊断
echo ===================================
echo.

REM ==========================================
REM 1. 检查进程状态
REM ==========================================
echo [1/5] 检查服务进程...
echo.

echo   --- 前端服务 (端口 5173) ---
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":5173" ^| findstr "LISTENING"') do (
    for /f "tokens=1,*" %%b in ('tasklist ^| findstr "%%a"') do (
        echo     进程: %%b (PID=%%a) ✅
    )
)
if errorlevel 1 echo     未检测到监听进程 ❌

echo.
echo   --- 后端服务 (端口 6200) ---
for /f "tokens=5" %%a in ('netstat -ano ^| findstr ":6200" ^| findstr "LISTENING"') do (
    for /f "tokens=1,*" %%b in ('tasklist ^| findstr "%%a"') do (
        echo     进程: %%b (PID=%%a) ✅
    )
)
if errorlevel 1 echo     未检测到监听进程 ❌

REM ==========================================
REM 2. HTTP 连通性测试
REM ==========================================
echo.
echo [2/5] HTTP 连通性测试...
echo.

echo   --- 测试 localhost:5173 ---
curl -s -o nul -w "     HTTP 状态: %%{http_code}, 耗时: %%{time_total}s" --max-time 5 http://localhost:5173/
echo.

echo   --- 测试 127.0.0.1:5173 ---
curl -s -o nul -w "     HTTP 状态: %%{http_code}, 耗时: %%{time_total}s" --max-time 5 http://127.0.0.1:5173/
echo.

echo   --- 测试 localhost:6200 ---
curl -s -o nul -w "     HTTP 状态: %%{http_code}, 耗时: %%{time_total}s" --max-time 5 http://localhost:6200/
echo.

REM ==========================================
REM 3. 防火墙检查
REM ==========================================
echo.
echo [3/5] 防火墙状态...
echo.
netsh advfirewall show currentprofile | findstr "State" | head -1

REM ==========================================
REM 4. 网络接口检查
REM ==========================================
echo.
echo [4/5] 本机 IP 地址...
echo.
for /f "tokens=2 delims=:" %%a in ('ipconfig ^| findstr "IPv4"') do (
    echo     可用地址: http://%%a:5173
)

REM ==========================================
REM 5. 修复建议
REM ==========================================
echo.
echo [5/5] 修复建议...
echo.

echo   如果以上测试有失败的，请尝试以下步骤:
echo.
echo   [方案 A] 刷新浏览器
echo     1. 按 Ctrl+Shift+Delete 清除缓存
echo     2. 或按 Ctrl+F5 强制刷新
echo.
echo   [方案 B] 更换访问地址
echo     - 尝试: http://127.0.0.1:5173
echo     - 尝试: http://[上面显示的IP]:5173
echo.
echo   [方案 C] 关闭防火墙测试
echo     1. 打开"Windows 安全中心"
echo     2. 点击"防火墙和网络保护"
echo     3. 临时关闭"专用网络"防火墙
echo     4. 刷新浏览器测试
echo     5. 测试完后记得重新开启防火墙!
echo.
echo   [方案 D] 重启所有服务
echo     1. 关闭所有 Node.js 和 Java 进程
echo     2. 双击运行 start-project.bat
echo.

echo ===================================
pause
