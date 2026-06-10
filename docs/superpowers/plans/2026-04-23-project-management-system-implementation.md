# 项目管理系统实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**目标**: 构建一个支持微信/钉钉登录的轻量级项目管理系统，包含任务管理、项目规划、团队协作、统计报表和日程管理功能

**架构**: 低代码平台 + MySQL后端 + OAuth2.0认证，采用模块化设计，前后端分离，RESTful API接口

**技术栈**: Node.js/Express + MySQL + Prisma ORM + React + 微信/钉钉OAuth2.0

---

## 项目文件结构

```
xiangmuguanli/
├── backend/                          # 后端服务
│   ├── src/
│   │   ├── config/                   # 配置文件
│   │   │   ├── database.ts
│   │   │   ├── jwt.ts
│   │   │   ├── oauth.ts              # 微信/钉钉配置
│   │   │   └── index.ts
│   │   ├── controllers/              # 控制器
│   │   │   ├── auth.controller.ts
│   │   │   ├── project.controller.ts
│   │   │   ├── task.controller.ts
│   │   │   ├── file.controller.ts
│   │   │   ├── comment.controller.ts
│   │   │   ├── stats.controller.ts
│   │   │   └── event.controller.ts
│   │   ├── services/                 # 业务逻辑
│   │   │   ├── auth.service.ts       # 认证服务（微信/钉钉）
│   │   │   ├── project.service.ts
│   │   │   ├── task.service.ts
│   │   │   ├── file.service.ts
│   │   │   ├── comment.service.ts
│   │   │   ├── stats.service.ts
│   │   │   └── event.service.ts
│   │   ├── middleware/               # 中间件
│   │   │   ├── auth.middleware.ts   # JWT认证
│   │   │   ├── permission.middleware.ts
│   │   │   ├── error.middleware.ts
│   │   │   └── logger.middleware.ts
│   │   ├── routes/                   # 路由
│   │   │   ├── auth.routes.ts
│   │   │   ├── project.routes.ts
│   │   │   ├── task.routes.ts
│   │   │   ├── file.routes.ts
│   │   │   ├── comment.routes.ts
│   │   │   ├── stats.routes.ts
│   │   │   ├── event.routes.ts
│   │   │   └── index.ts
│   │   ├── models/                   # Prisma模型
│   │   │   └── schema.prisma
│   │   ├── utils/                    # 工具函数
│   │   │   ├── crypto.utils.ts
│   │   │   ├── date.utils.ts
│   │   │   └── response.utils.ts
│   │   ├── types/                    # TypeScript类型
│   │   │   └── index.ts
│   │   └── server.ts                 # 服务器入口
│   ├── tests/                        # 测试文件
│   │   ├── unit/                     # 单元测试
│   │   │   ├── auth.service.test.ts
│   │   │   ├── project.service.test.ts
│   │   │   └── task.service.test.ts
│   │   └── integration/             # 集成测试
│   │       └── api.test.ts
│   ├── package.json
│   ├── tsconfig.json
│   └── .env.example
├── frontend/                         # 前端应用
│   ├── src/
│   │   ├── components/               # 组件
│   │   │   ├── common/               # 通用组件
│   │   │   │   ├── Button.tsx
│   │   │   │   ├── Modal.tsx
│   │   │   │   └── Table.tsx
│   │   │   ├── auth/                 # 认证组件
│   │   │   │   ├── Login.tsx
│   │   │   │   └── OAuthButton.tsx
│   │   │   ├── project/              # 项目组件
│   │   │   │   ├── ProjectList.tsx
│   │   │   │   ├── ProjectCard.tsx
│   │   │   │   ├── GanttChart.tsx
│   │   │   │   └── MilestoneList.tsx
│   │   │   ├── task/                 # 任务组件
│   │   │   │   ├── TaskBoard.tsx
│   │   │   │   ├── TaskList.tsx
│   │   │   │   ├── TaskCard.tsx
│   │   │   │   └── TaskForm.tsx
│   │   │   ├── collaboration/        # 协作组件
│   │   │   │   ├── CommentList.tsx
│   │   │   │   ├── FileUpload.tsx
│   │   │   │   └── ActivityFeed.tsx
│   │   │   ├── stats/                # 统计组件
│   │   │   │   ├── ProjectStats.tsx
│   │   │   │   ├── MemberStats.tsx
│   │   │   │   └── ChartWrapper.tsx
│   │   │   └── schedule/             # 日程组件
│   │   │       ├── Calendar.tsx
│   │   │       ├── EventList.tsx
│   │   │       └── EventForm.tsx
│   │   ├── pages/                    # 页面
│   │   │   ├── Login.tsx
│   │   │   ├── Dashboard.tsx
│   │   │   ├── ProjectDetail.tsx
│   │   │   └── TaskDetail.tsx
│   │   ├── hooks/                    # React Hooks
│   │   │   ├── useAuth.ts
│   │   │   ├── useProjects.ts
│   │   │   ├── useTasks.ts
│   │   │   └── useEvents.ts
│   │   ├── services/                 # API服务
│   │   │   ├── api.ts
│   │   │   ├── auth.service.ts
│   │   │   ├── project.service.ts
│   │   │   ├── task.service.ts
│   │   │   └── event.service.ts
│   │   ├── store/                    # 状态管理
│   │   │   ├── index.ts
│   │   │   └── modules/
│   │   │       ├── auth.ts
│   │   │       ├── project.ts
│   │   │       └── task.ts
│   │   ├── types/                    # TypeScript类型
│   │   │   └── index.ts
│   │   ├── utils/                    # 工具函数
│   │   │   ├── date.utils.ts
│   │   │   └── validation.utils.ts
│   │   └── main.tsx                  # 应用入口
│   ├── tests/                        # 测试文件
│   │   ├── components/
│   │   │   └── auth/
│   │   │       └── Login.test.tsx
│   │   └── hooks/
│   │       └── useAuth.test.ts
│   ├── package.json
│   ├── tsconfig.json
│   └── vite.config.ts
├── docs/                             # 文档
│   ├── api/                          # API文档
│   ├── database/                     # 数据库文档
│   └── deployment/                    # 部署文档
├── docker/                           # Docker配置
│   ├── Dockerfile
│   ├── docker-compose.yml
│   └── mysql/
│       └── init.sql
├── .gitignore
└── README.md
```

---

## 第一阶段：项目初始化与数据库设计（2周）

---

### Task 1: 初始化Git仓库

**Files:**
- Create: `.gitignore`

- [ ] **Step 1: 创建 .gitignore 文件**

```bash
# Node.js
node_modules/
npm-debug.log
yarn-error.log
.DS_Store

# Environment
.env
.env.local
.env.*.local

# Build
dist/
build/
*.tsbuildinfo

# IDE
.vscode/
.idea/

# Logs
logs/
*.log
```

- [ ] **Step 2: 初始化Git仓库**

Run: `git init`
Expected: Initialized empty Git repository

- [ ] **Step 3: 提交初始提交**

Run: `git add .gitignore && git commit -m "chore: initialize git repository with gitignore"`
Expected: Commit created

---

### Task 2: 初始化后端项目

**Files:**
- Create: `backend/package.json`
- Create: `backend/tsconfig.json`
- Create: `backend/.env.example`

- [ ] **Step 1: 创建后端 package.json**

```json
{
  "name": "project-management-backend",
  "version": "1.0.0",
  "description": "Project Management System Backend",
  "main": "dist/server.js",
  "scripts": {
    "dev": "nodemon src/server.ts",
    "build": "tsc",
    "start": "node dist/server.js",
    "test": "jest",
    "test:watch": "jest --watch",
    "prisma:generate": "prisma generate",
    "prisma:migrate": "prisma migrate dev",
    "prisma:studio": "prisma studio"
  },
  "dependencies": {
    "express": "^4.18.2",
    "cors": "^2.8.5",
    "dotenv": "^16.3.1",
    "jsonwebtoken": "^9.0.2",
    "bcrypt": "^5.1.1",
    "axios": "^1.6.0",
    "@prisma/client": "^5.7.0",
    "multer": "^1.4.5-lts.1",
    "helmet": "^7.1.0",
    "express-rate-limit": "^7.1.4"
  },
  "devDependencies": {
    "@types/express": "^4.17.21",
    "@types/node": "^20.10.5",
    "@types/cors": "^2.8.17",
    "@types/jsonwebtoken": "^9.0.5",
    "@types/bcrypt": "^5.1.1",
    "@types/multer": "^1.4.11",
    "@types/jest": "^29.5.11",
    "typescript": "^5.3.3",
    "nodemon": "^3.0.2",
    "ts-node": "^10.9.2",
    "jest": "^29.7.0",
    "ts-jest": "^29.1.1",
    "prisma": "^5.7.0"
  }
}
```

- [ ] **Step 2: 创建 tsconfig.json**

```json
{
  "compilerOptions": {
    "target": "ES2020",
    "module": "commonjs",
    "lib": ["ES2020"],
    "outDir": "./dist",
    "rootDir": "./src",
    "strict": true,
    "esModuleInterop": true,
    "skipLibCheck": true,
    "forceConsistentCasingInFileNames": true,
    "resolveJsonModule": true,
    "moduleResolution": "node",
    "declaration": true,
    "declarationMap": true,
    "sourceMap": true
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "dist", "**/*.test.ts"]
}
```

- [ ] **Step 3: 创建 .env.example**

```bash
# Server
PORT=3000
NODE_ENV=development

# Database
DATABASE_URL="mysql://root:password@localhost:3306/project_management"

# JWT
JWT_SECRET=your_jwt_secret_key_here
JWT_EXPIRES_IN=2h
JWT_REFRESH_EXPIRES_IN=30d

# OAuth - WeChat
WECHAT_APP_ID=your_wechat_app_id
WECHAT_APP_SECRET=your_wechat_app_secret
WECHAT_REDIRECT_URI=http://localhost:3000/api/v1/auth/wechat/callback

# OAuth - DingTalk
DINGTALK_APP_ID=your_dingtalk_app_id
DINGTALK_APP_SECRET=your_dingtalk_app_secret
DINGTALK_REDIRECT_URI=http://localhost:3000/api/v1/auth/dingtalk/callback

# File Upload
MAX_FILE_SIZE=10485760
UPLOAD_DIR=./uploads

# Rate Limit
RATE_LIMIT_WINDOW=15
RATE_LIMIT_MAX=100
```

- [ ] **Step 4: 安装依赖**

Run: `cd backend && npm install`
Expected: Dependencies installed successfully

- [ ] **Step 5: 提交代码**

Run: `cd .. && git add backend && git commit -m "feat: initialize backend project with TypeScript"`
Expected: Commit created

---

### Task 3: 配置Prisma ORM

**Files:**
- Create: `backend/src/models/schema.prisma`
- Create: `backend/src/config/database.ts`

- [ ] **Step 1: 创建 Prisma schema**

