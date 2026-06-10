<template>
  <div class="task-detail-dialog">
    <el-dialog
      :model-value="visible"
      title="任务详情"
      width="700px"
      :close-on-click-modal="false"
      @update:model-value="handleClose"
      @close="handleClose"
    >
      <div v-if="task" class="task-detail">
        <div class="detail-section">
          <h3>基本信息</h3>
          <div class="info-grid">
            <div class="info-item">
              <span class="label">标题：</span>
              <span class="value">{{ task.title }}</span>
            </div>
            <div class="info-item">
              <span class="label">状态：</span>
              <el-tag :type="getStatusType(task.status)">{{ getStatusText(task.status) }}</el-tag>
            </div>
            <div class="info-item">
              <span class="label">优先级：</span>
              <span class="value priority" :class="getPriorityClass(task.priority)">P{{ task.priority }}</span>
            </div>
            <div v-if="task.assigneeName" class="info-item">
              <span class="label">负责人：</span>
              <span class="value">{{ task.assigneeName }}</span>
            </div>
            <div v-if="task.startDate" class="info-item">
              <span class="label">开始时间：</span>
              <span class="value">{{ formatDateTime(task.startDate) }}</span>
            </div>
            <div v-if="task.dueDate" class="info-item">
              <span class="label">截止时间：</span>
              <span class="value">{{ formatDateTime(task.dueDate) }}</span>
            </div>
          </div>
        </div>

        <div v-if="task.description" class="detail-section">
          <h3>任务描述</h3>
          <p class="description">{{ task.description }}</p>
        </div>

        <div class="detail-section">
          <h3>操作</h3>
          <div class="actions">
            <el-button type="primary" @click="handleEdit" :icon="Edit">编辑任务</el-button>
            <el-button type="danger" @click="handleDelete" :icon="Delete">删除任务</el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Edit, Delete } from '@element-plus/icons-vue'
import type { Task } from '@/types'
import dayjs from 'dayjs'

interface Props {
  task: Task | null
  visible: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  close: []
  update: [data: Partial<Task>]
  delete: []
}>()

const getStatusType = (status: string) => {
  const typeMap: Record<string, any> = {
    TODO: '',
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

const getPriorityClass = (priority: number) => {
  const classMap: Record<number, string> = {
    0: 'priority-p0',
    1: 'priority-p1',
    2: 'priority-p2',
    3: 'priority-p3',
    4: 'priority-p4',
  }
  return classMap[priority] || 'priority-p3'
}

const formatDateTime = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm:ss')
}

const handleEdit = () => {
  if (props.task) {
    emit('update', props.task)
  }
}

const handleDelete = () => {
  if (props.task) {
    emit('delete')
  }
}

const handleClose = () => {
  emit('close')
}
</script>

<style scoped>
.task-detail-dialog {
  padding: 20px;
}

.task-detail {
  padding: 20px;
}

.detail-section {
  margin-bottom: 30px;
}

.detail-section h3 {
  margin: 0 0 15px 0;
  color: #333;
  font-size: 1.1em;
  border-bottom: 2px solid #f0f0f0;
  padding-bottom: 10px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px 30px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  font-weight: 500;
  color: #666;
  width: 80px;
}

.value {
  color: #333;
  flex: 1;
}

.value.priority {
  padding: 2px 8px;
  border-radius: 4px;
  font-size: 0.85em;
  font-weight: 600;
}

.priority-p0 {
  background: #fee;
  color: #721c24;
}

.priority-p1 {
  background: #ff6b6b;
  color: #92400e;
}

.priority-p2 {
  background: #e6f7ff;
  color: #198754;
}

.priority-p3 {
  background: #f3f4f6;
  color: #666;
}

.priority-p4 {
  background: #e5e7eb;
  color: #6c757d;
}

.description {
  margin: 0;
  color: #666;
  line-height: 1.6;
  white-space: pre-wrap;
}

.actions {
  display: flex;
  gap: 10px;
  margin-top: 20px;
}
</style>
