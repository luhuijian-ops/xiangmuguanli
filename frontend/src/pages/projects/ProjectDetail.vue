<template>
  <div class="project-detail">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="project" class="detail-container">
      <div class="detail-header">
        <h1>
          {{ project.name }}
          <el-tag v-if="isArchived" type="info" size="large" style="margin-left: 12px">
            已归档
          </el-tag>
        </h1>
        <div class="header-actions">
          <el-button @click="goToBoard" type="primary" :icon="Grid">
            看板
          </el-button>
          <el-button @click="goToTasks" :icon="List">
            任务列表
          </el-button>
          <el-button
            v-if="!isArchived"
            type="warning"
            :icon="Folder"
            @click="handleArchive"
          >
            归档
          </el-button>
          <el-button
            v-else
            type="success"
            :icon="FolderOpened"
            @click="handleUnarchive"
          >
            取消归档
          </el-button>
        </div>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="项目信息" name="info">
          <div class="info-section">
            <div class="info-item">
              <span class="label">项目编码：</span>
              <span class="value">{{ project.code || '无' }}</span>
            </div>
            <div class="info-item">
              <span class="label">项目状态：</span>
              <el-tag :type="getStatusType(project.status)">
                {{ getStatusText(project.status) }}
              </el-tag>
            </div>
            <div class="info-item">
              <span class="label">创建时间：</span>
              <span class="value">{{ formatDateTime(project.createdAt) }}</span>
            </div>
            <div v-if="project.startDate" class="info-item">
              <span class="label">开始时间：</span>
              <span class="value">{{ formatDate(project.startDate) }}</span>
            </div>
            <div v-if="project.endDate" class="info-item">
              <span class="label">结束时间：</span>
              <span class="value">{{ formatDate(project.endDate) }}</span>
            </div>
            <div v-if="project.description" class="info-item description">
              <span class="label">项目描述：</span>
              <div class="value">{{ project.description }}</div>
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="项目成员" name="members">
          <member-manager :project-id="projectId" :readonly="isArchived" @refresh="fetchProjectData" />
        </el-tab-pane>

        <el-tab-pane label="项目统计" name="statistics">
          <div class="statistics-section">
            <el-row :gutter="20">
              <el-col :span="6">
                <div class="stat-card">
                  <h3>总任务数</h3>
                  <p class="stat-number">{{ statistics.totalTasks || 0 }}</p>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <h3>进行中任务</h3>
                  <p class="stat-number">{{ statistics.inProgressTasks || 0 }}</p>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <h3>已完成任务</h3>
                  <p class="stat-number">{{ statistics.completedTasks || 0 }}</p>
                </div>
              </el-col>
              <el-col :span="6">
                <div class="stat-card">
                  <h3>成员数量</h3>
                  <p class="stat-number">{{ statistics.memberCount || 0 }}</p>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="甘特图" name="gantt" lazy>
          <div class="gantt-section">
            <div v-if="loadingTasks" class="loading-container">
              <el-skeleton :rows="5" animated />
            </div>
            <gantt-chart
              v-else
              :tasks="projectTasks"
              @task-click="handleGanttTaskClick"
            />
          </div>
        </el-tab-pane>

        <el-tab-pane label="文件" name="files" lazy>
          <div class="files-section">
            <div class="files-toolbar">
              <el-upload
                action="#"
                :auto-upload="false"
                :on-change="handleFileUpload"
                :show-file-list="false"
              >
                <el-button type="primary" :icon="Plus">上传文件</el-button>
              </el-upload>
            </div>

            <div v-if="loadingFiles" class="loading-container">
              <el-skeleton :rows="3" animated />
            </div>

            <div v-else-if="projectFiles.length > 0" class="files-list">
              <el-row :gutter="20">
                <el-col :span="8" v-for="file in projectFiles" :key="file.id">
                  <el-card :body-style="{ padding: '12px' }" shadow="hover" class="file-card">
                    <div class="file-header">
                      <el-icon :size="24" color="#667eea"><Document /></el-icon>
                      <el-tooltip :content="file.name" placement="top">
                        <el-link
                          :href="`/api/v1/files/${file.id}/download`"
                          target="_blank"
                          :underline="false"
                          class="file-name"
                        >
                          {{ file.name }}
                        </el-link>
                      </el-tooltip>
                    </div>
                    <div class="file-meta">
                      <span class="file-size">{{ formatFileSize(file.size) }}</span>
                      <span class="file-type">{{ getFileType(file.type) }}</span>
                    </div>
                    <div class="file-footer">
                      <span class="file-uploader">{{ file.uploaderName || '未知' }}</span>
                      <el-button
                        type="danger"
                        :icon="Delete"
                        size="small"
                        text
                        @click="handleDeleteFile(file)"
                      >
                        删除
                      </el-button>
                    </div>
                  </el-card>
                </el-col>
              </el-row>
            </div>

            <el-empty v-else description="暂无文件" />
          </div>
        </el-tab-pane>
      </el-tabs>
    </div>

    <div v-else class="error-container">
      <el-result icon="error" title="项目不存在" sub-title="请检查项目 ID 是否正确">
        <template #extra>
          <el-button type="primary" @click="goToList">返回项目列表</el-button>
        </template>
      </el-result>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Grid, List, Folder, FolderOpened, Plus, Delete, Document } from '@element-plus/icons-vue'
