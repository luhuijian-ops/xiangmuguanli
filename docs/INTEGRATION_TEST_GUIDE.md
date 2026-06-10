# 项目管理系统 - 前后端集成测试执行指南

## 📋 测试环境准备

### 前端环境

```bash
# 进入前端目录
cd frontend

# 安装测试依赖（如果还没有安装）
npm install -D vitest @vue/test-utils @playwright/test @playwright/test
```

### 后端环境

```bash
# 进入后端目录
cd backend

# 确保数据库和依赖
npm install

# 运行数据库迁移（如果需要）
npm run prisma:migrate
```

## 🧪 运行测试

### 前端测试

```bash
# 运行前端单元测试
npm run test:unit

# 运行前端 E2E 测试（需要端端服务运行）
npm run test:e2e

# 查看测试覆盖率
npm run test:coverage

# 运行所有测试
npm test
```

### 后端测试

```bash
# 运行所有后端测试
npm test

# 运行测试并生成覆盖率报告
npm run test:watch
```

## 📊 测试结果说明

### 前端测试结果

**测试报告位置：**
- 单元测试报告：`frontend/coverage/index.html`
- E2E 测试报告：`frontend/playwright-report/index.html`

**覆盖率目标：**
- 单元测试覆盖率：≥ 80%
- 关键文件覆盖率：100%

### 后端测试结果

**测试报告位置：**
- 测试报告：`backend/coverage/`

**测试覆盖：**
- 所有 API 接口测试
- 服务层业务逻辑测试
- 中间件功能测试

## 🔍 常见问题和解决方案

### 问题 1: 后端 Jest 不支持 ES6+ 语法

**错误信息：**
```
SyntaxError: Cannot use import statement outside a module
```

**原因：**
Jest 默认使用 Babel 转译器，但配置可能不支持 ES6+ 特性。

**解决方案：**

#### 方案 A：更新 Jest 配置

创建 `backend/jest.config.js`：
```javascript
module.exports = {
  testEnvironment: 'node',
  preset: 'ts-jest/presets/default',
  globals: {
    'ts-jest': {
      tsconfig: {
        esModuleInterop: true,
        allowSyntheticDefaultImports: true,
      },
    },
  },
  transform: {},
  moduleNameMapper: {},
}
```

#### 方案 B：添加 @swc/jest 依赖

```bash
cd backend
npm install -D @swc/jest
```

#### 方案 C：更新测试文件使用 CommonJS 语法

将测试文件中的 `async/await` 替换为 Promise 链式，确保与 Jest 兼容。

### 问题 2: 前端模板语法错误

**错误信息：**
```
RolldownError: Element is missing end tag.
```

**位置：**
- `frontend/src/pages/projects/ProjectDetail.vue:89`
- `frontend/src/pages/tasks/TaskDetail.vue:139`

**解决方案：**

#### 修复 ProjectDetail.vue

找到第 89 行附近，检查 `el-result` 组件的使用：

```vue
<!-- 正确的结构 -->
<el-result icon="error" title="项目不存在" sub-title="请检查项目 ID 是否正确">
  <template #extra>
    <el-button type="primary" @click="goToList">返回项目列表</el-button>
  </template>
</el-result>
```

#### 修复 TaskDetail.vue

检查第 139 行附件标签的闭合：

```vue
<!-- 确保标签正确闭合 -->
<el-tab-pane label="附件" name="attachments" lazy>
  <div class="attachments-section">
    <!-- 内容 -->
  </div>
</el-tab-pane>
```

## 📝 完整测试流程

### 阶段 1：准备环境

1. ✅ 确保前端依赖已安装
2. ✅ 确保后端依赖已安装
3. ✅ 数据库已配置
4. ✅ 测试文件已创建

### 阶段 2：修复语法错误

1. 🔧 修复后端 Jest 配置支持 ES6+
2. 🔧 修复前端模板语法错误
3. ✅ 验证构建成功

### 阶段 3：执行测试

1. 运行后端单元测试
   ```bash
   cd backend && npm test
   ```

2. 运行前端单元测试
   ```bash
   cd frontend && npm run test:unit
   ```

3. 查看测试覆盖率
   ```bash
   cd frontend && npm run test:coverage
   ```

4. 运行集成测试（可选）
   ```bash
   cd frontend && npm run test:e2e
   ```

## 🎯 测试成功标准

✅ **所有测试通过**
- 后端单元测试全部通过
- 前端单元测试全部通过
- E2E 集成测试全部通过（需要后端运行时）

✅ **测试覆盖率达标**
- 代码覆盖率 ≥ 80%
- 关键业务逻辑有测试覆盖

✅ **无构建错误**
- TypeScript 编译无错误
- 模板语法验证通过
- 组件渲染正常

✅ **集成测试通过**
- 前后端 API 通信正常
- 数据流转正确
- 状态同步正常

## 📚 测试检查清单

使用此清单验证测试完整性：

### 后端测试

- [ ] auth.service.test.ts - 认证成功
- [ ] auth.service.test.ts - 认证失败
- [ ] auth.routes.test.ts - 所有接口测试通过
- [ ] project.routes.test.ts - 项目 CRUD 测试
- [ ] task.routes.test.ts - 任务 CRUD 测试
- [ ] middleware/auth.middleware.test.ts - 权限验证
- [ ] 测试覆盖率报告生成

### 前端测试

- [ ] useAuth.test.ts - 登录/登出测试
- [ ] useProject.test.ts - 项目操作测试
- [ ] useTask.test.ts - 任务操作测试
- [ ] TaskCard.test.ts - 组件测试
- [ ] ProjectCard.test.ts - 组件测试
- [ ] Dashboard.test.ts - 页面测试
- [ ] 覆盖率 ≥ 80%
- [ ] 无控制台错误

### 集成测试

- [ ] auth-flow.spec.ts - 完整登录流程
- [ ] project-flow.spec.ts - 项目管理流程
- [ ] task-flow.spec.ts - 任务看板流程
- [ ] 所有页面导航正确
- [ ] 数据显示正确
- [ ] 表单提交成功
- [ ] 错误处理正确

## 🚨 问题处理

### 如果遇到问题

**后端 Jest 配置问题：**
1. 如果 Jest 仍然报错 ES6+ 语法错误
   - 使用方案 C：改用 CommonJS 语法编写测试
   - 或者安装 `@babel/preset-typescript` 预设

**前端构建错误：**
1. 如果仍然有模板语法错误
   - 检查具体行号和错误信息
   - 使用 Vue DevTools 查看组件渲染
   - 检查标签是否正确闭合

**测试失败：**
1. 检查失败原因
   - 确保后端服务正在运行
   - 检查 API 端点是否正确
   - 检查测试数据是否过期（如 token）
   - 查查数据库连接是否正常

## 📋 后续优化建议

1. **增加更多集成测试**
   - 用户设置更新流程测试
   - 权限管理测试
   - 文件上传下载测试
   - 评论和回复测试

2. **性能测试**
   - 使用 Lighthouse 进行前端性能测试
   - 使用 JMeter 进行后端负载测试

3. **安全测试**
   - XSS 攻击防护测试
   - CSRF 攻击防护测试
   - SQL 注入防护测试
   - 权限绕过测试

4. **持续集成**
   - 配置 CI/CD 自动运行测试
   - 测试失败自动通知
   - 测试报告自动归档

---

**最后更新时间：** 2026-04-25
**文档版本：** 1.0
