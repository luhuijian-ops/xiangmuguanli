<template>
  <div class="task-board">
  <div class="board-header">
      <h1>任务看板</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog" :icon="Plus">
          添加任务
        </el-button>
        <el-button @click="refreshTasks" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <div v-else class="board-container">
      <div class="board-column" v-for="column in columns" :key="column.status">
        <div class="column-header">
          <h3>{{ column.title }}</h3>
          <span class="task-count">{{ column.tasks.length }}</span>
        </div>

        <div
          class="column-tasks"
          :data-status="column.status"
          @drop="handleDrop($event, column.status)"
          @dragover="handleDragOver($event, column.status)"
          @dragleave="handleDragLeave"
        >
          <task-card
            v-for="(task, index) in column.tasks"
            :key="task.id"
            :task="task"
            :class="{ 'drag-over': dragOverColumn === column.status && dragOverIndex === index }"
            @click="handleTaskClick(task)"
            draggable
            @dragstart="handleDragStart($event, task)"
          />
        </div>
      </div>
    </div>

    <!-- 创建任务对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      title="创建任务"
      width="500px"
      :close-on-click-modal="false"
      >
      <task-form
        :project-id="projectId"
        :task="editingTask"
        @submit="handleCreateTask"
        @cancel="createDialogVisible = false"
      />
    </el-dialog>

    <!-- 任务详情对话框 -->
    <task-detail-dialog
      v-if="selectedTask"
      :task="selectedTask"
      :visible="taskDetailVisible"
      @close="closeTaskDetail"
      @update="handleTaskUpdate"
      @delete="handleTaskDelete"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { useTask } from '@/composables/useTask'
import { useUserStore } from '@/stores/user'
import TaskForm from '@/components/task/TaskForm.vue'
import TaskDetailDialog from '@/components/task/TaskDetailDialog.vue'
import TaskCard from '@/components/task/TaskCard.vue'
import type { Task } from '@/types'

const route = useRoute()
const userStore = useUserStore()
const projectId = (route.query.projectId as string) || ''

const {
  tasks,
  loading,
  createTask,
  updateTask,
  deleteTask,
  updateTaskStatus,
  fetchTasks,
  fetchTasksByAssignee,
  batchUpdateSortOrder,
} = useTask()

const createDialogVisible = ref(false)
const taskDetailVisible = ref(false)
const selectedTask = ref<Task | null>(null)
const editingTask = ref<Task | null>(null)

// 看板列定义
const columns = ref([
  {
    status: 'TODO',
    title: '待处理',
    tasks: [] as Task[],
  },
  {
    status: 'DOING',
    title: '进行中',
    tasks: [] as Task[],
  },
  {
    status: 'DONE',
    title: '已完成',
    tasks: [] as Task[],
  },
])

onMounted(() => {
  loadTasks()
})

const loadTasks = async () => {
  let result
  if (projectId) {
    result = await fetchTasks(projectId)
  } else if (userStore.userId) {
    result = await fetchTasksByAssignee(userStore.userId as string)
  } else {
    return
  }
  if (result.success && result.data) {
    const taskList = result.data as Task[]
    // 按状态分组任务
    columns.value[0].tasks = taskList.filter(t => t.status === 'TODO')
    columns.value[1].tasks = taskList.filter(t => t.status === 'DOING')
    columns.value[2].tasks = taskList.filter(t => t.status === 'DONE')
  }
}

const showCreateDialog = () => {
  editingTask.value = null
  createDialogVisible.value = true
}

const handleCreateTask = async (taskData: Partial<Task>) => {
  const payload: any = {
    ...taskData,
    creatorId: userStore.userId as number,
  }
  if (projectId) {
    payload.projectId = parseInt(projectId)
  }
  const result = await createTask(payload)
  if (result.success) {
    createDialogVisible.value = false
    loadTasks()
  }
}

const handleTaskClick = (task: Task) => {
  selectedTask.value = task
  taskDetailVisible.value = true
}

const closeTaskDetail = () => {
  taskDetailVisible.value = false
  selectedTask.value = null
}

const handleTaskUpdate = async (taskData: Partial<Task>) => {
  if (selectedTask.value) {
    const result = await updateTask(selectedTask.value.id, taskData)
    if (result.success) {
      loadTasks()
    }
  }
}

