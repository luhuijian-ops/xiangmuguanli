<template>
  <div class="activity-list">
    <div class="header">
      <h1>活动流</h1>
      <div class="header-actions">
        <el-select
          v-model="filterType"
          placeholder="筛选类型"
          clearable
          style="width: 150px"
          @change="handleFilterChange"
        >
          <el-option label="全部" value="" />
          <el-option label="项目" value="PROJECT" />
          <el-option label="任务" value="TASK" />
          <el-option label="评论" value="COMMENT" />
          <el-option label="文件" value="FILE" />
          <el-option label="日程" value="EVENT" />
          <el-option label="里程碑" value="MILESTONE" />
        </el-select>
        <el-button @click="refreshActivities" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else class="activities-container">
      <el-timeline v-if="activities.length > 0">
        <el-timeline-item
          v-for="activity in activities"
          :key="activity.id"
          :type="getActivityType(activity)"
          :timestamp="formatTime(activity.createdAt)"
          placement="top"
        >
          <el-card class="activity-card" shadow="hover">
            <div class="activity-content">
              <div class="activity-icon">
                <el-icon :size="18" :color="getIconColor(activity)">
                  <component :is="getIcon(activity)" />
                </el-icon>
              </div>
              <div class="activity-info">
                <div class="activity-text">
                  <span class="user-name">{{ activity.userName || '未知用户' }}</span>
                  <span class="action-text">{{ getActionText(activity) }}</span>
                  <span class="entity-type">{{ getEntityTypeLabel(activity.entityType) }}</span>
                  <el-link
                    v-if="activity.entityId"
                    type="primary"
                    :underline="false"
                    @click="handleEntityClick(activity)"
                  >
                    详情
                  </el-link>
                </div>
                <div class="activity-meta" v-if="activity.projectName">
                  <el-tag size="small" type="info">{{ activity.projectName }}</el-tag>
                </div>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>

      <div class="load-more" v-if="hasMore">
        <el-button @click="loadMore" :loading="loadingMore">
          加载更多
        </el-button>
      </div>
    </div>

    <div v-if="!loading && activities.length === 0" class="empty-state">
      <el-empty description="暂无活动记录" />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, FolderOpened, Document, ChatDotRound, Upload, Calendar, Flag } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { activityApi } from '@/api'
import type { Activity } from '@/types'
import { useRouter } from 'vue-router'
import dayjs from 'dayjs'
import relativeTime from 'dayjs/plugin/relativeTime'
import 'dayjs/locale/zh-cn'

dayjs.extend(relativeTime)
dayjs.locale('zh-cn')

const userStore = useUserStore()
const router = useRouter()

const activities = ref<Activity[]>([])
const loading = ref(false)
const loadingMore = ref(false)
const filterType = ref('')
const currentPage = ref(0)
const totalPages = ref(0)

const hasMore = ref(false)

onMounted(() => {
  loadActivities()
})

const loadActivities = async (page = 0) => {
  if (page === 0) loading.value = true
  else loadingMore.value = true

  try {
    const userId = userStore.userId as string
    const response = await activityApi.getActivitiesByUser(userId, page, 20)

    if (response?.data?.code === 200) {
      const data = response.data.data
      const newActivities = data.content || data || []

      if (page === 0) {
        activities.value = newActivities
      } else {
        activities.value.push(...newActivities)
      }

      currentPage.value = data.number ?? page
      totalPages.value = data.totalPages ?? 1
      hasMore.value = currentPage.value < totalPages.value - 1
    }
  } catch (error) {
    console.error('加载活动流失败:', error)
    ElMessage.error('加载活动流失败')
  } finally {
    loading.value = false
    loadingMore.value = false
  }
}

const loadMore = () => {
  loadActivities(currentPage.value + 1)
}

const refreshActivities = () => {
  activities.value = []
  loadActivities(0)
}

const handleFilterChange = () => {
  // 简单的客户端筛选
  // 如果需要服务端筛选，可以扩展 API
}

const handleEntityClick = (activity: Activity) => {
  switch (activity.entityType) {
    case 'PROJECT':
      if (activity.projectId) {
        router.push(`/projects/${activity.projectId}`)
      }
      break
    case 'TASK':
      if (activity.entityId) {
        router.push(`/tasks/${activity.entityId}`)
      }
      break
  }
}

const formatTime = (time: string) => {
  return dayjs(time).fromNow()
}

const getActivityType = (activity: Activity) => {
  switch (activity.entityType) {
    case 'PROJECT': return 'primary'
    case 'TASK': return 'warning'
    case 'COMMENT': return 'success'
    case 'FILE': return 'info'
    case 'EVENT': return 'danger'
    case 'MILESTONE': return 'warning'
    default: return 'info'
  }
}

const getIcon = (activity: Activity) => {
  switch (activity.entityType) {
    case 'PROJECT': return FolderOpened
    case 'TASK': return Document
    case 'COMMENT': return ChatDotRound
    case 'FILE': return Upload
    case 'EVENT': return Calendar
    case 'MILESTONE': return Flag
    default: return Document
  }
}

const getIconColor = (activity: Activity) => {
  switch (activity.entityType) {
    case 'PROJECT': return '#409eff'
    case 'TASK': return '#e6a23c'
    case 'COMMENT': return '#67c23a'
    case 'FILE': return '#909399'
    case 'EVENT': return '#f56c6c'
    case 'MILESTONE': return '#667eea'
    default: return '#909399'
  }
}

const getActionText = (activity: Activity) => {
  const actionMap: Record<string, string> = {
    CREATE: '创建了',
    UPDATE: '更新了',
    DELETE: '删除了',
    ASSIGN: '分配了',
    COMPLETE: '完成了',
    ARCHIVE: '归档了',
    RESTORE: '恢复了',
    JOIN: '加入了',
    LEAVE: '离开了',
    UPLOAD: '上传了',
    DOWNLOAD: '下载了',
    COMMENT: '评论了',
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
    USER: '用户',
  }
  return labelMap[entityType] || entityType
}
</script>

<style scoped>
.activity-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
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

.activities-container {
  max-width: 800px;
}

.activity-card {
  margin-bottom: 5px;
}

.activity-content {
  display: flex;
  align-items: flex-start;
  gap: 12px;
}

.activity-icon {
  flex-shrink: 0;
  margin-top: 2px;
}

.activity-info {
  flex: 1;
}

.activity-text {
  font-size: 14px;
  line-height: 1.6;
}

.user-name {
  font-weight: 600;
  color: #333;
  margin-right: 4px;
}

.action-text {
  color: #666;
  margin-right: 4px;
}

.entity-type {
  color: #409eff;
  font-weight: 500;
  margin-right: 8px;
}

.activity-meta {
  margin-top: 6px;
}

.load-more {
  text-align: center;
  padding: 20px;
}

.empty-state {
  padding: 50px;
  text-align: center;
}
</style>
