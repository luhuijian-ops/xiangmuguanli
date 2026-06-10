# 项目管理系统需求设计方案

## 一、项目概述

**项目名称**：轻量级项目管理系统
**目标用户**：10-50人小型团队/创业公司
**核心场景**：项目全生命周期管理
**技术方案**：低代码平台 + MySQL自建数据库 + 微信/钉钉登录集成

---

## 二、系统架构

### 2.1 总体架构

```
┌─────────────────────────────────────────┐
│           前端应用层                      │
│  低代码平台 + 自定义组件 + 拖拽式配置    │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│           API网关层                      │
│  统一认证 + 权限校验 + 接口路由         │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│           业务服务层                      │
│  任务管理 | 项目规划 | 协作 | 统计 | 日程 │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│           数据访问层                      │
│  ORM/SQL封装 + 缓存 + 事务管理          │
└────────────┬────────────────────────────┘
             │
┌────────────▼────────────────────────────┐
│           数据存储层                      │
│  MySQL (核心数据) + 文件存储            │
└─────────────────────────────────────────┘
```

### 2.2 外部集成（微信/钉钉登录）

```
┌──────────────┐    ┌──────────────┐
│  微信开放平台  │    │  钉钉开放平台  │
│  (扫码/授权)  │    │  (扫码/授权)  │
└──────┬───────┘    └──────┬───────┘
       │                    │
       └────────┬───────────┘
                │
┌───────────────▼────────────────┐
│        统一认证服务             │
│  - OAuth2.0标准协议            │
│  - 用户信息同步                │
│  - Token管理                   │
└──────────────┬────────────────┘
               │
┌──────────────▼────────────────┐
│        本地用户数据库          │
│  - 用户信息表                  │
│  - 绑定关系表                  │
│  - 登录日志                    │
└───────────────────────────────┘
```

---

## 三、核心功能模块

### 3.1 任务管理模块

**功能清单：**
- 任务看板（Kanban视图）
- 任务列表（列表视图）
- 任务创建/编辑/删除
- 任务分配（分配给成员）
- 优先级设置（P0-P4）
- 截止日期管理
- 任务标签/分类
- 任务关联（父子任务、依赖任务）
- 任务附件管理
- 任务评论与@提及

**数据结构：**
```sql
CREATE TABLE tasks (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  project_id BIGINT NOT NULL,
  assigned_to BIGINT,
  created_by BIGINT NOT NULL,
  status ENUM('todo', 'doing', 'done', 'archived') DEFAULT 'todo',
  priority INT DEFAULT 3 COMMENT '0-4, 0最高',
  due_date DATETIME,
  start_date DATETIME,
  tags JSON,
  attachments JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_project (project_id),
  INDEX idx_assigned (assigned_to),
  INDEX idx_status (status),
  INDEX idx_due_date (due_date),
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (assigned_to) REFERENCES users(id),
  FOREIGN KEY (created_by) REFERENCES users(id)
);
```

### 3.2 项目规划模块

**功能清单：**
- 项目创建/归档/删除
- 甘特图视图
- 里程碑管理
- 项目成员管理
- 项目模板
- 项目阶段划分
- 工期估算

