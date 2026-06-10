<template>
  <div class="gantt-chart">
    <div v-if="!hasValidTasks" class="empty-state">
      <el-empty description="暂无带有时间信息的任务" />
    </div>

    <div v-else class="gantt-container">
      <!-- 头部工具栏 -->
      <div class="gantt-toolbar">
        <div class="toolbar-left">
          <span class="timeline-label">
            {{ formatDate(timelineStart) }} ~ {{ formatDate(timelineEnd) }}
          </span>
          <span class="task-count">共 {{ validTasks.length }} 个任务</span>
        </div>
        <div class="toolbar-right">
          <el-radio-group v-model="zoom" size="small">
            <el-radio-button label="day">日视图</el-radio-button>
            <el-radio-button label="week">周视图</el-radio-button>
          </el-radio-group>
        </div>
      </div>

      <div class="gantt-body">
        <!-- 左侧任务列表 -->
        <div class="task-list">
          <div class="task-list-header">任务名称</div>
          <div
            v-for="task in validTasks"
            :key="task.id"
            class="task-list-item"
            :class="{ completed: task.status === 'DONE' }"
          >
            <div class="task-title" :title="task.title">{{ task.title }}</div>
            <div class="task-assignee">
              <el-tag v-if="task.assigneeName" size="small" type="info">
                {{ task.assigneeName }}
              </el-tag>
              <span v-else class="unassigned">未分配</span>
            </div>
          </div>
        </div>

        <!-- 右侧时间轴 -->
        <div class="timeline" ref="timelineRef">
          <!-- 日期头部 -->
          <div class="timeline-header">
            <div
              v-for="day in timelineDays"
              :key="day.date"
              class="day-header"
              :class="{ weekend: day.isWeekend, today: day.isToday }"
              :style="{ width: dayWidth + 'px' }"
            >
              <div class="day-date">{{ day.dayOfMonth }}</div>
              <div class="day-week">{{ day.weekday }}</div>
            </div>
          </div>

          <!-- 任务条 -->
          <div class="timeline-bars">
            <div
              v-for="task in validTasks"
              :key="task.id"
              class="task-bar-row"
            >
              <div
                class="task-bar"
                :class="getTaskBarClass(task)"
                :style="getTaskBarStyle(task)"
                :title="getTaskBarTitle(task)"
                @click="handleTaskClick(task)"
              >
                <span class="bar-text">{{ getBarText(task) }}</span>
              </div>
            </div>
          </div>

          <!-- 今天标记线 -->
          <div
            v-if="todayOffset >= 0"
            class="today-line"
            :style="{ left: todayOffset + 'px' }"
          >
            <div class="today-label">今天</div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed } from 'vue'
import dayjs from 'dayjs'
import type { Task } from '@/types'

interface Props {
  tasks: Task[]
  startDate?: string
  endDate?: string
}

const props = withDefaults(defineProps<Props>(), {
  tasks: () => [],
})

const emit = defineEmits<{
  (e: 'taskClick', task: Task): void
}>()

const zoom = ref<'day' | 'week'>('day')
const timelineRef = ref<HTMLDivElement>()

const dayWidth = computed(() => (zoom.value === 'day' ? 40 : 20))

const validTasks = computed(() => {
  return props.tasks.filter(
    (t) => t.startDate || t.dueDate
  )
})

const hasValidTasks = computed(() => validTasks.value.length > 0)

// 计算时间轴范围
const timelineStart = computed(() => {
  if (props.startDate) return dayjs(props.startDate)
  const dates = validTasks.value
    .map((t) => (t.startDate ? dayjs(t.startDate) : t.dueDate ? dayjs(t.dueDate).subtract(3, 'day') : null))
    .filter(Boolean) as dayjs.Dayjs[]
  if (dates.length === 0) return dayjs()
  return dates.reduce((a, b) => (a.isBefore(b) ? a : b)).subtract(1, 'day')
})

const timelineEnd = computed(() => {
  if (props.endDate) return dayjs(props.endDate)
  const dates = validTasks.value
    .map((t) => (t.dueDate ? dayjs(t.dueDate) : t.startDate ? dayjs(t.startDate).add(3, 'day') : null))
    .filter(Boolean) as dayjs.Dayjs[]
  if (dates.length === 0) return dayjs().add(7, 'day')
  return dates.reduce((a, b) => (a.isAfter(b) ? a : b)).add(2, 'day')
})

const totalDays = computed(() => {
  return timelineEnd.value.diff(timelineStart.value, 'day') + 1
})

const timelineDays = computed(() => {
  const days = []
  const start = timelineStart.value
  const end = timelineEnd.value
  let current = start.clone()

  while (current.isBefore(end) || current.isSame(end, 'day')) {
    const isWeekend = current.day() === 0 || current.day() === 6
    const isToday = current.isSame(dayjs(), 'day')
    days.push({
      date: current.format('YYYY-MM-DD'),
      dayOfMonth: current.format('MM-DD'),
      weekday: ['日', '一', '二', '三', '四', '五', '六'][current.day()],
      isWeekend,
      isToday,
    })
    current = current.add(1, 'day')
  }
  return days
})