```prisma
// backend/src/models/schema.prisma

generator client {
  provider = "prisma-client-js"
}

datasource db {
  provider = "mysql"
  url      = env("DATABASE_URL")
}

model User {
  id        BigInt   @id @default(autoincrement())
  username  String?  @unique
  email     String?  @unique
  avatar    String?
  name      String
  phone     String?
  status    UserStatus @default(ACTIVE)
  createdAt DateTime @default(now())
  updatedAt DateTime @updatedAt

  createdTasks       Task[]      @relation("TaskCreator")
  assignedTasks      Task[]      @relation("TaskAssignee")
  ownedProjects      Project[]   @relation("ProjectOwner")
  projectMemberships ProjectMember[]
  bindings           UserBinding[]
  comments           Comment[]
  activities         Activity[]
  uploadedFiles      File[]
  workHours          WorkHour[]
  roles              UserRole[]
  events             Event[]
  loginLogs          LoginLog[]
}

enum UserStatus {
  ACTIVE
  INACTIVE
  DELETED
}

model UserBinding {
  id        BigInt          @id @default(autoincrement())
  userId    BigInt
  platform  OAuthPlatform
  openId    String
  unionId   String?
  nickname  String?
  avatar    String?
  createdAt DateTime        @default(now())
  updatedAt DateTime        @updatedAt

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@unique([platform, openId])
  @@index([platform, openId])
}

enum OAuthPlatform {
  WECHAT
  DINGTALK
}

model Project {
  id        BigInt        @id @default(autoincrement())
  name      String
  description String?
  code      String        @unique
  ownerId   BigInt
  status    ProjectStatus @default(ACTIVE)
  startDate DateTime?
  endDate   DateTime?
  createdAt DateTime      @default(now())
  updatedAt DateTime      @updatedAt

  owner       User             @relation("ProjectOwner", fields: [ownerId], references: [id])
  members     ProjectMember[]
  tasks       Task[]
  milestones  Milestone[]
  files       File[]
  activities  Activity[]
  workHours   WorkHour[]
  events      Event[]
  comments    Comment[]
  userRoles   UserRole[]
}

enum ProjectStatus {
  ACTIVE
  ARCHIVED
  DELETED
}

model ProjectMember {
  id        BigInt         @id @default(autoincrement())
  projectId BigInt
  userId    BigInt
  role      MemberRole     @default(MEMBER)
  joinedAt  DateTime       @default(now())

  project Project @relation(fields: [projectId], references: [id], onDelete: Cascade)
  user    User    @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@unique([projectId, userId])
}

enum MemberRole {
  OWNER
  ADMIN
  MEMBER
  VIEWER
}

model Task {
  id          BigInt      @id @default(autoincrement())
  title       String
  description String?
  projectId   BigInt
  assignedTo  BigInt?
  createdBy   BigInt
  status      TaskStatus  @default(TODO)
  priority    Int         @default(3)
  dueDate     DateTime?
  startDate   DateTime?
  tags        String?
  attachments String?
  createdAt   DateTime    @default(now())
  updatedAt   DateTime    @updatedAt

  project        Project      @relation(fields: [projectId], references: [id], onDelete: Cascade)
  assignedUser   User?        @relation("TaskAssignee", fields: [assignedTo], references: [id])
  createdByUser  User         @relation("TaskCreator", fields: [createdBy], references: [id])
  comments       Comment[]
  activities     Activity[]
  workHours      WorkHour[]
  events         Event[]

  @@index([projectId])
  @@index([assignedTo])
  @@index([status])
  @@index([dueDate])
}

enum TaskStatus {
  TODO
  DOING
  DONE
  ARCHIVED
}

model Milestone {
  id          BigInt            @id @default(autoincrement())
  projectId   BigInt
  name        String
  description String?
  dueDate     DateTime
  status      MilestoneStatus   @default(PENDING)
  orderIndex  Int               @default(0)
  createdAt   DateTime          @default(now())
  updatedAt   DateTime          @updatedAt

  project Project @relation(fields: [projectId], references: [id], onDelete: Cascade)
}

enum MilestoneStatus {
  PENDING
  COMPLETED
}

model File {
  id          BigInt   @id @default(autoincrement())
  name        String
  path        String
  size        BigInt
  type        String
  uploadedBy  BigInt
  taskId      BigInt?
  projectId   BigInt?
  createdAt   DateTime @default(now())

  uploader User     @relation(fields: [uploadedBy], references: [id])
  task     Task?    @relation(fields: [taskId], references: [id], onDelete: Cascade)
  project  Project? @relation(fields: [projectId], references: [id], onDelete: Cascade)
}

model Comment {
  id          BigInt   @id @default(autoincrement())
  content     String
  entityType  String
  entityId    BigInt
  userId      BigInt
  parentId    BigInt?
  mentions    String?
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  user     User      @relation(fields: [userId], references: [id], onDelete: Cascade)
  parent   Comment?  @relation("CommentReplies", fields: [parentId], references: [id])
  replies  Comment[] @relation("CommentReplies")

  @@index([entityType, entityId])
}

model Activity {
  id         BigInt   @id @default(autoincrement())
  userId     BigInt
  action     String
  entityType String
  entityId   BigInt
  projectId  BigInt?
  metadata   String?
  createdAt  DateTime @default(now())

  user    User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  project Project? @relation(fields: [projectId], references: [id], onDelete: Cascade)

  @@index([projectId])
  @@index([userId])
  @@index([createdAt])
}

model WorkHour {
  id          BigInt   @id @default(autoincrement())
  userId      BigInt
  taskId      BigInt?
  projectId   BigInt?
  hours       Decimal  @db.Decimal(5, 2)
  date        DateTime
  description String?
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  user    User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  task    Task?    @relation(fields: [taskId], references: [id], onDelete: Cascade)
  project Project? @relation(fields: [projectId], references: [id], onDelete: Cascade)

  @@index([userId, date])
  @@index([projectId])
}

model Event {
  id               BigInt    @id @default(autoincrement())
  title            String
  description      String?
  location         String?
  userId           BigInt
  projectId        BigInt?
  taskId           BigInt?
  startTime        DateTime
  endTime          DateTime
  allDay           Boolean   @default(false)
  reminderMinutes  Int       @default(0)
  repeatRule       String?
  createdAt        DateTime  @default(now())
  updatedAt        DateTime  @updatedAt

  user    User     @relation(fields: [userId], references: [id], onDelete: Cascade)
  project Project? @relation(fields: [projectId], references: [id], onDelete: Cascade)
  task    Task?    @relation(fields: [taskId], references: [id], onDelete: Cascade)

  @@index([userId, startTime])
}

model Role {
  id          BigInt   @id @default(autoincrement())
  name        String   @unique
  description String?
  permissions String
  createdAt   DateTime @default(now())
  updatedAt   DateTime @updatedAt

  userRoles UserRole[]
}

model UserRole {
  id        BigInt   @id @default(autoincrement())
  userId    BigInt
  roleId    BigInt
  projectId BigInt?
  createdAt DateTime @default(now())

  user    User    @relation(fields: [userId], references: [id], onDelete: Cascade)
  role    Role    @relation(fields: [roleId], references: [id], onDelete: Cascade)
  project Project? @relation(fields: [projectId], references: [id], onDelete: Cascade)
}

model LoginLog {
  id      BigInt       @id @default(autoincrement())
  userId  BigInt
  ip      String?
  device  String?
  success Boolean      @default(true)
  platform String?
  createdAt DateTime    @default(now())

  user User @relation(fields: [userId], references: [id], onDelete: Cascade)

  @@index([userId])
  @@index([createdAt])
}
```

- [ ] **Step 2: 创建数据库配置文件**

```typescript
// backend/src/config/database.ts
import { PrismaClient } from '@prisma/client';

class Database {
  private static instance: PrismaClient;

  public static getInstance(): PrismaClient {
    if (!Database.instance) {
      Database.instance = new PrismaClient({
        log: process.env.NODE_ENV === 'development' ? ['query', 'error', 'warn'] : ['error'],
      });
    }

    return Database.instance;
  }

  public static async connect(): Promise<void> {
    try {
      await Database.getInstance().$connect();
      console.log('Database connected successfully');
    } catch (error) {
      console.error('Database connection failed:', error);
      process.exit(1);
    }
  }

  public static async disconnect(): Promise<void> {
    await Database.getInstance().$disconnect();
  }
}

export const prisma = Database.getInstance();
export { Database };
```

- [ ] **Step 3: 生成 Prisma Client**

Run: `cd backend && npx prisma generate`
Expected: Prisma Client generated

- [ ] **Step 4: 提交代码**

Run: `cd .. && git add backend/src/models backend/src/config && git commit -m "feat: configure Prisma ORM with database schema"`
Expected: Commit created

---

### Task 4: 创建配置管理模块

**Files:**
- Create: `backend/src/config/index.ts`
- Create: `backend/src/config/jwt.ts`
- Create: `backend/src/config/oauth.ts`

- [ ] **Step 1: 创建配置入口文件**

```typescript
// backend/src/config/index.ts
import dotenv from 'dotenv';

dotenv.config();

export const config = {
  port: parseInt(process.env.PORT || '3000', 10),
  nodeEnv: process.env.NODE_ENV || 'development',
  database: {
    url: process.env.DATABASE_URL || '',
  },
  jwt: {
    secret: process.env.JWT_SECRET || 'default-secret-change-in-production',
    expiresIn: process.env.JWT_EXPIRES_IN || '2h',
    refreshExpiresIn: process.env.JWT_REFRESH_EXPIRES_IN || '30d',
  },
  oauth: {
    wechat: {
      appId: process.env.WECHAT_APP_ID || '',
      appSecret: process.env.WECHAT_APP_SECRET || '',
      redirectUri: process.env.WECHAT_REDIRECT_URI || '',
    },
    dingtalk: {
      appId: process.env.DINGTALK_APP_ID || '',
      appSecret: process.env.DINGTALK_APP_SECRET || '',
      redirectUri: process.env.DINGTALK_REDIRECT_URI || '',
    },
  },
  upload: {
    maxSize: parseInt(process.env.MAX_FILE_SIZE || '10485760', 10),
    dir: process.env.UPLOAD_DIR || './uploads',
  },
  rateLimit: {
    windowMs: parseInt(process.env.RATE_LIMIT_WINDOW || '15', 10) * 60 * 1000,
    max: parseInt(process.env.RATE_LIMIT_MAX || '100', 10),
  },
} as const;
```

- [ ] **Step 2: 创建JWT配置文件**

```typescript
// backend/src/config/jwt.ts
import jwt from 'jsonwebtoken';
import { config } from './index';

export interface JwtPayload {
  userId: string;
  username?: string;
}

export const generateAccessToken = (payload: JwtPayload): string => {
  return jwt.sign(payload, config.jwt.secret, {
    expiresIn: config.jwt.expiresIn,
  });
};

export const generateRefreshToken = (payload: JwtPayload): string => {
  return jwt.sign(payload, config.jwt.secret, {
    expiresIn: config.jwt.refreshExpiresIn,
  });
};

export const verifyToken = (token: string): JwtPayload => {
  return jwt.verify(token, config.jwt.secret) as JwtPayload;
};

export const decodeToken = (token: string): JwtPayload | null => {
  try {
    return jwt.decode(token) as JwtPayload;
  } catch {
    return null;
  }
};
```

- [ ] **Step 3: 创建OAuth配置文件**

```typescript
// backend/src/config/oauth.ts
import axios from 'axios';
import { config } from './index';

export interface OAuthUser {
  openId: string;
  unionId?: string;
  nickname: string;
  avatar: string;
}

export interface WeChatAuthResponse {
  access_token: string;
  expires_in: number;
  refresh_token: string;
  openid: string;
  scope: string;
  unionid?: string;
  errcode?: number;
  errmsg?: string;
}

export interface DingTalkAuthResponse {
  access_token: string;
  expires_in: number;
  errcode?: number;
  errmsg?: string;
}

export class WeChatOAuth {
  private appId = config.oauth.wechat.appId;
  private appSecret = config.oauth.wechat.appSecret;

  getAuthUrl(state: string = 'state'): string {
    return `https://open.weixin.qq.com/connect/qrconnect?appid=${this.appId}&redirect_uri=${encodeURIComponent(config.oauth.wechat.redirectUri)}&response_type=code&scope=snsapi_login&state=${state}#wechat_redirect`;
  }

  async getAccessToken(code: string): Promise<WeChatAuthResponse> {
    const url = `https://api.weixin.qq.com/sns/oauth2/access_token?appid=${this.appId}&secret=${this.appSecret}&code=${code}&grant_type=authorization_code`;
    const response = await axios.get(url);
    return response.data;
  }

  async getUserInfo(accessToken: string, openId: string): Promise<OAuthUser> {
    const url = `https://api.weixin.qq.com/sns/userinfo?access_token=${accessToken}&openid=${openId}`;
    const response = await axios.get(url);

    return {
      openId: response.data.openid,
      unionId: response.data.unionid,
      nickname: response.data.nickname,
      avatar: response.data.headimgurl,
    };
  }
}

export class DingTalkOAuth {
  private appId = config.oauth.dingtalk.appId;
  private appSecret = config.oauth.dingtalk.appSecret;

  getAuthUrl(state: string = 'state'): string {
    return `https://login.dingtalk.com/oauth2/auth?redirect_uri=${encodeURIComponent(config.oauth.dingtalk.redirectUri)}&response_type=code&client_id=${this.appId}&scope=openid&state=${state}&prompt=consent`;
  }

