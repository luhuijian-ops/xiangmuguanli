<template>
  <div class="project-form">
    <h2>{{ isEdit ? '编辑项目' : '创建项目' }}</h2>

    <el-form
      ref="formRef"
      :model="formData"
      :rules="rules"
      label-width="100px"
      label-position="top"
    >
      <el-form-item label="项目名称" prop="name">
        <el-input
          v-model="formData.name"
          placeholder="请输入项目名称"
          maxlength="100"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="项目编码" prop="code">
        <el-input
          v-model="formData.code"
          placeholder="请输入项目编码"
          maxlength="50"
        />
      </el-form-item>

      <el-form-item label="项目描述" prop="description">
        <el-input
          v-model="formData.description"
          type="textarea"
          :rows="4"
          placeholder="请输入项目描述"
          maxlength="500"
          show-word-limit
        />
      </el-form-item>

      <el-form-item label="项目负责人" prop="ownerId">
        <el-select
          v-model="formData.ownerId"
          placeholder="请选择项目负责人"
          filterable
          style="width: 100%"
        >
          <el-option
            v-for="user in userList"
            :key="user.id"
            :label="user.name"
            :value="user.id"
          />
        </el-select>
      </el-form-item>

      <el-form-item label="项目等级" prop="priority">
        <el-select v-model="formData.priority" placeholder="请选择项目等级" style="width: 100%">
          <el-option label="高" value="HIGH" />
          <el-option label="中" value="MEDIUM" />
          <el-option label="低" value="LOW" />
        </el-select>
      </el-form-item>

      <el-form-item label="项目预算（元）" prop="budget">
        <el-input-number
          v-model="formData.budget"
          :min="0"
          :precision="2"
          :step="10000"
          placeholder="请输入项目预算"
          style="width: 100%"
        />
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

      <el-form-item label="结束时间" prop="endDate">
        <el-date-picker
          v-model="formData.endDate"
          type="date"
          placeholder="选择结束时间"
          format="YYYY-MM-DD"
          value-format="YYYY-MM-DD"
        />
      </el-form-item>

      <el-form-item label="项目备注" prop="remarks">
        <el-input
          v-model="formData.remarks"
          type="textarea"
          :rows="3"
          placeholder="请输入项目备注"
          maxlength="1000"
          show-word-limit
        />
      </el-form-item>
    </el-form>

    <div class="form-actions">
      <el-button @click="handleCancel">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="loading">
        {{ isEdit ? '更新' : '创建' }}
      </el-button>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Project, User } from '@/types'
import { userApi } from '@/api'
import { useUserStore } from '@/stores/user'

interface Props {
  project?: Project | null
  loading?: boolean
}

const props = defineProps<Props>()
const emit = defineEmits<{
  submit: [data: Partial<Project>]
  cancel: []
}>()

const formRef = ref<FormInstance>()

const isEdit = computed(() => !!props.project)
const userStore = useUserStore()

const userList = ref<User[]>([])

const fetchUsers = async () => {
  try {
    const response = await userApi.getUsers(0, 100)
    const data = response.data?.data
    if (data) {
      userList.value = data.content || data || []
    }
  } catch (err) {
    console.error('获取用户列表失败:', err)
  }
}

onMounted(() => {
  fetchUsers()
})

const formData = reactive({
  name: '',
  code: '',
  description: '',
  ownerId: undefined as number | undefined,
  priority: '' as string,
  budget: undefined as number | undefined,
  startDate: '',
  endDate: '',
  remarks: '',
})

const rules = reactive<FormRules>({
  name: [
    { required: true, message: '请输入项目名称', trigger: 'blur' },
    { min: 2, max: 100, message: '项目名称长度在 2 到 100 个字符', trigger: 'blur' },
  ],
  code: [
    { required: true, message: '请输入项目编码', trigger: 'blur' },
    { pattern: /^[a-zA-Z0-9_-]+$/, message: '只能包含字母、数字、下划线和横线', trigger: 'blur' },
  ],
  ownerId: [
    { required: true, message: '请选择项目负责人', trigger: 'change' },
  ],
  startDate: [
    { required: false, message: '请选择开始时间', trigger: 'change' },
  ],
  endDate: [
    { required: false, message: '请选择结束时间', trigger: 'change' },
    {
      validator: (_rule: any, value: any, callback: any) => {
        if (value && formData.startDate && value < formData.startDate) {
          callback(new Error('结束时间不能早于开始时间'))
        } else {
          callback()
        }
      },
      trigger: 'change',
    },
  ],
})

// 初始化表单数据
const initFormData = () => {
  if (props.project) {
    Object.assign(formData, {
      name: props.project.name,
      code: props.project.code || '',
      description: props.project.description || '',
      ownerId: props.project.ownerId,
      priority: props.project.priority || '',
      budget: props.project.budget,
      startDate: props.project.startDate || '',
      endDate: props.project.endDate || '',
      remarks: props.project.remarks || '',
    })
  } else {
    const currentUserId = userStore.userInfo?.id
    Object.assign(formData, {
      name: '',
      code: '',
      description: '',
      ownerId: currentUserId ? Number(currentUserId) : undefined,
      priority: '',
      budget: undefined,
      startDate: '',
      endDate: '',
      remarks: '',
    })
  }
}

const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate()
  if (!valid) return

  const data = isEdit.value
    ? { id: props.project!.id, ...formData }
    : { ...formData }

  emit('submit', data)
}

const handleCancel = () => {
  emit('cancel')
}

// 监听 project 变化
import { watch } from 'vue'
watch(() => props.project, initFormData, { immediate: true })
</script>

<style scoped>
.project-form {
  padding: 20px;
  background: white;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
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
