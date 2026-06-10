# 项目管理系统前端设计方案

## 一、技术栈选择

### 前端框架
- **框架**：Vue 3 (Composition API)
- **版本**：^3.4.x
- **理由**：轻量高效，响应式系统，学习曲线平缓，生态成熟

### UI 组件库
- **库**：Element Plus
- **版本**：^2.x
- **理由**：企业级 UI 组件，功能丰富，中文文档完善，主题定制灵活

### 状态管理
- **库**：Pinia
- **版本**：^2.x
- **理由**：简洁易用，响应式，Vue 3 官方推荐

### 构建工具
- **工具**：Vite
- **版本**：^5.x
- **理由**：极速 HMR，生态系统成熟，生产性能优秀

### 路由管理
- **库**：Vue Router 4
- **版本**：^4.x
- **理由**：Vue 3 官方推荐，功能完善

### HTTP 客户端
- **库**：Axios
- **版本**：^1.x
- **理由**：生态成熟，拦截器强大，易于配置

### 图表库
- **库**：ECharts
- **版本**：^5.x
- **理由**：功能丰富，性能优秀，定制灵活

### 日期处理
- **库**：Day.js
- **版本**：^1.x
- **理由**：功能强大，易于使用

---

## 二、项目结构设计

```
frontend/
├── public/                     # 公共静态资源
│   └── favicon.ico
├── src/
│   ├── assets/                  # 静态资源
│   │   ├── images/          # 图片资源
│   │   └── styles/           # 全局样式
│   ├── components/              # 公共组件
│   │   ├── common/            # 通用组件
│   │   │   ├── Button/       # 按钮组件
│   │   │   ├── Input/        # 输入框组件
│   │   │   ├── Modal/        # 模态框组件
│   │   │   ├── Upload/       # 上传组件
│   │   │   └── Avatar/       # 头像组件
│   │   ├── layout/            # 布局组件
│   │   │   ├── Header.vue    # 顶部导航
│   │   │   ├── Sidebar.vue   # 侧边栏
│   │   │   └── Footer.vue    # 底部信息
│   │   ├── task/              # 任务相关组件
│   │   │   ├── TaskCard.vue        # 任务卡片
│   │   │   ├── TaskBoard.vue       # 任务看板
│   │   │   ├── TaskList.vue        # 任务列表
│   │   │   ├── TaskDetail.vue      # 任务详情
│   │   │   └── TaskForm.vue       # 任务表单
│   │   ├── project/           # 项目相关组件
│   │   │   ├── ProjectCard.vue     # 项目卡片
│   │   │   ├── ProjectList.vue     # 项目列表
│   │   │   └── ProjectForm.vue    # 项目表单
│   │   ├── schedule/          # 日程相关组件
│   │   │   ├── Calendar.vue        # 日历视图
│   │   │   └── EventModal.vue     # 日程弹窗
│   │   └── statistics/        # 统计相关组件
│   │       └── Dashboard.vue      # 仪表板
│   ├── composables/            # 组合式函数
│   │   ├── useAuth.ts          # 认证相关
│   │   ├── useTask.ts          # 任务操作逻辑
│   │   ├── useProject.ts       # 项目操作逻辑
│   │   ├── useFile.ts          # 文件上传逻辑
│   │   └── useApi.ts           # API 通用逻辑
│   ├── layouts/                # 布局组件
│   │   ├── MainLayout.vue     # 主布局（带侧边栏）
│   │   ├── EmptyLayout.vue    # 空白布局（登录页）
│   │   └── AuthLayout.vue     # 认证布局
│   ├── pages/                  # 页面组件
│   │   ├── auth/
│   │   │   └── Login.vue      # 登录页
│   │   ├── dashboard/
│   │   │   └── Dashboard.vue  # 仪表板
│   │   ├── projects/
│   │   │   ├── ProjectList.vue   # 项目列表
│   │   │   ├── ProjectDetail.vue # 项目详情
│   │   │   └── ProjectBoard.vue # 项目看板
│   │   ├── tasks/
│   │   │   ├── TaskList.vue     # 任务列表
│   │   │   ├── TaskDetail.vue   # 任务详情
│   │   │   └── TaskBoard.vue    # 任务看板
│   │   ├── schedule/
│   │   │   └── Calendar.vue     # 日历页
│   │   └── statistics/
│   │       └── Statistics.vue   # 统计页
│   ├── router/                 # 路由配置
│   │   └── index.ts
│   ├── stores/                 # Pinia 状态管理
│   │   ├── user.ts            # 用户状态
│   │   ├── project.ts         # 项目状态
│   │   ├── task.ts            # 任务状态
│   │   └── ui.ts              # UI 状态
│   ├── api/                    # API 客户端
│   │   ├── request.ts         # Axios 配置
│   │   ├── index.ts           # API 统一导出
│   │   ├── auth.ts            # 认证 API
│   │   ├── project.ts         # 项目 API
│   │   ├── task.ts            # 任务 API
│   │   ├── file.ts            # 文件 API
│   │   ├── comment.ts         # 评论 API
│   │   ├── statistics.ts      # 统计 API
│   │   └── schedule.ts        # 日程 API
│   ├── types/                  # TypeScript 类型定义
│   │   ├── index.ts           # 类型导出
│   │   ├── api.ts             # API 类型
│   │   └── models.ts          # 数据模型
│   ├── utils/                  # 工具函数
│   │   ├── date.ts            # 日期处理
│   │   ├── file.ts            # 文件处理
│   │   ├── storage.ts         # 本地存储
│   │   └── validation.ts      # 数据校验
│   ├── App.vue                 # 根组件
│   └── main.ts                 # 入口文件
├── .env.development            # 开发环境变量
├── .env.production             # 生产环境变量
├── .eslintrc.js               # ESLint 配置
├── .prettierrc.json           # Prettier 配置
├── index.html                 # HTML 模板
├── package.json               # 依赖配置
├── tsconfig.json              # TypeScript 配置
├── vite.config.ts             # Vite 配置
└── README.md                 # 项目说明
```

