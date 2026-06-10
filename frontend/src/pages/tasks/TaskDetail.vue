<template>
  <div class="task-detail">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else-if="task" class="detail-container">
      <div class="detail-header">
        <el-page-header @icon="ArrowLeft" @back="goBack">
          <template #title>
            <span>{{ task.title }}</span>
          </template>
        </el-page-header>
      </div>

      <el-tabs v-model="activeTab">
        <el-tab-pane label="基本信息" name="info">
          <div class="info-section">
            <el-row :gutter="20">
              <el-col :span="8">
                <div class="info-item">
                  <span class="label">任务标题：</span>
                  <span class="value">{{ task.title }}</span>
                </div>
              </el-col>
              <el-col :span="16">
                <div class="info-item">
                  <span class="label">任务描述：</span>
                  <div class="value">{{ task.description || '无' }}</div>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="8">
                <div class="info-item">
                  <span class="label">任务状态：</span>
                  <el-tag :type="getStatusType(task.status)">{{ getStatusText(task.status) }}</el-tag>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-item">
                  <span class="label">优先级：</span>
                  <el-tag :type="getPriorityType(task.priority)" size="small">P{{ task.priority }}</el-tag>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="8">
                <div class="info-item">
                  <span class="label">负责人：</span>
                  <span class="value">{{ task.assigneeName || '未分配' }}</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-item">
                  <span class="label">开始时间：</span>
                  <span class="value">{{ formatDateTime(task.startDate) || '未设置' }}</span>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="8">
                <div class="info-item">
                  <span class="label">截止时间：</span>
                  <span class="value">{{ formatDateTime(task.dueDate) || '未设置' }}</span>
                </div>
              </el-col>
              <el-col :span="8">
                <div class="info-item">
                  <span class="label">创建人：</span>
                  <span class="value">{{ task.creatorName || '未知' }}</span>
                </div>
              </el-col>
            </el-row>

            <el-row :gutter="20">
              <el-col :span="8">
                <div class="info-item">
                  <span class="label">创建时间：</span>
                  <span class="value">{{ formatDateTime(task.createdAt) }}</span>
                </div>
              </el-col>
            </el-row>
          </div>
        </el-tab-pane>

        <el-tab-pane label="评论" name="comments" lazy>
          <div class="comments-section">
            <div v-if="loadingComments" class="loading-container">
              <el-skeleton :rows="3" animated />
            </div>

            <div v-else>
              <!-- 顶层评论输入 -->
              <div class="comment-inputting">
                <el-input
                  v-model="newComment"
                  type="textarea"
                  :rows="3"
                  placeholder="添加评论..."
                  maxlength="500"
                  show-word-limit
                />
                <el-button
                  type="primary"
                  :disabled="!newComment.trim()"
                  @click="handleAddComment"
                  :loading="commenting"
                >
                  发送
                </el-button>
              </div>

              <div v-if="topLevelComments.length > 0" class="comments-list">
                <div
                  v-for="comment in topLevelComments"
                  :key="comment.id"
                  class="comment-item"
                >
                  <!-- 评论头部 -->
                  <div class="comment-header">
                    <el-avatar :size="32" :src="comment.userAvatar">
                      {{ comment.userName?.charAt(0) || '?' }}
                    </el-avatar>
                    <div class="comment-info">
                      <span class="user-name">{{ comment.userName || '未知用户' }}</span>
                      <span class="comment-time">{{ formatDateTime(comment.createdAt) }}</span>
                    </div>
                    <div class="comment-actions">
                      <el-button
                        v-if="isCommentOwner(comment)"
                        size="small"
                        text
                        type="primary"
                        @click="startEditComment(comment)"
                      >
                        编辑
                      </el-button>
                      <el-button
                        v-if="isCommentOwner(comment)"
                        size="small"
                        text
                        type="danger"
                        @click="handleDeleteComment(comment)"
                      >
                        删除
                      </el-button>
                      <el-button
                        size="small"
                        text
                        @click="startReply(comment)"
                      >
                        回复
                      </el-button>
                    </div>
                  </div>

                  <!-- 评论内容（编辑模式） -->
                  <div v-if="editingCommentId === comment.id" class="comment-edit-area">
                    <el-input
                      v-model="editContent"
                      type="textarea"
                      :rows="2"
                      maxlength="500"
                      show-word-limit
                    />
                    <div class="edit-actions">
                      <el-button size="small" @click="cancelEdit">取消</el-button>
                      <el-button size="small" type="primary" @click="submitEditComment">保存</el-button>
                    </div>
                  </div>
                  <div v-else class="comment-content">{{ comment.content }}</div>

                  <!-- 回复输入 -->
                  <div v-if="replyingTo === comment.id" class="reply-input-area">
                    <el-input
                      v-model="replyContent"
                      type="textarea"
                      :rows="2"
                      placeholder="回复 @{{ comment.userName }}..."
                      maxlength="500"
                      show-word-limit
                    />
                    <div class="reply-actions">
                      <el-button size="small" @click="cancelReply">取消</el-button>
                      <el-button size="small" type="primary" @click="submitReply">回复</el-button>
                    </div>
                  </div>

                  <!-- 回复列表 -->
                  <div v-if="getReplies(comment.id).length > 0" class="replies-list">
                    <div
                      v-for="reply in getReplies(comment.id)"
                      :key="reply.id"
                      class="reply-item"
                    >
                      <div class="comment-header">
                        <el-avatar :size="28" :src="reply.userAvatar">
                          {{ reply.userName?.charAt(0) || '?' }}
                        </el-avatar>
                        <div class="comment-info">
                          <span class="user-name">{{ reply.userName || '未知用户' }}</span>
                          <span class="comment-time">{{ formatDateTime(reply.createdAt) }}</span>
                        </div>
                        <div class="comment-actions">
                          <el-button
                            v-if="isCommentOwner(reply)"
                            size="small"
                            text
                            type="danger"
                            @click="handleDeleteComment(reply)"
                          >
                            删除
                          </el-button>
                        </div>
                      </div>
                      <div class="comment-content">{{ reply.content }}</div>
                    </div>
                  </div>
                </div>
              </div>

              <el-empty v-else description="暂无评论" image-size="80" />
            </div>
          </div>
        </el-tab-pane>

        <el-tab-pane label="附件" name="attachments" lazy>
          <div class="attachments-section">
            <div v-if="loadingAttachments" class="loading-container">
              <el-skeleton :rows="3" animated />
            </div>

            <div v-else>
              <el-upload
                action="#"
                :auto-upload="false"
                :on-change="handleFileChange"
                :show-file-list="false"
              >
                <el-button type="primary" :icon="Plus">上传文件</el-button>
              </el-upload>

              <div v-if="attachments.length > 0" class="attachments-list">
                <el-row :gutter="20">
                  <el-col :span="20" v-for="file in attachments" :key="file.id">
                    <el-card :body-style="{ padding: '10px' }" shadow="hover">
                      <template #header>
                        <div class="file-header-content">
                          <div class="file-icon">
                            <el-icon :size="20" color="#667eea">
                              <Document />
                            </el-icon>
                          </div>
                          <el-tooltip :content="file.name" placement="top">
                            <div class="file-name">
                              <el-link :href="`/api/v1/files/${file.id}/download`" target="_blank" :underline="false">
                                {{ file.name }}
                              </el-link>
                            </div>
                          </el-tooltip>
                        </div>
                      </template>
                      <template #default>
                        <div class="file-actions">
                          <el-button type="danger" :icon="Delete" size="small" @click="handleDeleteFile(file)">
                            删除
                          </el-button>
                        </div>
                        <div class="file-info">
                          <span>{{ formatFileSize(file.size) }}</span>
                          <span>{{ getFileType(file.type) }}</span>
                        </div>
                      </template>
                    </el-card>
                  </el-col>
                </el-row>
              </div>

              <el-empty v-else description="暂无附件" image-size="80" />
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>

      <div class="detail-actions">
        <el-button type="primary" @click="handleEdit">编辑任务</el-button>
        <el-button type="success" @click="handleComplete">完成任务</el-button>
        <el-button type="info" @click="goToProject">返回项目</el-button>
      </div>
    </div>

    <div v-else class="error-container">
      <el-result icon="error" title="任务不存在" sub-title="请检查任务 ID 是否正确">
        <template #extra>
          <el-button type="primary" @click="goToList">返回任务列表</el-button>
        </template>
      </el-result>
    </div>

    <!-- 编辑任务对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑任务"
      width="600px"
      :close-on-click-modal="false"
    >
      <task-form
        v-if="task"
        :project-id="String(task.projectId)"
        :task="task"
        @submit="handleEditSubmit"
        @cancel="editDialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ArrowLeft, Delete, Plus, Document } from '@element-plus/icons-vue'
