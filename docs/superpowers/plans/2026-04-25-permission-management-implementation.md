# 权限管理实现计划

> **For agentic workers:** REQUIRED: Use superpowers:subagent-driven-development (if subagents available) or superpowers:executing-plans to implement this plan. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现项目级权限控制，用户只能看到自己参与的项目菜单

**Architecture:** 后端新增获取用户项目列表 API，前端 projectStore 存储项目数据，侧边栏动态渲染项目菜单，路由守卫验证项目访问权限

**Tech Stack:** Node.js/Express/TypeScript 后端，Vue 3/Pinia 前端，Prisma ORM

---

## 文件结构

### 后端
- Modify: `backend/src/controllers/auth.controller.ts` - 添加 getMyProjects 方法
- Modify: `backend/src/routes/auth.routes.ts` - 添加 /users/me/projects 路由
- Create: `backend/src/middleware/project.middleware.ts` - 项目成员验证中间件

### 前端
- Modify: `frontend/src/stores/user.ts` - 修复 fetchUserInfo 的 response.data.code 问题
- Create: `frontend/src/stores/project.ts` - 新增 projectStore
- Modify: `frontend/src/api/project.ts` - 添加获取我的项目 API
- Modify: `frontend/src/layouts/MainLayout.vue` - 侧边栏项目菜单动态化
- Modify: `frontend/src/router/index.ts` - 添加项目访问权限路由守卫

---

## Chunk 1: 后端 - 获取用户项目列表 API

### Task 1: 添加 getMyProjects 方法到 auth.controller.ts

**Files:**
- Modify: `backend/src/controllers/auth.controller.ts`

- [ ] **Step 1: 在 auth.controller.ts 添加 getMyProjects 方法**

在 `auth.controller.ts` 文件末尾（第 470 行附近）添加：

```typescript
// 获取当前用户参与的项目列表
async getMyProjects(req: Request, res: Response): Promise<void> {
  try {
    if (!req.user) {
      throw new AppError(401, '未认证');
    }

    const userId = BigInt(req.user.userId);

    // 查询用户参与的项目及其成员角色
    const memberships = await prisma.projectMember.findMany({
      where: { userId },
      include: {
        project: {
          select: {
            id: true,
            name: true,
            code: true,
            status: true,
          },
        },
      },
      orderBy: { joinedAt: 'desc' },
    });

    const projects = memberships.map((m) => ({
      id: m.project.id.toString(),
      name: m.project.name,
      code: m.project.code,
      role: m.role,
      status: m.project.status,
    }));

    res.json({
      code: 200,
      message: 'success',
      data: projects,
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
```

- [ ] **Step 2: 在 auth.routes.ts 添加路由**

**Files:**
- Modify: `backend/src/routes/auth.routes.ts`

在 auth routes 数组中添加：

```typescript
// 获取当前用户参与的项目列表
router.get('/users/me/projects', authenticate, authController.getMyProjects.bind(authController));
```

### Task 2: 创建项目成员验证中间件

**Files:**
- Create: `backend/src/middleware/project.middleware.ts`

- [ ] **Step 1: 创建项目中间件**

```typescript
import { Request, Response, NextFunction } from 'express';
import { prisma } from '../config/database';
import { AppError } from './error.middleware';

export async function requireProjectMember(
  req: Request,
  res: Response,
  next: NextFunction
): Promise<void> {
  try {
    if (!req.user) {
      throw new AppError(401, '未认证');
    }

    const projectId = req.params.id || req.body.projectId;
    if (!projectId) {
      throw new AppError(400, '缺少项目ID');
    }

    const userId = BigInt(req.user.userId);

    const membership = await prisma.projectMember.findUnique({
      where: {
        projectId_userId: {
          projectId: BigInt(projectId),
          userId,
        },
      },
    });

    if (!membership) {
      throw new AppError(403, '您没有权限访问该项目');
    }

    // 将成员角色附加到请求对象
    (req as any).projectRole = membership.role;
    next();
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
```

---

## Chunk 2: 前端 Store 和 API

### Task 3: 创建 projectStore

**Files:**
- Create: `frontend/src/stores/project.ts`

- [ ] **Step 1: 创建 projectStore**

```typescript
import { defineStore } from 'pinia'
import { projectApi } from '@/api'

export interface MyProject {
  id: string
  name: string
  code: string
  role: 'OWNER' | 'ADMIN' | 'MEMBER' | 'VIEWER'
  status: string
}

interface ProjectState {
  myProjects: MyProject[]
  currentProject: MyProject | null
}

export const useProjectStore = defineStore('project', {
  state: (): ProjectState => ({
    myProjects: [],
    currentProject: null,
  }),

  getters: {
    getProjectById: (state) => (id: string) => {
      return state.myProjects.find(p => p.id === id)
    },
    isProjectMember: (state) => (projectId: string) => {
      return state.myProjects.some(p => p.id === projectId)
    },
    isProjectOwnerOrAdmin: (state) => (projectId: string) => {
      const project = state.myProjects.find(p => p.id === projectId)
      return project && (project.role === 'OWNER' || project.role === 'ADMIN')
    },
  },

  actions: {
    async fetchMyProjects() {
      try {
        const response = await projectApi.getMyProjects()
        if (response && response.data && response.data.code === 200) {
          this.myProjects = response.data.data
        }
      } catch (error) {
        console.error('获取项目列表失败:', error)
      }
    },

    setCurrentProject(project: MyProject | null) {
      this.currentProject = project
    },

    clearProjects() {
      this.myProjects = []
      this.currentProject = null
    },
  },
})
```

