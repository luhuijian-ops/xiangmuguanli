# 权限管理设计方案

## 1. 概述

实现基于项目成员身份的项目级权限控制。用户只能看到自己参与的项目菜单，后端接口验证项目成员身份。

## 2. 现状分析

### 已有结构
- `ProjectMember` 模型 - 用户与项目关联（userId, projectId, role）
- `MemberRole` 枚举 - OWNER/ADMIN/MEMBER/VIEWER
- 用户管理菜单仅对 `isAdmin=true` 的用户可见

### 问题
- 侧边栏"项目"菜单为静态路由，所有登录用户都能看到
- 没有根据用户参与的项目动态显示菜单

## 3. 数据模型

### 复用现有结构
```prisma
model ProjectMember {
  id        BigInt         @id @default(autoincrement())
  projectId BigInt
  userId    BigInt
  role      MemberRole     @default(MEMBER)
  joinedAt  DateTime       @default(now())

  project Project @relation(fields: [projectId], references: [id])
  user    User    @relation(fields: [userId], references: [id])

  @@unique([projectId, userId])
}

enum MemberRole {
  OWNER    // 项目所有者 - 完全控制
  ADMIN    // 项目管理员 - 管理项目设置和成员
  MEMBER   // 普通成员 - 查看和操作任务
  VIEWER   // 访客 - 仅查看
}
```

## 4. 后端实现

### 4.1 新增 API

**获取当前用户参与的项目列表**

```
GET /api/v1/users/me/projects
```

响应：
```json
{
  "code": 200,
  "data": [
    {
      "id": "1",
      "name": "项目A",
      "code": "PROJECT_A",
      "role": "MEMBER",
      "status": "ACTIVE"
    }
  ]
}
```

### 4.2 路由保护

项目相关路由（`/projects/:id/*`）需验证：
1. 用户已登录
2. 用户是该项目成员（通过 ProjectMember 表验证）

## 5. 前端实现

### 5.1 Store 扩展

新增 `projectStore`：
```typescript
interface ProjectMember {
  id: string
  name: string
  code: string
  role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'
  status: string
}

state: {
  myProjects: ProjectMember[]
}

actions: {
  fetchMyProjects()  // 获取当前用户参与的项目
}
```

### 5.2 侧边栏菜单改造

将静态"项目"菜单改为动态列表：

```vue
<template>
  <!-- 项目列表（动态） -->
  <div v-if="userStore.isLoggedIn" class="nav-group">
    <div class="nav-group-title">项目</div>
    <router-link
      v-for="project in projectStore.myProjects"
      :key="project.id"
      :to="`/projects/${project.id}`"
      class="nav-item sub-item"
    >
      <span>{{ project.name }}</span>
      <span class="role-badge">{{ project.role }}</span>
    </router-link>
  </div>
</template>
```

### 5.3 权限检查

前端路由守卫检查项目成员身份：
```typescript
if (to.path.startsWith('/projects/')) {
  const projectId = to.params.id
  const hasAccess = projectStore.myProjects.some(p => p.id === projectId)
  if (!hasAccess) {
    ElMessage.error('您没有权限访问该项目')
    next('/')
    return
  }
}
```

## 6. 实现步骤

### Phase 1: 后端 API
1. 创建 `GET /api/v1/users/me/projects` 接口
2. 创建项目成员验证中间件

### Phase 2: 前端 Store
1. 创建 `projectStore`
2. 实现 `fetchMyProjects` action

### Phase 3: 侧边栏改造
1. 改造 `MainLayout.vue` 侧边栏
2. 项目列表改为动态渲染

### Phase 4: 路由守卫
1. 添加项目访问权限检查

## 7. 权限说明

| 角色 | 查看项目 | 查看任务 | 操作任务 | 管理项目设置 | 管理成员 |
|------|---------|---------|---------|------------|---------|
| OWNER | ✓ | ✓ | ✓ | ✓ | ✓ |
| ADMIN | ✓ | ✓ | ✓ | ✓ | ✓ |
| MEMBER | ✓ | ✓ | ✓ | ✗ | ✗ |
| VIEWER | ✓ | ✓ | ✗ | ✗ | ✗ |

注：前端菜单控制仅做初步筛选，实际权限验证在后端执行。
