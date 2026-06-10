# 审计日志实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现完整的审计日志系统，包括登录日志、全部操作审计日志、异常行为告警

**Architecture:** 后端新增审计服务和告警服务，集成到各业务服务；前端新增审计日志和告警管理页面

**Tech Stack:** Node.js/Express/TypeScript 后端，Vue 3/Pinia 前端，Prisma ORM

---

## 文件结构

### 后端
- Modify: `backend/src/models/schema.prisma` - 添加 Alert 模型
- Create: `backend/src/services/audit.service.ts` - 审计日志服务
- Create: `backend/src/services/alert.service.ts` - 告警服务
- Create: `backend/src/controllers/audit.controller.ts` - 审计 API 控制器
- Create: `backend/src/routes/audit.routes.ts` - 审计路由
- Modify: `backend/src/services/auth.service.ts` - 集成审计日志
- Modify: `backend/src/services/task.service.ts` - 集成审计日志
- Modify: `backend/src/services/project.service.ts` - 集成审计日志

### 前端
- Create: `frontend/src/pages/audit/AuditLog.vue` - 审计日志页面
- Create: `frontend/src/pages/audit/AlertList.vue` - 告警列表页面
- Modify: `frontend/src/api/index.ts` - 添加审计 API
- Modify: `frontend/src/router/index.ts` - 添加审计路由
- Modify: `frontend/src/layouts/MainLayout.vue` - 添加审计菜单入口

---

## Chunk 1: 数据模型

### Task 1: 添加 Alert 模型到 Prisma Schema

**Files:**
- Modify: `backend/src/models/schema.prisma`

- [ ] **Step 1: 在 schema.prisma 末尾添加 Alert 模型和枚举**

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
  LOGIN_FAILURE
  SUSPICIOUS_ACTIVITY
  PERMISSION_DENIED
}

enum Severity {
  LOW
  MEDIUM
  HIGH
  CRITICAL
}
```

- [ ] **Step 2: 执行 Prisma 迁移**

Run: `cd backend && npx prisma migrate dev --name add_alert_model`

---

## Chunk 2: 后端服务

### Task 2: 创建审计日志服务

**Files:**
- Create: `backend/src/services/audit.service.ts`

- [ ] **Step 1: 创建 audit.service.ts**

```typescript
import { prisma } from '../config/database';

export interface AuditLogParams {
  userId: string;
  action: string;
  entityType: string;
  entityId?: string;
  projectId?: string;
  metadata?: Record<string, any>;
  ip?: string;
}

export interface AuditLogFilters {
  userId?: string;
  entityType?: string;
  action?: string;
  projectId?: string;
  startDate?: Date;
  endDate?: Date;
  page?: number;
  pageSize?: number;
}

export class AuditService {
  async logActivity(params: AuditLogParams): Promise<void> {
    const { userId, action, entityType, entityId, projectId, metadata, ip } = params;

    await prisma.activity.create({
      data: {
        userId: BigInt(userId),
        action,
        entityType,
        entityId: entityId ? BigInt(entityId) : BigInt(0),
        projectId: projectId ? BigInt(projectId) : BigInt(0),
        metadata: metadata ? JSON.stringify(metadata) : null,
      },
    });
  }