---

## 三、核心功能模块设计

### 3.1 认证模块

#### 功能说明
- **登录方式**：微信扫码登录、钉钉扫码登录
- **二维码展示**：动态二维码生成与轮询
- **Token 管理**：自动刷新机制
- **用户信息**：本地存储与状态同步

#### 页面设计
```
pages/auth/Login.vue
├── 扫码登录按钮区
├── 二维码展示组件
├── 登录状态提示
└── 自动跳转逻辑
```

#### 状态管理
```typescript
stores/user.ts
{
  token: string              // 访问 Token
  refreshToken: string      // 刷新 Token
  userInfo: User | null     // 用户信息
  permissions: string[]     // 用户权限
}
```

#### API 接口
```
POST /api/v1/auth/wechat/login      // 微信登录
POST /api/v1/auth/dingtalk/login    // 钉钉登录
POST /api/v1/auth/refresh          // 刷新 Token
```

### 3.2 任务管理模块

#### 功能说明
- **看板视图**：拖拽任务卡片，状态流转
- **列表视图**：分页展示，多条件筛选
- **任务详情**：完整信息展示，评论区
- **快速操作**：批量操作，快捷菜单

#### 组件设计
```
components/task/
├── TaskCard.vue          # 任务卡片（支持拖拽）
├── TaskBoard.vue         # 看板容器（三列布局）
├── TaskList.vue          # 列表容器（表格布局）
├── TaskDetail.vue        # 任务详情弹窗
└── TaskForm.vue          # 任务创建/编辑表单
```

#### 状态流转
```
TODO → DOING → DONE → ARCHIVED
```

#### 优先级设置
```
P0 (最高) → P1 → P2 → P3 (默认) → P4 (最低)
```