import { ElMessage, ElMessageBox, type MessageBoxType } from 'element-plus'
import { useTask } from '@/composables/useTask'
import { commentApi } from '@/api/comment'
import { fileApi } from '@/api'
import { projectApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { Task } from '@/types'
import dayjs from 'dayjs'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const taskId = route.params.id as string

const {
  fetchTask,
  updateTask,
  updateTaskStatus,
  deleteTask,
} = useTask()

const loading = ref(false)
const task = ref<Task | null>(null)
const activeTab = ref('info')
const newComment = ref('')
const commenting = ref(false)
const loadingComments = ref(false)
const comments = ref<any[]>([])
const loadingAttachments = ref(false)
const attachments = ref<any[]>([])
const editDialogVisible = ref(false)
const projectMembers = ref<any[]>([])

// 评论相关状态
const replyingTo = ref<number | null>(null)
const replyContent = ref('')
const editingCommentId = ref<number | null>(null)
const editContent = ref('')
const commentReplies = ref<Record<number, any[]>>({})

onMounted(async () => {
  await loadTaskData()
})

watch(activeTab, (tab) => {
  if (tab === 'attachments') {
    loadAttachments()
  }
})

const loadTaskData = async () => {
  const result = await fetchTask(taskId)
  if (result.success) {
    task.value = result.data
    await loadComments()
  } else {
    ElMessage.error('加载任务失败')
  }
}

const getStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    TODO: 'info',
    DOING: 'warning',
    DONE: 'success',
    ARCHIVED: 'info',
  }
  return typeMap[status] || ''
}