  async getAuditLogs(filters: AuditLogFilters): Promise<{ logs: any[], total: number }> {
    const {
      userId,
      entityType,
      action,
      projectId,
      startDate,
      endDate,
      page = 1,
      pageSize = 20,
    } = filters;

    const where: any = {};

    if (userId) {
      where.userId = BigInt(userId);
    }
    if (entityType) {
      where.entityType = entityType;
    }
    if (action) {
      where.action = action;
    }
    if (projectId) {
      where.projectId = BigInt(projectId);
    }
    if (startDate || endDate) {
      where.createdAt = {};
      if (startDate) {
        where.createdAt.gte = startDate;
      }
      if (endDate) {
        where.createdAt.lte = endDate;
      }
    }

    const [logs, total] = await prisma.$transaction([
      prisma.activity.findMany({
        where,
        include: {
          user: {
            select: {
              id: true,
              name: true,
              username: true,
            },
          },
        },
        orderBy: { createdAt: 'desc' },
        skip: (page - 1) * pageSize,
        take: pageSize,
      }),
      prisma.activity.count({ where }),
    ]);

    const formattedLogs = logs.map((log) => ({
      id: log.id.toString(),
      userId: log.userId.toString(),
      userName: log.user?.name || log.user?.username || '未知',
      action: log.action,
      entityType: log.entityType,
      entityId: log.entityId.toString(),
      projectId: log.projectId?.toString(),
      metadata: log.metadata ? JSON.parse(log.metadata) : null,
      createdAt: log.createdAt,
    }));

    return { logs: formattedLogs, total };
  }
}
```

### Task 3: 创建告警服务

**Files:**
- Create: `backend/src/services/alert.service.ts`

- [ ] **Step 1: 创建 alert.service.ts**

```typescript
import { prisma } from '../config/database';
import { AlertType, Severity } from '@prisma/client';

export interface AlertParams {
  type: AlertType;
  severity: Severity;
  userId?: string;
  targetId?: string;
  targetType?: string;
  message: string;
  metadata?: Record<string, any>;
}

export interface AlertFilters {
  type?: AlertType;
  severity?: Severity;
  resolved?: boolean;
  userId?: string;
  page?: number;
  pageSize?: number;
}

export class AlertService {
  private readonly FAILED_LOGIN_THRESHOLD = 5;
  private readonly FAILED_LOGIN_WINDOW_MINUTES = 15;

  async createAlert(params: AlertParams): Promise<void> {
    const { type, severity, userId, targetId, targetType, message, metadata } = params;

    await prisma.alert.create({
      data: {
        type,
        severity,
        userId: userId ? BigInt(userId) : BigInt(0),
        targetId: targetId ? BigInt(targetId) : BigInt(0),
        targetType,
        message,
        metadata: metadata ? JSON.stringify(metadata) : null,
      },
    });
  }

  async checkFailedLogins(userId?: string): Promise<any | null> {
    const windowStart = new Date(
      Date.now() - this.FAILED_LOGIN_WINDOW_MINUTES * 60 * 1000
    );

    const failedLogins = await prisma.loginLog.count({
      where: {
        success: false,
        createdAt: {
          gte: windowStart,
        },
        ...(userId ? { userId: BigInt(userId) } : {}),
      },
    });

    if (failedLogins >= this.FAILED_LOGIN_THRESHOLD) {
      // 检查是否已经存在相同的未处理告警
      const existingAlert = await prisma.alert.findFirst({
        where: {
          type: 'LOGIN_FAILURE',
          resolved: false,
          createdAt: {
            gte: windowStart,
          },
          ...(userId ? { userId: BigInt(userId) } : {}),
        },
      });

      if (!existingAlert) {
        await this.createAlert({
          type: 'LOGIN_FAILURE',
          severity: 'CRITICAL',
          userId,
          message: `用户在${this.FAILED_LOGIN_WINDOW_MINUTES}分钟内连续失败登录${failedLogins}次`,
          metadata: {
            failedCount: failedLogins,
            windowMinutes: this.FAILED_LOGIN_WINDOW_MINUTES,
            threshold: this.FAILED_LOGIN_THRESHOLD,
          },
        });
      }
    }

    return null;
  }

