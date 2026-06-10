<template>
  <div class="notification-center">
    <div class="header">
      <h1>通知中心</h1>
      <div class="header-actions">
        <el-radio-group v-model="filterRead" size="small" @change="handleFilterChange">
          <el-radio-button :label="undefined">全部</el-radio-button>
          <el-radio-button :label="false">未读</el-radio-button>
          <el-radio-button :label="true">已读</el-radio-button>
        </el-radio-group>
        <el-button
          v-if="unreadCount > 0"
          size="small"
          type="primary"
          @click="handleMarkAllRead"
        >
          全部已读
        </el-button>
        <el-button size="small" @click="refreshNotifications" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else class="notification-list">
      <div
        v-for="notification in notifications"
        :key="notification.id"
        class="notification-item"
        :class="{ unread: !notification.isRead }"
        @click="handleNotificationClick(notification)"
      >
        <div class="notification-icon">
          <el-icon :size="20" :color="getIconColor(notification.type)">
            <component :is="getIcon(notification.type)" />
          </el-icon>
        </div>
        <div class="notification-content">
          <div class="notification-header">
            <span class="notification-title">{{ notification.title }}</span>
            <el-tag v-if="!notification.isRead" type="danger" size="small">未读</el-tag>
          </div>
          <div class="notification-body">{{ notification.content }}</div>
          <div class="notification-time">{{ formatTime(notification.createdAt) }}</div>
        </div>
        <div class="notification-actions">
          <el-button
            v-if="!notification.isRead"
            size="small"
            text
            type="primary"
            @click.stop="handleMarkRead(notification)"
          >
            标记已读
          </el-button>
        </div>
      </div>
    </div>

    <div v-if="!loading && notifications.length === 0" class="empty-state">
      <el-empty :description="filterRead === false ? '暂无未读通知' : '暂无通知'" />
    </div>

    <div v-if="hasMore" class="load-more">
      <el-button @click="loadMore" :loading="loadingMore">加载更多</el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Bell, ChatDotRound, User, Check, Flag, Warning } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { notificationApi } from '@/api'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const userStore = useUserStore()
const router = useRouter()

const notifications = ref<any[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const filterRead = ref<boolean | undefined>(undefined)
const currentPage = ref(0)
const totalPages = ref(0)
const unreadCount = ref(0)
const hasMore = ref(false)

onMounted(() => {
  loadNotifications()
  loadUnreadCount()
})

const loadNotifications = async (page = 0) => {
  if (page === 0) loading.value = true
  else loadingMore.value = true

  try {
    const userId = userStore.userId as string
    const response = await notificationApi.getNotifications(
      userId,
      filterRead.value,
      page,
      20
    )

    if (response?.data?.code === 200) {
      const data = response.data.data
      const newNotifications = data.content || data || []

      if (page === 0) {
        notifications.value = newNotifications
      } else {
        notifications.value.push(...newNotifications)
      }

      currentPage.value = data.number ?? page
      totalPages.value = data.totalPages ?? 1
      hasMore.value = currentPage.value < totalPages.value - 1
    }
  } catch (error) {
    console.error('加载通知失败:', error)
    ElMessage.error('加载通知失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadUnreadCount = async () => {
  try {
    const userId = userStore.userId as string
    const response = await notificationApi.getUnreadCount(userId)
    if (response?.data?.code === 200) {
      unreadCount.value = response.data.data || 0
    }
  } catch (error) {
    console.error('加载未读数失败:', error)
  }
}

const handleFilterChange = () => {
  notifications.value = []
  loadNotifications(0)
}

const refreshNotifications = () => {
  notifications.value = []
  loadNotifications(0)
  loadUnreadCount()
}

const loadMore = () => {
  loadNotifications(currentPage.value + 1)
}

const handleMarkRead = async (notification: any) => {
  try {
    const userId = userStore.userId as string
    const response = await notificationApi.markAsRead(
      notification.id.toString(),
      userId
    )
    if (response?.data?.code === 200) {
      notification.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
      ElMessage.success('已标记为已读')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleMarkAllRead = async () => {
  try {
    const userId = userStore.userId as string
    const response = await notificationApi.markAllAsRead(userId)
    if (response?.data?.code === 200) {
      notifications.value.forEach(n => (n.isRead = true))
      unreadCount.value = 0
      ElMessage.success('全部已标记为已读')
    }
  } catch (error) {
    ElMessage.error('操作失败')
  }
}

const handleNotificationClick = (notification: any) => {
  if (!notification.isRead) {
    handleMarkRead(notification)
  }
  // 根据通知类型跳转
  if (notification.entityType === 'TASK' && notification.entityId) {
    router.push(`/tasks/${notification.entityId}`)
  } else if (notification.entityType === 'PROJECT' && notification.entityId) {
    router.push(`/projects/${notification.entityId}`)
  }
}

const getIcon = (type: string) => {
  const iconMap: Record<string, any> = {
    TASK_ASSIGNED: Check,
    COMMENT: ChatDotRound,
    MENTION: User,
    PROJECT_INVITE: Bell,
    MILESTONE_DUE: Flag,
    SYSTEM: Warning,
  }
  return iconMap[type] || Bell
}

const getIconColor = (type: string) => {
  const colorMap: Record<string, string> = {
    TASK_ASSIGNED: '#52c41a',
    COMMENT: '#1890ff',
    MENTION: '#fa8c16',
    PROJECT_INVITE: '#722ed1',
    MILESTONE_DUE: '#f5222d',
    SYSTEM: '#8c8c8c',
  }
  return colorMap[type] || '#8c8c8c'
}

const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}
</script>

<style scoped>
.notification-center {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h1 {
  margin: 0;
  color: #333;
  font-size: 2em;
}

.header-actions {
  display: flex;
  gap: 10px;
  align-items: center;
}

.loading-container {
  padding: 20px;
}

.notification-list {
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.notification-item {
  display: flex;
  align-items: flex-start;
  gap: 15px;
  padding: 15px 20px;
  border-bottom: 1px solid #f0f0f0;
  cursor: pointer;
  transition: background 0.3s;
}

.notification-item:last-child {
  border-bottom: none;
}

.notification-item:hover {
  background: #f5f7fa;
}

.notification-item.unread {
  background: #e6f7ff;
}

.notification-item.unread:hover {
  background: #d9f2ff;
}

.notification-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.notification-content {
  flex: 1;
  min-width: 0;
}

.notification-header {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 6px;
}

.notification-title {
  font-weight: 600;
  color: #333;
}

.notification-body {
  color: #666;
  font-size: 14px;
  line-height: 1.5;
  margin-bottom: 4px;
}

.notification-time {
  font-size: 12px;
  color: #999;
}

.notification-actions {
  flex-shrink: 0;
}

.empty-state {
  padding: 50px;
  text-align: center;
}

.load-more {
  text-align: center;
  padding: 20px;
}
</style>