**数据结构：**
```sql
CREATE TABLE projects (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  code VARCHAR(50) UNIQUE NOT NULL,
  owner_id BIGINT NOT NULL,
  status ENUM('active', 'archived', 'deleted') DEFAULT 'active',
  start_date DATE,
  end_date DATE,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_owner (owner_id),
  INDEX idx_status (status),
  FOREIGN KEY (owner_id) REFERENCES users(id)
);

CREATE TABLE milestones (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  name VARCHAR(255) NOT NULL,
  description TEXT,
  due_date DATETIME NOT NULL,
  status ENUM('pending', 'completed') DEFAULT 'pending',
  order_index INT DEFAULT 0,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE project_members (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  project_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  role ENUM('owner', 'admin', 'member', 'viewer') DEFAULT 'member',
  joined_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_project_user (project_id, user_id),
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 3.3 团队协作模块

**功能清单：**
- 文件共享（上传/下载/预览）
- 评论系统（任务/项目评论）
- @提及功能（@成员收到通知）
- 活动流（动态时间线）
- 消息通知（站内信/邮件）
- 版本历史记录

**数据结构：**
```sql
CREATE TABLE files (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255) NOT NULL,
  path VARCHAR(500) NOT NULL,
  size BIGINT NOT NULL,
  type VARCHAR(100) NOT NULL,
  uploaded_by BIGINT NOT NULL,
  task_id BIGINT,
  project_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (uploaded_by) REFERENCES users(id),
  FOREIGN KEY (task_id) REFERENCES tasks(id),
  FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE comments (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  content TEXT NOT NULL,
  entity_type VARCHAR(50) NOT NULL COMMENT 'task/project/file',
  entity_id BIGINT NOT NULL,
  user_id BIGINT NOT NULL,
  parent_id BIGINT,
  mentions JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_entity (entity_type, entity_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (parent_id) REFERENCES comments(id)
);

CREATE TABLE activities (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  action VARCHAR(100) NOT NULL,
  entity_type VARCHAR(50) NOT NULL,
  entity_id BIGINT NOT NULL,
  project_id BIGINT,
  metadata JSON,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_project (project_id),
  INDEX idx_user (user_id),
  INDEX idx_created (created_at),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

### 3.4 统计报表模块

**功能清单：**
- 项目进度统计
- 成员贡献分析
- 任务完成率
- 工时统计
- 可视化图表（折线图/饼图/柱状图）
- 自定义报表

**数据结构：**
```sql
CREATE TABLE work_hours (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  task_id BIGINT,
  project_id BIGINT,
  hours DECIMAL(5,2) NOT NULL,
  date DATE NOT NULL,
  description TEXT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_date (user_id, date),
  INDEX idx_project (project_id),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (task_id) REFERENCES tasks(id),
  FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE stats_cache (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  cache_key VARCHAR(255) UNIQUE NOT NULL,
  data JSON NOT NULL,
  expired_at DATETIME NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

### 3.5 日程管理模块

**功能清单：**
- 日历视图（月/周/日）
- 会议安排
- 任务截止日期展示
- 提醒通知
- 日程导入/导出（iCal）

**数据结构：**
```sql
CREATE TABLE events (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255) NOT NULL,
  description TEXT,
  location VARCHAR(255),
  user_id BIGINT NOT NULL,
  project_id BIGINT,
  task_id BIGINT,
  start_time DATETIME NOT NULL,
  end_time DATETIME NOT NULL,
  all_day BOOLEAN DEFAULT FALSE,
  reminder_minutes INT DEFAULT 0,
  repeat_rule VARCHAR(100),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_user_time (user_id, start_time),
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (project_id) REFERENCES projects(id),
  FOREIGN KEY (task_id) REFERENCES tasks(id)
);
```

### 3.6 认证授权模块（微信/钉钉）

**功能清单：**
- 微信扫码登录
- 钉钉扫码登录
- 用户信息绑定
- 权限管理（RBAC）
- Token管理（JWT）
- 登录日志

**数据结构：**
```sql
CREATE TABLE users (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(50) UNIQUE,
  email VARCHAR(100) UNIQUE,
  avatar VARCHAR(500),
  name VARCHAR(100) NOT NULL,
  phone VARCHAR(20),
  status ENUM('active', 'inactive', 'deleted') DEFAULT 'active',
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  INDEX idx_email (email),
  INDEX idx_phone (phone)
);

CREATE TABLE user_bindings (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  platform ENUM('wechat', 'dingtalk') NOT NULL,
  open_id VARCHAR(255) NOT NULL,
  union_id VARCHAR(255),
  nickname VARCHAR(100),
  avatar VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  UNIQUE KEY uk_platform_openid (platform, open_id),
  FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(255),
  permissions JSON NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE user_roles (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  role_id BIGINT NOT NULL,
  project_id BIGINT,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  FOREIGN KEY (user_id) REFERENCES users(id),
  FOREIGN KEY (role_id) REFERENCES roles(id),
  FOREIGN KEY (project_id) REFERENCES projects(id)
);

CREATE TABLE login_logs (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  ip VARCHAR(45),
  device VARCHAR(255),
  success BOOLEAN DEFAULT TRUE,
  platform VARCHAR(50),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  INDEX idx_user (user_id),
  INDEX idx_created (created_at),
  FOREIGN KEY (user_id) REFERENCES users(id)
);
```

---

## 四、数据模型设计

### 4.1 核心ER图关系

```
┌─────────┐       ┌─────────┐       ┌─────────┐
│ users   │───────│projects │───────│ tasks   │
└─────────┘ 1:N  └─────────┘ 1:N  └─────────┘
                    │
                    ├─────────┐
                    │         │
            ┌───────▼────┐  ┌▼────────┐
            │milestones  │  │ files   │
            └────────────┘  └─────────┘

┌─────────┐       ┌─────────┐       ┌─────────┐
│ users   │───────│comments │───────│ tasks   │
└─────────┘ 1:N  └─────────┘  N:1  └─────────┘

┌─────────┐       ┌─────────┐
│ users   │───────│user_bindings│
└─────────┘ 1:N  └────────────┘
```

---

## 五、API设计规范

### 5.1 RESTful API规范

```
基础路径：/api/v1

用户认证：
POST   /auth/wechat/login     - 微信登录
POST   /auth/dingtalk/login    - 钉钉登录
POST   /auth/refresh          - 刷新Token
POST   /auth/logout           - 登出

项目管理：
GET    /projects              - 项目列表
POST   /projects              - 创建项目
GET    /projects/:id          - 项目详情
PUT    /projects/:id          - 更新项目
DELETE /projects/:id          - 删除项目
GET    /projects/:id/members  - 成员列表
POST   /projects/:id/members  - 添加成员
DELETE /projects/:id/members/:userId - 移除成员

任务管理：
GET    /projects/:id/tasks    - 任务列表
POST   /projects/:id/tasks    - 创建任务
GET    /tasks/:id             - 任务详情
PUT    /tasks/:id             - 更新任务
DELETE /tasks/:id             - 删除任务
PATCH  /tasks/:id/status      - 更新状态
GET    /tasks/:id/comments    - 任务评论
POST   /tasks/:id/comments    - 添加评论

文件管理：
POST   /files/upload          - 上传文件
GET    /files/:id             - 下载文件
DELETE /files/:id             - 删除文件

统计报表：
GET    /stats/project/:id     - 项目统计
GET    /stats/member/:id      - 成员统计
GET    /stats/tasks           - 任务统计

日程管理：
GET    /events                - 日程列表
POST   /events                - 创建日程
GET    /events/:id            - 日程详情
PUT    /events/:id            - 更新日程
DELETE /events/:id            - 删除日程
```

### 5.2 响应格式

```json
// 成功响应
{
  "code": 200,
  "message": "success",
  "data": {}
}

// 错误响应
{
  "code": 400,
  "message": "参数错误",
  "errors": []
}

// 分页响应
{
  "code": 200,
  "message": "success",
  "data": {
    "list": [],
    "total": 100,
    "page": 1,
    "pageSize": 20
  }
}
```

---

## 六、安全设计

### 6.1 认证机制
- JWT Token认证
- Token有效期：2小时
- Refresh Token有效期：30天
- Token黑名单机制

### 6.2 权限控制
- RBAC角色权限模型
- 项目级权限隔离
- 操作权限校验中间件

### 6.3 数据安全
- 敏感信息加密存储
- SQL注入防护
- XSS攻击防护
- CSRF Token验证

### 6.4 审计日志
- 登录日志记录
- 敏感操作审计
- 异常行为告警

---

## 七、技术选型

### 7.1 前端技术
- **低代码平台**：Budibase / Appsmith
- **UI组件库**：Ant Design / Element Plus
- **图表库**：ECharts
- **状态管理**：Pinia / Zustand

### 7.2 后端技术
- **开发语言**：Node.js / Python
- **Web框架**：Express / FastAPI
- **ORM**：Prisma / SQLAlchemy
- **认证库**：Passport.js / FastAPI Security

### 7.3 数据库
- **主数据库**：MySQL 8.0
- **缓存**：Redis（可选）
- **文件存储**：本地存储 / 阿里云OSS

### 7.4 第三方集成
- **微信开放平台**：OAuth2.0
- **钉钉开放平台**：OAuth2.0
- **消息通知**：邮件 / 站内信

---

## 八、部署架构

```
┌─────────────────────────────────────┐
│           负载均衡（可选）            │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│           应用服务器集群              │
│  ┌─────┐  ┌─────┐  ┌─────┐        │
│  │App1 │  │App2 │  │App3 │        │
│  └─────┘  └─────┘  └─────┘        │
└────────────┬────────────────────────┘
             │
┌────────────▼────────────────────────┐
│           MySQL主从架构              │
│  ┌─────┐        ┌─────┐            │
│  │Master│──复制──▶│Slave │        │
│  └─────┘        └─────┘            │
└─────────────────────────────────────┘
```

---

## 九、开发计划

### 9.1 第一阶段（核心功能）- 2个月
- 用户认证（微信/钉钉）
- 项目管理基础功能
- 任务管理基础功能
- 基础权限控制

### 9.2 第二阶段（协作功能）- 1个月
- 文件上传与管理
- 评论系统
- 活动流
- 消息通知

### 9.3 第三阶段（高级功能）- 1个月
- 甘特图/里程碑
- 统计报表
- 日程管理
- 数据导出

### 9.4 第四阶段（优化完善）- 1个月
- 性能优化
- 用户体验优化
- 文档完善
- 测试与上线

**总计：5个月**

---

## 十、非功能性需求

### 10.1 性能要求
- 页面加载时间 < 2秒
- API响应时间 < 500ms
- 支持并发用户 > 1000

### 10.2 可用性要求
- 系统可用性 > 99.9%
- 数据备份频率：每日全量 + 每小时增量

### 10.3 扩展性要求
- 支持水平扩展
- 模块化设计，易于新增功能

### 10.4 兼容性要求
- 支持Chrome/Edge/Safari最新版本
- 支持移动端响应式布局

---

## 十一、验收标准

### 11.1 功能验收
- 所有核心功能模块正常运行
- 微信/钉钉登录功能稳定
- 数据统计准确无误

### 11.2 性能验收
- 页面加载时间符合要求
- API响应时间符合要求
- 并发测试通过

### 11.3 安全验收
- 安全扫描无高危漏洞
- 渗透测试通过
- 审计日志完整

---

**文档版本**：v1.0
**创建日期**：2026-04-23
**文档状态**：已确认
