#!/bin/bash

echo "====================================="
echo "项目管理系统 - 完整测试运行脚本"
echo "====================================="
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
NC='\033[0m'

# 统计变量
TOTAL_TESTS=0
PASSED_TESTS=0
FAILED_TESTS=0
ERRORS=""

# 函数：添加错误
add_error() {
    ERRORS="$ERRORS
$1"
}

# 函数：添加通过的测试
add_pass() {
    PASSED_TESTS=$((PASSED_TESTS + 1))
}

# 函数：添加失败的测试
add_fail() {
    FAILED_TESTS=$((FAILED_TESTS + 1))
}

# 函数：添加总测试
add_total() {
    TOTAL_TESTS=$((TOTAL_TESTS + 1))
}

echo -e "${YELLOW}[1/7] 前端测试准备...${NC}"

# 检查环境
echo -e "检测 Node.js..."
if command -v node &> /dev/null; then
    echo -e "${GREEN}✓ Node.js 已安装: $(node --version)${NC}"
else
    echo -e "${RED}✗ Node.js 未安装${NC}"
    add_error "Node.js 未安装"
fi

echo ""
echo -e "检测 npm..."
if command -v npm &> /dev/null; then
    echo -e "${GREEN}✓ npm 已安装: $(npm --version)${NC}"
else
    echo -e "${RED}✗ npm 未安装${NC}"
    add_error "npm 未安装"
fi

echo ""
echo -e "${YELLOW}[2/7] 前端单元测试...${NC}"

cd frontend || { echo -e "${RED}✗ 前端目录不存在${NC}"; exit 1; }

echo ""
echo "运行 Vitest 单元测试..."
echo ""

# 运行测试并捕获结果
if npm run test:unit 2>&1; then
    echo -e "${GREEN}✓ 前端单元测试完成${NC}"

    # 解析测试结果
    TEST_OUTPUT=$(npm run test:unit 2>&1)

    # 计算通过/失败的测试
    PASSED=$(echo "$TEST_OUTPUT" | grep -o "PASS" | wc -l)
    FAILED=$(echo "$TEST" | grep -o "FAIL" | wc -l)
    echo "$TEST_OUTPUT" | grep -o "FAIL" | wc -l)

    # 添加到统计
    PASSED_TESTS=$((PASSED_TESTS + PASSED))
    FAILED_TESTS=$((FAILED_TESTS + FAILED))
    TOTAL_TESTS=$((TOTAL_TESTS + PASSED + FAILED))

else
    echo -e "${RED}✗ 前端单元测试失败${NC}"
    add_error "前端单元测试失败"
fi

echo ""
echo -e "${YELLOW}[3/7] 检查前端代码质量...${NC}"

# 检查是否有编译错误
echo "检查 TypeScript 编译错误..."
if npx vue-tsc --noEmit 2>&1 | grep -E "error|Error" > /dev/null; then
    echo -e "${GREEN}✓ 无 TypeScript 编译错误${NC}"
else
    echo -e "${RED}✗ 存在 TypeScript 编译错误${NC}"
    add_error "存在 TypeScript 编译错误"
fi

echo ""
echo -e "${YELLOW}[4/7] 检查后端测试环境...${NC}"

cd backend || { echo -e "${RED}✗ 后端目录不存在${NC}"; exit 1; }

echo ""
echo "检查后端依赖..."
if [ -f "backend/package.json" ]; then
    echo -e "${GREEN}✓ 后端 package.json 存在${NC}"
else
    echo -e "${RED}✗ 后端 package.json 不存在${NC}"
    add_error "后端 package.json 不存在"
fi

echo ""
echo -e "检查测试依赖..."
if npm list backend | grep -E "jest|supertest" > /dev/null; then
    echo -e "${GREEN}✓ Jest/Supertest 已安装${NC}"
else
    echo -e "${RED}✗ Jest/Supertest 未安装${NC}"
    add_error "Jest/Supertest 未安装"
fi

echo ""
echo -e "${YELLOW}[5/7] 后端单元测试...${NC}"

echo "运行 Jest 后端单元测试..."
echo ""

# 运行后端测试
cd backend

# 运行 Jest 测试
if npm test 2>&1; then
    echo -e "${GREEN}✓ 后端单元测试完成${NC}"

    # 解析测试结果
    TEST_OUTPUT=$(npm test 2>&1)

    # 检查是否有测试错误
    if echo "$TEST_OUTPUT" | grep -E "PASS" > /dev/null; then
        PASSED=$(echo "$TEST_OUTPUT" | grep -o "PASS" | wc -l)
        echo "$TEST_OUTPUT" | grep -o "PASS" | wc -l)

        PASSED_TESTS=$((PASSED_TESTS + PASSED))
        TOTAL_TESTS=$((TOTAL_TESTS + PASSED))
    else
        FAILED_TESTS=$((FAILED_TESTS + 1))
        TOTAL_TESTS=$((TOTAL_TESTS + 1))
    fi
else
    echo -e "${RED}✗ 后端单元测试失败${NC}"
    add_error "后端单元测试失败"
fi

echo ""
echo -e "${YELLOW}[6/7] 生成测试报告...${NC}"

# 生成前端覆盖率报告
cd frontend
if npm run test:coverage 2>&1; then
    echo -e "${GREEN}✓ 前端覆盖率报告已生成${NC}"
    echo -e "报告位置: frontend/coverage/index.html"
else
    echo -e "${YELLOW}⚧ 前端覆盖率报告生成跳过（需要先修复测试）${NC}"
fi

echo ""
echo -e "${YELLOW}[7/7] 检查构建状态...${NC}"

# 前端构建检查
cd frontend
if npx vite build 2>&1 | grep -E "error|Error" > /dev/null; then
    echo -e "${RED}✗ 前端构建存在错误${NC}"
    add_error "前端构建失败"
else
    echo -e "${GREEN}✓ 前端构建成功${NC}"
fi

echo ""
echo "====================================="
echo "测试总结"
echo "====================================="
echo ""
echo -e "${YELLOW}测试统计：${NC}"
echo -e "  总测试数: $TOTAL_TESTS${NC}"
echo -e " 通过测试: $PASSED_TESTS${NC}"
echo -e " 失败测试: $FAILED_TESTS${NC}"
echo -e " 错误数: $ERRORS${NC}"
echo ""

if [ $FAILED_TESTS -eq 0 ] && [ $ERRORS -eq 0 ]; then
    echo -e "${GREEN}✅ 所有测试通过！${NC}"
    echo -e "${GREEN}✅ 系统已准备好进行完整业务测试${NC}"
    exit 0
elif [ $FAILED_TESTS -gt 0 ]; then
    echo -e "${RED}✗ 存在失败的测试${NC}"
    echo -e "${YELLOW}⚧ 请查看上面的错误信息${NC}"
    echo -e "${YELLOW}⚧ 修复后重新运行测试脚本${NC}"
    exit 1
else
    echo -e "${YELLOW}⚧ 存在未知错误${NC}"
    echo -e "${YELLOW}⚧ 请手动运行测试查看详细错误${NC}"
    exit 1
fi
