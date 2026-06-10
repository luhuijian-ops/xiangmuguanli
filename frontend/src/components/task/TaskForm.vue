<template>
  <div class="task-form">
    <h2>{{ isEdit ? '编辑任务' : '创建任务' }}</h2>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      label-position="top"
    >
      <el-form-item v-if="!hasProjectId" label="所属项目" prop="projectId">
        <el-select
          v-model="selectedProjectId"
          placeholder="请选择项目"
          style="width: 100%"
          @change="onProjectChange"
        >
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.name"
            :value="String(project.id)"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="任务标题" prop="title">
        <el-input
          v-model="formData.title"
          placeholder="请输入任务标题"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="任务描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入任务描述"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="优先级" prop="priority">
        <el-select v-model="formData.priority" placeholder="请选择优先级">
          <el-option label="P0 - 最高" :value="0" />
          <el-option label="P1 - 高" :value="1" />
          <el-option label="P2 - 中" :value="2" />
          <el-option label="P3 - 普通" :value="3" />
          <el-option label="P4 - 最低" :value="4" />
        </el-select>
      </el-form-item>

      <el-form-item label="负责人" prop="assigneeId">
        <el-select
          v-model="formData.assigneeId"
          placeholder="请选择负责人"
          filterable
          clearable
        >
          <el-option
            v-for="user in users"
            :key="user.id"
            :label="user.name"
            :value="user.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="开始时间" prop="startDate">
        <el-date-picker
          v-model="formData.startDate"
          type="date"
          placeholder="选择开始时间"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="截止时间" prop="dueDate">
        <el-date-picker
          v-model="formData.dueDate"
          type="date"
          placeholder="选择截止时间"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="标签" prop="tags">
        <el-input
          v-model="tagsInput"
          placeholder="输入标签，用逗号分隔，如：前端,紧急,需求"
          maxlength="200"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <div class="form-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitting">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import type { Task } from '@/types'
import { projectApi, userApi } from '@/api'
import { useUserStore } from '@/stores/user'

interface Props {
  projectId?: string
  task?: Task | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  submit: [data: Partial<Task>]
  cancel: []
}>()

const userStore = useUserStore()
const formRef = ref<FormInstance>()
const submitting = ref(false)
const users = ref<any[]>([])
const projects = ref<any[]>([])
const selectedProjectId = ref(props.projectId || '')

const isEdit = computed(() => !!props.task)
const hasProjectId = computed(() => !!props.projectId && props.projectId !== '0')
const activeProjectId = computed(() => hasProjectId.value ? props.projectId : selectedProjectId.value)

const tagsInput = ref('')

const formData = reactive({
  title: '',
  description: '',
  priority: 3,
  assigneeId: undefined as number | undefined,
  startDate: '',
  dueDate: '',
})

const rules = reactive<FormRules>({
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' },
  ],
  priority: [
    { required: true, message: '请选择优先级', trigger: 'change' },
  ],
  startDate: [
    { type: 'date', message: '请选择开始时间', trigger: 'change' },
  ],
  dueDate: [
    { type: 'date', message: '请选择截止时间', trigger: 'change' },
  ],
})

const parseTags = (tags?: string): string => {
  if (!tags) return ''
  try {
    const parsed = JSON.parse(tags)
    if (Array.isArray(parsed)) return parsed.join(',')
  } catch {
    // not JSON
  }
  return tags
}

const initFormData = () => {
  if (props.task) {
    Object.assign(formData, {
      title: props.task.title,
      description: props.task.description || '',
      priority: props.task.priority,
      assigneeId: props.task.assigneeId || undefined,
      startDate: props.task.startDate || '',
      dueDate: props.task.dueDate || '',
    })
    tagsInput.value = parseTags(props.task.tags)
  } else {
    Object.assign(formData, {
      title: '',
      description: '',
      priority: 3,
      assigneeId: undefined,
      startDate: '',
      dueDate: '',
    })
    tagsInput.value = ''
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate()
  if (!valid) return

  submitting.value = true

  try {
    // 构建提交数据
    const tags = tagsInput.value
      .split(',')
      .map(t => t.trim())
      .filter(Boolean)

    const submitData: any = {
      title: formData.title,
      description: formData.description,
      priority: formData.priority,
      assigneeId: formData.assigneeId,
      startDate: formData.startDate,
      dueDate: formData.dueDate,
      tags: tags.length > 0 ? JSON.stringify(tags) : undefined,
    }

    const pid = activeProjectId.value
    if (!pid) {
      ElMessage.error('请选择所属项目')
      return
    }

    if (isEdit.value) {
      emit('submit', { id: props.task!.id, ...submitData })
    } else {
      emit('submit', { ...submitData, projectId: pid })
    }
  } finally {
    submitting.value = false
  }
}

const handleCancel = () => {
  emit('cancel')
}

const fetchUsers = async () => {
  try {
    const response = await userApi.getUsers(0, 100)
    if (response && response.data && response.data.code === 200) {
      const data = response.data.data
      users.value = data.content || data || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

const fetchProjects = async () => {
  try {
    const response = await projectApi.getMyProjects({ size: 100 })
    if (response && response.data && response.data.code === 200) {
      const data = response.data.data
      projects.value = data.content || data || []
    }
  } catch (error) {
    console.error('获取项目列表失败:', error)
  }
}

const onProjectChange = () => {
  formData.assigneeId = undefined
}

// 监听 props 变化
import { watch } from 'vue'
watch(() => [props.task, props.projectId], () => {
  selectedProjectId.value = props.projectId || ''
  initFormData()
}, { immediate: true })
onMounted(() => {
  if (!hasProjectId.value) {
    fetchProjects()
  }
  fetchUsers()
})
</script>

<style scoped>
.task-form {
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

h2 {
  margin: 0 0 20px 0;
  color: #333;
}

.form-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 20px;
  padding-top: 20px;
  border-top: 1px solid #f0f0f0;
}
</style>
