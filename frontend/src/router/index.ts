import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { useProjectStore } from '@/stores/project'
import { ElMessage } from 'element-plus'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/pages/auth/Login.vue'),
    meta: { requiresAuth: false },
  },
  {
    path: '/oauth/callback',
    name: 'OAuthCallback',
    component: () => import('@/pages/auth/OAuthCallback.vue'),
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
        path: 'tasks/board',
        name: 'TaskBoard',
        component: () => import('@/pages/tasks/TaskBoard.vue'),
      },
      {
        path: 'tasks/:id',
        name: 'TaskDetail',
        component: () => import('@/pages/tasks/TaskDetail.vue'),
      },
      {
        path: 'schedule',
        name: 'ScheduleList',
        component: () => import('@/pages/schedule/ScheduleList.vue'),
      },
      {
        path: 'milestones',
        name: 'MilestoneList',
        component: () => import('@/pages/milestones/MilestoneList.vue'),
      },
      {
        path: 'workhours',
        name: 'WorkHourList',
        component: () => import('@/pages/workhours/WorkHourList.vue'),
      },
      {
        path: 'activities',
        name: 'ActivityList',
        component: () => import('@/pages/activities/ActivityList.vue'),
      },
      {
        path: 'profile',
        name: 'Profile',
        component: () => import('@/pages/profile/Profile.vue'),
      },
      {
        path: 'notifications',
        name: 'NotificationCenter',
        component: () => import('@/pages/notifications/NotificationCenter.vue'),
      },
      {
        path: 'statistics',
        name: 'Statistics',
        component: () => import('@/pages/statistics/Statistics.vue'),
      },
      {
        path: 'users',
        name: 'UserManagement',
        component: () => import('@/pages/users/UserList.vue'),
        meta: { requiresAdmin: true },
      },
      {
        path: 'audit',
        name: 'AuditManagement',
        component: () => import('@/pages/audit/AuditIndex.vue'),
        meta: { requiresAdmin: true },
        children: [
          {
            path: 'logs',
            name: 'AuditLogs',
            component: () => import('@/pages/audit/AuditLog.vue'),
          },
          {
            path: 'alerts',
            name: 'AlertList',
            component: () => import('@/pages/audit/AlertList.vue'),
          },
        ],
      },
      {
        path: 'dingtalk-config',
        name: 'DingTalkConfigDoc',
        component: () => import('@/pages/admin/DingTalkConfigDoc.vue'),
        meta: { requiresAdmin: true },
      },
      {
        path: 'dingtalk-admin-config',
        name: 'DingTalkConfig',
        component: () => import('@/pages/admin/DingTalkConfig.vue'),
        meta: { requiresAdmin: true },
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
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()

  // 需要认证的路由
  if (to.meta.requiresAuth && !userStore.isLoggedIn) {
    next({ name: 'Login', query: { redirect: to.fullPath } })
    return
  }

  // 登录页且已登录，跳转首页
  if (to.path === '/login' && userStore.isLoggedIn) {
    next({ name: 'Dashboard' })
    return
  }

  // 如果已登录但没有用户信息，先获取用户信息
  if (userStore.isLoggedIn && !userStore.userInfo) {
    await userStore.fetchUserInfo()
  }

  // 需要管理员权限的路由
  if (to.meta.requiresAdmin) {
    // 如果用户信息还没有，先获取
    if (!userStore.userInfo) {
      await userStore.fetchUserInfo()
    }

    if (!userStore.isAdmin) {
      ElMessage.error('需要管理员权限')
      next({ name: 'Dashboard' })
      return
    }
  }

  // 检查项目访问权限
  if (to.path.startsWith('/projects/') && to.params.id) {
    const projectId = to.params.id as string
    const projectStore = useProjectStore()

    // 如果还没有获取项目列表，先获取
    if (projectStore.myProjects.length === 0) {
      await projectStore.fetchMyProjects()
    }

    // 检查是否是项目成员（先从本地缓存检查）
    let hasAccess = projectStore.isProjectMember(projectId)

    // 如果本地缓存没有该项目，尝试直接获取项目详情验证权限
    if (!hasAccess) {
      try {
        const projectApi = (await import('@/api')).projectApi
        const response = await projectApi.getProject(projectId)
        if (response && response.data && response.data.code === 200) {
          hasAccess = true
        }
      } catch (e) {
        // 无法访问该项目
      }
    }

    if (!hasAccess) {
      ElMessage.error('您没有权限访问该项目')
      next({ name: 'Dashboard' })
      return
    }
  }

  next()
})

export default router
