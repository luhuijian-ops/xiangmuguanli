<template>
  <div class="dashboard">
    <div class="welcome-section">
      <h1>欢迎回来，{{ userStore.userName }}！</h1>
      <p class="subtitle">今天是 {{ today }}，祝你工作愉快</p>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else>
      <!-- 统计卡片 -->
      <el-row :gutter="20" class="stats-row">
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <el-icon :size="32" color="#667eea"><FolderOpened /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.totalProjects || 0 }}</div>
                <div class="stat-label">总项目数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <el-icon :size="32" color="#52c41a"><DocumentChecked /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.totalTasks || 0 }}</div>
                <div class="stat-label">总任务数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <el-icon :size="32" color="#faad14"><User /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ statistics.totalUsers || 0 }}</div>
                <div class="stat-label">用户总数</div>
              </div>
            </div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card class="stat-card" shadow="hover">
            <div class="stat-content">
              <el-icon :size="32" color="#13c2c2"><Collection /></el-icon>
              <div class="stat-info">
                <div class="stat-number">{{ myProjectCount }}</div>
                <div class="stat-label">我的项目</div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 快捷操作 -->
      <el-row :gutter="20" class="quick-actions-row">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>快捷操作</span>
              </div>
            </template>
            <div class="quick-actions">
              <el-button type="primary" @click="router.push('/projects')">
                <el-icon><FolderOpened /></el-icon> 查看项目
              </el-button>
              <el-button type="success" @click="router.push('/tasks')">
                <el-icon><DocumentChecked /></el-icon> 任务列表
              </el-button>
              <el-button type="warning" @click="router.push('/schedule')">
                <el-icon><Calendar /></el-icon> 日程安排
              </el-button>
              <el-button type="info" @click="router.push('/workhours')">
                <el-icon><Timer /></el-icon> 记录工时
              </el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 我的任务 & 最近动态 -->
      <el-row :gutter="20" class="main-content-row">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>我的任务</span>
                <el-link type="primary" @click="router.push('/tasks')">查看全部</el-link>
              </div>
            </template>
            <div v-if="myTasks.length > 0" class="task-list">
              <div
                v-for="task in myTasks.slice(0, 5)"
                :key="task.id"
                class="task-item"
                @click="goToTask(task)"
              >
                <div class="task-main">
                  <el-tag :type="getStatusType(task.status)" size="small">{{ getStatusText(task.status) }}</el-tag>
                  <span class="task-title">{{ task.title }}</span>
                </div>
                <div class="task-meta">
                  <el-tag v-if="task.priority !== undefined" :type="getPriorityType(task.priority)" size="small">
                    P{{ task.priority }}
                  </el-tag>
                  <span v-if="task.dueDate" :class="['due-date', isOverdue(task.dueDate) ? 'overdue' : '']">
                    <el-icon><Clock /></el-icon>
                    {{ formatDate(task.dueDate) }}
                  </span>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无分配给你的任务" />
          </el-card>
        </el-col>

        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>最近动态</span>
                <el-link type="primary" @click="router.push('/activities')">查看全部</el-link>
              </div>
            </template>
            <div v-if="recentActivities.length > 0" class="activity-list">
              <div
                v-for="activity in recentActivities.slice(0, 6)"
                :key="activity.id"
                class="activity-item"
              >
                <div class="activity-icon">
                  <el-icon :size="16"><component :is="getActivityIcon(activity.type)" /></el-icon>
                </div>
                <div class="activity-content">
                  <div class="activity-text">{{ activity.content }}</div>
                  <div class="activity-time">{{ formatRelativeTime(activity.createdAt) }}</div>
                </div>
              </div>
            </div>
            <el-empty v-else description="暂无最近动态" />
          </el-card>
        </el-col>
      </el-row>

      <!-- 即将到期 -->
      <el-row :gutter="20" class="upcoming-row">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>即将到期</span>
                <span class="header-desc">未来7天内到期的任务</span>
              </div>
            </template>
            <div v-if="upcomingTasks.length > 0" class="upcoming-list">
              <el-table :data="upcomingTasks" size="small">
                <el-table-column prop="title" label="任务标题" min-width="200">
                  <template #default="{ row }">
                    <el-link type="primary" @click="goToTask(row)">{{ row.title }}</el-link>
                  </template>
                </el-table-column>
                <el-table-column prop="projectName" label="所属项目" width="150">
                  <template #default="{ row }">
                    {{ row.projectName || '-' }}
                  </template>
                </el-table-column>
                <el-table-column prop="status" label="状态" width="100">
                  <template #default="{ row }">
                    <el-tag :type="getStatusType(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
                  </template>
                </el-table-column>
                <el-table-column prop="dueDate" label="截止日期" width="150">
                  <template #default="{ row }">
                    <span :class="isOverdue(row.dueDate) ? 'overdue-text' : ''">
                      {{ formatDate(row.dueDate) }}
                    </span>
                  </template>
                </el-table-column>
                <el-table-column prop="daysLeft" label="剩余天数" width="100">
                  <template #default="{ row }">
                    <el-tag v-if="isOverdue(row.dueDate)" type="danger" size="small">已逾期</el-tag>
                    <el-tag v-else-if="getDaysLeft(row.dueDate) <= 2" type="warning" size="small">{{ getDaysLeft(row.dueDate) }} 天</el-tag>
                    <span v-else>{{ getDaysLeft(row.dueDate) }} 天</span>
                  </template>
                </el-table-column>
              </el-table>
            </div>
            <el-empty v-else description="未来7天内没有即将到期的任务" />
          </el-card>
        </el-col>
      </el-row>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { statisticsApi, taskApi, activityApi } from '@/api'