#### API 接口
```
POST   /api/v1/tasks                    # 创建任务
GET    /api/v1/tasks/project/:projectId   # 获取项目任务
GET    /api/v1/tasks/board/:projectId     # 获取看板数据
GET    /api/v1/tasks/:id                # 获取任务详情
PUT    /api/v1/tasks/:id                # 更新任务
PATCH  /api/v1/tasks/:id/status         # 更新状态
DELETE /api/v1/tasks/:id                # 删除任务
```

### 3.3 项目规划模块

#### 功能说明
- **项目列表**：卡片式展示，快速搜索
- **项目详情**：任务看板，成员管理
- **甘特图视图**：项目进度可视化
- **成员管理**：邀请成员，设置权限

#### 组件设计
```
components/project/
├── ProjectCard.vue        # 项目卡片
├── ProjectList.vue        # 项目列表
├── ProjectDetail.vue      # 项目详情容器
├── GanttChart.vue         # 甘特图组件
└── MemberManager.vue       # 成员管理组件
```

#### API 接口
```
POST   /api/v1/projects              # 创建项目
GET    /api/v1/projects              # 获取项目列表
GET    /api/v1/projects/:id            # 获取项目详情
PUT    /api/v1/projects/:id            # 更新项目
DELETE /api/v1/projects/:id            # 删除项目
GET    /api/v1/projects/:id/members    # 获取成员
POST   /api/v1/projects/:id/members    # 添加成员
DELETE /api/v1/projects/:id/members/:userId  # 移除成员
```

### 3.4 日程管理模块

#### 功能说明
- **日历视图**：月/周/日视图切换
- **日程创建**：弹窗快速创建
- **提醒功能**：截止日期提醒
- **日程类型**：会议、提醒、截止日期

#### 组件设计
```
components/schedule/
├── Calendar.vue          # 日历主视图
└── EventModal.vue        # 日程创建/编辑弹窗
```

#### 日程类型
```
- MEETING: 会议
- REMINDER: 提醒
- DEADLINE: 截止日期
```

#### API 接口
```
POST   /api/v1/schedule                 # 创建日程
GET    /api/v1/schedule                 # 获取日程列表
GET    /api/v1/schedule/upcoming         # 获取即将到来日程
GET    /api/v1/schedule/:id             # 获取日程详情
PUT    /api/v1/schedule/:id             # 更新日程
DELETE /api/v1/schedule/:id             # 删除日程
```

### 3.5 统计分析模块

#### 功能说明
- **数据仪表板**：关键指标展示
- **图表可视化**：多维度数据展示
- **报表导出**'支持导出功能

#### 组件设计
```
components/statistics/
├── Dashboard.vue        # 主仪表板
├── ChartCard.vue        # 图表卡片
└── MetricCard.vue       # 指标卡片
```

#### 统计指标
```
- 任务完成率
- 项目总览
- 成员活跃度
- 工时统计
- 活动时间轴
```

#### API 接口
```
GET /api/v1/statistics/project/:projectId  # 项目统计
GET /api/v1/statistics/user/:userId        # 用户统计
```

---

## 四、API 客户端设计

### 4.1 Axios 配置

```typescript
// src/api/request.ts
import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const baseURL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:3000'

const request = axios.create({
  baseURL,
  timeout: 15000,
  headers: {
    'Content-Type': 'application/json',
  },
})

// 请求拦截器
request.interceptors.request.use(
  (config) => {
    const token = useUserStore().token
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  (response) => {
    return response.data
  },
  (error) => {
    if (error.response) {
      const { status, data } = error.response

      switch (status) {
        case 401:
          // Token 过期，跳转
          ElMessage.error('登录已过期，请重新登录')
          window.location.href = '/login'
          break

        case 403:
          ElMessage.error('没有权限访问')
          break

        case 404:
          ElMessage.error('请求的资源不存在')
          break

        case 500:
          ElMessage.error(data?.message || '服务器错误')
          break

        default:
          ElMessage.error(data?.message || '请求失败')
      }
    } else {
      ElMessage.error('网络错误，请检查连接')
    }

    return Promise.reject(error)
  }
)

export default request
```

