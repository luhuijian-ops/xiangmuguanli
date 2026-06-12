<template>
  <div class="workhour-list">
    <div class="header">
      <h1>工时管理</h1>
      <div class="header-actions">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />
        <el-button type="primary" @click="showCreateDialog" :icon="Plus">
          记录工时
        </el-button>
        <el-button @click="refreshWorkHours" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-value">{{ totalHours.toFixed(1) }}</div>
          <div class="stat-label">总工时（小时）</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-value">{{ workHours.length }}</div>
          <div class="stat-label">记录条数</div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card class="stat-card">
          <div class="stat-value">{{ avgHours.toFixed(1) }}</div>
          <div class="stat-label">平均工时/天</div>
        </el-card>
      </el-col>
    </el-row>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else class="workhours-container">
      <el-table :data="workHours" stripe style="width: 100%">
        <el-table-column prop="date" label="日期" width="120">
          <template #default="{ row }">
            {{ formatDate(row.date) }}
          </template>
        </el-table-column>
        <el-table-column prop="projectName" label="项目" width="180">
          <template #default="{ row }">
            {{ row.projectName || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="taskTitle" label="任务" width="200">
          <template #default="{ row }">
            {{ row.taskTitle || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="hours" label="工时" width="100">
          <template #default="{ row }">
            <el-tag :type="getHoursType(row.hours)">{{ row.hours }}h</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="description" label="描述">
          <template #default="{ row }">
            {{ row.description || '-' }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150">
          <template #default="{ row }">
            <el-button-group>
              <el-button size="small" type="primary" @click="handleEdit(row)">
                编辑
              </el-button>
              <el-button size="small" type="danger" @click="handleDelete(row)">
                删除
              </el-button>
            </el-button-group>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <div v-if="!loading && workHours.length === 0" class="empty-state">
      <el-empty description="暂无工时记录">
        <el-button type="primary" @click="showCreateDialog">记录第一条工时</el-button>
      </el-empty>
    </div>

    <!-- 创建/编辑工时对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingWorkHour ? '编辑工时' : '记录工时'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="日期" prop="date">
          <el-date-picker
            v-model="form.date"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="项目">
          <el-select
            v-model="form.projectId"
            placeholder="选择项目（可选）"
            clearable
            style="width: 100%"
            @change="handleProjectChange"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="任务">
          <el-select
            v-model="form.taskId"
            placeholder="选择任务（可选）"
            clearable
            style="width: 100%"
          >
            <el-option
              v-for="task in projectTasks"
              :key="task.id"
              :label="task.title"
              :value="task.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="工时" prop="hours">
          <el-input-number
            v-model="form.hours"
            :min="0.1"
            :max="24"
            :precision="1"
            :step="0.5"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="描述工作内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitting">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox, type MessageBoxType } from 'element-plus'
import { Plus, Refresh } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { workHourApi } from '@/api'
import { projectApi } from '@/api'
import { taskApi } from '@/api'
import type { WorkHour, Project, Task } from '@/types'
import dayjs from 'dayjs'

const userStore = useUserStore()

const workHours = ref<WorkHour[]>([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingWorkHour = ref<WorkHour | null>(null)
const dateRange = ref<[string, string] | null>(null)
const projects = ref<Project[]>([])
const projectTasks = ref<Task[]>([])

const formRef = ref()
const form = ref({
  date: dayjs().format('YYYY-MM-DD'),
  projectId: undefined as number | undefined,
  taskId: undefined as number | undefined,
  hours: 1,
  description: '',
})

const rules = {
  date: [{ required: true, message: '请选择日期', trigger: 'change' }],
  hours: [{ required: true, message: '请填写工时', trigger: 'change' }],
}

onMounted(async () => {
  // 默认加载本月数据
  const now = dayjs()
  dateRange.value = [
    now.startOf('month').format('YYYY-MM-DD'),
    now.endOf('month').format('YYYY-MM-DD'),
  ]
  await Promise.all([
    loadWorkHours(),
    loadProjects(),
  ])
})

const totalHours = computed(() => {
  return workHours.value.reduce((sum, wh) => sum + Number(wh.hours), 0)
})

const avgHours = computed(() => {
  if (workHours.value.length === 0) return 0
  const uniqueDays = new Set(workHours.value.map(wh => wh.date)).size
  return totalHours.value / (uniqueDays || 1)
})

const loadProjects = async () => {
  try {
    const response = await projectApi.getMyProjects({ page: 0, pageSize: 1000 })
    if (response && response.data && response.data.code === 200) {
      const data = response.data.data
      projects.value = data.content || data || []
    }
  } catch (error) {
    console.error('加载项目失败:', error)
  }
}

const loadProjectTasks = async (projectId: number) => {
  try {
    const response = await taskApi.getTasks(projectId)
    if (response && response.data && response.data.code === 200) {
      const data = response.data.data
      projectTasks.value = data.content || data || []
    }
  } catch (error) {
    console.error('加载任务失败:', error)
  }
}

const loadWorkHours = async () => {
  loading.value = true
  try {
    const userId = userStore.userId as string
    let startDate = dateRange.value?.[0] || dayjs().startOf('month').format('YYYY-MM-DD')
    let endDate = dateRange.value?.[1] || dayjs().endOf('month').format('YYYY-MM-DD')

    const response = await workHourApi.getWorkHoursByUser(userId, startDate, endDate)
    if (response && response.data && response.data.code === 200) {
      workHours.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载工时记录失败:', error)
    ElMessage.error('加载工时记录失败')
  } finally {
    loading.value = false
  }
}

const handleDateChange = () => {
  loadWorkHours()
}

const refreshWorkHours = () => {
  loadWorkHours()
}

const handleProjectChange = (projectId: number) => {
  form.value.taskId = undefined
  if (projectId) {
    loadProjectTasks(projectId)
  } else {
    projectTasks.value = []
  }
}

const showCreateDialog = () => {
  editingWorkHour.value = null
  form.value = {
    date: dayjs().format('YYYY-MM-DD'),
    projectId: undefined,
    taskId: undefined,
    hours: 1,
    description: '',
  }
  projectTasks.value = []
  dialogVisible.value = true
}

const handleEdit = (workHour: WorkHour) => {
  editingWorkHour.value = workHour
  form.value = {
    date: workHour.date,
    projectId: workHour.projectId,
    taskId: workHour.taskId,
    hours: workHour.hours,
    description: workHour.description || '',
  }
  if (workHour.projectId) {
    loadProjectTasks(workHour.projectId)
  }
  dialogVisible.value = true
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingWorkHour.value) {
      const response = await workHourApi.updateWorkHour(
        editingWorkHour.value.id.toString(),
        {
          hours: form.value.hours,
          date: form.value.date,
          description: form.value.description,
        }
      )
      if (response && response.data && response.data.code === 200) {
        ElMessage.success('工时更新成功')
        dialogVisible.value = false
        loadWorkHours()
      } else {
        ElMessage.error(response?.data?.message || '更新失败')
      }
    } else {
      const response = await workHourApi.createWorkHour({
        projectId: form.value.projectId,
        taskId: form.value.taskId,
        hours: form.value.hours,
        date: form.value.date,
        description: form.value.description,
      })
      if (response && response.data && response.data.code === 200) {
        ElMessage.success('工时记录成功')
        dialogVisible.value = false
        loadWorkHours()
      } else {
        ElMessage.error(response?.data?.message || '记录失败')
      }
    }
  } catch (error: any) {
    console.error('提交工时失败:', error)
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (workHour: WorkHour) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除 ${formatDate(workHour.date)} 的工时记录吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning' as MessageBoxType,
      }
    )

    const response = await workHourApi.deleteWorkHour(workHour.id.toString())
    if (response && response.data && response.data.code === 200) {
      ElMessage.success('删除成功')
      loadWorkHours()
    } else {
      ElMessage.error(response?.data?.message || '删除失败')
    }
  } catch (error) {
    // 用户取消
  }
}

const formatDate = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD')
}

const getHoursType = (hours: number) => {
  if (hours >= 8) return 'success'
  if (hours >= 4) return 'warning'
  return 'info'
}
</script>

<style scoped>
.workhour-list {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
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

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-value {
  font-size: 2em;
  font-weight: bold;
  color: #667eea;
  margin-bottom: 8px;
}

.stat-label {
  color: #666;
  font-size: 0.9em;
}

.loading-container {
  padding: 20px;
}

.workhours-container {
  min-height: 400px;
}

.empty-state {
  padding: 50px;
  text-align: center;
}
</style>