const handleTaskDelete = async () => {
  if (selectedTask.value) {
    const result = await deleteTask(selectedTask.value.id)
    if (result.success) {
      taskDetailVisible.value = false
      selectedTask.value = null
      loadTasks()
    }
  }
}

const refreshTasks = () => {
  loadTasks()
}

// 拖拽跟踪
const dragOverColumn = ref<string | null>(null)
const dragOverIndex = ref<number | null>(null)
const draggedTask = ref<Task | null>(null)

// 拖拽开始
const handleDragStart = (event: DragEvent, task: Task) => {
  event.dataTransfer?.setData('task', JSON.stringify(task))
  draggedTask.value = task
}

// 拖拽在列上移动，计算插入位置
const handleDragOver = (event: DragEvent, columnStatus: string) => {
  event.preventDefault()
  dragOverColumn.value = columnStatus

  const columnEl = event.currentTarget as HTMLElement
  const cards = Array.from(columnEl.children) as HTMLElement[]
  let insertIndex = cards.length

  for (let i = 0; i < cards.length; i++) {
    const rect = cards[i].getBoundingClientRect()
    const midY = rect.top + rect.height / 2
    if (event.clientY < midY) {
      insertIndex = i
      break
    }
  }

  dragOverIndex.value = insertIndex
}

const handleDragLeave = () => {
  dragOverColumn.value = null
  dragOverIndex.value = null
}

// 拖拽放下
const handleDrop = async (event: DragEvent, newStatus: string) => {
  event.preventDefault()

  const taskData = event.dataTransfer?.getData('task')
  if (!taskData) return

  try {
    const task = JSON.parse(taskData) as Task
    const originalStatus = task.status
    const targetColumn = columns.value.find(c => c.status === newStatus)
    if (!targetColumn) return

    let insertIndex = dragOverIndex.value ?? targetColumn.tasks.length

    if (originalStatus === newStatus) {
      // 同列内排序
      const currentIndex = targetColumn.tasks.findIndex(t => t.id === task.id)
      if (currentIndex === -1) return

      // 调整插入索引：如果当前位置在插入位置之前，移除后索引会减1
      if (currentIndex < insertIndex) {
        insertIndex--
      }

      // 本地移动
      const [moved] = targetColumn.tasks.splice(currentIndex, 1)
      targetColumn.tasks.splice(insertIndex, 0, moved)

      // 批量更新后端排序
      if (projectId) {
        const ids = targetColumn.tasks.map(t => t.id)
        await batchUpdateSortOrder(parseInt(projectId), ids)
      }
    } else {
      // 跨列移动：先更新状态
      const result = await updateTaskStatus(task.id, newStatus)
      if (!result.success) {
        loadTasks()
        return
      }

      // 从原列移除
      const sourceColumn = columns.value.find(c => c.status === originalStatus)
      if (sourceColumn) {
        sourceColumn.tasks = sourceColumn.tasks.filter(t => t.id !== task.id)
      }

      // 更新任务状态并插入目标列
      task.status = newStatus as any
      targetColumn.tasks.splice(insertIndex, 0, task)

      // 批量更新目标列排序
      if (projectId) {
        const ids = targetColumn.tasks.map(t => t.id)
        await batchUpdateSortOrder(parseInt(projectId), ids)
      }
    }
  } catch (error) {
    console.error('处理拖拽失败:', error)
    loadTasks()
  } finally {
    dragOverColumn.value = null
    dragOverIndex.value = null
    draggedTask.value = null
  }
}
</script>

<style scoped>
.task-board {
  padding: 20px;
}

.board-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.board-header h1 {
  margin: 0;
  color: #333;
  font-size: 2em;
}

.header-actions {
  display: flex;
  gap: 10px;
}

.loading-container {
  padding: 20px;
}

.board-container {
  display: flex;
  gap: 20px;
  min-height: calc(100vh - 200px);
}

.board-column {
  flex: 1;
  background: #f5f7fa;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  flex-direction: column;
}

.column-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e5e7eb;
}

.column-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.1em;
}

.task-count {
  background: #667eea;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 0.85em;
  font-weight: 500;
}

.column-tasks {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 15px;
  overflow-y: auto;
  min-height: 300px;
}

.drag-over {
  outline: 2px dashed #667eea;
  outline-offset: 2px;
}
</style>