const getStatusText = (status: string) => {
  const textMap: Record<string, string> = {
    TODO: '待处理',
    DOING: '进行中',
    DONE: '已完成',
    ARCHIVED: '已归档',
  }
  return textMap[status] || '未知'
}

const getPriorityType = (priority: number) => {
  const typeMap: Record<number, any> = {
    0: 'danger',
    1: 'warning',
    2: '',
    3: 'info',
    4: 'info',
  }
  return typeMap[priority] || ''
}

const formatDateTime = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const goBack = () => {
  router.back()
}

const goToList = () => {
  router.push('/tasks')
}

const goToProject = () => {
  if (task.value) {
    router.push(`/projects/${task.value.projectId}`)
  }
}

const handleEdit = async () => {
  if (task.value) {
    await fetchProjectMembers()
    editDialogVisible.value = true
  }
}

const fetchProjectMembers = async () => {
  if (!task.value) return
  try {
    const response = await projectApi.getMembers(String(task.value.projectId))
    if (response && response.data && response.data.code === 200) {
      projectMembers.value = response.data.data || []
    }
  } catch (error) {
    console.error('获取项目成员失败:', error)
  }
}

const handleEditSubmit = async (formData: Partial<Task>) => {
  if (!task.value) return
  const result = await updateTask(String(task.value.id), formData)
  if (result.success) {
    ElMessage.success('任务更新成功')
    editDialogVisible.value = false
    await loadTaskData()
  } else {
    ElMessage.error(result.message || '更新失败')
  }
}

