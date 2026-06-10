<template>
  <div class="task-card" draggable @click="handleClick">
    <div class="card-header">
      <div class="task-info">
        <h4 class="task-title">{{ task.title }}</h4>
        <div class="task-meta">
          <span class="task-priority" :class="priorityClass">
            P{{ task.priority }}
          </span>
          <el-tag size="small" :type="statusType">{{ statusText }}</el-tag>
        </div>
      </div>
      <div class="task-avatar">
        <el-avatar v-if="task.assigneeName" :size="24">
          {{ task.assigneeName.charAt(0) }}
        </el-avatar>
      </div>
    </div>

    <div v-if="taskTags.length > 0 || task.description" class="card-body">
      <div v-if="taskTags.length > 0" class="task-tags">
        <el-tag v-for="tag in taskTags" :key="tag" size="small" class="tag-item">{{ tag }}</el-tag>
      </div>
      <p v-if="task.description" class="task-description">{{ task.description }}</p>
    </div>

    <div v-if="task.dueDate" class="card-footer">
      <div class="due-info">
        <el-icon><Clock /></el-icon>
        <span>截止：{{ formatDate(task.dueDate) }}</span>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import { Clock } from '@element-plus/icons-vue'
import type { Task } from '@/types'
import dayjs from 'dayjs'

interface Props {
  task: Task
}

const props = defineProps<Props>()
const emit = defineEmits<{
  click: [task: Task]
}>()

const handleClick = () => {
  emit('click', props.task)
}

const statusType = computed(() => {
  const typeMap: Record<string, any> = {
    TODO: '',
    DOING: 'warning',
    DONE: 'success',
    ARCHIVED: 'info',
  }
  return typeMap[props.task.status] || ''
})

const statusText = computed(() => {
  const textMap: Record<string, string> = {
    TODO: '待处理',
    DOING: '进行中',
    DONE: '已完成',
    ARCHIVED: '已归档',
  }
  return textMap[props.task.status] || '待处理'
})

const priorityClass = computed(() => {
  const classMap: Record<string, string> = {
    0: 'priority-p0',
    1: 'priority-p1',
    2: 'priority-p2',
    3: 'priority-p3',
    4: 'priority-p4',
  }
  return classMap[props.task.priority] || 'priority-p3'
})

const taskTags = computed(() => {
  if (!props.task.tags) return []
  try {
    const parsed = JSON.parse(props.task.tags)
    if (Array.isArray(parsed)) return parsed.filter(Boolean)
  } catch {
    // not JSON, split by comma
  }
  return props.task.tags.split(',').map(t => t.trim()).filter(Boolean)
})

const formatDate = (date: string) => {
  return dayjs(date).format('MM-DD HH:mm')
}
</script>

<style scoped>
.task-card {
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: move;
  transition: all 0.3s;
}

.task-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.task-card:active {
  outline: 2px solid #667eea;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  padding: 15px;
  border-bottom: 1px solid #f0f0f0;
}

.task-info {
  flex: 1;
  min-width: 0;
}

.task-title {
  margin: 0 0 8px 0;
  font-size: 1em;
  font-weight: 500;
  color: #333;
}

.task-meta {
  display: flex;
  gap: 8px;
  align-items: center;
}

.task-priority {
  padding: 2px 6px;
  border-radius: 4px;
  font-size: 0.75em;
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

.task-avatar {
  flex-shrink: 0;
}

.card-body {
  padding: 12px 15px;
}

.task-description {
  margin: 0;
  color: #666;
  font-size: 0.9em;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.card-footer {
  padding: 10px 15px;
  border-top: 1px solid #f0f0f0;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.due-info {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 0.85em;
  color: #666;
}

.due-info span {
  font-weight: 500;
}

.task-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
  margin-bottom: 8px;
}

.tag-item {
  margin: 0;
}
</style>
