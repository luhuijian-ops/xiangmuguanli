#!/bin/bash
set -e

echo "==================================="
echo "  项目管理系统启动脚本"
echo "==================================="

# ==========================================
# 1. 环境检查
# ==========================================

check_command() {
    if ! command -v "$1" &> /dev/null; then
        echo "[错误] $1 未安装或未配置环境变量"
        echo "$2"
        exit 1
    fi
}

check_command java "请安装 JDK 17+ 并配置 JAVA_HOME"
check_command mvn "请安装 Maven 3.8+ 并配置 PATH"
check_command node "请安装 Node.js 18+"
check_command npm "请安装 npm"

echo "[环境检查通过]"
echo "  Java: $(java --version 2>/dev/null | head -1)"
echo "  Maven: $(mvn --version 2>/dev/null | grep 'Apache Maven' | head -1)"
echo "  Node.js: $(node --version)"

# ==========================================
# 2. 端口冲突检测与处理
# ==========================================

echo ""
echo "[端口检查] 检查 5173 和 6200 端口..."

kill_port() {
    local port=$1
    local pid
    pid=$(lsof -ti:$port 2>/dev/null || netstat -tlnp 2>/dev/null | grep ":$port " | awk '{print $7}' | cut -d'/' -f1 | head -1)
    if [ -n "$pid" ] && [ "$pid" != "-" ]; then
        echo "[警告] 端口 $port 已被进程 PID=$pid 占用"
        echo "[信息] 正在关闭占用 $port 端口的进程..."
        kill -9 "$pid" 2>/dev/null || true
        sleep 2
    fi
}

kill_port 5173
kill_port 6200

echo "[端口检查] 5173 和 6200 端口已就绪"

# ==========================================
# 3. 启动后端 (Spring Boot)
# ==========================================

echo ""
echo "[后端] 编译并启动 Spring Boot 服务..."
cd "$(dirname "$0")/backend-spring"

if [ ! -f "pom.xml" ]; then
    echo "[错误] backend-spring/pom.xml 不存在"
    exit 1
fi

# 编译（如果 target 不存在）
if [ ! -d "target/classes" ]; then
    echo "[后端] 正在编译..."
    mvn clean compile -q
fi

# 设置默认 OAuth 回调地址（开发环境）
export WECHAT_REDIRECT_URI="http://localhost:5173/oauth/callback"
export DINGTALK_REDIRECT_URI="http://localhost:5173/oauth/callback"

# 在后台启动后端
echo "[后端] 启动服务中..."
nohup mvn spring-boot:run -q > ../backend-spring.log 2>&1 &
BACKEND_PID=$!
echo "[后端] PID=$BACKEND_PID"

# ==========================================
# 4. 等待后端启动
# ==========================================

echo ""
echo "[等待] 等待后端服务启动 (约 30 秒)..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt 15 ]; do
    sleep 3
    RETRY_COUNT=$((RETRY_COUNT + 1))

    # 测试后端端口
    if curl -s --max-time 2 http://localhost:6200/actuator/health >/dev/null 2>&1 || \
       curl -s --max-time 2 http://localhost:6200 >/dev/null 2>&1; then
        echo "[后端] 服务已启动 ✅"
        break
    fi

done

if [ $RETRY_COUNT -ge 15 ]; then
    echo "[警告] 后端服务启动超时，请检查日志: backend-spring.log"
fi

# ==========================================
# 5. 启动前端 (Vite)
# ==========================================

echo ""
echo "[前端] 安装依赖并启动 Vite 开发服务器..."
cd "$(dirname "$0")/frontend"

if [ ! -f "package.json" ]; then
    echo "[错误] frontend/package.json 不存在"
    exit 1
fi

# 安装依赖
if [ ! -d "node_modules" ]; then
    echo "[前端] 正在安装依赖..."
    npm install
fi

# 在后台启动前端
echo "[前端] 启动开发服务器..."
nohup npm run dev > ../frontend.log 2>&1 &
FRONTEND_PID=$!
echo "[前端] PID=$FRONTEND_PID"

# ==========================================
# 6. 等待前端启动
# ==========================================

echo ""
echo "[等待] 等待前端服务启动..."
RETRY_COUNT=0
while [ $RETRY_COUNT -lt 15 ]; do
    sleep 2
    RETRY_COUNT=$((RETRY_COUNT + 1))

    if curl -s --max-time 2 http://localhost:5173/ >/dev/null 2>&1; then
        echo "[前端] 服务已启动 ✅"
        break
    fi
done

if [ $RETRY_COUNT -ge 15 ]; then
    echo "[警告] 前端服务启动超时，请检查日志: frontend.log"
fi

# ==========================================
# 7. 完成提示
# ==========================================

echo ""
echo "==================================="
echo "   项目启动成功!"
echo "==================================="
echo ""
echo "[访问地址]"
echo "  前端页面: http://localhost:5173"
echo "  后端 API: http://localhost:6200"
echo ""
echo "[进程信息]"
echo "  后端 PID: $BACKEND_PID"
echo "  前端 PID: $FRONTEND_PID"
echo ""
echo "[日志文件]"
echo "  后端日志: backend-spring.log"
echo "  前端日志: frontend.log"
echo ""
echo "[停止服务]"
echo "  运行: kill $BACKEND_PID $FRONTEND_PID"
echo ""

# 尝试自动打开浏览器
if command -v xdg-open &> /dev/null; then
    xdg-open http://localhost:5173/ >/dev/null 2>&1 || true
elif command -v open &> /dev/null; then
    open http://localhost:5173/ >/dev/null 2>&1 || true
fi

# 捕获 Ctrl+C，关闭所有进程
cleanup() {
    echo ""
    echo "[停止] 正在关闭服务..."
    kill $BACKEND_PID $FRONTEND_PID 2>/dev/null || true
    echo "[完成] 服务已停止"
    exit 0
}
trap cleanup INT

echo "按 Ctrl+C 停止所有服务"
wait