const handleComplete = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要完成任务吗？',
      '确认完成',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'success',
      }
    )

    if (task.value) {
      const result = await updateTaskStatus(task.value.id, 'DONE')
      if (result.success) {
        ElMessage.success('任务已完成')
      if (task.value.status !== 'DONE') {
          task.value.status = 'DONE' as any
        }
      } else {
        ElMessage.error(result.message || '完成任务失败')
      }
    }
  } catch {
    // 用户取消
  }
}

const loadComments = async () => {
  loadingComments.value = true

  try {
    const response = await commentApi.getComments('task', taskId)
    if (response && response.data && response.data.code === 200) {
      comments.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载评论失败:', error)
  } finally {
    loadingComments.value = false
  }
}

const topLevelComments = computed(() => {
  return comments.value.filter(c => !c.parentId)
})

const getReplies = (parentId: number) => {
  return comments.value.filter(c => c.parentId === parentId)
}

const isCommentOwner = (comment: any) => {
  return String(comment.userId) === String(userStore.userId)
}

const startReply = (comment: any) => {
  replyingTo.value = comment.id
  replyContent.value = ''
}

const cancelReply = () => {
  replyingTo.value = null
  replyContent.value = ''
}

const submitReply = async () => {
  if (!replyContent.value.trim()) return
  try {
    const response = await commentApi.createComment({
      content: replyContent.value,
      entityType: 'task',
      entityId: parseInt(taskId),
      userId: userStore.userId,
      parentId: replyingTo.value,
    })
    if (response?.data?.code === 200) {
      ElMessage.success('回复成功')
      cancelReply()
      await loadComments()
    } else {
      ElMessage.error(response?.data?.message || '回复失败')
    }
  } catch (error) {
    ElMessage.error('回复失败')
  }
}

const startEditComment = (comment: any) => {
  editingCommentId.value = comment.id
  editContent.value = comment.content
}

const cancelEdit = () => {
  editingCommentId.value = null
  editContent.value = ''
}

const submitEditComment = async () => {
  if (!editContent.value.trim() || !editingCommentId.value) return
  try {
    const response = await commentApi.updateComment(
      editingCommentId.value.toString(),
      {
        content: editContent.value,
        userId: userStore.userId,
      }
    )
    if (response?.data?.code === 200) {
      ElMessage.success('评论更新成功')
      cancelEdit()
      await loadComments()
    } else {
      ElMessage.error(response?.data?.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新失败')
  }
}

const handleDeleteComment = async (comment: any) => {
  try {
    await ElMessageBox.confirm('确定要删除这条评论吗？', '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning' as MessageBoxType,
    })
    const response = await commentApi.deleteComment(comment.id.toString(), {
      userId: userStore.userId,
    })
    if (response?.data?.code === 200) {
      ElMessage.success('删除成功')
      await loadComments()
    } else {
      ElMessage.error(response?.data?.message || '删除失败')
    }
  } catch (error) {
    // 取消
  }
}

const handleAddComment = async () => {
  if (!newComment.value.trim() || commenting.value) return

  commenting.value = true

  try {
    const response = await commentApi.createComment({
      content: newComment.value,
      entityType: 'task',
      entityId: parseInt(taskId),
      userId: userStore.userId
    })
    if (response && response.data && response.data.code === 200) {
      ElMessage.success('评论发送成功')
      newComment.value = ''
      await loadComments()
    } else {
      ElMessage.error(response.data?.message || '评论发送失败')
    }
  } catch (error) {
    ElMessage.error('评论发送失败')
  } finally {
    commenting.value = false
  }
}

const handleFileChange = async (uploadFile: any) => {
  const rawFile = uploadFile.raw as File
  if (!rawFile || !task.value) return

  // 文件大小验证 (50MB)
  const maxSize = 50 * 1024 * 1024
  if (rawFile.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return
  }

  try {
    const response = await fileApi.upload(rawFile, {
      taskId: task.value.id,
      entity: 'task',
      uploadedById: parseInt(userStore.userId) || 0,
    })
    if (response && response.data && response.data.code === 200) {
      ElMessage.success('文件上传成功')
      await loadAttachments()
    } else {
      ElMessage.error(response?.data?.message || '上传失败')
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '文件上传失败')
  }
}

const beforeUpload = (file: any) => {
  const maxSize = 50 * 1024 * 1024 // 50MB
  if (file.size > maxSize) {
    ElMessage.error('文件大小不能超过 50MB')
    return false
  }
  return true
}

const handleDeleteFile = async (file: any) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${file.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning' as MessageBoxType,
      }
    )

    const response = await fileApi.delete(file.id)
    if (response && response.data && response.data.code === 200) {
      ElMessage.success('文件删除成功')
      await loadAttachments()
    } else {
      ElMessage.error(response?.data?.message || '删除失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '文件删除失败')
    }
  }
}

