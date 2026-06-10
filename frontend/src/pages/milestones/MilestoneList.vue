<template>
  <div class="milestone-list">
    <div class="header">
      <h1>里程碑管理</h1>
      <div class="header-actions">
        <el-select
          v-model="selectedProjectId"
          placeholder="选择项目"
          clearable
          style="width: 200px"
          @change="handleProjectChange"
        >
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>
        <el-button type="primary" @click="showCreateDialog" :icon="Plus">
          创建里程碑
        </el-button>
        <el-button @click="refreshMilestones" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>

    <div v-else class="milestones-container">
      <el-timeline v-if="milestones.length > 0">
        <el-timeline-item
          v-for="milestone in sortedMilestones"
          :key="milestone.id"
          :type="getTimelineType(milestone)"
          :color="getTimelineColor(milestone)"
          :timestamp="formatDate(milestone.dueDate)"
        >
          <el-card class="milestone-card" :class="{ completed: milestone.status === 'COMPLETED' }">
            <template #header>
              <div class="milestone-header">
                <div class="milestone-title">
                  <el-icon v-if="milestone.status === 'COMPLETED'" color="#67c23a" :size="20">
                    <CircleCheck />
                  </el-icon>
                  <el-icon v-else color="#e6a23c" :size="20">
                    <Clock />
                  </el-icon>
                  <span>{{ milestone.name }}</span>
                  <el-tag :type="milestone.status === 'COMPLETED' ? 'success' : 'warning'" size="small">
                    {{ milestone.status === 'COMPLETED' ? '已完成' : '待完成' }}
                  </el-tag>
                </div>
                <div class="milestone-actions">
                  <el-button size="small" type="primary" text @click="handleEdit(milestone)">
                    编辑
                  </el-button>
                  <el-button
                    v-if="milestone.status !== 'COMPLETED'"
                    size="small"
                    type="success"
                    text
                    @click="handleComplete(milestone)"
                  >
                    完成
                  </el-button>
                  <el-button size="small" type="danger" text @click="handleDelete(milestone)">
                    删除
                  </el-button>
                </div>
              </div>
            </template>
            <div class="milestone-body">
              <p v-if="milestone.description" class="description">
                {{ milestone.description }}
              </p>
              <div class="milestone-meta">
                <span class="project-name" v-if="milestone.projectName">
                  {{ milestone.projectName }}
                </span>
                <span class="days-left" :class="getDaysLeftClass(milestone)">
                  {{ getDaysLeftText(milestone) }}
                </span>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </div>

    <div v-if="!loading && milestones.length === 0" class="empty-state">
      <el-empty :description="selectedProjectId ? '该项目暂无里程碑' : '请选择项目查看里程碑'">
        <el-button v-if="selectedProjectId" type="primary" @click="showCreateDialog">创建第一个里程碑</el-button>
      </el-empty>
    </div>

    <!-- 创建/编辑里程碑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingMilestone ? '编辑里程碑' : '创建里程碑'"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form :model="form" label-width="80px" :rules="rules" ref="formRef">
        <el-form-item label="项目" prop="projectId">
          <el-select
            v-model="form.projectId"
            placeholder="选择项目"
            style="width: 100%"
            :disabled="!!editingMilestone"
          >
            <el-option
              v-for="project in projects"
              :key="project.id"
              :label="project.name"
              :value="project.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="里程碑名称" />
        </el-form-item>
        <el-form-item label="截止日期" prop="dueDate">
          <el-date-picker
            v-model="form.dueDate"
            type="date"
            placeholder="选择截止日期"
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="状态" v-if="editingMilestone">
          <el-radio-group v-model="form.status">
            <el-radio-button label="PENDING">待完成</el-radio-button>
            <el-radio-button label="COMPLETED">已完成</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.orderIndex" :min="0" style="width: 100%" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input
            v-model="form.description"
            type="textarea"
            :rows="3"
            placeholder="里程碑描述"
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
import { Plus, Refresh, CircleCheck, Clock } from '@element-plus/icons-vue'
import { milestoneApi, projectApi } from '@/api'
import type { Milestone, Project } from '@/types'
import dayjs from 'dayjs'

