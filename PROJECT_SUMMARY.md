# 项目管理系统 - 项目总结

## 项目概述

这是一个基于钉钉 Teambition 功能参考的项目管理系统，支持微信和钉钉登录。项目采用前后端分离架构，包含完整的项目管理、任务管理、日程管理、文件管理、评论和统计分析功能。

## 技术栈

### 后端技术栈
- **Node.js 20** - 运行时环境
- **TypeScript** - 类型安全编程语言
- **Express.js** - Web 框架
- **Prisma ORM** - 数据库访问层
- **MySQL 8.0** - 数据库
- **JWT 认证** - 用户身份验证
- **OAuth 2.0** - 微信和钉钉第三方登录

### 前端技术栈
- **Vue 3** - 前端框架
- **Element Plus** - UI 组件库
- **Pinia** - 状态管理
- **Vite** - 构建工具
- **TypeScript** - 类型安全
- **Vue Router 4** - 路由管理
- **Axios** - HTTP 客户端
- **ECharts** - 图表库
- **Day.js** - 日期处理

## 项目结构

```
xiangmuguanli/
├── backend/                 # 后端项目
│   ├── src/
│   │   ├── config/         # 配置文件
│   │   ├── controllers/    # 控制器
│   │   ├── middleware/     # 中间件
│   │   ├── models/         # 数据模型 (Prisma Schema)
│   │   ├── routes/         # 路由定义
│   │   ├── services/       # 业务逻辑层
│   │   └── types/          # 类型定义
│   ├── package.json
│   └── tsconfig.json
├── frontend/                # 前端项目
│   ├── src/
│   │   ├── api/            # API 接口定义
│   │   ├── assets/         # 静态资源
│   │   ├── components/     # 组件库
│   │   ├── composables/    # 组合式函数
│   │   ├── layouts/        # 布局组件
│   │   ├── pages/          # 页面组件
│   │   ├── router/         # 路由配置
│   │   ├── stores/         # Pinia 状态管理
│   │   ├── types/          # 类型定义
│   │   └── utils/          # 工具函数
│   ├── package.json
│   └── vite.config.ts
├── docs/                   # 文档
├── docker-compose.yml      # Docker 编排配置
└── README.md
```

## 功能模块

### 1. 认证模块
- 微信扫码登录
- 钉钉扫码登录
- JWT Token 管理
- 自动 Token 刷新
- 用户信息同步

### 2. 项目管理模块
- 项目 CRUD 操作
- 项目成员管理
- 权限控制 (RBAC)
- 项目状态管理
- 项目看板视图

### 3. 任务管理模块
- 任务 CRUD 操作
- 任务状态流转 (TODO → DOING → DONE → ARCHIVED)
- 任务看板视图
- 任务分配
- 优先级管理
- 任务拖拽排序

### 4. 文件管理模块
- 文件上传 (支持 50MB)
- 文件下载
- 文件删除
- 项目文件列表

### 5. 评论模块
- 评论发布
- 嵌套回复
- @提及功能
- 评论删除

### 6. 统计模块
- 项目统计 (任务完成率、成员数、活动记录)
- 用户统计 (任务统计、工时统计)
- 最近活动记录

### 7. 日程管理模块
- 日程 CRUD
- 会议、提醒、截止日期类型
- 参与人员管理
- 即将到来的日程查询
- 日历视图

## API 接口

### 认证接口
- `GET /api/v1/auth/wechat/auth-url` - 获取微信登录URL
- `GET /api/v1/auth/wechat/callback` - 微信登录回调
- `GET /api/v1/auth/dingtalk/auth-url` - 获取钉钉登录URL
- `GET /api/v1/auth/dingtalk/callback` - 钉钉登录回调
- `POST /api/v1/auth/refresh` - 刷新 Token
- `POST /api/v1/auth/logout` - 登出
- `GET /api/v1/auth/me` - 获取当前用户信息

### 项目接口
- `POST /api/v1/projects` - 创建项目
- `GET /api/v1/projects` - 获取项目列表
- `GET /api/v1/projects/:id` - 获取项目详情
- `PUT /api/v1/projects/:id` - 更新项目信息
- `DELETE /api/v1/projects/:id` - 删除项目
- `POST /api/v1/projects/:id/members` - 添加成员
- `DELETE /api/v1/projects/:id/members/:userId` - 移除成员

### 任务接口
- `POST /api/v1/tasks` - 创建任务
- `GET /api/v1/tasks/project/:projectId` - 获取项目任务列表
- `GET /api/v1/tasks/board/:projectId` - 获取看板视图
- `GET /api/v1/tasks/:id` - 获取任务详情
- `PUT /api/v1/tasks/:id` - 更新任务
- `PATCH /api/v1/tasks/:id/status` - 更新任务状态
- `DELETE /api/v1/tasks/:id` - 删除任务

### 文件接口
- `POST /api/v1/files/upload` - 上传文件
- `GET /api/v1/files/:id` - 获取文件
- `GET /api/v1/files/project/:projectId` - 获取项目文件列表

### 评论接口
- `POST /api/v1/comments` - 创建评论
- `GET /api/v1/comments/:entityType/:entityId` - 获取评论

### 统计接口
- `GET /api/v1/statistics/project/:projectId` - 获取项目统计
- `GET /api/v1/statistics/user/:userId` - 获取用户统计

### 日程接口
- `POST /api/v1/schedule` - 创建日程
- `GET /api/v1/schedule` - 获取日程列表
- `GET /api/v1/schedule/upcoming` - 获取即将到来的日程

## 数据库模型

系统使用 Prisma ORM 定义了 13 个主要数据模型：

- **User** - 用户信息
- **UserBinding** - OAuth 绑定信息
- **Project** - 项目信息
- **ProjectMember** - 项目成员关系
- **Task** - 任务信息
- **Milestone** - 里程碑
- **File** - 文件信息
- **Comment** - 评论
- **Activity** - 活动记录
- **WorkHour** - 工时记录
- **Event** - 日程事件
- **Role** - 角色定义
- **UserRole** - 用户角色关系
- **LoginLog** - 登录日志

## 部署配置

### 环境变量

#### 后端 (.env)
- `PORT` - 服务端口
- `DATABASE_URL` - 数据库连接字符串
- `JWT_SECRET` - JWT 密钥
- `WECHAT_APP_ID/SECRET` - 微信应用配置
- `DINGTALK_APP_ID/SECRET` - 钉钉应用配置
- `CORS_ORIGIN` - CORS 允许域名

#### 前端 (.env)
- `VITE_API_BASE_URL` - 后端 API 地址

## 开发流程

1. **安装依赖**
   ```bash
   cd backend && npm install
   cd frontend && npm install
   ```

2. **配置数据库**
   ```bash
   # 创建数据库并配置 .env
   npm run prisma:generate
   npm run prisma:migrate
   ```

3. **启动服务**
   ```bash
   # 后端
   npm run dev
   # 前端
   npm run dev
   ```

4. **生产构建**
   ```bash
   npm run build
   ```

## 安全特性

- JWT Token 认证
- OAuth 2.0 第三方登录
- 数据库访问权限控制
- 输入验证和清理
- 安全头部配置 (Helmet)
- 速率限制
- CORS 配置

## 特殊功能

- **扫码登录**：支持微信和钉钉扫码登录
- **任务看板**：拖拽式任务管理界面
- **实时协作**：多用户实时协作
- **文件管理**：支持大文件上传
- **日程管理**：集成日历功能
- **数据统计**：可视化数据展示
- **权限控制**：细粒度权限管理