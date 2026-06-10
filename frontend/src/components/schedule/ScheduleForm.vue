<template>
  <el-form
    ref="formRef"
    :model="formData"
    :rules="rules"
    label-width="100px"
    size="large"
  >
    <el-form-item label="日程标题" prop="title">
      <el-input v-model="formData.title" placeholder="请输入日程标题" />
    </el-form-item>

    <el-form-item label="开始时间" prop="startTime">
      <el-date-picker
        v-model="formData.startTime"
        type="datetime"
        placeholder="选择开始时间"
        format="YYYY-MM-DD HH:mm"
        value-format="YYYY-MM-DDTHH:mm:ss"
      />
    </el-form-item>

    <el-form-item label="结束时间" prop="endTime">
      <el-date-picker
        v-model="formData.endTime"
        type="datetime"
        placeholder="选择结束时间"
        format="YYYY-MM-DD HH:mm"
        value-format="YYYY-MM-DDTHH:mm:ss"
      />
    </el-form-item>

    <el-form-item label="地点" prop="location">
      <el-input v-model="formData.location" placeholder="请输入地点" />
    </el-form-item>

    <el-form-item label="备注" prop="description">
      <el-input
        v-model="formData.description"
        type="textarea"
        :rows="4"
        placeholder="请输入备注信息"
      />
    </el-form-item>

    <el-form-item label="全天事件">
      <el-switch v-model="formData.allDay" />
    </el-form-item>

    <el-form-item>
      <el-button type="primary" @click="handleSubmit">提交</el-button>
      <el-button @click="handleCancel">取消</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup lang="ts">
import { ref, reactive, watch } from 'vue'
import type { FormInstance, FormRules } from 'element-plus'
import type { Event } from '@/types'

interface Props {
  schedule?: Event | null
}

const props = defineProps<Props>()
const emit = defineEmits<{
  submit: [data: Partial<Event>]
  cancel: []
}>()

const formRef = ref<FormInstance>()

const formData = reactive({
  title: '',
  description: '',
  location: '',
  startTime: '',
  endTime: '',
  allDay: false,
})

const rules: FormRules = {
  title: [{ required: true, message: '请输入日程标题', trigger: 'blur' }],
  startTime: [{ required: true, message: '请选择开始时间', trigger: 'change' }],
  endTime: [{ required: true, message: '请选择结束时间', trigger: 'change' }],
}

// 如果编辑，填充表单
watch(
  () => props.schedule,
  (schedule) => {
    if (schedule) {
      Object.assign(formData, {
        title: schedule.title || '',
        description: schedule.description || '',
        location: (schedule as any).location || '',
        startTime: schedule.startTime || '',
        endTime: schedule.endTime || '',
        allDay: schedule.allDay || false,
      })
    } else {
      Object.assign(formData, {
        title: '',
        description: '',
        location: '',
        startTime: '',
        endTime: '',
        allDay: false,
      })
    }
  },
  { immediate: true }
)

const handleSubmit = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate().catch(() => false)
  if (valid) {
    emit('submit', { ...formData })
  }
}

const handleCancel = () => {
  emit('cancel')
}
</script>
