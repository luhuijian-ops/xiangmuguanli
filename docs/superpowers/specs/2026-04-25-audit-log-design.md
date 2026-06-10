# 审计日志设计方案

## 1. 概述

实现完整的审计日志系统，包括：
- 登录日志记录（已有 LoginLog）
- 所有操作的审计日志（复用 Activity 表）
- 异常行为告警（失败登录超阈值）

## 2. 现状分析

### 已有结构
- **LoginLog** 模型 - 记录用户登录日志（userId, ip, device, success, platform, createdAt）
- **Activity** 模型 - 记录项目活动（userId, action, entityType, entityId, projectId, metadata, createdAt）

### 缺失部分
- 没有统一的审计日志服务
- 没有异常检测和告警机制
- 没有前端审计日志查看页面

## 3. 数据模型

### 3.1 新增 Alert 模型

```prisma
model Alert {
  id         BigInt     @id @default(autoincrement())
  type       AlertType
  severity   Severity
  userId     BigInt?
  targetId   BigInt?
  targetType String?
  message    String
  metadata   String?
  resolved   Boolean    @default(false)
  createdAt  DateTime   @default(now())

  user User? @relation(fields: [userId], references: [id])

  @@index([type, createdAt])
  @@index([resolved, createdAt])
}

enum AlertType {
  LOGIN_FAILURE    // 登录失败
  SUSPICIOUS_ACTIVITY  // 可疑活动
  PERMISSION_DENIED    // 权限拒绝
}

enum Severity {
  LOW
  MEDIUM
  HIGH
  CRITICAL
}
```

### 3.2 复用 Activity 模型

Activity 表结构已足够记录审计日志：
- `userId` - 执行操作的用户
- `action` - 操作类型（如 "CREATE", "UPDATE", "DELETE"）
- `entityType` - 实体类型（如 "Task", "Project", "Comment"）
- `entityId` - 实体 ID
- `projectId` - 关联项目（可选）
- `metadata` - 操作的详细数据（JSON 格式）
- `createdAt` - 操作时间

## 4. 后端实现

### 4.1 审计日志服务

创建 `backend/src/services/audit.service.ts`：

```typescript
interface AuditLogParams {
  userId: string
  action: string
  entityType: string
  entityId?: string
  projectId?: string
  metadata?: Record<string, any>
  ip?: string
}

async logActivity(params: AuditLogParams): Promise<void>
async getAuditLogs(filters: AuditLogFilters): Promise<{ logs: any[], total: number }>
```

### 4.2 告警服务

创建 `backend/src/services/alert.service.ts`：

```typescript
interface AlertParams {
  type: AlertType
  severity: Severity
  userId?: string
  targetId?: string
  targetType?: string
  message: string
  metadata?: Record<string, any>
}

async createAlert(params: AlertParams): Promise<void>
async checkFailedLogins(userId?: string): Promise<Alert | null>
async getAlerts(filters: AlertFilters): Promise<{ alerts: any[], total: number }>
async resolveAlert(alertId: string): Promise<boolean>
```

### 4.3 集成点

在以下服务中集成审计日志：

| 服务 | 操作 | action |
|------|------|--------|
| auth.service.ts | 登录成功/失败 | LOGIN_SUCCESS, LOGIN_FAILURE |
| auth.service.ts | 登出 | LOGOUT |
| task.service.ts | 创建任务 | TASK_CREATE |
| task.service.ts | 更新任务 | TASK_UPDATE |
| task.service.ts | 删除任务 | TASK_DELETE |
| project.service.ts | 创建项目 | PROJECT_CREATE |
| project.service.ts | 更新项目 | PROJECT_UPDATE |
| project.service.ts | 删除项目 | PROJECT_DELETE |
| comment.service.ts | 创建评论 | COMMENT_CREATE |
| file.service.ts | 上传文件 | FILE_UPLOAD |

## 5. 前端实现

### 5.1 新增页面

- `frontend/src/pages/audit/AuditLog.vue` - 审计日志页面
- `frontend/src/pages/audit/AlertList.vue` - 告警列表页面

### 5.2 路由配置

```typescript
{
  path: 'audit',
  name: 'AuditManagement',
  component: () => import('@/pages/audit/Index.vue'),
  meta: { requiresAdmin: true },
  children: [
    { path: 'logs', name: 'AuditLogs', component: () => import('@/pages/audit/AuditLog.vue') },
    { path: 'alerts', name: 'AlertList', component: () => import('@/pages/audit/AlertList.vue') }
  ]
}
```

### 5.3 API

```typescript
// 获取审计日志
GET /api/v1/audit/logs?page=1&pageSize=20&userId=&entityType=&action=&startDate=&endDate=

// 获取告警列表
GET /api/v1/audit/alerts?page=1&pageSize=20&type=&severity=&resolved=

// 标记告警为已处理
POST /api/v1/audit/alerts/:id/resolve
```

## 6. 异常检测规则

### 6.1 失败登录检测

- 触发条件：同一用户在 15 分钟内连续失败登录超过 5 次
- 告警级别：CRITICAL
- 告警类型：LOGIN_FAILURE

### 6.2 检测流程

```typescript
async checkFailedLogins(userId?: string) {
  const threshold = 5
  const windowMinutes = 15

  const failedLogins = await prisma.loginLog.count({
    where: {
      success: false,
      createdAt: { gte: new Date(Date.now() - windowMinutes * 60 * 1000) },
      ...(userId ? { userId: BigInt(userId) } : {})
    }
  })

  if (failedLogins >= threshold) {
    await this.createAlert({
      type: 'LOGIN_FAILURE',
      severity: 'CRITICAL',
      userId,
      message: `用户在15分钟内连续失败登录${failedLogins}次`
    })
  }
}
```

## 7. 实现步骤

### Phase 1: 数据模型
1. 创建 Alert 模型
2. 执行数据库迁移

### Phase 2: 后端服务
1. 创建 audit.service.ts
2. 创建 alert.service.ts
3. 集成到各业务服务

### Phase 3: API
1. 添加审计日志 API 路由
2. 添加告警 API 路由

### Phase 4: 前端
1. 创建审计日志页面
2. 创建告警列表页面
3. 添加路由和菜单入口