  async getAccessToken(code: string): Promise<DingTalkAuthResponse> {
    const url = `https://api.dingtalk.com/v1.0/oauth2/userAccessToken`;
    const response = await axios.post(url, {
      clientId: this.appId,
      clientSecret: this.appSecret,
      code: code,
      grantType: 'authorization_code',
    });
    return response.data;
  }

  async getUserInfo(accessToken: string): Promise<OAuthUser> {
    const url = `https://api.dingtalk.com/v1.0/contact/users/me`;
    const response = await axios.get(url, {
      headers: {
        'x-acs-dingtalk-access-token': accessToken,
      },
    });

    return {
      openId: response.data.openId,
      unionId: response.data.unionId,
      nickname: response.data.nick || response.data.name,
      avatar: response.data.avatar,
    };
  }
}

export const weChatOAuth = new WeChatOAuth();
export const dingTalkOAuth = new DingTalkOAuth();
```

- [ ] **Step 4: 提交代码**

Run: `git add backend/src/config && git commit -m "feat: create configuration management module"`
Expected: Commit created

---

## Chunk 2: 认证模块（微信/钉钉登录）

---

### Task 5: 创建认证服务

**Files:**
- Create: `backend/src/services/auth.service.ts`
- Create: `backend/src/services/auth.service.test.ts`

- [ ] **Step 1: 编写认证服务测试**

```typescript
// backend/src/services/auth.service.test.ts
import { describe, it, expect, beforeEach, afterEach, vi } from '@jest/globals';
import { AuthService } from './auth.service';
import { prisma } from '../config/database';

describe('AuthService', () => {
  let authService: AuthService;

  beforeEach(() => {
    authService = new AuthService();
  });

  afterEach(() => {
    vi.clearAllMocks();
  });

  describe('微信登录', () => {
    it('应该成功创建新用户', async () => {
      const mockUser = {
        openId: 'test_open_id',
        unionId: 'test_union_id',
        nickname: '测试用户',
        avatar: 'https://test.com/avatar.jpg',
      };

      // Mock the findFirst to return null (user doesn't exist)
      vi.spyOn(prisma.userBinding, 'findFirst').mockResolvedValue(null);

      // Mock the create user
      const createdUser = {
        id: BigInt(1),
        name: '测试用户',
        username: null,
        email: null,
        avatar: 'https://test.com/avatar.jpg',
        phone: null,
        status: 'ACTIVE',
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      vi.spyOn(prisma.user, 'create').mockResolvedValue(createdUser as any);

      const result = await authService.loginWithWeChat(mockUser);

      expect(result).toBeDefined();
      expect(result.userId).toBe('1');
    });

    it('应该成功返回已存在的用户', async () => {
      const mockUser = {
        openId: 'test_open_id',
        unionId: 'test_union_id',
        nickname: '测试用户',
        avatar: 'https://test.com/avatar.jpg',
      };

      // Mock the findFirst to return existing user
      const existingBinding = {
        id: BigInt(1),
        userId: BigInt(1),
        platform: 'WECHAT',
        openId: 'test_open_id',
        unionId: 'test_union_id',
        nickname: '测试用户',
        avatar: 'https://test.com/avatar.jpg',
        createdAt: new Date(),
        updatedAt: new Date(),
        user: {
          id: BigInt(1),
          name: '测试用户',
          username: null,
          email: null,
          avatar: 'https://test.com/avatar.jpg',
          phone: null,
          status: 'ACTIVE',
          createdAt: new Date(),
          updatedAt: new Date(),
        },
      };

      vi.spyOn(prisma.userBinding, 'findFirst').mockResolvedValue(existingBinding as any);

      const result = await authService.loginWithWeChat(mockUser);

      expect(result).toBeDefined();
      expect(result.userId).toBe('1');
    });
  });

  describe('钉钉登录', () => {
    it('应该成功创建新用户', async () => {
      const mockUser = {
        openId: 'test_open_id',
        unionId: 'test_union_id',
        nickname: '测试用户',
        avatar: 'https://test.com/avatar.jpg',
      };

      vi.spyOn(prisma.userBinding, 'findFirst').mockResolvedValue(null);

      const createdUser = {
        id: BigInt(1),
        name: '测试用户',
        username: null,
        email: null,
        avatar: 'https://test.com/avatar.jpg',
        phone: null,
        status: 'ACTIVE',
        createdAt: new Date(),
        updatedAt: new Date(),
      };

      vi.spyOn(prisma.user, 'create').mockResolvedValue(createdUser as any);

      const result = await authService.loginWithDingTalk(mockUser);

      expect(result).toBeDefined();
      expect(result.userId).toBe('1');
    });
  });

  describe('Token管理', () => {
    it('应该成功生成Access Token', () => {
      const payload = { userId: '1' };
      const token = authService.generateAccessToken(payload);

      expect(token).toBeDefined();
      expect(typeof token).toBe('string');
    });

    it('应该成功生成Refresh Token', () => {
      const payload = { userId: '1' };
      const token = authService.generateRefreshToken(payload);

      expect(token).toBeDefined();
      expect(typeof token).toBe('string');
    });

    it('应该成功验证Token', () => {
      const payload = { userId: '1' };
      const token = authService.generateAccessToken(payload);

      const decoded = authService.verifyToken(token);

      expect(decoded).toBeDefined();
      expect(decoded.userId).toBe('1');
    });
  });
});
```

- [ ] **Step 2: 运行测试（应该失败）**

Run: `cd backend && npm test -- services/auth.service.test.ts`
Expected: FAIL with service not defined

- [ ] **Step 3: 实现认证服务**

```typescript
// backend/src/services/auth.service.ts
import { Prisma } from '@prisma/client';
import { prisma } from '../config/database';
import {
  generateAccessToken,
  generateRefreshToken,
  verifyToken,
  JwtPayload,
} from '../config/jwt';

export interface OAuthUser {
  openId: string;
  unionId?: string;
  nickname: string;
  avatar: string;
}

export interface LoginResult {
  userId: string;
  accessToken: string;
  refreshToken: string;
  user: {
    id: string;
    name: string;
    avatar?: string;
    email?: string;
  };
}

export class AuthService {
  async findOrCreateUser(
    platform: 'WECHAT' | 'DINGTALK',
    oauthUser: OAuthUser
  ): Promise<LoginResult> {
    // 查找是否已存在该绑定
    const existingBinding = await prisma.userBinding.findFirst({
      where: {
        platform,
        openId: oauthUser.openId,
      },
      include: {
        user: true,
      },
    });

    if (existingBinding) {
      return this.generateLoginResult(existingBinding.user);
    }

    // 创建新用户
    const user = await prisma.user.create({
      data: {
        name: oauthUser.nickname,
        avatar: oauthUser.avatar,
      },
    });

    // 创建绑定关系
    await prisma.userBinding.create({
      data: {
        userId: user.id,
        platform,
        openId: oauthUser.openId,
        unionId: oauthUser.unionId,
        nickname: oauthUser.nickname,
        avatar: oauthUser.avatar,
      },
    });

    return this.generateLoginResult(user);
  }

  async loginWithWeChat(oauthUser: OAuthUser): Promise<LoginResult> {
    return this.findOrCreateUser('WECHAT', oauthUser);
  }

  async loginWithDingTalk(oauthUser: OAuthUser): Promise<LoginResult> {
    return this.findOrCreateUser('DINGTALK', oauthUser);
  }

  async refreshAccessToken(refreshToken: string): Promise<LoginResult | null> {
    try {
      const payload = verifyToken(refreshToken);

      const user = await prisma.user.findUnique({
        where: { id: BigInt(payload.userId) },
      });

      if (!user || user.status !== 'ACTIVE') {
        return null;
      }

      return this.generateLoginResult(user);
    } catch {
      return null;
    }
  }

  async logout(userId: string): Promise<void> {
    // 记录登出日志
    await prisma.loginLog.create({
      data: {
        userId: BigInt(userId),
        success: true,
        action: 'logout',
      },
    });
  }

  private generateLoginResult(user: any): LoginResult {
    const payload: JwtPayload = {
      userId: user.id.toString(),
      username: user.username || undefined,
    };

    return {
      userId: user.id.toString(),
      accessToken: generateAccessToken(payload),
      refreshToken: generateRefreshToken(payload),
      user: {
        id: user.id.toString(),
        name: user.name,
        avatar: user.avatar || undefined,
        email: user.email || undefined,
      },
    };
  }

  generateAccessToken(payload: JwtPayload): string {
    return generateAccessToken(payload);
  }

  generateRefreshToken(payload: JwtPayload): string {
    return generateRefreshToken(payload);
  }

  verifyToken(token: string): JwtPayload {
    return verifyToken(token);
  }
}
```

- [ ] **Step 4: 运行测试（应该通过）**

Run: `cd backend && npm test -- services/auth.service.test.ts`
Expected: PASS

- [ ] **Step 5: 提交代码**

Run: `git add backend/src/services && git commit -m "feat: implement auth service with WeChat/DingTalk login"`
Expected: Commit created

---

### Task 6: 创建认证中间件

**Files:**
- Create: `backend/src/middleware/auth.middleware.ts`
- Create: `backend/src/middleware/permission.middleware.ts`
- Create: `backend/src/middleware/error.middleware.ts`

- [ ] **Step 1: 创建认证中间件**

```typescript
// backend/src/middleware/auth.middleware.ts
import { Request, Response, NextFunction } from 'express';
import { verifyToken, JwtPayload } from '../config/jwt';

declare global {
  namespace Express {
    interface Request {
      user?: JwtPayload;
    }
  }
}

export const authenticate = async (
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const authHeader = req.headers.authorization;

    if (!authHeader || !authHeader.startsWith('Bearer ')) {
      res.status(401).json({
        code: 401,
        message: '未提供认证令牌',
      });
      return;
    }

    const token = authHeader.substring(7);
    const payload = verifyToken(token);

    req.user = payload;
    next();
  } catch (error) {
    res.status(401).json({
      code: 401,
      message: '无效的认证令牌',
    });
  }
};