const milestones = ref<Milestone[]>([])
const projects = ref<Project[]>([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const editingMilestone = ref<Milestone | null>(null)
const selectedProjectId = ref<string>('')

const formRef = ref()
const form = ref({
  projectId: undefined as number | undefined,
  name: '',
  dueDate: dayjs().add(7, 'day').format('YYYY-MM-DD'),
  description: '',
  status: 'PENDING' as string,
  orderIndex: 0,
})

const rules = {
  projectId: [{ required: true, message: '请选择项目', trigger: 'change' }],
  name: [{ required: true, message: '请输入里程碑名称', trigger: 'blur' }],
  dueDate: [{ required: true, message: '请选择截止日期', trigger: 'change' }],
}

onMounted(async () => {
  await loadProjects()
})

const sortedMilestones = computed(() => {
  return [...milestones.value].sort((a, b) => {
    if (a.status !== b.status) {
      return a.status === 'PENDING' ? -1 : 1
    }
    return dayjs(a.dueDate).diff(dayjs(b.dueDate))
  })
})

const loadProjects = async () => {
  try {
    const response = await projectApi.getMyProjects({ page: 0, pageSize: 1000 })
    if (response?.data?.code === 200) {
      const data = response.data.data
      projects.value = data.content || data || []
    }
  } catch (error) {
    console.error('加载项目失败:', error)
  }
}

const loadMilestones = async () => {
  if (!selectedProjectId.value) {
    milestones.value = []
    return
  }
  loading.value = true
  try {
    const response = await milestoneApi.getMilestonesByProject(selectedProjectId.value)
    if (response?.data?.code === 200) {
      milestones.value = response.data.data || []
    }
  } catch (error) {
    console.error('加载里程碑失败:', error)
    ElMessage.error('加载里程碑失败')
  } finally {
    loading.value = false
  }
}

const handleProjectChange = () => {
  loadMilestones()
}

const refreshMilestones = () => {
  loadMilestones()
}

const showCreateDialog = () => {
  editingMilestone.value = null
  form.value = {
    projectId: selectedProjectId.value ? Number(selectedProjectId.value) : undefined,
    name: '',
    dueDate: dayjs().add(7, 'day').format('YYYY-MM-DD'),
    description: '',
    status: 'PENDING',
    orderIndex: 0,
  }
  dialogVisible.value = true
}

const handleEdit = (milestone: Milestone) => {
  editingMilestone.value = milestone
  form.value = {
    projectId: milestone.projectId,
    name: milestone.name,
    dueDate: milestone.dueDate,
    description: milestone.description || '',
    status: milestone.status,
    orderIndex: milestone.orderIndex || 0,
  }
  dialogVisible.value = true
}

const handleComplete = async (milestone: Milestone) => {
  try {
    const response = await milestoneApi.updateMilestone(milestone.id.toString(), {
      name: milestone.name,
      dueDate: milestone.dueDate,
      status: 'COMPLETED',
    })
    if (response?.data?.code === 200) {
      ElMessage.success('里程碑已完成')
      loadMilestones()
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  }
}

const handleSubmit = async () => {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  try {
    if (editingMilestone.value) {
      const response = await milestoneApi.updateMilestone(
        editingMilestone.value.id.toString(),
        {
          name: form.value.name,
          description: form.value.description,
          dueDate: form.value.dueDate,
          status: form.value.status,
          orderIndex: form.value.orderIndex,
        }
      )
      if (response?.data?.code === 200) {
        ElMessage.success('里程碑更新成功')
        dialogVisible.value = false
        loadMilestones()
      } else {
        ElMessage.error(response?.data?.message || '更新失败')
      }
    } else {
      const response = await milestoneApi.createMilestone({
        projectId: form.value.projectId!,
        name: form.value.name,
        description: form.value.description,
        dueDate: form.value.dueDate,
        orderIndex: form.value.orderIndex,
      })
      if (response?.data?.code === 200) {
        ElMessage.success('里程碑创建成功')
        dialogVisible.value = false
        loadMilestones()
      } else {
        ElMessage.error(response?.data?.message || '创建失败')
      }
    }
  } catch (error: any) {
    ElMessage.error(error.response?.data?.message || '操作失败')
  } finally {
    submitting.value = false
  }
}

const handleDelete = async (milestone: Milestone) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除里程碑 "${milestone.name}" 吗？`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning' as MessageBoxType,
      }
    )
    const response = await milestoneApi.deleteMilestone(milestone.id.toString())
    if (response?.data?.code === 200) {
      ElMessage.success('删除成功')
      loadMilestones()
    } else {
      ElMessage.error(response?.data?.message || '删除失败')
    }
  } catch (error) {
    // 用户取消
  }
}

const formatDate = (date: string) => dayjs(date).format('YYYY-MM-DD')

const getTimelineType = (milestone: Milestone) =>
  milestone.status === 'COMPLETED' ? 'success' : 'warning'

const getTimelineColor = (milestone: Milestone) => {
  if (milestone.status === 'COMPLETED') return '#67c23a'
  const daysLeft = dayjs(milestone.dueDate).diff(dayjs(), 'day')
  if (daysLeft < 0) return '#f56c6c'
  if (daysLeft <= 3) return '#e6a23c'
  return '#409eff'
}

const getDaysLeftClass = (milestone: Milestone) => {
  if (milestone.status === 'COMPLETED') return 'completed'
  const daysLeft = dayjs(milestone.dueDate).diff(dayjs(), 'day')
  if (daysLeft < 0) return 'overdue'
  if (daysLeft <= 3) return 'urgent'
  return 'normal'
}

const getDaysLeftText = (milestone: Milestone) => {
  if (milestone.status === 'COMPLETED') return '已完成'
  const daysLeft = dayjs(milestone.dueDate).diff(dayjs(), 'day')
  if (daysLeft < 0) return `逾期 ${Math.abs(daysLeft)} 天`
  if (daysLeft === 0) return '今天截止'
  return `还剩 ${daysLeft} 天`
}
</script>

<style scoped>
.milestone-list {
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

.milestones-container {
  max-width: 800px;
}

.milestone-card.completed {
  opacity: 0.8;
}

.milestone-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.milestone-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 500;
}

.milestone-actions {
  display: flex;
  gap: 5px;
}

.milestone-body {
  padding: 10px 0;
}

.description {
  color: #666;
  margin: 0 0 10px 0;
}

.milestone-meta {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.9em;
}

.project-name {
  color: #666;
}

.days-left {
  font-weight: 500;
}

.days-left.completed { color: #67c23a; }
.days-left.overdue { color: #f56c6c; }
.days-left.urgent { color: #e6a23c; }
.days-left.normal { color: #409eff; }

.empty-state {
  padding: 50px;
  text-align: center;
}
</style>