  async getAlerts(filters: AlertFilters): Promise<{ alerts: any[], total: number }> {
    const { type, severity, resolved, userId, page = 1, pageSize = 20 } = filters;

    const where: any = {};

    if (type) {
      where.type = type;
    }
    if (severity) {
      where.severity = severity;
    }
    if (resolved !== undefined) {
      where.resolved = resolved;
    }
    if (userId) {
      where.userId = BigInt(userId);
    }

    const [alerts, total] = await prisma.$transaction([
      prisma.alert.findMany({
        where,
        include: {
          user: {
            select: {
              id: true,
              name: true,
              username: true,
            },
          },
        },
        orderBy: [{ severity: 'desc' }, { createdAt: 'desc' }],
        skip: (page - 1) * pageSize,
        take: pageSize,
      }),
      prisma.alert.count({ where }),
    ]);

    const formattedAlerts = alerts.map((alert) => ({
      id: alert.id.toString(),
      type: alert.type,
      severity: alert.severity,
      userId: alert.userId?.toString(),
      userName: alert.user?.name || alert.user?.username || '未知',
      targetId: alert.targetId?.toString(),
      targetType: alert.targetType,
      message: alert.message,
      metadata: alert.metadata ? JSON.parse(alert.metadata) : null,
      resolved: alert.resolved,
      createdAt: alert.createdAt,
    }));

    return { alerts: formattedAlerts, total };
  }

  async resolveAlert(alertId: string): Promise<boolean> {
    try {
      await prisma.alert.update({
        where: { id: BigInt(alertId) },
        data: { resolved: true },
      });
      return true;
    } catch {
      return false;
    }
  }
}
```

### Task 4: 创建审计控制器和路由

**Files:**
- Create: `backend/src/controllers/audit.controller.ts`
- Create: `backend/src/routes/audit.routes.ts`

- [ ] **Step 1: 创建 audit.controller.ts**

```typescript
import { Request, Response } from 'express';
import { AuditService, AuditLogFilters } from '../services/audit.service';
import { AlertService, AlertFilters } from '../services/alert.service';
import { AppError } from '../middleware/error.middleware';

export class AuditController {
  private auditService = new AuditService();
  private alertService = new AlertService();