export const optionalAuth = async (
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> => {
  try {
    const authHeader = req.headers.authorization;

    if (authHeader && authHeader.startsWith('Bearer ')) {
      const token = authHeader.substring(7);
      const payload = verifyToken(token);
      req.user = payload;
    }

    next();
  } catch {
    next();
  }
};
```

- [ ] **Step 2: 创建权限中间件**

```typescript
// backend/src/middleware/permission.middleware.ts
import { Request, Response, NextFunction } from 'express';
import { prisma } from '../config/database';

export const checkProjectPermission = (
  ...roles: string[]
) => {
  return async (req: Request, res: Response, next: NextFunction): Promise<void> => {
    try {
      if (!req.user) {
        res.status(401).json({
          code: 401,
          message: '未认证',
        });
        return;
      }

      const projectId = req.params.projectId || req.body.projectId;

      if (!projectId) {
        res.status(400).json({
          code: 400,
          message: '缺少项目ID',
        });
        return;
      }

      const membership = await prisma.projectMember.findUnique({
        where: {
          projectId_userId: {
            projectId: BigInt(projectId),
            userId: BigInt(req.user.userId),
          },
        },
      });

      if (!membership) {
        res.status(403).json({
          code: 403,
          message: '无权限访问该项目',
        });
        return;
      }

      if (roles.length > 0 && !roles.includes(membership.role)) {
        res.status(403).json({
          code: 403,
          message: '权限不足',
        });
        return;
      }

      req.projectMembership = membership;
      next();
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  };
};

declare global {
  namespace Express {
    interface Request {
      projectMembership?: any;
    }
  }
}
```

- [ ] **Step 3: 创建错误处理中间件**

```typescript
// backend/src/middleware/error.middleware.ts
import { Request, Response, NextFunction } from 'express';

export class AppError extends Error {
  constructor(
    public code: number,
    public message: string,
    public errors?: any[]
  ) {
    super(message);
    this.name = 'AppError';
  }
}

export const errorHandler = (
  err: Error,
  req: Request,
  res: Response,
  next: NextFunction
): void => {
  if (err instanceof AppError) {
    res.status(err.code).json({
      code: err.code,
      message: err.message,
      errors: err.errors,
    });
    return;
  }

  console.error('Error:', err);

  res.status(500).json({
    code: 500,
    message: '服务器内部错误',
  });
};

export const notFoundHandler = (
  req: Request,
  res: Response
): void => {
  res.status(404).json({
    code: 404,
    message: '接口不存在',
  });
};
```

- [ ] **Step 4: 提交代码**

Run: `git add backend/src/middleware && git commit -m "feat: create auth and permission middleware"`
Expected: Commit created

---

### Task 7: 创建认证路由和控制器

**Files:**
- Create: `backend/src/controllers/auth.controller.ts`
- Create: `backend/src/routes/auth.routes.ts`

- [ ] **Step 1: 创建认证控制器**

```typescript
// backend/src/controllers/auth.controller.ts
import { Request, Response, NextFunction } from 'express';
import { AuthService } from '../services/auth.service';
import { weChatOAuth, dingTalkOAuth, OAuthUser } from '../config/oauth';
import { prisma } from '../config/database';
import { AppError } from '../middleware/error.middleware';

export class AuthController {
  private authService = new AuthService();

  // 获取微信登录URL
  getWeChatAuthUrl(req: Request, res: Response): void {
    const state = req.query.state as string || 'state';
    const url = weChatOAuth.getAuthUrl(state);
    res.json({ code: 200, message: 'success', data: { url } });
  }

  // 微信OAuth回调
  async weChatCallback(req: Request, res: Response): Promise<void> {
    try {
      const { code } = req.query;

      if (!code) {
        throw new AppError(400, '缺少授权码');
      }

      // 获取access token
      const tokenResponse = await weChatOAuth.getAccessToken(code as string);

      if (tokenResponse.errcode) {
        throw new AppError(400, '微信授权失败', [tokenResponse.errmsg]);
      }

      // 获取用户信息
      const oauthUser = await weChatOAuth.getUserInfo(
        tokenResponse.access_token,
        tokenResponse.openid
      );

      // 登录或创建用户
      const result = await this.authService.loginWithWeChat(oauthUser);

      // 记录登录日志
      await this.recordLoginLog(result.userId, 'WECHAT', req.ip);

      res.json({ code: 200, message: 'success', data: result });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
          errors: error.errors,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
'        });
      }
    }
  }

  // 获取钉钉登录URL
  getDingTalkAuthUrl(req: Request, res: Response): void {
    const state = req.query.state as string || 'state';
    const url = dingTalkOAuth.getAuthUrl(state);
    res.json({ code: 200, message: 'success', data: { url } });
  }

  // 钉钉OAuth回调
  async dingTalkCallback(req: Request, res: Response): Promise<void> {
    try {
      const { code } = req.query;

      if (!code) {
        throw new AppError(400, '缺少授权码');
      }

      // 获取access token
      const tokenResponse = await dingTalkOAuth.getAccessToken(code as string);

      if (tokenResponse.errcode) {
        throw new AppError(400, '钉钉授权失败', [tokenResponse.errmsg]);
      }

      // 获取用户信息
      const oauthUser = await dingTalkOAuth.getUserInfo(tokenResponse.access_token);

      // 登录或创建用户
      const result = await this.authService.loginWithDingTalk(oauthUser);

      // 记录登录日志
      await this.recordLoginLog(result.userId, 'DINGTALK', req.ip);

      res.json({ code: 200, message: 'success', data: result });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
          errors: error.errors,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  // 刷新Token
  async refreshToken(req: Request, res: Response): Promise<void> {
    try {
      const { refreshToken } = req.body;

      if (!refreshToken) {
        throw new AppError(400, '缺少刷新令牌');
      }

      const result = await this.authService.refreshAccessToken(refreshToken);

      if (!result) {
        throw new AppError('401', '无效的刷新令牌');
      }

      res.json({ code: 200, message: 'success', data: result });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
          errors: error.errors,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  // 登出
  async logout(req: Request, res: Response): Promise<void> {
    try {
      if (req.user) {
        await this.authService.logout(req.user.userId);
      }

      res.json({ code: 200, message: 'success' });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  // 获取当前用户信息
  async getCurrentUser(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const user = await prisma.user.findUnique({
        where: { id: BigInt(req.user.userId) },
        select: {
          id: true,
          name: true,
          username: true,
          email: true,
          avatar: true,
          phone: true,
          createdAt: true,
        },
      });

      if (!user) {
        throw new AppError(404, '用户不存在');
      }

      res.json({
        code: 200,
        message: 'success',
        data: {
          ...user,
          id: user.id.toString(),
        },
      });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  private async recordLoginLog(
    userId: string,
    platform: string,
    ip?: string
  ): Promise<void> {
    await prisma.loginLog.create({
      data: {
        userId: BigInt(userId),
        platform,
        ip,
        success: true,
      },
    });
  }
}
```

- [ ] **Step 2: 创建认证路由**

```typescript
// backend/src/routes/auth.routes.ts
import { Router } from 'express';
import { AuthController } from '../controllers/auth.controller';
import { authenticate } from '../middleware/auth.middleware';

const router = Router();
const authController = new AuthController();

// 公开路由
router.get('/wechat/auth-url', authController.getWeChatAuthUrl.bind(authController));
router.get('/wechat/callback', authController.weChatCallback.bind(authController));
router.get('/dingtalk/auth-url', authController.getDingTalkAuthUrl.bind(authController));
router.get('/dingtalk/callback', authController.dingTalkCallback.bind(authController));
router.post('/refresh', authController.refreshToken.bind(authController));

// 需要认证的路由
router.post('/logout', authenticate, authController.logout.bind(authController));
router.get('/me', authenticate, authController.getCurrentUser.bind(authController));

export default router;
```

- [ ] **Step 3: 提交代码**

Run: `git add backend/src/controllers backend/src/routes && git commit -m "feat: implement auth routes and controllers"`
Expected: Commit created

---

## Chunk 3: 项目管理模块

---

### Task 8: 创建项目服务

**Files:**
- Create: `backend/src/services/project.service.ts`

- [ ] **Step 1: 创建项目服务**

```typescript
// backend/src/services/project.service.ts
import { Prisma } from '@prisma/client';
import { prisma } from '../config/database';

export interface CreateProjectInput {
  name: string;
  description?: string;
  code: string;
  ownerId: string;
  startDate?: Date;
  endDate?: Date;
}

export interface UpdateProjectInput {
  name?: string;
  description?: string;
  startDate?: Date;
  endDate?: Date;
}

export interface AddMemberInput {
  userId: string;
  role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER';
}

export class ProjectService {
  async createProject(input: CreateProjectInput) {
    const project = await prisma.project.create({
      data: {
        name: input.name,
        description: input.description,
        code: input.code,
        ownerId: BigInt(input.ownerId),
        startDate: input.startDate,
        endDate: input.endDate,
      },
      include: {
        owner: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
      },
    });

    // 自动创建所有者成员
    await prisma.projectMember.create({
      data: {
        projectId: project.id,
        userId: project.ownerId,
        role: 'OWNER',
      },
    });

    return this.transformProject(project);
  }

  async getProjects(userId: string, page = 1, pageSize = 20) {
    const skip = (page - 1) * pageSize;

    const [projects, total] = await Promise.all([
      prisma.project.findMany({
        where: {
          members: {
            some: {
              userId: BigInt(userId),
            },
          },
        },
        include: {
          owner: {
            select: {
              id: true,
              name: true,
              avatar: true,
            },
          },
          members: {
            select: {
              user: {
                select: {
                  id: true,
                  name: true,
                  avatar: true,
                },
              },
            },
          },
          _count: {
            select: {
              tasks: true,
            },
          },
        },
        skip,
        take: pageSize,
        orderBy: {
          createdAt: 'desc',
        },
      }),
      prisma.project.count({
        where: {
          members: {
            some: {
              userId: BigInt(userId),
            },
          },
        },
      }),
    ]);

    return {
      list: projects.map(this.transformProject),
      total,
      page,
      pageSize,
    };
  }

  async getProjectById(projectId: string, userId?: string) {
    const project = await prisma.project.findUnique({
      where: { id: BigInt(projectId) },
      include: {
        owner: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
        members: {
          include: {
            user: {
              select: {
                id: true,
                name: true,
                avatar: true,
                email: true,
              },
            },
          },
        },
        tasks: {
          where: {
            status: {
              not: 'ARCHIVED',
            },
          },
          include: {
            assignedUser: {
              select: {
                id: true,
                name: true,
                avatar: true,
              },
            },
          },
          orderBy: {
            createdAt: 'desc',
          },
        },
        milestones: {
          orderBy: {
            dueDate: 'asc',
          },
        },
        _count: {
          select: {
            tasks: true,
            members: true,
          },
        },
      },
    });

    if (!project) {
      throw new Error('项目不存在');
    }

    // 检查用户权限
    if (userId) {
      const member = project.members.find(m => m.userId === BigInt(userId));
      if (!member) {
        throw new Error('无权限访问该项目');
      }
    }

    return {
      ...this.transformProject(project),
      members: project.members.map(m => ({
        ...m,
        userId: m.userId.toString(),
        user: {
          ...m.user,
          id: m.user.id.toString(),
        },
      })),
      tasks: project.tasks.map(t => ({
        ...t,
        id: t.id.toString(),
        projectId: t.projectId.toString(),
        assignedTo: t.assignedTo?.toString(),
        createdBy: t.createdBy.toString(),
      })),
      milestones: project.milestones.map(m => ({
        ...m,
        id: m.id.toString(),
        projectId: m.projectId.toString(),
      })),
    };
  }

  async updateProject(projectId: string, input: UpdateProjectInput) {
    const project = await prisma.project.update({
      where: { id: BigInt(projectId) },
      data: {
        name: input.name,
        description: input.description,
        startDate: input.startDate,
        endDate: input.endDate,
      },
      include: {
        owner: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
      },
    });

    return this.transformProject(project);
  }

  async deleteProject(projectId: string) {
    await prisma.project.delete({
      where: { id: BigInt(projectId) },
    });

    return { success: true };
  }

  async addMember(projectId: string, input: AddMemberInput) {
    // 查找用户
    const user = await prisma.user.findUnique({
      where: { id: BigInt(input.userId) },
    });

    if (!user) {
      throw new Error('用户不存在');
    }

    const member = await prisma.projectMember.create({
      data: {
        projectId: BigInt(projectId),
        userId: BigInt(input.userId),
        role: input.role,
      },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            avatar: true,
            email: true,
          },
        },
      },
    });

    return {
      ...member,
      userId: member.userId.toString(),
      user: {
        ...member.user,
        id: member.user.id.toString(),
      },
    };
  }

  async removeMember(projectId: string, userId: string) {
    await prisma.projectMember.delete({
      where: {
        projectId_userId: {
          projectId: BigInt(projectId),
          userId: BigInt(userId),
        },
      },
    });

    return { success: true };
  }

  async getMembers(projectId: string) {
    const members = await prisma.projectMember.findMany({
      where: { projectId: BigInt(projectId) },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            avatar: true,
            email: true,
          },
        },
      },
    });

    return members.map(m => ({
      ...m,
      userId: m.userId.toString(),
      user: {
        ...m.user,
        id: m.user.id.toString(),
      },
    }));
  }

  private transformProject(project: any) {
    return {
      ...project,
      id: project.id.toString(),
      ownerId: project.ownerId.toString(),
      owner: project.owner ? {
        ...project.owner,
        id: project.owner.id.toString(),
      } : undefined,
    };
  }
}
```

- [ ] **Step 2: 提交代码**

Run: `git add backend/src/services/project.service.ts && git commit -m "feat: implement project service"`
Expected: Commit created

---

### Task 9: 创建项目控制器和路由

**Files:**
- Create: `backend/src/controllers/project.controller.ts`
- Create: `backend/src/routes/project.routes.ts`

- [ ] **Step 1: 创建项目控制器**

```typescript
// backend/src/controllers/project.controller.ts
import { Request, Response } from 'express';
import { ProjectService } from '../services/project.service';
import { AppError } from '../middleware/error.middleware';

export class ProjectController {
  private projectService = new ProjectService();

  async createProject(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const input = {
        name: req.body.name,
        description: req.body.description,
        code: req.body.code,
        ownerId: req.user.userId,
        startDate: req.body.startDate ? new Date(req.body.startDate) : undefined,
        endDate: req.body.endDate ? new Date(req.body.endDate) : undefined,
      };

      const project = await this.projectService.createProject(input);

      res.json({ code: 200, message: 'success', data: project });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: error instanceof Error ? error.message : '服务器错误',
        });
      }
    }
  }

  async getProjects(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const page = parseInt(req.query.page as string) || 1;
      const pageSize = parseInt(req.query.pageSize as string) || 20;

      const result = await this.projectService.getProjects(req.user.userId, page, pageSize);

      res.json({ code: 200, message: 'success', data: result });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  async getProjectById(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const projectId = req.params.id;
      const project = await this.projectService.getProjectById(projectId, req.user.userId);

      res.json({ code: 200, message: 'success', data: project });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(404).json({
          code: 404,
          message: '项目不存在或无权限',
        });
      }
    }
  }

  async updateProject(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const projectId = req.params.id;
      const input = {
        name: req.body.name,
        description: req.body.description,
        startDate: req.body.startDate ? new Date(req.body.startDate) : undefined,
        endDate: req.body.endDate ? new Date(req.body.endDate) : undefined,
      };

      const project = await this.projectService.updateProject(projectId, input);

      res.json({ code: 200, message: 'success', data: project });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  async deleteProject(req: Request, res: Response): Promise<void> {
    try {
      const projectId = req.params.id;
      await this.projectService.deleteProject(projectId);

      res.json({ code: 200, message: 'success', data: { success: true } });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  async getMembers(req: Request, res: Response): Promise<void> {
    try {
      const projectId = req.params.id;
      const members = await this.projectService.getMembers(projectId);

      res.json({ code: 200, message: 'success', data: members });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  async addMember(req: Request, res: Response): Promise<void> {
    try {
      const projectId = req.params.id;
      const input = {
        userId: req.body.userId,
        role: req.body.role || 'MEMBER',
      };

      const member = await this.projectService.addMember(projectId, input);

      res.json({ code: 200, message: 'success', data: member });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: error instanceof Error ? error.message : '服务器错误',
      });
    }
  }

  async removeMember(req: Request, res: Response): Promise<void> {
    try {
      const projectId = req.params.id;
      const userId = req.params.userId;

      await this.projectService.removeMember(projectId, userId);

      res.json({ code: 200, message: 'success', data: { success: true } });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }
}
```

- [ ] **Step 2: 创建项目路由**

```typescript
// backend/src/routes/project.routes.ts
import { Router } from 'express';
import { ProjectController } from '../controllers/project.controller';
import { authenticate } from '../middleware/auth.middleware';

const router = Router();
const projectController = new ProjectController();

router.use(authenticate);

router.post('/', projectController.createProject.bind(projectController));
router.get('/', projectController.getProjects.bind(projectController));
router.get('/:id', projectController.getProjectById.bind(projectController));
router.put('/:id', projectController.updateProject.bind(projectController));
router.delete('/:id', projectController.deleteProject.bind(projectController));
router.get('/:id/members', projectController.getMembers.bind(projectController));
router.post('/:id/members', projectController.addMember.bind(projectController));
router.delete('/:id/members/:userId', projectController.removeMember.bind(projectController));

export default router;
```

- [ ] **Step 3: 提交代码**

Run: `git add backend/src/controllers/project.controller.ts backend/src/routes/project.routes.ts && git commit -m "feat: implement project routes and controllers"`
Expected: Commit created

---

## Chunk 4: 任务管理模块

---

### Task 10: 创建任务服务

**Files:**
- Create: `backend/src/services/task.service.ts`

- [ ] **Step 1: 创建任务服务**

```typescript
// backend/src/services/task.service.ts
import { Prisma } from '@prisma/client';
import { prisma } from '../config/database';

export interface CreateTaskInput {
  title: string;
  description?: string;
  projectId: string;
  assignedTo?: string;
  createdBy: string;
  status?: 'TODO' | 'DOING' | 'DONE' | 'ARCHIVED';
  priority?: number;
  dueDate?: Date;
  startDate?: Date;
  tags?: string;
  attachments?: string;
}

export interface UpdateTaskInput {
  title?: string;
  description?: string;
  assignedTo?: string;
  status?: 'TODO' | 'DOING' | 'DONE' | 'ARCHIVED';
  priority?: number;
  dueDate?: Date;
  startDate?: Date;
  tags?: string;
  attachments?: string;
}

export class TaskService {
  async createTask(input: CreateTaskInput) {
    const task = await prisma.task.create({
      data: {
        title: input.title,
        description: input.description,
        projectId: BigInt(input.projectId),
        assignedTo: input.assignedTo ? BigInt(input.assignedTo) : null,
        createdBy: BigInt(input.createdBy),
        status: input.status || 'TODO',
        priority: input.priority ?? 3,
        dueDate: input.dueDate,
        startDate: input.startDate,
        tags: input.tags,
        attachments: input.attachments,
      },
      include: {
        assignedUser: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
        createdByUser: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
      },
    });

    // 创建活动记录
    await this.createActivity(
      input.createdBy,
      input.projectId,
      'create_task',
      task.id.toString()
    );

    return this.transformTask(task);
  }

  async getTasks(projectId: string, options: {
    status?: string;
    assignedTo?: string;
    page?: number;
    pageSize?: number;
  } = {}) {
    const where: any = {
      projectId: BigInt(projectId),
    };

    if (options.status) {
      where.status = options.status;
    }

    if (options.assignedTo) {
      where.assignedTo = BigInt(options.assignedTo);
    }

    const skip = ((options.page || 1) - 1) * (options.pageSize || 20);

    const [tasks, total] = await Promise.all([
      prisma.task.findMany({
        where,
        include: {
          assignedUser: {
            select: {
              id: true,
              name: true,
              avatar: true,
            },
          },
          createdByUser: {
            select: {
              id: true,
              name: true,
              avatar: true,
            },
          },
          _count: {
            select: {
              comments: true,
            },
          },
        },
        skip,
        take: options.pageSize || 20,
        orderBy: {
          createdAt: 'desc',
        },
      }),
      prisma.task.count({ where }),
    ]);

    return {
      list: tasks.map(this.transformTask),
      total,
      page: options.page || 1,
      pageSize: options.pageSize || 20,
    };
  }

  async getTaskById(taskId: string) {
    const task = await prisma.task.findUnique({
      where: { id: BigInt(taskId) },
      include: {
        assignedUser: {
          select: {
            id: true,
            name: true,
            avatar: true,
            email: true,
          },
        },
        createdByUser: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
        comments: {
          include: {
            user: {
              select: {
                id: true,
                name: true,
                avatar: true,
              },
            },
          },
          orderBy: {
            createdAt: 'asc',
          },
        },
      },
    });

    if (!task) {
      throw new Error('任务不存在');
    }

    return {
      ...this.transformTask(task),
      comments: task.comments.map(c => ({
        ...c,
        userId: c.userId.toString(),
        user: {
          ...c.user,
          id: c.user.id.toString(),
        },
      })),
    };
  }

  async updateTask(taskId: string, input: UpdateTaskInput, userId: string) {
    const task = await prisma.task.update({
      where: { id: BigInt(taskId) },
      data: {
        title: input.title,
        description: input.description,
        assignedTo: input.assignedTo ? BigInt(input.assignedTo) : null,
        status: input.status,
        priority: input.priority,
        dueDate: input.dueDate,
        startDate: input.startDate,
        tags: input.tags,
        attachments: input.attachments,
      },
      include: {
        assignedUser: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
        createdByUser: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
      },
    });

    // 创建活动记录
    await this.createActivity(userId, task.projectId.toString(), 'update_task', taskId);

    return this.transformTask(task);
  }

  async updateTaskStatus(taskId: string, status: string, userId: string) {
    const task = await prisma.task.update({
      where: { id: BigInt(taskId) },
      data: { status: status as any },
    });

    // 创建活动记录
    await this.createActivity(userId, task.projectId.toString(), 'update_task_status', taskId, {
      from: task.status,
      to: status,
    });

    return this.transformTask(task);
  }

  async deleteTask(taskId: string, userId: string) {
    const task = await prisma.task.findUnique({
      where: { id: BigInt(taskId) },
    });

    if (!task) {
      throw new Error('任务不存在');
    }

    await prisma.task.delete({
      where: { id: BigInt(taskId) },
    });

    // 创建活动记录
    await this.createActivity(userId, task.projectId.toString(), 'delete_task', taskId);

    return { success: true };
  }

  async getTasksByStatus(projectId: string) {
    const tasks = await prisma.task.findMany({
      where: {
        projectId: BigInt(projectId),
        status: {
          not: 'ARCHIVED',
        },
      },
      include: {
        assignedUser: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
        createdByUser: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
      },
      orderBy: {
        priority: 'asc',
      },
    });

    // 按状态分组
    const grouped = {
      TODO: tasks.filter(t => t.status === 'TODO').map(this.transformTask),
      DOING: tasks.filter(t => t.status === 'DOING').map(this.transformTask),
      DONE: tasks.filter(t => t.status === 'DONE').map(this.transformTask),
    };

    return grouped;
  }

  private async createActivity(
    userId: string,
    projectId: string,
    action: string,
    entityId: string,
    metadata?: any
  ) {
    await prisma.activity.create({
      data: {
        userId: BigInt(userId),
        action,
        entityType: 'task',
        entityId: BigInt(entityId),
        projectId: BigInt(projectId),
        metadata: metadata ? JSON.stringify(metadata) : null,
      },
    });
  }

  private transformTask(task: any) {
    return {
      ...task,
      id: task.id.toString(),
      projectId: task.projectId.toString(),
      assignedTo: task.assignedTo?.toString(),
      createdBy: task.createdBy.toString(),
      assignedUser: task.assignedUser ? {
        ...task.assignedUser,
        id: task.assignedUser.id.toString(),
      } : undefined,
      createdByUser: task.createdByUser ? {
        ...task.createdByUser,
        id: task.createdByUser.id.toString(),
      } : undefined,
    };
  }
}
```

- [ ] **Step 2: 提交代码**

Run: `git add backend/src/services/task.service.ts && git commit -`m "feat: implement task service"`
Expected: Commit created

---

### Task 11: 创建任务控制器和路由

**Files:**
- Create: `backend/src/controllers/task.controller.ts`
- Create: `backend/src/routes/task.routes.ts`

- [ ] **Step 1: 创建任务控制器**

```typescript
// backend/src/controllers/task.controller.ts
import { Request, Response } from 'express';
import { TaskService } from '../services/task.service';
import { AppError } from '../middleware/error.middleware';

export class TaskController {
  private taskService = new TaskService();

  async createTask(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const input = {
        title: req.body.title,
        description: req.body.description,
        projectId: req.body.projectId,
        assignedTo: req.body.assignedTo,
        createdBy: req.user.userId,
        status: req.body.status,
        priority: req.body.priority,
        dueDate: req.body.dueDate ? new Date(req.body.dueDate) : undefined,
        startDate: req.body.startDate ? new Date(req.body.startDate) : undefined,
        tags: req.body.tags,
        attachments: req.body.attachments,
      };

      const task = await this.taskService.createTask(input);

      res.json({ code: 200, message: 'success', data: task });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: error instanceof Error ? error.message : '服务器错误',
        });
      }
    }
  }

  async getTasks(req: Request, res: Response): Promise<void> {
    try {
      const projectId = req.params.projectId;
      const options = {
        status: req.query.status as string,
        assignedTo: req.query.assignedTo as string,
        page: parseInt(req.query.page as string) || 1,
        pageSize: parseInt(req.query.pageSize as string) || 20,
      };

      const result = await this.taskService.getTasks(projectId, options);

      res.json({ code: 200, message: 'success', data: result });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  async getTaskById(req: Request, res: Response): Promise<void> {
    try {
      const taskId = req.params.id;
      const task = await this.taskService.getTaskById(taskId);

      res.json({ code: 200, message: 'success', data: task });
    } catch (error) {
      res.status(404).json({
        code: 404,
        message: '任务不存在',
      });
    }
  }

  async updateTask(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const taskId = req.params.id;
      const input = {
        title: req.body.title,
        description: req.body.description,
        assignedTo: req.body.assignedTo,
        status: req.body.status,
        priority: req.body.priority,
        dueDate: req.body.dueDate ? new Date(req.body.dueDate) : undefined,
        startDate: req.body.startDate ? new Date(req.body.startDate) : undefined,
        tags: req.body.tags,
        attachments: req.body.attachments,
      };

      const task = await this.taskService.updateTask(taskId, input, req.user.userId);

      res.json({ code: 200, message: 'success', data: task });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  async updateTaskStatus(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const taskId = req.params.id;
      const { status } = req.body;

      if (!status) {
        throw new AppError(400, '缺少状态参数');
      }

      const task = await this.taskService.updateTaskStatus(taskId, status, req.user.userId);

      res.json({ code: 200, message: 'success', data: task });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  async deleteTask(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const taskId = req.params.id;
      await this.taskService.deleteTask(taskId, req.user.userId);

      res.json({ code: 200, message: 'success', data: { success: true } });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  async getTasksByStatus(req: Request, res: Response): Promise<void> {
    try {
      const projectId = req.params.projectId;
      const groupedTasks = await this.taskService.getTasksByStatus(projectId);

      res.json({ code: 200, message: 'success', data: groupedTasks });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }
}
```

- [ ] **Step 2: 创建任务路由**

```typescript
// backend/src/routes/task.routes.ts
import { Router } from 'express';
import { TaskController } from '../controllers/task.controller';
import { authenticate } from '../middleware/auth.middleware';

const router = Router();
const taskController = new TaskController();

router.use(authenticate);

router.post('/', taskController.createTask.bind(taskController));
router.get('/project/:projectId', taskController.getTasks.bind(taskController));
router.get('/board/:projectId', taskController.getTasksByStatus.bind(taskController));
router.get('/:id', taskController.getTaskById.bind(taskController));
router.put('/:id', taskController.updateTask.bind(taskController));
router.patch('/:id/status', taskController.updateTaskStatus.bind(taskController));
router.delete('/:id', taskController.deleteTask.bind(taskController));

export default router;
```

- [ ] **Step 3: 提交代码**

Run: `git add backend/src/controllers/task.controller.ts backend/src/routes/task.routes.ts && git commit -m "feat: implement task routes and controllers"`
Expected: Commit created

---

## Chunk 5: 其他核心模块（文件、评论、统计、日程）

---

### Task 12: 创建文件服务

**Files:**
- Create: `backend/src/services/file.service.ts`
- Create: `backend/src/controllers/file.controller.ts`
- Create: `backend/src/routes/file.routes.ts`

- [ ] **Step 1: 创建文件服务**

```typescript
// backend/src/services/file.service.ts
import { promises as fs } from 'fs';
import path from 'path';
import { prisma } from '../config/database';
import { config } from '../config/index';

export class FileService {
  async uploadFile(
    file: Express.Multer.File,
    projectId?: string,
    taskId?: string,
    uploadedBy: string
  ) {
    const savedFile = await prisma.file.file.create({
      data: {
        name: file.originalname,
        path: file.path,
        size: BigInt(file.size),
        type: file.mimetype,
        uploadedBy: BigInt(uploadedBy),
        taskId: taskId ? BigInt(taskId) : null,
        projectId: projectId ? BigInt(projectId) : null,
      },
    });

    return {
      id: savedFile.id.toString(),
      name: savedFile.name,
      size: savedFile.size.toString(),
      type: savedFile.type,
      createdAt: savedFile.createdAt,
    };
  }

  async getFile(fileId: string) {
    const file = await prisma.file.file.findUnique({
      where: { id: BigInt(fileId) },
    });

    if (!file) {
      throw new Error('文件不存在');
    }

    return file;
  }

  async deleteFile(fileId: string) {
    const file = await this.getFile(fileId);

    // 删除物理文件
    try {
      await fs.unlink(file.path);
    } catch (error) {
      console.error('删除文件失败:', error);
    }

    // 删除数据库记录
    await prisma.file.file.delete({
      where: { id: BigInt(file(fileId)) },
    });

    return { success: true };
  }

  async getProjectFiles(projectId: string) {
    const files = await prisma.file.file.findMany({
      where: { projectId: BigInt(projectId) },
      include: {
        uploader: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
      },
      orderBy: {
        createdAt: 'desc',
      },
    });

    return files.map(f => ({
      ...f,
      id: f.id.toString(),
      uploadedBy: f.uploadedBy.toString(),
      uploader: {
        ...f.uploader,
        id: f.uploader.id.toString(),
      },
    }));
  }
}
```

- [ ] **Step 2: 创建文件控制器**

```typescript
// backend/src/controllers/file.controller.ts
import { Request, Response } from 'express';
import { FileService } from '../services/file.service';
import { AppError } from '../middleware/error.middleware';

export class FileController {
  private fileService = new FileService();

  async uploadFile(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      if (!req.file) {
        throw new AppError(400, '没有上传文件');
      }

      const file = await this.fileService.uploadFile(
        req.file,
        req.body.projectId,
        req.body.taskId,
        req.user.userId
      );

      res.json({ code: 200, message: 'success', data: file });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  async getProjectFiles(req: Request, res: Response): Promise<void> {
    try {
      const projectId = req.params.projectId;
      const files = await this.fileService.getProjectFiles(projectId);

      res.json({ code: 200, message: 'success', data: files });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  async deleteFile(req: Request, res: Response): Promise<void> {
    try {
      const fileId = req.params.id;
      await this.fileService.deleteFile(fileId);

      res.json({ code: 200, message: 'success', data: { success: true } });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }
}
```

- [ ] **Step 3: 创建文件路由**

```typescript
// backend/src/routes/file.routes.ts
import { Router } from 'express';
import { FileController } from '../controllers/file.controller';
import { authenticate } from '../middleware/auth.middleware';
import multer from 'multer';
import { config } from '../config/index';

const router = Router();
const fileController = new FileController();

// 配置multer
const upload = multer({
  dest: config.upload.dir,
  limits: {
    fileSize: config.maxSize,
  },
});

router.use(authenticate);

router.post('/upload', upload.single('file'), fileController.uploadFile.bind(fileController));
router.get('/project/:projectId', fileController.getProjectFiles.bind(fileController));
router.delete('/:id', fileController.deleteFile.bind(fileController));

export default router;
```

- [ ] **Step 4: 提交代码**

Run: `git add backend/src/services/file.service.ts backend/src/controllers/file.controller.ts backend/src/routes/file.routes.ts && git commit -m "feat: implement file management service"`
Expected: Commit created

---

### Task 13: 创建评论服务

**Files:**
- Create: `backend/src/services/comment.service.ts`
- Create: `backend/src/controllers/comment.controller.ts`
- Create: `backend/src/routes/comment.routes.ts`

- [ ] **Step 1: 创建评论服务**

```typescript
// backend/src/services/comment.service.ts
import { prisma } from '../config/database';

export class CommentService {
  async createComment(
    content: string,
    entityType: string,
    entityId: string,
    userId: string,
    parentId?: string,
    mentions?: string[]
  ) {
    const comment = await prisma.comment.create({
      data: {
        content,
        entityType,
        entityId: BigInt(entityId),
        userId: BigInt(userId),
        parentId: parentId ? BigInt(parentId) : null,
        mentions: mentions ? JSON.stringify(mentions) : null,
      },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
      },
    });

    return {
      ...comment,
      userId: comment.userId.toString(),
      user: {
        ...comment.user,
        id: comment.user.id.toString(),
      },
    };
  }

  async getComments(entityType: string, entityId: string) {
    const comments = await prisma.comment.findMany({
      where: {
        entityType,
        entityId: BigInt(entityId),
        parentId: null,
      },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
        replies: {
          include: {
            user: {
              select: {
                id: true,
                name: true,
                avatar: true,
              },
            },
          },
          orderBy: {
            createdAt: 'asc',
          },
        },
      },
      orderBy: {
        createdAt: 'asc',
      },
    });

    return comments.map(c => ({
      ...c,
      userId: c.userId.toString(),
      user: {
        ...c.user,
        id: c.user.id.toString(),
      },
      replies: c.replies.map(r => ({
        ...r,
        userId: r.userId.toString(),
        parentId: r.parentId?.toString(),
        user: {
          ...r.user,
          id: r.user.id.toString(),
        },
      })),
    }));
  }

  async deleteComment(commentId: string, userId: string) {
    const comment = await prisma.comment.findUniqueUnique({
      where: { id: BigInt(commentId) },
    });

    if (!comment) {
      throw new Error('评论不存在');
    }

    if (comment.userId.toString() !== userId) {
      throw new Error('无权删除此评论');
    }

    await prisma.comment.delete({
      where: { id: BigInt(commentId) },
    });

    return { success: true };
  }
}
```

- [ ] **Step 2: 创建评论控制器**

```typescript
// backend/src/controllers/comment.controller.ts
import { Request, Response } from 'express';
import { CommentService } from '../services/comment.service';
import { AppError } from '../middleware/error.middleware';

export class CommentController {
  private commentService = new CommentService();

  async createComment(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const comment = await this.commentService.createComment(
        req.body.content,
        req.body.entityType,
        req.body.entityId,
        req.user.userId,
        req.body.parentId,
        req.body.mentions
      );

      res.json({ code: 200, message: 'success', data: comment });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: error instanceof Error ? error.message : '服务器错误',
        });
      }
    }
  }

  async getComments(req: Request, res: Response): Promise<void> {
    try {
      const { entityType, entityId } = req.params;
      const comments = await this.commentService.getComments(entityType, entityId);

      res.json({ code: 200, message: 'success', data: comments });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  async deleteComment(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const commentId = req.params.id;
      await this.commentService.deleteComment(commentId, req.user.userId);

      res.json({ code: 200, message: 'success', data: { success: true } });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: error instanceof Error ? error.message : '服务器错误',
        });
      }
    }
  }
}
```

- [ ] **Step 3: 创建评论路由**

```typescript
// backend/src/routes/comment.routes.ts
import { Router } from 'express';
import { CommentController } from '../controllers/comment.controller';
import { authenticate } from '../'middleware/auth.middleware';

const router = Router();
const commentController = new CommentController();

router.use(authenticate);

router.post('/', commentController.createComment.bind(commentController));
router.get('/:entityType/:entityId', commentController.getComments.bind(commentController));
router.delete('/:id', commentController.deleteComment.bind(commentController));

export default router;
```

- [ ] **Step 4: 提交代码**

Run: `git add backend/src/services/comment.service.ts backend/src/controllers/comment.controller.ts backend/src/routes/comment.routes.ts && git commit -m "feat: implement comment service"`
Expected: Commit created

---

### Task 14: 创建统计服务

**Files:**
- Create: `backend/src/services/stats.service.ts`
- Create: `backend/src/controllers/stats.controller.ts`
- Create: `backend/src/routes/stats.routes.ts`

- [ ] **Step 1: 创建统计服务**

```typescript
// backend/src/services/stats.service.ts
import { prisma } from '../config/database';

export class StatsService {
  async getProjectStats(projectId: string) {
    const [taskStats, memberStats, milestoneStats] = await Promise.all([
      this.getTaskStats(projectId),
      this.getMemberStats(projectId),
      this.getMilestoneStats(projectId),
    ]);

    return {
      tasks: taskStats,
      members: memberStats,
      milestones: milestoneStats,
    };
  }

  private async getTaskStats(projectId: string) {
    const [total, todo, doing, done, overdue] = await Promise.all([
      prisma.task.count({
        where: { projectId: BigInt(projectId) },
      }),
      prisma.task.count({
        where: { projectId: BigInt(projectId), status: 'TODO' },
      }),
      prisma.task.count({
        where: { projectId: BigInt(projectId), status: 'DOING' },
      }),
      prisma.task.count({
        where: { projectId: BigInt(projectId), status: 'DONE' },
      }),
      prisma.task.count({
        where: {
          projectId: BigInt(projectId),
          dueDate: { lt: new Date() },
          status: { not: 'DONE' },
        },
      }),
    ]);

    return {
      total,
      status: { todo, doing, done },
      overdue,
      completionRate: total > 0 ? (done / total * 100).toFixed(2) : '0',
    };
  }

  private async getMemberStats(projectId: string) {
    const members = await prisma.projectMember.findMany({
      where: { projectId: BigInt(projectId) },
      include: {
        user: {
          select: {
            id: true,
            name: true,
            avatar: true,
          },
        },
      },
    });

    const memberStats = await Promise.all(
      members.map(async (member) => {
        const [assignedTasks, completedTasks] = await Promise.all([
          prisma.task.count({
            where: {
              projectId: BigInt(projectId),
              assignedTo: member.userId,
            },
          }),
          prisma.task.count({
            where: {
              projectId: BigInt(projectId),
              assignedTo: member.userId,
              status: 'DONE',
            },
          }),
        ]);

        const workHours = await prisma.workHour.aggregate({
          where: {
            projectId: BigInt(projectId),
            userId: member.userId,
          },
          _sum: {
            hours: true,
          },
        });

        return {
          userId: member.userId.toString(),
          name: member.user.name,
          avatar: member.user.avatar,
          assignedTasks,
          completedTasks,
          completionRate: assignedTasks > 0
            ? (completedTasks / assignedTasks * 100).toFixed(2)
            : '0',
          totalHours: workHours._sum.hours?.toString() || '0',
        };
      })
    );

    return memberStats;
  }

  private async getMilestoneStats(projectId: string) {
    const milestones = await prisma.milestone.findMany({
      where: { projectId: BigInt(projectId) },
      orderBy: { dueDate: 'asc' },
    });

    return {
      total: milestones.length,
      completed: milestones.filter(m => m.status === 'COMPLETED').length,
      pending: milestones.filter(m => m.status === 'PENDING').length,
    };
  }

  async getMemberStatsOverall(userId: string) {
    const [assignedTasks, completedTasks, workHoursResult] = await Promise.all([
      prisma.task.count({
        where: { assignedTo: BigInt(userId) },
      }),
      prisma.task.count({
        where: {
          assignedTo: BigInt(userId),
          status: 'DONE',
        },
      }),
      prisma.workHour.aggregate({
        where: { userId: BigInt(userId) },
        _sum: {
          hours: true,
        },
      }),
    ]);

    return {
      assignedTasks,
      completedTasks,
      completionRate: assignedTasks > 0
        ? (completedTasks / assignedTasks * 100).toFixed(2)
        : '0',
      totalHours: workHoursResult._sum.hours?.toString() || '0',
    };
  }
}
```

- [ ] **Step 2: 创建统计控制器**

```typescript
// backend/src/controllers/stats.controller.ts
import { Request, Response } from 'express';
import { StatsService } from '../services/stats.service';

export class StatsController {
  private statsService = new StatsService();

  async getProjectStats(req: Request, res: Response): Promise<void> {
    try {
      const projectId = req.params.id;
      const stats = await this.statsService.getProjectStats(projectId);

      res.json({ code: 200, message: 'success', data: stats });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  async getMemberStats(req: Request, res: Response): Promise<void> {
    try {
      const userId = { req.params.id;
      const stats = await this.statsService.getMemberStatsOverall(userId);

      res.json({ code: 200, message: 'success', data: stats });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }
}
```

- [ ] **Step 3: 创建统计路由**

```typescript
// backend/src/routes/stats.routes.ts
import { Router } from 'express';
import { StatsController } from '../controllers/stats.controller';
import { authenticate } from '../middleware/auth.middleware';

const router = Router();
const statsController = new StatsController();

router.use(authenticate);

router.get('/project/:id', statsController.getProjectStats.bind(statsController));
router.get('/member/:id', statsController.getMemberStats.bind(statsController));

export default router;
```

- [ ] **Step 4: 提交代码**

Run: `git add backend/src/services/stats.service.ts backend/src/controllers/stats.controller.ts backend/src/routes/stats.routes.ts && git commit -m "feat: implement stats service"`
Expected: Commit created

---

### Task 15: 创建日程服务

**Files:**
- Create: `backend/src/services/event.service.ts`
- Create: `backend/src/controllers/event.controller.ts`
- Create: `backend/src/routes/event.routes.ts`

- [ ] **Step 1: 创建日程服务**

```typescript
// backend/src/services/event.service.ts
import { prisma } from '../config/database';

export interface CreateEventInput {
  title: string;
  description?: string;
  location?: string;
  userId: string;
  projectId?: string;
  taskId?: string;
  startTime: Date;
  endTime: Date;
  allDay?: boolean;
  reminderMinutes?: number;
  repeatRule?: string;
}

export class EventService {
  async createEvent(input: CreateEventInput) {
    const event = await prisma.event.create({
      data: {
        title: input.title,
        description: input.description,
        location: input.location,
        userId: BigInt(input.userId),
        projectId: input.projectId ? BigInt(input.projectId) : null,
        taskId: input.taskId ? BigInt(input.taskId) : null,
        startTime: input.startTime,
        endTime: input.endTime,
        allDay: input.allDay || false,
        reminderMinutes: input.reminderMinutes || 0,
        repeatRule: input.repeatRule,
      },
    });

    return this.transformEvent(event);
  }

  async getEvents(userId: string, startDate?: Date, endDate?: Date) {
    const where: any = {
      userId: BigInt(userId),
    };

    if (startDate && endDate) {
      where.startTime = {
        gte: startDate,
        lte: endDate,
      };
    }

    const events = await prisma.event.findMany({
      where,
      orderBy: {
        startTime: 'asc',
      },
    });

    return events.map(this.transformEvent);
  }

  async getEventById(eventId: string) {
    const event = await prisma.event.findUnique({
      where: { id: BigInt(eventId) },
    });

    if (!event) {
      throw new Error('日程不存在');
    }

    return this.transformEvent(event);
  }

  async updateEvent(eventId: string, input: Partial<CreateEventInput>) {
    const event = await prisma.event.update({
      where: { id: BigInt(eventId) },
      data: {
        title: input.title,
        description: input.description,
        location: input.location,
        projectId: input.projectId ? BigInt(input.projectId) : undefined,
        taskId: input.taskId ? BigInt(input.taskId) : undefined,
        startTime: input.startTime,
        endTime: input.endTime,
        allDay: input.allDay,
        reminderMinutes: input.reminderMinutes,
        repeatRule: input.repeatRule,
      },
    });

    return this.transformEvent(event);
  }

  async deleteEvent(eventId: string) {
    await prisma.event.delete({
      where: { id: BigInt(eventId) },
    });

    return { success: true };
  }

  private transformEvent(event: any) {
    return {
      ...event,
      id: event.id.toString(),
      userId: event.userId.toString(),
      projectId: event.projectId?.toString(),
      taskId: event.taskId?.toString(),
    };
  }
}
```

- [ ] **Step 2: 创建日程控制器**

```typescript
// backend/src/controllers/event.controller.ts
import { Request, Response } from 'express';
import { EventService } from '../services/event.service';
import { AppError } from '../middleware/error.middleware';

export class EventController {
  private eventService = new EventService();

  async createEvent(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const input = {
        title: req.body.title,
        description: req.body.description,
        location: req.body.location,
        userId: req.user.userId,
        projectId: req.body.projectId,
        taskId: req.body.taskId,
        startTime: new Date(req.body.startTime),
        endTime: new Date(req.body.endTime),
        allDay: req.body.allDay,
        reminderMinutes: req.body.reminderMinutes,
        repeatRule: req.body.repeatRule,
      };

      const event = await this.eventService.createEvent(input);

      res.json({ code: 200, message: 'success', data: event });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: error instanceof Error ? error.message : '服务器错误',
        });
      }
    }
  }

  async getEvents(req: Request, res: Response): Promise<void> {
    try {
      if (!req.user) {
        throw new AppError(401, '未认证');
      }

      const startDate = req.query.startDate ? new Date(req.query.startDate as string) : undefined;
      const endDate = req.query.endDate ? new Date(req.query.endDate as string) : undefined;

      const events = await this.eventService.getEvents(req.user.userId, startDate, endDate);

      res.json({ code: 200, message: 'success', data: events });
    } catch (error) {
      if (error instanceof AppError) {
        res.status(error.code).json({
          code: error.code,
          message: error.message,
        });
      } else {
        res.status(500).json({
          code: 500,
          message: '服务器错误',
        });
      }
    }
  }

  async getEventById(req: Request, res: Response): Promise<void> {
    try {
      const eventId = req.params.id;
      const event = await this.eventService.getEventById(eventId);

      res.json({ code: 200, message: 'success', data: event });
    } catch (error) {
      res.status(404).json({
        code: 404,
        message: '日程不存在',
      });
    }
  }

  async updateEvent(req: Request, res: Response): Promise<void> {
    try {
      const eventId = req.params.id;
      const input = {
        title: req.body.title,
        description: req.body.description,
        location: req.body.location,
        projectId: req.body.projectId,
        taskId: req.body.taskId,
        startTime: req.body.startTime ? new Date(req.body.startTime) : undefined,
        endTime: req.body.endTime ? new Date(req.body.endTime) : undefined,
        allDay: req.body.allDay,
        reminderMinutes: req.body.reminderMinutes,
        repeatRule: req.body.repeatRule,
      };

      const event = await this.eventService.updateEvent(eventId, input);

      res.json({ code: 200, message: 'success', data: event });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  async deleteEvent(req: Request, res: Response): Promise<void> {
    try {
      const eventId = req.params.id;
      await this.eventService.deleteEvent(eventId);

      res.json({ code: 200, message: 'success', data: { success: true } });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }
}
```

- [ ] **Step 3: 创建日程路由**

```typescript
// backend/src/routes/event.routes.ts
import { Router } from 'express';
import { EventController } from '../controllers/event.controller';
import { authenticate } from '../middleware/auth.middleware';

const router = Router();
const eventController = new EventController();

router.use(authenticate);

router.post('/', eventController.createEvent.bind(eventController));
router.get('/', eventController.getEvents.bind(eventController));
router.get('/:id', eventController.getEventById.bind(eventController));
router.put('/:id', eventController.updateEvent.bind(eventController));
router.delete('/:id', eventController.deleteEvent.bind(eventController));

export default router;
```

- [ ] **Step 4: 提交代码**

Run: `git add backend/src/services/event.service.ts backend/src/controllers/event.controller.ts backend/src/routes/event.routes.ts && git commit -m "feat: implement event service"`
Expected: Commit created

---

## Chunk 6: 服务器主入口和路由整合

---

### Task 16: 创建主路由文件和服务器入口

**Files:**
- Create: `backend/src/routes/index.ts`
- Create: `backend/src/middleware/logger.middleware.ts`
- Create: `backend/src/server.ts`

- [ ] **Step 1: 创建主路由文件**

```typescript
// backend/src/routes/index.ts
import { Router } from 'express';
import authRoutes from './auth.routes';
import projectRoutes from './project.routes';
import taskRoutes from './task.routes';
import fileRoutes from './file.routes';
import commentRoutes from './comment.routes';
import statsRoutes from './stats.routes';
import eventRoutes from './event.routes';

const router = Router();

router.use('/auth', authRoutes);
router.use('/projects', projectRoutes);
router.use('/tasks', taskRoutes);
router.use('/files', fileRoutes);
router.use('/comments', commentRoutes);
router.use('/stats', statsRoutes);
router.use('/events', eventRoutes);

router.get('/health', (req, res) => {
  res.json({ code: 200, message: 'Server is healthy' });
});

export default router;
```

- [ ] **Step 2: 创建日志中间件**

```typescript
// backend/src/middleware/logger.middleware.ts
import { Request, Response, NextFunction } from 'express';

export const requestLogger = (req: Request, res: Response, next: NextFunction): void => {
  const start = Date.now();

  res.on('finish', () => {
    const duration = Date.now() - start;
    console.log(`${req.method} ${req.path} ${res.statusCode} - ${duration}ms`);
  });

  next();
};
```

- [ ] **Step 3: 创建服务器入口**

```typescript
// backend/src/server.ts
import express, { Application } from 'express';
import cors from 'cors';
import helmet from 'helmet';
import rateLimit from 'express-rate-limit';
import { config } from './config';
import { Database } from './config/database';
import routes from './routes';
import { requestLogger } from './middleware/logger.middleware';
import { errorHandler, notFoundHandler } from './middleware/error.middleware';

const app: Application = express();

// 安全中间件
app.use(helmet());

// CORS
app.use(cors({
  origin: process.env.FRONTEND_URL || 'http://localhost:5173',
  credentials: true,
}));

// 限流
app.use(rateLimit({
  windowMs: config.rateLimit.windowMs,
  max: config.rateLimit.max,
}));

// 请求日志
app.use(requestLogger);

// 解析请求体
app.use(express.json({ limit: '10mb' }));
app.use(express.urlencoded({ extended: true, limit: '10mb' }));

// 健康检查
app.get('/health', (req, res) => {
  res.json({ status: 'ok', timestamp: new Date().toISOString() });
});

// API路由
app.use('/api/v1', routes);

// 404处理
app.use(notFoundHandler);

// 错误处理
app.use(errorHandler);

async function startServer() {
  try {
    // 连接数据库
    await Database.connect();

    // 启动服务器
    app.listen(config.port, () => {
      console.log(`
====================================
🚀 Server running on port ${config.port}
📅 Environment: ${config.nodeEnv}
🌐 API: http://localhost:${config.port}/api/v1
====================================
      `);
    });
  } catch (error) {
    console.error('Failed to start server:', error);
    process.exit(1);
  }
}

// 优雅关闭
process.on('SIGTERM', async () => {
  console.log('SIGTERM signal received.');
  await Database.disconnect();
  process.exit(0);
});

process.on('SIGINT', async () => {
  console.log('SIGINT signal received.');
  await Database.disconnect();
  process.exit(0);
});

startServer();
```

- [ ] **Step` 4: 创建Jest配置**

```typescript
// backend/jest.config.ts
module.exports = {
  preset: 'ts-jest',
  testEnvironment: 'node',
  roots: ['<rootDir>/src', '<rootDir>/tests'],
  testMatch: ['**/*.test.ts'],
  collectCoverageFrom: [
    'src/**/*.ts',
    '!src/**/*.d.ts',
  ],
  moduleDirectories: ['node_modules', 'src'],
};
```

- [ ] **Step 5: 更新package.json scripts**

```bash
cd backend && npm pkg set scripts.dev="nodemon src/server.ts" && npm pkg set scripts.build="tsc" && npm pkg set scripts.start="node dist/server.js" && npm pkg set scripts.test="jest" && npm pkg set scripts.test:watch="jest --watch" && npm pkg set scripts.prisma:generate="prisma generate" && npm pkg set scripts.prisma:migrate="prisma migrate dev" && npm pkg set scripts.prisma:studio="prisma studio"
```

- [ ] **Step 6: 提交代码**

Run: `cd .. && git add backend && git commit -m "feat: implement server main entry and route integration"`
Expected: Commit created

---

### Task 17: 创建Docker配置

**Files:**
- Create: `docker/docker-compose.yml`
- Create: `docker/mysql/init.sql`

- [ ] **Step 1: 创建docker-compose.yml**

```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: project_management_mysql
    environment:
      MYSQL_ROOT_PASSWORD: rootpassword
      MYSQL_DATABASE: project_management
      MYSQL_USER: project_user
      MYSQL_PASSWORD: userpassword
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql/init.sql:/docker-entrypoint-initdb.d/init.sql
    healthcheck:
      test: ["CMD", "mysqladmin", "ping", "-h", "localhost"]
      interval: 10s
      timeout: 5s
      retries: 5

  backend:
    build:
      context: ../backend
      dockerfile: ../docker/Dockerfile
    container_name: project_management_backend
    environment:
      NODE_ENV: production
      DATABASE_URL: mysql://project_user:userpassword@mysql:3306/project_management
      JWT_SECRET: ${JWT_SECRET}
      JWT_EXPIRES_IN: 2h
      JWT_REFRESH_EXPIRES_IN: 30d
      WECHAT_APP_ID: ${WECHAT_APP_ID}
      WECHAT_APP_SECRET: ${WECHAT_APP_SECRET}
      WECHAT_REDIRECT_URI: ${WECHAT_REDIRECT_URI}
      DINGTALK_APP_ID: ${DINGTALK_APP_ID}
      DINGTALK_APP_SECRET: ${DINGTALK_APP_SECRET}
      DINGTALK_REDIRECT_URI: ${DINGTALK_REDIRECT_URI}
      MAX_FILE_SIZE: 10485760
      UPLOAD_DIR: /app/uploads
      RATE_LIMIT_WINDOW: 15
      RATE_LIMIT_MAX: 100
    ports:
      - "3000:3000"
    depends_on:
      mysql:
        condition: service_healthy
    volumes:
      - backend_uploads:/app/uploads

volumes:
  mysql_data:
  backend_uploads:
```

- [ ] **Step 2: 创建MySQL初始化脚本**

```sql
-- docker/mysql/init.sql
CREATE DATABASE IF NOT EXISTS project_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

USE project_management;

-- 创建默认角色
INSERT INTO roles (name, description, permissions) VALUES
('ADMIN', '系统管理员', JSON_ARRAY('*')),
('USER', '普通用户', JSON_ARRAY('project:read', 'project:create', 'task:read', 'task:create', 'task:update'));
```

- [ ] **Step 3: 创建Dockerfile**

```dockerfile
# docker/Dockerfile
FROM node:20-alpine

WORKDIR /app

COPY ../backend/package*.json ./

RUN npm ci --only=production

COPY ../backend/tsconfig.json ./
COPY ../backend/src ./src

RUN npm run build

EXPOSE 3000

CMD ["node", "dist/server.js"]
```

- [ ] **Step 4: 提交代码**

Run: `git add docker && git commit -m "feat: add Docker configuration for deployment"`
Expected: Commit created

---

### Task 18: 创建README和文档

**Files:**
- Create: `README.md`
- Create: `docs/deployment.md`

- [ ] **Step 1: 创建项目README**

```markdown
# 项目管理系统

一个轻量级的项目管理系统，支持微信/钉钉登录，适用于10-50人小型团队。

## 功能特性

- ✅ 用户认证（微信/钉钉OAuth2.0）
- ✅ 项目管理（创建、编辑、归档、成员管理）
- ✅ 任务管理（看板、列表、状态跟踪）
- ✅ 团队协作（文件共享、评论、活动流）
- ✅ 统计报表（项目统计、成员贡献）
- ✅ 日程管理（日历视图、会议安排）

## 技术栈

### 后端
- Node.js + Express
- TypeScript
- Prisma ORM
- MySQL 8.0

### 前端
- React
- TypeScript
- Vite

## 快速开始

### 前置要求

- Node.js 20+
- MySQL 8.0+
- npm or yarn

### 安装

```bash
# 克隆仓库
git clone <repository-url>
cd xiangmuguanli

# 后端安装
cd backend
npm install

# 配置环境变量
cp .env.example .env
# 编辑.env文件，填写必要的配置

# 生成Prisma Client
npm run prisma:generate

# 运行数据库迁移
npm run prisma:migrate

# 启动开发服务器
npm run dev
```

### 环境变量配置

参考 `backend/.env.example` 文件，配置以下必要环境变量：

- `DATABASE_URL`: MySQL数据库连接字符串
- `JWT_SECRET`: JWT密钥
- `WECHAT_APP_ID`: 微信开放平台AppID
- `WECHAT_APP_SECRET`: 微信开放平台AppSecret
- `DINGTALK_APP_ID`: 钉钉开放平台AppID
- `DINGTALK_APP_SECRET`: 钉钉开放平台AppSecret

### 使用Docker

```bash
# 启动所有服务
cd docker
docker-compose up -d

# 查看日志
docker-compose logs -f

# 停止服务
docker-compose down
```

## API文档

API基础路径：`http://localhost:3000/api/v1`

### 认证接口

- `GET /auth/wechat/auth-url` - 获取微信登录URL
- `GET /wechat/callback` - 微信OAuth回调
- `GET /auth/dingtalk/auth-url` - 获取钉钉登录URL
- `GET /dingtalk/callback` - 钉钉OAuth回调
- `POST /auth/refresh` - 刷新Token
- `POST /auth/logout` - 登出
- `GET /auth/me` - 获取当前用户信息

### 项目接口

- `GET /projects` - 获取项目列表
- `POST /projects` - 创建项目
- `GET /projects/:id` - 获取项目详情
- `PUT /projects/:id` - 更新项目
- `DELETE /projects/:id` - 删除项目
- `GET /projects/:id/members` - 获取项目成员
- `POST /projects/:id/members` - 添加成员
- `DELETE /projects/:id/members/:userId` - 移除成员

### 任务接口

- `GET /tasks/project/:projectId` - 获取项目任务列表
- `POST /tasks` - 创建任务
- `GET /tasks/:id` - 获取任务详情
- `PUT /tasks/:id` - 更新任务
- `PATCH /tasks/:id/status` - 更新任务状态
- `DELETE /tasks/:id` - 删除任务

## 开发

### 运行测试

```bash
cd backend
npm test
```

### 代码规范

```bash
# 格式化代码
npm run format

# 检查代码规范
npm run lint
```

## 部署

详细部署文档请参考 [docs/deployment.md](docs/deployment.md)

## License

MIT
```

- [ ] **Step 2: 创建部署文档**

```markdown
# 部署文档

本文档提供项目管理系统在不同环境下的部署指南。

## 生产环境部署

### 使用Docker部署

1. **准备环境变量文件**

```bash
cp .env.example .env.production
```

编辑 `.env.production`，填写生产环境配置：

```bash
NODE_ENV=production
DATABASE_URL=mysql://user:password@mysql-host:3306/project_management
JWT_SECRET=your-production-secret-key
WECHAT_APP_ID=your-wechat-app-id
WECHAT_APP_SECRET=your-wechat-app-secret
DINGTALK_APP_ID=your-dingtalk-app-id
DINGTALK_APP_SECRET=your-dingtalk-app-secret
```

2. **使用Docker Compose启动**

```bash
cd docker
docker-compose -f docker-compose.yml --env-file ../.env.production up -d
```

3. **验证部署**

访问健康检查接口：`http://your-domain/health`

### 手动部署

#### 1. 服务器准备

- 操作系统：Ubuntu 20.04+ 或 CentOS 8+
- Node.js 20+
- MySQL 8.0+
- Nginx（可选，用于反向代理）

#### 2. 数据库配置

```bash
# 登录MySQL
mysql -u root -p

# 创建数据库
CREATE DATABASE project_management CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# 创建用户
CREATE USER 'project_user'@'%' IDENTIFIED BY 'secure_password';
GRANT ALL PRIVILEGES ON project_management.* TO 'project_user'@'%';
FLUSH PRIVILEGES;
```

#### 3. 部署后端

```bash
# 上传代码到服务器
scp -r backend user@server:/path/to/

# 安装依赖
cd /path/to/backend
npm ci --only=production

# 配置环境变量
cp .env.example .env
# 编辑.env文件

# 构建项目
npm run build

# 运行数据库迁移
npm run prisma:migrate

# 使用PM2管理进程
npm install -g pm2
pm2 start dist/server.js --name "project-management-backend"
pm2 save
pm2 startup
```

#### 4. 配置Nginx反向代理（可选）

```nginx
server {
    listen 80;
    server_name your-domain.com;

    location / {
        proxy_pass http://localhost:3000;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection 'upgrade';
        proxy_set_header Host $host;
        proxy_cache_bypass $http_upgrade;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```

#### 5. 配置SSL（推荐）

使用Let's Encrypt免费证书：

```bash
sudo apt install certbot python3-certbot-nginx
sudo certbot --nginx -d your-domain.com
```

## 监控和维护

### 日志

```bash
# PM2日志
pm2 logs project-management-backend

# 应用日志
tail -f /path/to/backend/logs/app.log
```

### 数据库备份

```bash
# 备份
mysqldump -u project_user -p project_management > backup_$(date +%Y%m%d).sql

# 恢复
mysql -u project_user -p project_management < backup_20240401.sql
```

### 性能监控

推荐使用以下工具：
- PM2 Plus：进程监控
- Grafana + Prometheus：系统监控
- Sentry：错误追踪

## 故障排查

### 常见问题

1. **数据库连接失败**

检查 `DATABASE_URL` 配置是否正确，确保MySQL服务运行正常。

2. **端口被占用**

修改 `backend/.env` 中的 `PORT` 配置。

3. **OAuth登录失败**

检查微信/钉钉AppID和AppSecret配置，确保回调URL配置正确。

## 安全建议

1. 定期更新依赖包
2. 使用强密码和密钥
3. 启用HTTPS
4. 配置防火墙规则
5. 定期备份数据
6. 限制数据库远程访问