### 4.2 API 模块设计

```typescript
// src/api/index.ts
import request from './request'

export * as authApi from './auth'
export * as projectApi from './project'
export * as taskApi from './task'
export * as fileApi from './file'
export * as commentApi from './comment'
export * as statisticsApi from './statistics'
export * as scheduleApi from './schedule'
```

### 4.3 类型定义

```typescript
// src/types/api.ts
export interface ApiResponse<T = any> {
  code: number
  message: string
  data: T
}

export interface User {
  id: string
  name: string
  email?: string
  avatar?: string
  username?: string
  status: 'ACTIVE' | 'INACTIVE' | 'DELETED'
  createdAt: string
}

export interface Project {
  id: string
  name: string
  description?: string
  code?: string
  ownerId: string
  status: 'ACTIVE' | 'ARCHIVED' | 'DELETED'
  startDate?: string
  endDate?: string
  createdAt: string
  updatedAt: string
}

export interface Task {
  id: string
  title: string
  description?: string
  projectId: string
  assignedTo?: string
  createdBy: string
    status: 'TODO' | 'DOING' | 'DONE' | 'ARCHIVED'
  priority: number
  dueDate?: string
  startDate?: string
  tags?: string
  attachments?: string
  createdAt: string
  updatedAt: string
  assignedUser?: User
  createdByUser?: User
  comments?: Comment[]
}

export interface Comment {
  id: string
  content: string
  entityType: string
  entityId: string
  userId: string
  parentId?: string
  mentions?: string[]
  createdAt: string
  user?: User
  replies?: Comment[]
}

export interface File {
  id: string
  name: string
  size: string
  type: string
  path: string
  uploadedBy: string
  taskId?: string
  projectId?: string
  createdAt: string
}

export interface Event {
  id: string
  title: string
  description?: string
  location?: string
  allDay?: boolean
  reminderMinutes?: number
  repeatRule?: string
  projectId?: string
  taskId?: string
  userId: string
  startTime: string
  endTime: string
  createdAt: string
  updatedAt: string
}

export interface ProjectStatistics {
  tasks: {
    total: number
    byStatus: Record<string, number>
    completionRate: number
  }
  members: Array<{
    userId: string
    projectId: string
    role: string
    user?: User
  }>
  totalMembers: number
  recentActivity: any[]
  workHours: {
    total: number
  }
}
```

---

## 五、路由设计

### 5.1 路由配置

```typescript
// src/router/index.ts
import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/',
    component: () => import('@/layouts/MainLayout.vue'),
    meta: { requiresAuth: true },
    children: [
      {
        path: '',
        name: 'Dashboard',
        component: () => import('@/pages/dashboard/Dashboard.vue'),
      },
      {
        path: 'projects',
        name: 'ProjectList',
        component: () => import('@/pages/projects/ProjectList.vue'),
      },
      {
        path: 'projects/:id',
        name: 'ProjectDetail',
        component: () => import('@/pages/projects/ProjectDetail.vue'),
      },
      {
        path: 'projects/:id/board',
        name: 'ProjectBoard',
        component: () => import('@/pages/projects/ProjectBoard.vue'),
      },
      {
        path: 'tasks',
        name: 'TaskList',
        component: () => import('@/pages/tasks/TaskList.vue'),
      },
      {
        path: 'tasks/:id',
        name: 'TaskDetail',
        component: () => import('@/pages/tasks/TaskDetail.vue'),
      },
      {
        path: 'schedule',
        name: 'Calendar',
        component: () => import('@/pages/schedule/Calendar.vue'),
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/pages/statistics/Statistics.vue'),
      },
    ],
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior(to, from, savedPosition) {
    if (savedPosition) {
      return savedPosition
    } else {
      return { top: 0 }
    }
  },
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 需要认证的路由
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
  }
  // 登录页且已登录，跳转首页
  else if (to.path === '/login' && userStore.isLoggedIn) {
    next({ name: 'Dashboard' })
  } else {
    next()
  }
})

export default router
```

