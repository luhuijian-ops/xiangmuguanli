<template>
  <div class="task-list">
    <div class="header">
      <h1>任务列表</h1>
      <div class="header-actions">
        <el-button type="primary" @click="showCreateDialog" :icon="Plus">
          添加任务
        </el-button>
        <el-select
          v-model="statusFilter"
          placeholder="任务状态"
          clearable
          style="width: 150px"
        >
          <el-option label="全部" value="" />
          <el-option label="待处理" value="TODO" />
          <el-option label="进行中" value="DOING" />
          <el-option label="已完成" value="DONE" />
          <el-option label="已归档" value="ARCHIVED" />
        </el-select>

        <el-select
          v-model="priorityFilter"
          placeholder="优先级"
          clearable
          style="width: 150px"
        >
          <el-option label="全部" value="" />
          <el-option label="P0 - 最高" value="0" />
          <el-option label="P1 - 高" value="1" />
          <el-option label="P2 - 中" value="2" />
          <el-option label="P3 - 普通" value="3" />
          <el-option label="P4 - 最低" value="4" />
        </el-select>

        <el-select
          v-model="tagFilter"
          placeholder="标签筛选"
          clearable
          style="width: 150px"
        >
          <el-option label="全部" value="" />
          <el-option
            v-for="tag in allTags"
            :key="tag"
            :label="tag"
            :value="tag"
          />
        </el-select>

        <el-input
          v-model="searchKeyword"
          placeholder="搜索任务"
          :prefix-icon="Search"
          clearable
          style="width: 200px"
          @clear="handleSearch"
        />

        <el-button @click="refreshTasks" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else class="tasks-container">
      <el-table :data="filteredTasks" stripe style="width: 100%">
        <el-table-column prop="projectCode" label="项目编号" width="100" align="center" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.projectCode || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="项目名称" width="140" show-overflow-tooltip>
          <template #default="{ row }">
            {{ row.projectName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="title" label="任务标题" width="200" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityType(row.priority)" size="small">
              P{{ row.priority }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="标签" width="150">
          <template #default="{ row }">
            <div v-if="getTaskTags(row).length > 0" class="task-tags">
              <el-tag
                v-for="tag in getTaskTags(row)"
                :key="tag"
                size="small"
                class="tag-item"
              >
                {{ tag }}
              </el-tag>
            </div>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="120">
          <template #default="{ row }">
            {{ row.assigneeName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="dueDate" label="截止时间" width="150">
          <template #default="{ row }">
            <span v-if="row.dueDate">{{ formatDate(row.dueDate) }}</span>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" @click="handleViewDetail(row)">
                详情
              </el-button>
              <el-button size="small" type="primary" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleTaskDelete(row)">
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="!loading && filteredTasks.length === 0" class="empty-state">
      <el-empty description="暂无任务">
        <el-button type="primary" @click="showCreateDialog">创建第一个任务</el-button>
      </el-empty>
    </div>

    <!-- 创建/编辑任务对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="editingTask ? '编辑任务' : '创建任务'"
      width="600px"
      :close-on-click-modal="false"
    >
      <task-form
        :project-id="currentProjectId"
        :task="editingTask"
        @submit="handleTaskSubmit"
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
import { ref, computed, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox, type MessageBoxType } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useTask } from '@/composables/useTask'
import { useUserStore } from '@/stores/user'
import TaskForm from '@/components/task/TaskForm.vue'
import TaskDetailDialog from '@/components/task/TaskDetailDialog.vue'
import type { Task } from '@/types'
import dayjs from 'dayjs'

const userStore = useUserStore()

const router = useRouter()
const route = useRoute()
const {
  tasks,
  loading,
  createTask,
  updateTask,
  updateTaskStatus,
  deleteTask,
  fetchTasks,
  fetchTasksByAssignee,
} = useTask()

const searchKeyword = ref('')
const statusFilter = ref('')
const priorityFilter = ref('')
const tagFilter = ref('')
const createDialogVisible = ref(false)
const taskDetailVisible = ref(false)
const selectedTask = ref<Task | null>(null)
const editingTask = ref<Task | null>(null)
const currentProjectId = ref('')

// 从路由获取项目 ID
onMounted(() => {
  const projectId = route.query.projectId as string
  if (projectId) {
    currentProjectId.value = projectId
    fetchTasks(projectId)
  } else if (userStore.userId) {
    // 没有项目 ID 时，获取当前用户的任务
    fetchTasksByAssignee(userStore.userId as string)
  }
})

// 从所有任务中提取唯一标签列表
const allTags = computed(() => {
  const tagSet = new Set<string>()
  tasks.value.forEach(task => {
    getTaskTags(task).forEach(tag => tagSet.add(tag))
  })
  return Array.from(tagSet).sort()
})

const getTaskTags = (task: Task): string[] => {
  if (!task.tags) return []
  // 支持逗号分隔或JSON数组格式
  try {
    const parsed = JSON.parse(task.tags)
    if (Array.isArray(parsed)) return parsed.filter(Boolean)
  } catch {
    // 不是JSON，按逗号分隔
  }
  return task.tags.split(',').map(t => t.trim()).filter(Boolean)
}

const filteredTasks = computed(() => {
  let result = tasks.value

  // 按状态筛选
  if (statusFilter.value) {
    result = result.filter(t => t.status === statusFilter.value)
  }

  // 按优先级筛选
  if (priorityFilter.value !== '') {
    const priority = parseInt(priorityFilter.value)
    result = result.filter(t => t.priority === priority)
  }

  // 按标签筛选
  if (tagFilter.value) {
    result = result.filter(t => getTaskTags(t).includes(tagFilter.value))
  }

  // 按关键字搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(t =>
      t.title.toLowerCase().includes(keyword) ||
      (t.description && t.description.toLowerCase().includes(keyword))
    )
  }

  return result
})

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
  return textMap[status] || status
}

const getPriorityType = (priority: number) => {
  const typeMap: Record<number, string> = {
    0: 'danger',
    1: 'warning',
    2: '',
    3: 'info',
    4: 'info',
  }
  return typeMap[priority] || ''
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const showCreateDialog = () => {
  editingTask.value = null
  createDialogVisible.value = true
}

const refreshTasks = () => {
  if (currentProjectId.value) {
    fetchTasks(currentProjectId.value)
  }
}

const handleSearch = () => {
  if (currentProjectId.value) {
    fetchTasks(currentProjectId.value)
  }
}

const handleViewDetail = (task: Task) => {
  selectedTask.value = task
  taskDetailVisible.value = true
}

const handleEdit = (task: Task) => {
  editingTask.value = task
  createDialogVisible.value = true
}

const closeTaskDetail = () => {
  taskDetailVisible.value = false
  selectedTask.value = null
  editingTask.value = null
}

const handleTaskUpdate = async (taskData: Partial<Task>) => {
  if (selectedTask.value) {
    const result = await updateTask(selectedTask.value.id, taskData)
    if (result.success) {
      ElMessage.success('任务更新成功')
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  }
}

const handleTaskDelete = async (task: Task) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除任务 "${task.title}" 吗`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning' as MessageBoxType,
      }
    )

    const result = await deleteTask(task.id)
    if (result.success) {
      ElMessage.success('任务删除成功')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

const handleTaskSubmit = async (taskData: Partial<Task>) => {
  if (editingTask.value) {
    const result = await updateTask(editingTask.value.id, taskData)
    if (result.success) {
      ElMessage.success('任务更新成功')
      createDialogVisible.value = false
      refreshTasks()
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  } else {
    // 优先使用表单中返回的 projectId（如从无 projectId 场景选择了项目）
    const projectId = (taskData as any).projectId || currentProjectId.value
    if (!projectId || projectId === 'NaN') {
      ElMessage.error('缺少项目信息，无法创建任务')
      return
    }
    const result = await createTask({
      ...taskData,
      creatorId: userStore.userId as number,
      projectId: typeof projectId === 'string' ? parseInt(projectId) : projectId,
    } as any)
    if (result.success) {
      ElMessage.success('任务创建成功')
      createDialogVisible.value = false
      refreshTasks()
    } else {
      ElMessage.error(result.message || '创建失败')
    }
  }
}
</script>

<style scoped>
.task-list {
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

.tasks-container {
  min-height: 400px;
}

.empty-state {
  padding: 50px;
  text-align: center;
}

.task-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 4px;
}

.tag-item {
  margin: 0;
}
</style>