const loadAttachments = async () => {
  loadingAttachments.value = true

  try {
    const response = await fileApi.getFilesByTask(taskId)
    if (response && response.data && response.data.code === 200) {
      attachments.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载附件失败:', error)
  } finally {
    loadingAttachments.value = false
  }
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

const formatFileSize = (bytes: number) => {
  if (bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}
</script>

<style scoped>
.task-detail {
  padding: 20px;
}

.loading-container {
  padding: 20px;
}

.detail-header {
  margin-bottom: 30px;
}

.detail-container {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.info-section {
  padding: 20px;
}

.info-item {
  margin-bottom: 15px;
}

.label {
  font-weight: 500;
  color: #666;
  width: 100px;
  flex-shrink: 0;
}

.value {
  color: #333;
  flex: 1;
}

.comments-section {
  padding: 20px;
}

.comment-inputting {
  margin-bottom: 20px;
  display: flex;
  gap: 10px;
}

.comment-inputting .el-textarea {
  flex: 1;
}

.attachments-section {
  padding: 20px;
}

.attachments-list {
  margin-top: 20px;
}

.file-icon {
  text-align: center;
}

.file-name {
  text-align: left;
  white-space: pre-wrap;
}

.file-header-content {
  display: flex;
  align-items: center;
  gap: 10px;
}

.file-actions {
  margin-top: 8px;
}

.file-info {
  display: flex;
  gap: 10px;
  align-items: center;
}

.comments-list {
  margin-top: 20px;
}

.comment-item {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 8px;
  margin-bottom: 10px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 10px;
}

.comment-avatar {
  flex-shrink: 0;
}

.comment-info {
  flex: 1;
  min-width: 0;
}

.user-name {
  font-weight: 500;
  color: #333;
}

.comment-time {
  font-size: 0.85em;
  color: #999;
}

.comment-content {
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}

.detail-actions {
  display: flex;
  gap: 10px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}

.error-container {
  padding: 50px;
}

.comment-actions {
  margin-left: auto;
  display: flex;
  gap: 2px;
}

.comment-edit-area {
  margin-top: 10px;
}

.edit-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.reply-input-area {
  margin-top: 10px;
  padding: 10px;
  background: white;
  border-radius: 6px;
}

.reply-actions {
  margin-top: 8px;
  display: flex;
  justify-content: flex-end;
  gap: 8px;
}

.replies-list {
  margin-top: 10px;
  margin-left: 20px;
  padding-left: 15px;
  border-left: 2px solid #e0e0e0;
}

.reply-item {
  background: white;
  padding: 10px;
  border-radius: 6px;
  margin-bottom: 8px;
}
</style>