---

## 六、状态管理设计

### 6.1 用户状态

```typescript
// src/stores/user.ts
import { defineStore } from 'pinia'
import { authApi } from '@/api'

interface UserState {
  token: string
  refreshToken: string
  userInfo: User | null
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: null,
    permissions: [],
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    userId: (state) => state.userInfo?.id || '',
    userName: (state) => state.userInfo?.name || '',
    userAvatar: (state) => state.userInfo?.avatar || '',
  },

  actions: {
    async login(code: string, platform: 'wechat' | 'dingtalk') {
      try {
        const { data } = await authApi.login(platform, code)

        this.token = data.token
        this.refreshToken = data.refreshToken
        this.userInfo = data.user

        // 保存到本地存储
        localStorage.setItem('token', data.token)
        localStorage.setItem('refreshToken', data.refreshToken)

        return { success: true }
      } catch (error) {
        return { success: false, message: '登录失败' }
      }
    },

    async logout() {
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      this.permissions = []

      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    },

    async refreshToken() {
      if (!this.refreshToken) return

      try {
        const { data } = await authApi.refresh(this.refreshToken)

        this.token = data.token
        this.refreshToken = data.refreshToken

        localStorage.setItem('token', data.token)
        localStorage.setItem('refreshToken', data.refreshToken)
      } catch (error) {
        // 刷新失败，清除登录状态
        await this.logout()
      }
    },

    setUserInfo(user: User) {
      this.userInfo = user
    },
  },
})
```

### 6.2 项目状态

```typescript
// src/stores/project.ts
import { defineStore } from 'pinia'

interface ProjectState {
  currentProject: Project | null
  projectList: Project[]
  loading: boolean
}

export const useProjectStore = defineStore('project', {
  state: (): ProjectState => ({
    currentProject: null,
    projectList: [],
    loading: false,
  }),

  actions: {
    setCurrentProject(project: Project | null) {
      this.currentProject = project
    },

    setProjectList(projects: Project[]) {
      this.projectList = projects
    },

    setLoading(loading: boolean) {
      this.loading = loading
    },
  },
})
```

---

## 七、组合式函数设计

### 7.1 认证相关

```typescript
// src/composables/useAuth.ts
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'

export function useAuth() {
  const userStore = useUserStore()
  const router = useRouter()

  const handleLogin = async (code: string, platform: 'wechat' | 'dingtalk') => {
    const result = await userStore.login(code, platform)
    if (result.success) {
      // 跳转到重定向地址或首页
      const redirect = router.currentRoute.value.query.redirect as string
      router.push(redirect || '/')
    }
  }

  const handleLogout = async () => {
    await userStore.logout()
    router.push('/login')
  }

  return {
    userStore,
    handleLogin,
    handleLogout,
  }
}
```

### 7.2 任务操作

```typescript
// src/composables/useTask.ts
import { ref, computed } from 'vue'
import { taskApi } from'@/api'

export function useTaskTask(taskId?: string) {
  const tasks = ref([])
  const loading = ref(false)
  const currentTask = ref<Task | null>(null)

  const fetchTasks = async (projectId: string) => {
    loading.value = true
    try {
      const { data } = await taskApi.getTasks(projectId)
      tasks.value = data.list
    } finally {
      loading.value = false
    }
  }

  const createTask = async (taskData: Partial<Task>) => {
    loading.value = true
    try {
      const { data } = await taskApi.createTask(taskData)
      tasks.value.unshift(data)
      return data
    } finally {
      loading.value = false
    }
  }

  const updateTask = async (id: string, updates: Partial<Task>) => {
    loading.value = true
    try {
      const { data } = await taskApi.updateTask(id, updates)
      const index = tasks.value.findIndex(t => t.id === id)
      if (index !== -1) {
        tasks.value[index] = { ...tasks.value[index], ...data }
      }
      return data
    } finally {
      loading.value = false
    }
  }

  const deleteTask = async (id: string) => {
    loading.value = true
    try {
      await taskApi.deleteTask(id)
      tasks.value = tasks.value.filter(t => t.id !== id)
    } finally {
      loading.value = false
    }
  }

  return {
    tasks,
    loading,
    currentTask,
    fetchTasks,
    createTask,
    updateTask,
    deleteTask,
  }
}
```