import { handleApiResponse } from '@/utils/response'
import { useProject } from '@/composables/useProject'
import dayjs from 'dayjs'
import {
  FolderOpened, DocumentChecked, User, Collection,
  Clock, Calendar, Timer, Plus, Bell, Check, Edit
} from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const { projects, fetchProjects } = useProject()

const loading = ref(false)
const statistics = ref<any>({})
const myProjectCount = ref(0)
const myTasks = ref<any[]>([])
const recentActivities = ref<any[]>([])

const today = computed(() => dayjs().format('YYYY年MM月DD日'))

const upcomingTasks = computed(() => {
  const now = dayjs()
  const sevenDaysLater = now.add(7, 'day')
  return myTasks.value
    .filter(task => task.dueDate)
    .filter(task => {
      const due = dayjs(task.dueDate)
      return due.isBefore(sevenDaysLater) || due.isSame(sevenDaysLater, 'day')
    })
    .sort((a, b) => dayjs(a.dueDate).unix() - dayjs(b.dueDate).unix())
})

const loadDashboardStatistics = async () => {
  loading.value = true
  try {
    const response = await statisticsApi.getDashboardStatistics()
    const result = handleApiResponse(response, '获取统计数据失败')
    if (result.success && result.data) {
      statistics.value = result.data
    }
  } catch (error) {
    console.error('获取统计数据失败:', error)
  }
}

const loadMyTasks = async () => {
  const userId = userStore.userId
  if (!userId) return
  try {
    const response = await taskApi.getTasksByAssignee(userId.toString(), { page: 0, pageSize: 20 })
    const result = handleApiResponse(response, '获取任务列表失败')
    if (result.success && result.data) {
      myTasks.value = result.data.content || result.data || []
    }
  } catch (error) {
    console.error('获取我的任务失败:', error)
  }
}

const loadRecentActivities = async () => {
  const userId = userStore.userId
  if (!userId) return
  try {
    const response = await activityApi.getActivitiesByUser(userId.toString(), 0, 10)
    const result = handleApiResponse(response, '获取动态失败')
    if (result.success && result.data) {
      const data = result.data.content || result.data
      recentActivities.value = Array.isArray(data) ? data : []
    }
  } catch (error) {
    console.error('获取最近动态失败:', error)
  }
}