  async getAuditLogs(req: Request, res: Response): Promise<void> {
    try {
      const { userId, entityType, action, projectId, startDate, endDate, page, pageSize } = req.query;

      const filters: AuditLogFilters = {
        userId: userId as string,
        entityType: entityType as string,
        action: action as string,
        projectId: projectId as string,
        startDate: startDate ? new Date(startDate as string) : undefined,
        endDate: endDate ? new Date(endDate as string) : undefined,
        page: parseInt(page as string) || 1,
        pageSize: parseInt(pageSize as string) || 20,
      };

      const result = await this.auditService.getAuditLogs(filters);

      res.json({
        code: 200,
        message: 'success',
        data: {
          logs: result.logs,
          total: result.total,
          page: filters.page,
          pageSize: filters.pageSize,
        },
      });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  async getAlerts(req: Request, res: Response): Promise<void> {
    try {
      const { type, severity, resolved, userId, page, pageSize } = req.query;

      const filters: AlertFilters = {
        type: type as any,
        severity: severity as any,
        resolved: resolved === 'true' ? true : resolved === 'false' ? false : undefined,
        userId: userId as string,
        page: parseInt(page as string) || 1,
        pageSize: parseInt(pageSize as string) || 20,
      };

      const result = await this.alertService.getAlerts(filters);

      res.json({
        code: 200,
        message: 'success',
        data: {
          alerts: result.alerts,
          total: result.total,
          page: filters.page,
          pageSize: filters.pageSize,
        },
      });
    } catch (error) {
      res.status(500).json({
        code: 500,
        message: '服务器错误',
      });
    }
  }

  async resolveAlert(req: Request, res: Response): Promise<void> {
    try {
      const { id } = req.params;

      const result = await this.alertService.resolveAlert(id);

      if (!result) {
        throw new AppError(400, '告警不存在或已处理');
      }

      res.json({
        code: 200,
        message: 'success',
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
}
```

- [ ] **Step 2: 创建 audit.routes.ts**

```typescript
import { Router } from 'express';
import { AuditController } from '../controllers/audit.controller';
import { authenticate } from '../middleware/auth.middleware';
import { requireAdmin } from '../middleware/auth.middleware';

const router = Router();
const auditController = new AuditController();

// 获取审计日志
router.get('/logs', authenticate, requireAdmin, auditController.getAuditLogs.bind(auditController));

// 获取告警列表
router.get('/alerts', authenticate, requireAdmin, auditController.getAlerts.bind(auditController));

// 标记告警为已处理
router.post('/alerts/:id/resolve', authenticate, requireAdmin, auditController.resolveAlert.bind(auditController));

export default router;
```

- [ ] **Step 3: 在 index.ts 中注册路由**

Modify `backend/src/index.ts`，在路由部分添加：

```typescript
import auditRoutes from './routes/audit.routes';

// 在其他 app.use 之后添加：
app.use('/api/v1/audit', auditRoutes);
```

### Task 5: 集成审计日志到 auth.service.ts

**Files:**
- Modify: `backend/src/services/auth.service.ts`

- [ ] **Step 1: 在登录成功后调用审计日志**

在 `loginWithUsernamePassword` 方法中，登录成功返回前添加：

```typescript
// 记录审计日志
await this.logAudit(user.id.toString(), 'LOGIN_SUCCESS', 'User', user.id.toString(), undefined, { username }, req?.ip);
```

在登录失败后添加：

```typescript
// 检查是否触发告警
await this.alertService.checkFailedLogins(user?.id.toString());
```

需要：
1. 导入 AuditService 和 AlertService
2. 在类中初始化这两个服务
3. 添加 logAudit 辅助方法

### Task 6: 集成审计日志到 task.service.ts

**Files:**
- Modify: `backend/src/services/task.service.ts`

- [ ] **Step 1: 在任务创建、更新、删除时调用审计日志**

在 `createTask` 成功后：
```typescript
await this.auditService.logActivity({
  userId: createdBy.toString(),
  action: 'TASK_CREATE',
  entityType: 'Task',
  entityId: task.id.toString(),
  projectId: projectId.toString(),
  metadata: { title: task.title },
});
```

---

## Chunk 3: 前端实现

### Task 7: 添加审计 API

**Files:**
- Modify: `frontend/src/api/index.ts`

- [ ] **Step 1: 在 api/index.ts 中添加审计 API**

```typescript
export const auditApi = {
  getAuditLogs: (params: any) => request.get('/api/v1/audit/logs', { params }),
  getAlerts: (params: any) => request.get('/api/v1/audit/alerts', { params }),
  resolveAlert: (id: string) => request.post(`/api/v1/audit/alerts/${id}/resolve`),
};
```

### Task 8: 创建审计日志页面

**Files:**
- Create: `frontend/src/pages/audit/AuditLog.vue`

- [ ] **Step 1: 创建 AuditLog.vue**

创建完整的审计日志页面，包含：
- 时间范围筛选
- 用户筛选
- 操作类型筛选
- 实体类型筛选
- 分页表格显示

### Task 9: 创建告警列表页面

**Files:**
- Create: `frontend/src/pages/audit/AlertList.vue`

- [ ] **Step 1: 创建 AlertList.vue**

创建完整的告警列表页面，包含：
- 按类型、严重程度、已解决状态筛选
- 分页表格
- 标记为已处理按钮

### Task 10: 添加路由和菜单

**Files:**
- Modify: `frontend/src/router/index.ts`
- Modify: `frontend/src/layouts/MainLayout.vue`

- [ ] **Step 1: 在路由中添加审计页面**

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

- [ ] **Step 2: 在 MainLayout.vue 侧边栏添加审计菜单**

在用户管理菜单后添加：

```vue
<router-link v-if="userStore.isAdmin" to="/audit/logs" class="nav-item">
  <span>📋</span>
  <span>审计日志</span>
</router-link>
```

---

## 验证步骤

1. 重新编译后端：`npm run build`
2. 执行数据库迁移：`npx prisma migrate dev --name add_alert_model`
3. 重启后端服务器
4. 使用 admin 登录前端
5. 访问审计日志页面验证数据
6. 尝试失败登录 5 次以上，验证告警触发