---

## 八、配置文件设计

### 8.1 Vite 配置

```typescript
// vite.config.ts
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { resolve } from 'path'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': resolve(__dirname, './src'),
    },
  },
  server: {
    port: 3001,
    proxy: {
      '/api': {
        target: 'http://localhost:3000',
        changeOrigin: true,
      },
    },
  },
  build: {
    outDir: 'dist',
    sourcemap: true,
  },
})
```

### 8.2 TypeScript 配置

```json
// tsconfig.json
{
  "compilerOptions": {
    "target": "ES2020",
    "useDefineForClassFields": true,
    "module": "ESNext",
    "lib": ["ES2020", "DOM", "DOM.Iterable"],
    "skipLibCheck": true,
    "esModuleInterop": true,
    "allowSyntheticDefaultImports": true,
    "strict": true,
    "forceConsistentCasingInFileNames": true,
    "moduleResolution": "bundler",
    "resolveJsonModule": true,
    "isolatedModules": true,
    "noEmit": true,
    "jsx": "preserve",
    "jsxImportSource": false,
    "baseUrl": ".",
    "paths": {
      "@/*": ["./src/*"]
    }
  },
  "include": ["src/**/*.ts", "src/**/*.d.ts", "src/**/*.tsx", "src/**/*.vue"],
  "references": [{ "path": "./tsconfig.node.json" }]
}
```

### 8.3 环境变量

```env
# .env.development
VITE_APP_TITLE=项目管理系统
VITE_API_BASE_URL=http://localhost:3000
```

```env
# .env.production
VITE_APP_TITLE=项目管理系统
VITE_API_BASE_URL=https://api.yourdomain.com
```

---

## 九、开发规范

### 9.1 命名规范

- **组件文件**：PascalCase (如：TaskCard.vue)
- **工具文件**：camelCase (如：dateUtils.ts)
- **状态文件**：camelCase (如：useUser.ts)
- **类型文件**：PascalCase (如：UserModel.ts)

### 9.2 组件设计规范

- **单一职责**：每个组件只负责一个功能
- **组件拆分**：超过 300 行考虑拆分
- **Props 明确**：使用 TypeScript 定义 Props 类型
- **Emits 命名**：事件名使用驼峰命名

### 9.3 API 调用规范

- **统一错误处理**：使用 Axios 拦截器
- **Loading 状态**：显示加载动画
- **数据缓存**：避免重复请求

### 9.4 样式规范

- **使用 SCSS/预处理器**：便于管理样式
- **组件样式**：优先使用 scoped 样式
- **主题定制**：支持 Element Plus 主题切换

---

## 十、部署说明

### 10.1 开发环境

```bash
# 安装依赖
npm install

# 启动开发服务器
npm run dev

# 访问地址
http://localhost:3001
```

### 10.2 生产构建

```bash
# 构建生产版本
npm run build

# 构建产物在 dist 目录
```

### 10.3 部署方式

#### 静态部署
将 `dist` 目录部署到 Nginx/Apache 服务器

#### Docker 部署
```bash
# Dockerfile
FROM node:20-alpine
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build
EXPOSE 3001
CMD ["npm", "preview"]
```

---

## 十一、后续优化建议

1. **性能优化**
   - 路由懒加载
   - 组件按需加载
   - 图片懒加载
   - 虚拟列表优化

2. **用户体验**
   - 骨架屏适配
   - 加载动画优化
   - 错误提示优化
   - 离线提示

3. **可维护性**
   - 代码注释完善
   - TypeScript 类型严格
   - 组件文档完善
   - 单元测试覆盖
