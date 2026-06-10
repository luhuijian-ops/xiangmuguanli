# IntelliJ IDEA 运行指南

## 导入项目

1. 打开 IntelliJ IDEA
2. 选择 `File` → `Open`
3. 导航到项目目录 `xiangmuguanli/backend`
4. 点击 `OK` 打开项目

## 安装依赖

在 IDEA 的 Terminal 中运行：

```bash
npm install
```

## 配置运行环境

### 方式一：使用 npm 脚本（推荐）

1. 在 IDEA 顶部工具栏，点击 `Add Configuration...`
2. 选择 `npm`
3. 配置如下：
   - **Name**: Dev Server
   - **Package**: backend/package.json
   - **Command**: dev
   - **Node interpreter**: 选择项目 Node.js
4. 点击 `OK` 保存

### 方式二：使用 Node.js 运行配置

1. 在 IDEA 顶部工具栏，点击 `Add Configuration...`
2. 选择 `Node.js`
3. 配置如下：
   - **Name**: Dev Server
   - **Node interpreter**: 选择项目 Node.js
   - **Node parameters**: 留空
   - **Working directory**: $ProjectFileDir$
   - **JavaScript file**: node_modules/nodemon/bin/nodemon.js
   - **Application parameters**: src/index.ts --exec ts-node
4. 点击 ``Environment variables``，添加：
   - `NODE_ENV = development`
5. 点击 `OK` 保存

## 初始化数据库

在 Terminal 中运行：

```bash
# 生成 Prisma Client
npm run prisma:generate

# 运行数据库迁移
npm run prisma:migrate
```

## 配置环境变量

1. 复制环境变量示例文件：
```bash
cp .env.example .env
```

2. 编辑 `.env` 文件，配置必要的环境变量：
```env
# 数据库配置
DATABASE_URL="mysql://user:password@localhost:3306/project_management"

# JWT 密钥（生产环境请使用强密码）
JWT_SECRET=your-jwt-secret-key

# 微信登录配置（可选）
WECHAT_APP_ID=your-wechat-app-id
WECHAT_APP_SECRET=your-wechat-app-secret

# 钉钉登录配置（可选）
DINGTALK_APP_KEY=your-dingtalk-app-key
DINGTALK_APP_SECRET=your-dingtalk-app-secret
```

## 启动开发服务器

1. 在顶部工具栏选择 `Dev Server` 运行配置
2. 点击绿色运行按钮 ▶️
3. 服务器将在 `http://localhost:3000` 启动

## 常见问题

### 问题：找不到 ts-node

```bash
npm install -D ts-node
```

### 问题：Prisma Client 未生成

```bash
npm run prisma:generate
```

### 问题：数据库连接失败

检查：
1. MySQL 是否运行
2. `.env` 中的 `DATABASE_URL` 是否正确
3. 数据库是否已创建

### 问题：端口被占用

在 `.env` 中修改端口：
```env
PORT=3001
```

## 调试模式

要启用调试模式，修改运行配置：

1. 打开运行配置编辑
2. 在 `Node parameters` 中添加：`--inspect`
3. 保存并重新运行
4. 在 IDEA 中设置断点即可调试

## 查看 API 文档

启动服务器后，访问：

- 健康检查：http://localhost:3000/health
- 认证接口：http://localhost:3000/api/v1/auth/wechat/login
- 项目接口：http://localhost:3000/api/v1/projects
- 其他接口详见 README.md

## 使用 IDEA 插件（可选）

推荐安装以下插件：

- **NodeCode Runner**: 快速运行 Node.js 代码
- **Rainbow Brackets**: 彩虹括号
- **HighlightBracketPair**: 高亮配对括号
- **One Dark Theme**: 深色主题