import { useProject } from '@/composables/useProject'
import { statisticsApi, taskApi, fileApi } from '@/api'
import { projectApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { Project, Task } from '@/types'
import GanttChart from '@/components/project/GanttChart.vue'
import dayjs from 'dayjs'
import { ElMessage, ElMessageBox } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const projectId = route.params.id as string

const {
  fetchProject,
  archiveProject,
  unarchiveProject,
  loading,
} = useProject()

const project = ref<Project | null>(null)
const activeTab = ref('info')
const isArchived = computed(() => project.value?.status === 'ARCHIVED')
const statistics = ref({
  totalTasks: 0,
  inProgressTasks: 0,
  completedTasks: 0,
  memberCount: 0,
})

// 甘特图和文件相关状态
const projectTasks = ref<Task[]>([])
const loadingTasks = ref(false)
const projectFiles = ref<any[]>([])
const loadingFiles = ref(false)

onMounted(async () => {
  await fetchProjectData()
})

const fetchProjectData = async () => {
  const result = await fetchProject(projectId)
  if (result.success) {
    project.value = result.data
    await loadProjectStatistics()
  }
}

const loadProjectStatistics = async () => {
  try {
    const [statsResponse, membersResponse] = await Promise.all([
      statisticsApi.getProjectStatistics(projectId),
      projectApi.getMembers(projectId),
    ])

    if (statsResponse && statsResponse.data && statsResponse.data.code === 200) {
      const data = statsResponse.data.data || {}
      statistics.value = {
        totalTasks: data.totalTasks || 0,
        inProgressTasks: data.doingTasks || 0,
        completedTasks: data.doneTasks || 0,
        memberCount: 0,
      }
    }

    if (membersResponse && membersResponse.data && membersResponse.data.code === 200) {
      statistics.value.memberCount = (membersResponse.data.data || []).length
    }
  } catch (error) {
    console.error('获取项目统计失败:', error)
  }
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    ACTIVE: 'success',
    ARCHIVED: 'info',
    DELETED: 'danger',
  }
  return typeMap[status] || 'info'
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    ACTIVE: '进行中',
    ARCHIVED: '已归档',
    DELETED: '已删除',
  }
  return textMap[status] || status
}

const formatDateTime = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const goToBoard = () => {
  router.push(`/projects/${projectId}/board`)
}

const goToTasks = () => {
  router.push(`/tasks?projectId=${projectId}`)
}

const goToList = () => {
  router.push('/projects')
}

const handleArchive = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要归档项目 "${project.value?.name}" 吗？归档后项目将变为只读。`,
      '确认归档',
      { confirmButtonText: '归档', cancelButtonText: '取消', type: 'warning' }
    )
    const result = await archiveProject(projectId)
    if (result.success) {
      ElMessage.success('项目已归档')
      await fetchProjectData()
    } else {
      ElMessage.error(result.message || '归档失败')
    }
  } catch {
    // 取消
  }
}

const handleUnarchive = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要取消归档项目 "${project.value?.name}" 吗？`,
      '确认取消归档',
      { confirmButtonText: '取消归档', cancelButtonText: '取消', type: 'info' }
    )
    const result = await unarchiveProject(projectId)
    if (result.success) {
      ElMessage.success('项目已恢复')
      await fetchProjectData()
    } else {
      ElMessage.error(result.message || '取消归档失败')
    }
  } catch {
    // 取消
  }
}