### Task 4: 添加项目 API

**Files:**
- Modify: `frontend/src/api/project.ts`

- [ ] **Step 1: 在 project.ts 添加 getMyProjects API**

```typescript
// 获取当前用户参与的项目列表
getMyProjects: () => request.get('/api/v1/auth/users/me/projects'),
```

### Task 5: 修复 userStore fetchUserInfo

**Files:**
- Modify: `frontend/src/stores/user.ts`

- [ ] **Step 1: 修复 fetchUserInfo response.data.code 问题**

修改第 46 行：
```typescript
if (response && response.data && response.data.code === 200) {
  this.setUserInfo(response.data.data)
  return response.data.data
}
```

---

## Chunk 3: 侧边栏动态项目菜单

### Task 6: 改造 MainLayout.vue 侧边栏

**Files:**
- Modify: `frontend/src/layouts/MainLayout.vue`

- [ ] **Step 1: 修改模板部分，将静态项目菜单改为动态**

将原来的：
```vue
<router-link to="/projects" class="nav-item">
  <span>📁</span>
  <span>项目</span>
</router-link>
```

改为：
```vue
<div class="nav-group">
  <div class="nav-group-title" @click="projectsExpanded = !projectsExpanded">
    <span>📁</span>
    <span>项目</span>
    <span class="expand-icon">{{ projectsExpanded ? '▼' : '▶' }}</span>
  </div>
  <div v-show="projectsExpanded" class="nav-sub-items">
    <router-link
      v-for="project in projectStore.myProjects"
      :key="project.id"
      :to="`/projects/${project.id}`"
      class="nav-item sub-item"
    >
      <span class="project-name">{{ project.name }}</span>
      <span :class="['role-badge', project.role.toLowerCase()]">{{ project.role }}</span>
    </router-link>
    <div v-if="projectStore.myProjects.length === 0" class="no-projects">
      暂未参与任何项目
    </div>
  </div>
</div>
```

- [ ] **Step 2: 添加 script setup 部分**

```typescript
import { ref } from 'vue'
import { useProjectStore } from '@/stores/project'

const projectStore = useProjectStore()
const projectsExpanded = ref(true)

onMounted(async () => {
  // 获取用户参与的项目列表
  if (userStore.isLoggedIn) {
    await projectStore.fetchMyProjects()
  }
})
```

- [ ] **Step 3: 添加样式**

```css
.nav-group {
  margin-bottom: 5px;
}

.nav-group-title {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  color: rgba(255, 255, 255, 0.7);
  cursor: pointer;
  transition: all 0.3s;
}

.nav-group-title:hover {
  background: rgba(255, 255, 255, 0.1);
  color: white;
}

.expand-icon {
  margin-left: auto;
  font-size: 10px;
}

.nav-sub-items {
  margin-left: 10px;
}

.nav-item.sub-item {
  padding: 10px 20px;
  font-size: 14px;
}

.project-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.role-badge {
  font-size: 10px;
  padding: 2px 6px;
  border-radius: 4px;
  background: rgba(255, 255, 255, 0.2);
}

.role-badge.owner,
.role-badge.admin {
  background: #667eea;
}

.role-badge.member {
  background: #07c160;
}

.role-badge.viewer {
  background: #909399;
}

.no-projects {
  padding: 10px 20px;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
}
```

---

## Chunk 4: 路由守卫

### Task 7: 添加项目访问权限路由守卫

**Files:**
- Modify: `frontend/src/router/index.ts`

- [ ] **Step 1: 修改 router.beforeEach 添加项目权限检查**

在现有的 beforeEach 中，在检查 isAdmin 之后添加：

```typescript
// 检查项目访问权限
if (to.path.startsWith('/projects/') && to.params.id) {
  const projectId = to.params.id as string

  // 如果还没有获取项目列表，先获取
  if (projectStore.myProjects.length === 0) {
    await projectStore.fetchMyProjects()
  }

  // 检查是否是项目成员
  if (!projectStore.isProjectMember(projectId)) {
    ElMessage.error('您没有权限访问该项目')
    next({ name: 'Dashboard' })
    return
  }
}
```

- [ ] **Step 2: 确保导入 useProjectStore**

```typescript
import { useUserStore } from '@/stores/user'
import { useProjectStore } from '@/stores/project'
```

---

## 验证步骤

1. 重新编译后端：`npm run build`
2. 重启后端服务器
3. 使用 admin 登录前端
4. 验证侧边栏显示所有项目（因为 admin 是所有项目的 OWNER）
5. 使用普通用户登录，验证只显示该用户参与的项目