const todayOffset = computed(() => {
  const today = dayjs()
  if (today.isBefore(timelineStart.value) || today.isAfter(timelineEnd.value)) {
    return -1
  }
  const diff = today.diff(timelineStart.value, 'day')
  return diff * dayWidth.value + dayWidth.value / 2
})

const formatDate = (date: dayjs.Dayjs) => date.format('YYYY-MM-DD')

const getTaskBarClass = (task: Task) => {
  const statusClass: Record<string, string> = {
    TODO: 'status-todo',
    DOING: 'status-doing',
    DONE: 'status-done',
    ARCHIVED: 'status-archived',
  }
  return statusClass[task.status] || 'status-todo'
}

const getTaskBarStyle = (task: Task) => {
  const start = task.startDate ? dayjs(task.startDate) : dayjs(task.dueDate).subtract(1, 'day')
  const end = task.dueDate ? dayjs(task.dueDate) : dayjs(task.startDate).add(1, 'day')

  const offsetDays = start.diff(timelineStart.value, 'day')
  const durationDays = end.diff(start, 'day') + 1

  const left = offsetDays * dayWidth.value
  const width = Math.max(durationDays * dayWidth.value - 4, dayWidth.value - 4)

  return {
    left: left + 'px',
    width: width + 'px',
  }
}

const getTaskBarTitle = (task: Task) => {
  const start = task.startDate ? formatDate(dayjs(task.startDate)) : '未设置'
  const end = task.dueDate ? formatDate(dayjs(task.dueDate)) : '未设置'
  return `${task.title}\n负责人: ${task.assigneeName || '未分配'}\n${start} ~ ${end}`
}

const getBarText = (task: Task) => {
  const duration = task.startDate && task.dueDate
    ? dayjs(task.dueDate).diff(dayjs(task.startDate), 'day') + 1
    : 1
  // 只有足够宽时才显示文字
  if (duration >= 2 || dayWidth.value >= 40) {
    return task.title
  }
  return ''
}

const handleTaskClick = (task: Task) => {
  emit('taskClick', task)
}
</script>

<style scoped>
.gantt-chart {
  padding: 10px 0;
}

.gantt-toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding: 0 10px;
}

.toolbar-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.timeline-label {
  font-weight: 500;
  color: #333;
}

.task-count {
  color: #666;
  font-size: 0.9em;
}

.gantt-body {
  display: flex;
  border: 1px solid #e0e0e0;
  border-radius: 8px;
  overflow: hidden;
}

.task-list {
  width: 220px;
  flex-shrink: 0;
  border-right: 1px solid #e0e0e0;
  background: #fafafa;
}

.task-list-header {
  height: 50px;
  line-height: 50px;
  padding: 0 12px;
  font-weight: 600;
  border-bottom: 1px solid #e0e0e0;
  background: #f0f0f0;
}

.task-list-item {
  height: 40px;
  padding: 0 12px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  border-bottom: 1px solid #f0f0f0;
}

.task-list-item.completed {
  opacity: 0.6;
}

.task-title {
  font-size: 13px;
  color: #333;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.task-assignee {
  margin-top: 2px;
}

.unassigned {
  font-size: 11px;
  color: #999;
}

.timeline {
  flex: 1;
  overflow-x: auto;
  position: relative;
  background: white;
}

.timeline-header {
  display: flex;
  height: 50px;
  border-bottom: 1px solid #e0e0e0;
  background: #f0f0f0;
}

.day-header {
  flex-shrink: 0;
  text-align: center;
  padding: 4px 0;
  border-right: 1px solid #e8e8e8;
}

.day-header.weekend {
  background: #f5f5f5;
}

.day-header.today {
  background: #e6f7ff;
}

.day-date {
  font-size: 12px;
  font-weight: 500;
  color: #333;
}

.day-week {
  font-size: 11px;
  color: #999;
}

.timeline-bars {
  padding: 0;
}

.task-bar-row {
  height: 40px;
  position: relative;
  border-bottom: 1px solid #f5f5f5;
}

.task-bar {
  position: absolute;
  top: 8px;
  height: 24px;
  border-radius: 4px;
  display: flex;
  align-items: center;
  padding: 0 8px;
  cursor: pointer;
  transition: opacity 0.2s;
  font-size: 12px;
  overflow: hidden;
  white-space: nowrap;
}

.task-bar:hover {
  opacity: 0.85;
}

.bar-text {
  overflow: hidden;
  text-overflow: ellipsis;
}

.status-todo {
  background: #e6f7ff;
  border: 1px solid #91d5ff;
  color: #096dd9;
}

.status-doing {
  background: #fff7e6;
  border: 1px solid #ffd591;
  color: #d46b08;
}

.status-done {
  background: #f6ffed;
  border: 1px solid #b7eb8f;
  color: #389e0d;
}

.status-archived {
  background: #f5f5f5;
  border: 1px solid #d9d9d9;
  color: #666;
}

.today-line {
  position: absolute;
  top: 0;
  bottom: 0;
  width: 2px;
  background: #f5222d;
  z-index: 10;
  pointer-events: none;
}

.today-label {
  position: absolute;
  top: 2px;
  left: -16px;
  background: #f5222d;
  color: white;
  font-size: 10px;
  padding: 1px 4px;
  border-radius: 2px;
}

.empty-state {
  padding: 50px;
  text-align: center;
}
</style>
