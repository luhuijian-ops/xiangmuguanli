<template>
  <div class="profile-page">
    <el-row :gutter="20">
      <!-- 左侧用户信息卡片 -->
      <el-col :span="8">
        <el-card class="user-card">
          <div class="user-avatar-section">
            <el-avatar :size="100" :src="userStore.userAvatar || defaultAvatar" />
            <h2 class="user-name">{{ userStore.userName }}</h2>
            <el-tag :type="userStore.isAdmin ? 'danger' : 'info'" size="small">
              {{ userStore.isAdmin ? '管理员' : '普通用户' }}
            </el-tag>
          </div>

          <el-divider />

          <div class="user-info-section">
            <el-descriptions :column="1" border>
              <el-descriptions-item label="用户名">
                {{ userStore.userInfo?.username || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="邮箱">
                {{ userStore.userInfo?.email || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="手机号">
                {{ userStore.userInfo?.phone || '-' }}
              </el-descriptions-item>
              <el-descriptions-item label="状态">
                <el-tag :type="getUserStatusType(userStore.userInfo?.status)">
                  {{ getUserStatusLabel(userStore.userInfo?.status) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="注册时间">
                {{ formatDate(userStore.userInfo?.createdAt) }}
              </el-descriptions-item>
            </el-descriptions>
          </div>

          <el-divider />

          <div class="user-actions">
            <el-button type="primary" @click="showEditDialog" :icon="Edit">
              编辑资料
            </el-button>
            <el-button @click="showPasswordDialog" :icon="Lock">
              修改密码
            </el-button>
          </div>
        </el-card>
      </el-col>

      <!-- 右侧统计和项目信息 -->
      <el-col :span="16">
        <!-- 统计卡片 -->
        <el-row :gutter="20" class="stats-row">
          <el-col :span="8">
            <el-card class="stat-card">
              <div class="stat-value">{{ stats.projectCount }}</div>
              <div class="stat-label">参与项目</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stat-card">
              <div class="stat-value">{{ stats.taskCount }}</div>
              <div class="stat-label">负责任务</div>
            </el-card>
          </el-col>
          <el-col :span="8">
            <el-card class="stat-card">
              <div class="stat-value">{{ stats.completedTaskCount }}</div>
              <div class="stat-label">已完成任务</div>
            </el-card>
          </el-col>
        </el-row>

        <!-- 最近参与的项目 -->
        <el-card class="projects-card">
          <template #header>
            <div class="card-header">
              <span>最近参与的项目</span>
              <el-button text type="primary" @click="router.push('/projects')">
                查看全部
              </el-button>
            </div>
          </template>

          <div v-if="projects.length > 0">
            <div
              v-for="project in projects.slice(0, 5)"
              :key="project.id"
              class="project-item"
              @click="router.push(`/projects/${project.id}`)"
            >
              <div class="project-info">
                <div class="project-name">{{ project.name }}</div>
                <div class="project-desc">{{ project.description || '暂无描述' }}</div>
              </div>
              <el-tag :type="getProjectStatusType(project.status)" size="small">
                {{ getProjectStatusLabel(project.status) }}
              </el-tag>
            </div>
          </div>
          <el-empty v-else description="暂无参与的项目" />
        </el-card>

        <!-- 最近活动 -->
        <el-card class="activities-card">
          <template #header>
            <div class="card-header">
              <span>最近活动</span>
              <el-button text type="primary" @click="router.push('/activities')">
                查看全部
              </el-button>
            </div>
          </template>

          <div v-if="recentActivities.length > 0">
            <el-timeline>
              <el-timeline-item
                v-for="activity in recentActivities"
                :key="activity.id"
                :type="getActivityType(activity)"
                :timestamp="formatTime(activity.createdAt)"
              >
                <div class="activity-item">
                  <span class="activity-action">{{ getActionText(activity) }}</span>
                  <span class="activity-entity">{{ getEntityTypeLabel(activity.entityType) }}</span>
                </div>
              </el-timeline-item>
            </el-timeline>
          </div>
          <el-empty v-else description="暂无活动记录" />
        </el-card>
      </el-col>
    </el-row>

    <!-- 编辑资料对话框 -->
    <el-dialog v-model="editDialogVisible" title="编辑资料" width="400px">
      <el-form :model="editForm" label-width="80px">
        <el-form-item label="昵称">
          <el-input v-model="editForm.name" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="editForm.email" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="editForm.phone" />
        </el-form-item>
        <el-form-item label="头像">
          <el-input v-model="editForm.avatar" placeholder="头像URL" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="editDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleUpdateProfile" :loading="updating">
          保存
        </el-button>
      </template>
    </el-dialog>

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
        <el-button type="primary" @click="handleChangePassword" :loading="changingPassword">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Edit, Lock } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useProjectStore } from '@/stores/project'
import { userApi, activityApi, statisticsApi } from '@/api'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const userStore = useUserStore()
const projectStore = useProjectStore()
const router = useRouter()

const defaultAvatar = 'https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png'

const loading = ref(false)
const recentActivities = ref<any[]>([])
const stats = reactive({
  projectCount: 0,
  taskCount: 0,
  completedTaskCount: 0,
})

const editDialogVisible = ref(false)
const updating = ref(false)
const editForm = reactive({
  name: '',
  email: '',
  phone: '',
  avatar: '',
})

const passwordDialogVisible = ref(false)
const changingPassword = ref(false)
const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const projects = computed(() => projectStore.myProjects)

onMounted(async () => {
  await loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    await Promise.all([
      loadUserStats(),
      loadRecentActivities(),
    ])
  } finally {
    loading.value = false
  }
}

const loadUserStats = async () => {
  try {
    stats.projectCount = projects.value.length

    const userId = userStore.userId as string
    if (userId) {
      const response = await statisticsApi.getUserStatistics(userId)
      if (response?.data?.code === 200) {
        const data = response.data.data
        stats.taskCount = data.totalTasks || 0
        stats.completedTaskCount = data.completedTasks || 0
      }
    }
  } catch (error) {
    console.error('加载统计数据失败:', error)
  }
}

const loadRecentActivities = async () => {
  try {
    const userId = userStore.userId as string
    if (!userId) return

    const response = await activityApi.getActivitiesByUser(userId, 0, 5)
    if (response?.data?.code === 200) {
      const data = response.data.data
      recentActivities.value = data.content || data || []
    }
  } catch (error) {
    console.error('加载活动记录失败:', error)
  }
}

const showEditDialog = () => {
  editForm.name = userStore.userInfo?.name || ''
  editForm.email = userStore.userInfo?.email || ''
  editForm.phone = userStore.userInfo?.phone || ''
  editForm.avatar = userStore.userInfo?.avatar || ''
  editDialogVisible.value = true
}

const handleUpdateProfile = async () => {
  updating.value = true
  try {
    const userId = userStore.userId as string
    const response = await userApi.updateUser(userId, {
      name: editForm.name,
      email: editForm.email,
      phone: editForm.phone,
      avatar: editForm.avatar,
    })
    if (response?.data?.code === 200) {
      ElMessage.success('资料更新成功')
      userStore.setUserInfo({ ...userStore.userInfo!, ...response.data.data })
      editDialogVisible.value = false
    } else {
      ElMessage.error(response?.data?.message || '更新失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '更新失败')
  } finally {
    updating.value = false
  }
}

const showPasswordDialog = () => {
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
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

  changingPassword.value = true
  try {
    const userId = userStore.userId as string
    const response = await userApi.changePassword(
      userId,
      passwordForm.oldPassword,
      passwordForm.newPassword
    )
    if (response?.data?.code === 200) {
      ElMessage.success('密码修改成功')
      passwordDialogVisible.value = false
    } else {
      ElMessage.error(response?.data?.message || '密码修改失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '密码修改失败')
  } finally {
    changingPassword.value = false
  }
}

const formatDate = (date?: string) => {
  if (!date) return '-'
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

const getUserStatusType = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'INACTIVE': return 'warning'
    case 'DELETED': return 'danger'
    default: return 'info'
  }
}

const getUserStatusLabel = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return '正常'
    case 'INACTIVE': return '已停用'
    case 'DELETED': return '已删除'
    default: return status || '未知'
  }
}

const getProjectStatusType = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return 'success'
    case 'ARCHIVED': return 'warning'
    case 'DELETED': return 'danger'
    default: return 'info'
  }
}

const getProjectStatusLabel = (status?: string) => {
  switch (status) {
    case 'ACTIVE': return '进行中'
    case 'ARCHIVED': return '已归档'
    case 'DELETED': return '已删除'
    default: return status || '未知'
  }
}

const getActivityType = (activity: any) => {
  switch (activity.entityType) {
    case 'PROJECT': return 'primary'
    case 'TASK': return 'warning'
    case 'COMMENT': return 'success'
    default: return 'info'
  }
}

const getActionText = (activity: any) => {
  const actionMap: Record<string, string> = {
    CREATE: '创建了',
    UPDATE: '更新了',
    DELETE: '删除了',
    COMPLETE: '完成了',
    ARCHIVE: '归档了',
    JOIN: '加入了',
  }
  return actionMap[activity.action] || activity.action
}

const getEntityTypeLabel = (entityType: string) => {
  const labelMap: Record<string, string> = {
    PROJECT: '项目',
    TASK: '任务',
    COMMENT: '评论',
    FILE: '文件',
    EVENT: '日程',
    MILESTONE: '里程碑',
  }
  return labelMap[entityType] || entityType
}
</script>

<style scoped>
.profile-page {
  padding: 20px;
}

.user-card {
  text-align: center;
}

.user-avatar-section {
  padding: 20px 0;
}

.user-name {
  margin: 15px 0 8px;
  font-size: 1.3em;
  color: #333;
}

.user-info-section {
  text-align: left;
}

.user-actions {
  display: flex;
  gap: 10px;
  justify-content: center;
  padding: 10px 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 2em;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 0.9em;
}

.projects-card,
.activities-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.project-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}

.project-item:last-child {
  border-bottom: none;
}

.project-item:hover {
  background: #f5f7fa;
}

.project-info {
  flex: 1;
  min-width: 0;
}

.project-name {
  font-weight: 500;
  color: #333;
}

.project-desc {
  font-size: 0.85em;
  color: #999;
  margin-top: 4px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.activity-item {
  display: flex;
  align-items: center;
  gap: 8px;
}

.activity-action {
  color: #666;
}

.activity-entity {
  color: #409eff;
  font-weight: 500;
}
</style>