onMounted(async () => {
  await Promise.all([
    loadDashboardStatistics(),
    fetchProjects(),
    loadMyTasks(),
    loadRecentActivities(),
  ])
  myProjectCount.value = projects.value.length
  loading.value = false
})

const getStatusType = (status: string) => {
  const map: Record<string, any> = { TODO: '', DOING: 'warning', DONE: 'success', ARCHIVED: 'info' }
  return map[status] || ''
}

const getStatusText = (status: string) => {
  const map: Record<string, string> = { TODO: '待处理', DOING: '进行中', DONE: '已完成', ARCHIVED: '已归档' }
  return map[status] || status
}

const getPriorityType = (priority: number) => {
  const map: Record<number, string> = { 0: 'danger', 1: 'warning', 2: '', 3: 'info', 4: 'info' }
  return map[priority] || ''
}

const getActivityIcon = (type: string) => {
  const map: Record<string, any> = {
    TASK_CREATED: Plus,
    TASK_UPDATED: Edit,
    TASK_COMPLETED: Check,
    COMMENT_ADDED: Bell,
  }
  return map[type] || Bell
}

const formatDate = (date: string) => dayjs(date).format('MM-DD')

const formatRelativeTime = (date: string) => {
  const d = dayjs(date)
  const now = dayjs()
  const diffHours = now.diff(d, 'hour')
  if (diffHours < 1) return '刚刚'
  if (diffHours < 24) return `${diffHours}小时前`
  const diffDays = now.diff(d, 'day')
  if (diffDays < 7) return `${diffDays}天前`
  return d.format('MM-DD HH:mm')
}

const isOverdue = (date: string) => dayjs(date).isBefore(dayjs(), 'day')

const getDaysLeft = (date: string) => dayjs(date).diff(dayjs(), 'day')

const goToTask = (task: any) => {
  if (task.projectId) {
    router.push({ path: '/tasks', query: { projectId: task.projectId.toString() } })
  }
}
</script>

<style scoped>
.dashboard {
  padding: 20px;
}

.welcome-section {
  margin-bottom: 24px;
}

.welcome-section h1 {
  color: #333;
  margin: 0 0 8px 0;
  font-size: 1.8em;
}

.subtitle {
  color: #666;
  margin: 0;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.stat-info {
  flex: 1;
}

.stat-number {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin: 0 0 4px 0;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.quick-actions-row {
  margin-bottom: 20px;
}

.quick-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.quick-actions .el-button {
  display: flex;
  align-items: center;
  gap: 6px;
}

.main-content-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-desc {
  font-size: 12px;
  color: #999;
}

.task-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.task-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  border-radius: 6px;
  background: #f8f9fa;
  cursor: pointer;
  transition: all 0.3s;
}

.task-item:hover {
  background: #e8eaf6;
}

.task-main {
  display: flex;
  align-items: center;
  gap: 8px;
  flex: 1;
  min-width: 0;
}

.task-main .task-title {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: #333;
}

.task-meta {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}

.due-date {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  color: #666;
}

.due-date.overdue {
  color: #f5222d;
}

.overdue-text {
  color: #f5222d;
  font-weight: 500;
}

.activity-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.activity-item {
  display: flex;
  gap: 10px;
  padding: 8px 0;
  border-bottom: 1px solid #f0f0f0;
}

.activity-item:last-child {
  border-bottom: none;
}

.activity-icon {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e8eaf6;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  color: #667eea;
}

.activity-content {
  flex: 1;
  min-width: 0;
}

.activity-text {
  color: #333;
  font-size: 14px;
  line-height: 1.5;
}

.activity-time {
  color: #999;
  font-size: 12px;
  margin-top: 2px;
}

.upcoming-row {
  margin-bottom: 20px;
}

.loading-container {
  padding: 40px;
}
</style>