// 加载项目任务（用于甘特图）
const loadProjectTasks = async () => {
  if (projectTasks.value.length > 0) return
  loadingTasks.value = true
  try {
    const response = await taskApi.getTasks(Number(projectId), { page: 0, pageSize: 1000 })
    if (response?.data?.code === 200) {
      const data = response.data.data
      projectTasks.value = data.content || data || []
    }
  } catch (error) {
    console.error('加载项目任务失败:', error)
  } finally {
    loadingTasks.value = false
  }
}

// 加载项目文件
const loadProjectFiles = async () => {
  if (projectFiles.value.length > 0) return
  loadingFiles.value = true
  try {
    const response = await fileApi.getFilesByProject(projectId)
    if (response?.data?.code === 200) {
      projectFiles.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载项目文件失败:', error)
  } finally {
    loadingFiles.value = false
  }
}

// 处理文件上传
const handleFileUpload = async (uploadFile: any) => {
  const rawFile = uploadFile.raw as File
  if (!rawFile) return

  const maxSize = 50 * 1024 * 1024
  if (rawFile.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return
  }

  try {
    const response = await fileApi.upload(rawFile, {
      projectId: Number(projectId),
      entity: 'project',
      uploadedById: parseInt(userStore.userId as string) || 0,
    })
    if (response?.data?.code === 200) {
      ElMessage.success('文件上传成功')
      projectFiles.value = []
      await loadProjectFiles()
    } else {
      ElMessage.error(response?.data?.message || '上传失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '文件上传失败')
  }
}

// 删除项目文件
const handleDeleteFile = async (file: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${file.name}" 吗？`,
      '确认删除',
      { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
    )
    const response = await fileApi.delete(file.id)
    if (response?.data?.code === 200) {
      ElMessage.success('文件删除成功')
      projectFiles.value = projectFiles.value.filter(f => f.id !== file.id)
    } else {
      ElMessage.error(response?.data?.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '删除失败')
    }
  }
}

// 处理甘特图任务点击
const handleGanttTaskClick = (task: Task) => {
  router.push(`/tasks/${task.id}`)
}

// 监听标签页切换，懒加载数据
watch(activeTab, (tab) => {
  if (tab === 'gantt') {
    loadProjectTasks()
  } else if (tab === 'files') {
    loadProjectFiles()
  }
})

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const getFileType = (type: string) => {
  const typeMap: Record<string, string> = {
    'image/jpeg': 'JPG',
    'image/png': 'PNG',
    'image/gif': 'GIF',
    'application/pdf': 'PDF',
    'application/msword': 'Word',
    'application/vnd.ms-excel': 'Excel',
    'application/vnd.ms-powerpoint': 'PPT',
  }
  return typeMap[type] || '文件'
}
</script>

<style scoped>
.project-detail {
  padding: 20px;
}

.loading-container {
  padding: 20px;
}

.detail-header {
  margin-bottom: 30px;
}

.detail-header h1 {
  margin: 0 0 20px 0;
  color: #333;
  font-size: 2em;
}

.header-actions {
  display: flex;
  gap: 15px;
}

.info-section {
  padding: 20px;
}

.info-item {
  display: flex;
  margin-bottom: 20px;
  align-items: flex-start;
}

.info-item.description {
  flex-direction: column;
  gap: 10px;
}

.label {
  width: 120px;
  font-weight: 500;
  color: #666;
  flex-shrink: 0;
}

.value {
  color: #333;
  flex: 1;
}

.statistics-section {
  padding: 20px;
}

.stat-card {
  background: white;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.stat-card h3 {
  margin: 0 0 10px 0;
  color: #666;
  font-size: 0.9em;
}

.stat-number {
  font-size: 2.5em;
  font-weight: bold;
  color: #667eea;
  margin: 0;
}

.error-container {
  padding: 50px;
}

.gantt-section {
  padding: 10px;
}

.files-section {
  padding: 20px;
}

.files-toolbar {
  margin-bottom: 20px;
}

.files-list {
  margin-top: 10px;
}

.file-card {
  margin-bottom: 15px;
}

.file-header {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 8px;
}

.file-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
}

.file-meta {
  display: flex;
  gap: 10px;
  font-size: 12px;
  color: #999;
  margin-bottom: 8px;
}

.file-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #666;
}

.file-uploader {
  color: #999;
}
</style>
