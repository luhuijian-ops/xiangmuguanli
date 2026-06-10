<template>
  <div class="project-card" @click="handleClick">
    <div class="project-header">
      <h3 class="project-name">{{ project.name }}</h3>
      <span class="project-status" :class="statusClass">
        {{ statusText }}
      </span>
    </div>

    <div class="project-body">
      <p v-if="project.description" class="project-description">
        {{ project.description }}
      </p>

      <div class="project-meta">
        <div class="meta-item">
          <span class="label">项目编号：</span>
          <span class="value">{{ project.code || '-' }}</span>
        </div>

        <div class="meta-item">
          <span class="label">负责人：</span>
          <span class="value">{{ project.ownerName || '未知' }}</span>
        </div>

        <div v-if="project.budget != null" class="meta-item">
          <span class="label">项目预算：</span>
          <span class="value">{{ formatBudget(project.budget) }}</span>
        </div>

        <div v-if="project.startDate || project.endDate" class="meta-item">
          <span class="label">起止日期：</span>
          <span class="value">{{ dateRangeText }}</span>
        </div>

        <div v-if="project.memberNames && project.memberNames.length > 0" class="meta-item">
          <span class="label">参与人：</span>
          <span class="value member-names">{{ project.memberNames.join('、') }}</span>
        </div>

        <div v-if="project.fileCount != null && project.fileCount > 0" class="meta-item">
          <span class="label">附件：</span>
          <span class="value">{{ project.fileCount }} 个</span>
        </div>

        <div v-if="project.remarks" class="meta-item remarks-item">
          <span class="label">备注：</span>
          <span class="value">{{ project.remarks }}</span>
        </div>

        <div class="meta-item">
          <span class="label">创建时间：</span>
          <span class="value">{{ formatDate(project.createdAt) }}</span>
        </div>

        <!-- 工期进度条 -->
        <div v-if="showProgress" class="progress-section">
          <div class="progress-header">
            <span class="label">进度</span>
            <span class="progress-text">{{ progressPercent }}%</span>
          </div>
          <el-progress
            :percentage="progressPercent"
            :status="progressStatus"
            :stroke-width="8"
            :show-text="false"
          />
        </div>
      </div>
    </div>

    <div class="project-actions">
      <button @click.stop="handleEdit" class="action-btn edit">
        编辑
      </button>
      <button
        v-if="project.status === 'ACTIVE'"
        @click.stop="handleArchive"
        class="action-btn archive"
      >
        归档
      </button>
      <button
        v-else-if="project.status === 'ARCHIVED'"
        @click.stop="handleUnarchive"
        class="action-btn unarchive"
      >
        恢复
      </button>
      <button @click.stop="handleDelete" class="action-btn delete">
        删除
      </button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed } from 'vue'
import type { Project } from '@/types'
import dayjs from 'dayjs'

interface Props {
  project: Project
}

const props = defineProps<Props>()

const emit = defineEmits<{
  click: [project: Project]
  edit: [project: Project]
  delete: [project: Project]
  archive: [project: Project]
  unarchive: [project: Project]
}>()

const statusClass = computed(() => {
  const statusMap: Record<string, string> = {
    ACTIVE: 'active',
    ARCHIVED: 'archived',
    DELETED: 'deleted',
  }
  return statusMap[props.project.status] || 'active'
})

const statusText = computed(() => {
  const textMap: Record<string, string> = {
    ACTIVE: '进行中',
    ARCHIVED: '已归档',
    DELETED: '已删除',
  }
  return textMap[props.project.status] || '进行中'
})

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const formatBudget = (budget: number) => {
  return budget.toLocaleString('zh-CN', { style: 'currency', currency: 'CNY' })
}

const dateRangeText = computed(() => {
  const start = props.project.startDate ? formatDate(props.project.startDate) : '未设置'
  const end = props.project.endDate ? formatDate(props.project.endDate) : '未设置'
  return `${start} ~ ${end}`
})

// 是否显示进度条
const showProgress = computed(() => {
  return props.project.startDate && props.project.endDate && props.project.status === 'ACTIVE'
})

// 工期进度百分比
const progressPercent = computed(() => {
  if (!props.project.startDate || !props.project.endDate) return 0

  const start = dayjs(props.project.startDate)
  const end = dayjs(props.project.endDate)
  const now = dayjs()

  if (now.isBefore(start)) return 0
  if (now.isAfter(end)) return 100

  const totalDays = end.diff(start, 'day') || 1
  const elapsedDays = now.diff(start, 'day')
  return Math.min(100, Math.round((elapsedDays / totalDays) * 100))
})

const progressStatus = computed(() => {
  if (progressPercent.value >= 100) return 'success'
  if (progressPercent.value >= 80) return 'warning'
  return ''
})

const handleClick = () => {
  emit('click', props.project)
}

const handleEdit = () => {
  emit('edit', props.project)
}

const handleDelete = () => {
  emit('delete', props.project)
}

const handleArchive = () => {
  emit('archive', props.project)
}

const handleUnarchive = () => {
  emit('unarchive', props.project)
}
</script>

<style scoped>
.project-card {
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  cursor: pointer;
  border: 2px solid transparent;
  display: flex;
  flex-direction: column;
}

.project-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  border-color: #667eea;
  transform: translateY(-2px);
}

.project-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.project-name {
  margin: 0;
  color: #333;
  font-size: 1.2em;
  word-break: break-all;
}

.project-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 500;
  flex-shrink: 0;
  margin-left: 10px;
}

.project-status.active {
  background: #e6f7ff;
  color: #198754;
}

.project-status.archived {
  background: #f5f5f5;
  color: #6c757d;
}

.project-status.deleted {
  background: #fee;
  color: #721c24;
}

.project-body {
  padding: 20px;
  flex: 1;
}

.project-description {
  color: #666;
  margin-bottom: 15px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

.project-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  display: flex;
  justify-content: space-between;
  font-size: 0.9em;
}

.meta-item .member-names {
  max-width: 60%;
  text-align: right;
  word-break: break-all;
}

.meta-item.remarks-item {
  flex-direction: column;
  gap: 4px;
}

.meta-item.remarks-item .value {
  color: #999;
  font-size: 0.85em;
  word-break: break-all;
}

.label {
  color: #999;
}

.value {
  color: #666;
  font-weight: 500;
}

.progress-section {
  margin-top: 8px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 6px;
}

.progress-text {
  color: #667eea;
  font-weight: 500;
  font-size: 0.85em;
}

.project-actions {
  display: flex;
  gap: 8px;
  padding: 15px 20px;
  border-top: 1px solid #f0f0f0;
}

.action-btn {
  flex: 1;
  padding: 8px 12px;
  border: none;
  border-radius: 6px;
  font-size: 0.85em;
  cursor: pointer;
  transition: all 0.3s;
}

.action-btn.edit {
  background: #667eea;
  color: white;
}

.action-btn.edit:hover {
  background: #5a6fd8;
}

.action-btn.archive {
  background: #fff7e6;
  color: #d46b08;
  border: 1px solid #ffd591;
}

.action-btn.archive:hover {
  background: #ffe7ba;
}

.action-btn.unarchive {
  background: #f6ffed;
  color: #389e0d;
  border: 1px solid #b7eb8f;
}

.action-btn.unarchive:hover {
  background: #d9f7be;
}

.action-btn.delete {
  background: #fff;
  color: #dc3545;
  border: 1px solid #dc3545;
}

.action-btn.delete:hover {
  background: #fee;
  color: #a71d2a;
  border-color: #a71d2a;
}
</style>
