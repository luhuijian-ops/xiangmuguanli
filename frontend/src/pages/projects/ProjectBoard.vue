<template>
  <div class="project-board">
    <div class="board-header">
      <h1>项目看板</h1>
      <div class="header-actions">
        <el-tag v-if="isReadonly" type="info" size="large">已归档（只读）</el-tag>
        <el-button v-if="!isReadonly" type="primary" @click="showCreateDialog" :icon="Plus">
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

        <div class="column-tasks" @drop="handleDrop($event, column.status)" @dragover.prevent>
          <task-card
            v-for="task in column.tasks"
            :key="task.id"
            :task="task as Task"
            @click="handleTaskClick"
            :draggable="!isReadonly"
            @dragstart="!isReadonly && handleDragStart($event, task as Task)"
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
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { useTask } from '@/composables/useTask'
import { projectApi } from '@/api'
import { handleApiResponse } from '@/utils/response'
import TaskForm from '@/components/task/TaskForm.vue'
import TaskDetailDialog from '@/components/task/TaskDetailDialog.vue'
import type { Task } from '@/types'

const router = useRouter()
const route = useRoute()
const projectId = route.params.id as string

const {
  tasks,
  loading,
  createTask,
  updateTask,
  deleteTask,
  fetchTasks,
} = useTask()

const projectStatus = ref('')
const isReadonly = computed(() => projectStatus.value === 'ARCHIVED')

const createDialogVisible = ref(false)
const taskDetailVisible = ref(false)
const selectedTask = ref<Task | null>(null)

// 看板列定义
const columns = ref([
  {
    status: 'TODO',
    title: '待处理',
    tasks: [],
  },
  {
    status: 'DOING',
    title: '进行中',
    tasks: [],
  },
  {
    status: 'DONE',
    title: '已完成',
    tasks: [],
  },
])

onMounted(() => {
  loadTasks()
  fetchProjectStatus()
})

const fetchProjectStatus = async () => {
  try {
    const response = await projectApi.getProject(projectId)
    const result = handleApiResponse(response, '获取项目信息失败')
    if (result.success && result.data) {
      projectStatus.value = result.data.status || ''
    }
  } catch (e) {
    console.error('获取项目状态失败:', e)
  }
}

const loadTasks = async () => {
  const result = await fetchTasks(projectId)
  if (result.success && result.data) {
    const taskList = result.data as Task[]
    // 按状态分组任务
    columns.value[0].tasks = taskList.filter((t: Task) => t.status === 'TODO')
    columns.value[1].tasks = taskList.filter((t: Task) => t.status === 'DOING')
    columns.value[2].tasks = taskList.filter((t: Task) => t.status === 'DONE')
  }
}

const showCreateDialog = () => {
  createDialogVisible.value = true
}

const handleCreateTask = async (taskData: Partial<Task>) => {
  const result = await createTask(taskData)
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

// 拖拽开始
const handleDragStart = (event: DragEvent, task: Task) => {
  event.dataTransfer?.setData('task', JSON.stringify(task))
}

// 拖拽放下
const handleDrop = async (event: DragEvent, newStatus: string) => {
  event.preventDefault()
  if (isReadonly.value) return

  const taskData = event.dataTransfer?.getData('task')
  if (!taskData) return

  try {
    const task = JSON.parse(taskData) as Task

    // 如果状态不同，则更新
    if (task.status !== newStatus) {
      const result = await updateTask(task.id, { status: newStatus as any })
      if (result.success) {
        loadTasks()
      }
    }
  } catch (error) {
    console.error('处理拖拽失败:', error)
  }
}
</script>

<style scoped>
.project-board {
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
</style>
