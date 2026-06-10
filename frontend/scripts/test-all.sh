#!/bin/bash

# 项目管理系统测试执行脚本
# 按照顺序执行所有测试

echo "====================================="
echo "项目管理系统 - 测试执行脚本"
echo "====================================="
echo ""

# 设置颜色输出
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
RED='\033[0;31m'
NC='\033[0m'

# 检查环境
echo -e "${YELLOW}[1/7] 检查环境...${NC}"

if ! command -v node &> /dev/null; then
    echo -e "${RED}[✗] Node.js 未安装${NC}"
    exit 1
else
    echo -e "${GREEN}[✓] Node.js 已安装: $(node --version)${NC}"
fi

if ! command -v npm &> /dev/null; then
    echo -e "${RED}[✗] npm 未安装${NC}"
    exit 1
else
    echo -e "${GREEN}[✓] npm 已安装: $(npm --version)${NC}"
fi

echo ""
echo -e "${YELLOW}[2/7] 安装依赖...${NC}"
npm install

echo ""
echo -e "${YELLOW}[3/7] 运行单元测试...${NC}"
npm run test:unit

echo ""
echo -e "${YELLOW}[4/7] 运行 E2E 测试...${NC}"
npm run test:e2e

echo ""
echo -e "${YELLOW}[5/7] 生成测试覆盖率报告...${NC}"
npm run test:coverage

echo ""
echo -e "${YELLOW}[6/7] 运行集成测试...${NC}"
echo -e "${YELLOW}    注意: 集成测试需要后端服务运行中${NC}"
echo -e "${YELLOW}    如果后端未运行，E2E 测试会失败${NC}"
npm run test:integration

echo ""
echo -e "${GREEN}[7/7] 测试完成！${NC}"
echo ""
echo -e "测试报告位置："
echo -e "  - 覆盖率报告: coverage/index.html"
echo -e "  - E2E 测试报告: playwright-report/index.html"
echo ""
