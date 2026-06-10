<template>
  <div class="main-layout">
    <aside class="sidebar">
      <div class="logo">
        <h2>项目管理系统</h2>
      </div>
      <nav class="nav-menu">
        <router-link to="/" class="nav-item">
          <span>📊</span>
          <span>仪表板</span>
        </router-link>
        <div class="nav-group">
          <div class="nav-group-title" @click="projectsExpanded = !projectsExpanded">
            <span>📁</span>
            <span>项目</span>
            <span class="expand-icon">{{ projectsExpanded ? '▼' : '▶' }}</span>
          </div>
          <div v-show="projectsExpanded" class="nav-sub-items">
            <router-link to="/projects" class="nav-item sub-item">
              <span>📋</span>
              <span>项目列表</span>
            </router-link>
          </div>
        </div>
        <div class="nav-group">
          <div class="nav-group-title" @click="tasksExpanded = !tasksExpanded">
            <span>✅</span>
            <span>任务</span>
            <span class="expand-icon">{{ tasksExpanded ? '▼' : '▶' }}</span>
          </div>
          <div v-show="tasksExpanded" class="nav-sub-items">
            <router-link to="/tasks" class="nav-item sub-item">
              <span>📋</span>
              <span>任务列表</span>
            </router-link>
            <router-link to="/tasks/board" class="nav-item sub-item">
              <span>📊</span>
              <span>任务看板</span>
            </router-link>
          </div>
        </div>
        <router-link to="/schedule" class="nav-item">
          <span>📅</span>
          <span>日程</span>
        </router-link>
        <router-link to="/milestones" class="nav-item">
          <span>🚩</span>
          <span>里程碑</span>
        </router-link>
        <router-link to="/workhours" class="nav-item">
          <span>⏱️</span>
          <span>工时管理</span>
        </router-link>
        <router-link to="/activities" class="nav-item">
          <span>📋</span>
          <span>活动流</span>
        </router-link>
        <router-link to="/statistics" class="nav-item">
          <span>📈</span>
          <span>统计</span>
        </router-link>
        <div v-if="userStore.isAdmin" class="nav-group">
          <div class="nav-group-title" @click="systemExpanded = !systemExpanded">
            <span>⚙️</span>
            <span>系统管理</span>
            <span class="expand-icon">{{ systemExpanded ? '▼' : '▶' }}</span>
          </div>
          <div v-show="systemExpanded" class="nav-sub-items">
            <router-link to="/users" class="nav-item sub-item">
              <span>👥</span>
              <span>用户管理</span>
            </router-link>
            <router-link to="/audit/logs" class="nav-item sub-item">
              <span>📋</span>
              <span>审计日志</span>
            </router-link>
            <router-link to="/audit/alerts" class="nav-item sub-item">
              <span>🚨</span>
              <span>告警列表</span>
            </router-link>
          </div>
        </div>
      </nav>
    </aside>
    <div class="main-content">
      <header class="header">
        <div class="user-info">
          <!-- 通知铃铛 -->
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" class="notification-badge">
            <el-icon :size="22" class="notification-icon" @click="router.push('/notifications')">
              <Bell />
            </el-icon>
          </el-badge>
          <span class="user-name">{{ userStore.userName }}</span>
          <el-dropdown @command="handleCommand">
            <span class="user-dropdown">
              <el-avatar :size="32" :src="userStore.userAvatar || 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'" />
              <span class="user-role">{{ userStore.isAdmin ? '管理员' : '用户' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="changePassword">修改密码</el-dropdown-item>
                <el-dropdown-item divided command="logout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>
      <main class="content">
        <router-view />
      </main>
    </div>

    <!-- 修改密码对话框 -->
    <el-dialog v-model="passwordDialogVisible" title="修改密码" width="400px">
      <el-form :model="passwordForm" label-width="80px">
        <el-form-item label="旧密码">
          <el-input v-model="passwordForm.oldPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="新密码">
          <el-input v-model="passwordForm.newPassword" type="password" show-password />
        </el-form-item>
        <el-form-item label="确认密码">
          <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleChangePassword">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { userApi, notificationApi } from '@/api'
import { Bell } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()

const projectsExpanded = ref(true)
const tasksExpanded = ref(false)
const systemExpanded = ref(false)
const unreadCount = ref(0)
let unreadTimer: ReturnType<typeof setInterval> | null = null

const passwordDialogVisible = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

onMounted(async () => {
  // 获取用户信息
  if (userStore.isLoggedIn && !userStore.userInfo) {
    await userStore.fetchUserInfo()
  }
  // 获取未读通知数
  if (userStore.isLoggedIn) {
    await loadUnreadCount()
    // 每30秒刷新一次未读通知数
    unreadTimer = setInterval(loadUnreadCount, 30000)
  }
})

onUnmounted(() => {
  if (unreadTimer) {
    clearInterval(unreadTimer)
  }
})

const loadUnreadCount = async () => {
  try {
    const userId = userStore.userId as string
    if (!userId) return
    const response = await notificationApi.getUnreadCount(userId)
    if (response?.data?.code === 200) {
      unreadCount.value = response.data.data || 0
    }
  } catch (error) {
    // 静默失败，不影响用户体验
  }
}

const handleCommand = (command: string) => {
  switch (command) {
    case 'profile':
      router.push('/profile')
      break
    case 'changePassword':
      passwordDialogVisible.value = true
      break
    case 'logout':
      handleLogout()
      break
  }
}

const handleLogout = async () => {
  await userStore.logout()
  router.push('/login')
}

const handleChangePassword = async () => {
  if (!passwordForm.oldPassword || !passwordForm.newPassword) {
    ElMessage.error('请填写所有字段')
    return
  }

  if (passwordForm.newPassword !== passwordForm.confirmPassword) {
    ElMessage.error('两次输入的密码不一致')
    return
  }

  if (passwordForm.newPassword.length < 6) {
    ElMessage.error('新密码长度不能少于6位')
    return
  }

  try {
    const response = await userApi.changePassword(
      userStore.userId,
      passwordForm.oldPassword,
      passwordForm.newPassword
    )
    if (response && response.code === 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '密码修改失败')
  }
}
</script>

<style scoped>
.main-layout {
  display: flex;
  min-height: 100vh;
}

.sidebar {
  width: 250px;
  background: #1e293b;
  color: white;
  display: flex;
  flex-direction: column;
  position: sticky;
  top: 0;
  height: 100vh;
}

.logo {
  padding: 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
}

.logo h2 {
  margin: 0;
  font-size: 1.2em;
}

.nav-menu {
  flex: 1;
  padding: 20px 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 20px;
  color: white;
  text-decoration: none;
  transition: all 0.3s;
  margin-bottom: 5px;
}

.nav-item:hover {
  background: rgba(255, 255, 255, 0.1);
}

.nav-item.router-link-active {
  background: #667eea;
}

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

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.header {
  background: white;
  padding: 15px 30px;
  border-bottom: 1px solid #e5e7eb;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.notification-badge {
  cursor: pointer;
  margin-right: 10px;
}

.notification-icon {
  color: #666;
  transition: color 0.3s;
}

.notification-icon:hover {
  color: #667eea;
}

.user-name {
  font-weight: 500;
}

.user-dropdown {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.user-role {
  font-size: 12px;
  color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  padding: 2px 8px;
  border-radius: 10px;
}

.content {
  flex: 1;
  padding: 30px;
  background: #f5f7fa;
}
</style>
