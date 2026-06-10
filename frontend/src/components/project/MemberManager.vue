<template>
  <div class="member-manager">
    <div class="header">
      <h2>项目成员</h2>
      <el-button v-if="!readonly" type="primary" @click="showAddDialog" :icon="Plus">
        添加成员
      </el-button>
    </div>

    <el-table :data="members" stripe style="width: 100%" v-loading="loading">
      <el-table-column label="成员姓名" width="150">
        <template #default="{ row }">
          {{ row.user?.name || row.userName || '-' }}
        </template>
      </el-table-column>
      <el-table-column label="角色" width="120">
        <template #default="{ row }">
          <el-select
            v-if="!readonly && editingRoleId === row.id"
            v-model="editRoleForm.role"
            size="small"
            style="width: 90px"
            @change="handleRoleChange(row)"
          >
            <el-option label="管理员" value="ADMIN" />
            <el-option label="成员" value="MEMBER" />
            <el-option label="观察者" value="VIEWER" />
          </el-select>
          <el-tag v-else :type="getRoleType(row.role)" :style="readonly ? '' : 'cursor: pointer'" @click="!readonly && startEditRole(row)">
            {{ getRoleText(row.role) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="邮箱" width="200">
        <template #default="{ row }">
          {{ row.user?.email || row.userEmail || '-' }}
        </template>
      </el-table-column>
      <el-table-column v-if="!readonly" label="操作" width="200">
        <template #default="{ row }">
          <el-button
            type="primary"
            size="small"
            @click="startEditRole(row)"
          >
            修改角色
          </el-button>
          <el-button
            type="danger"
            size="small"
            @click="handleRemoveMember(row)"
          >
            移除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 添加成员对话框 -->
    <el-dialog
      v-model="addDialogVisible"
      title="添加成员"
      width="500px"
      :close-on-click-modal="false"
    >
      <el-form
        ref="formRef"
        :model="memberForm"
        :rules="rules"
        label-width="100px"
      >
        <el-form-item label="选择用户" prop="userId">
          <el-select
            v-model="memberForm.userId"
            placeholder="请选择用户"
            filterable
            style="width: 100%"
          >
            <el-option
              v-for="user in users"
              :key="user.id"
              :label="user.name + ' (' + user.email + ')'"
              :value="user.id.toString()"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="memberForm.role" placeholder="请选择角色">
            <el-option label="管理员" value="ADMIN" />
            <el-option label="成员" value="MEMBER" />
            <el-option label="观察者" value="VIEWER" />
          </el-select>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="addDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddMember" :loading="submitting">
          添加
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from '@element-plus/icons-vue'
import type { FormInstance, FormRules } from 'element-plus'
import { projectApi } from '@/api'
import { userApi } from '@/api/user'
import { handleApiResponse } from '@/utils/response'

interface Props {
  projectId: string
  readonly?: boolean
}

const props = withDefaults(defineProps<Props>(), {
  readonly: false,
})

const emit = defineEmits<{
  refresh: []
}>()

const formRef = ref<FormInstance>()
const addDialogVisible = ref(false)
const submitting = ref(false)
const members = ref<any[]>([])
const users = ref<any[]>([])
const loading = ref(false)
const editingRoleId = ref<number | null>(null)
const editRoleForm = reactive({
  role: 'MEMBER',
})

const memberForm = reactive({
  userId: '',
  role: 'MEMBER',
})

const rules = reactive<FormRules>({
  userId: [
    { required: true, message: '请选择用户', trigger: 'change' },
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' },
  ],
})

const getRoleType = (role: string) => {
  const typeMap: Record<string, any> = {
    OWNER: 'warning',
    ADMIN: 'danger',
    MEMBER: 'primary',
    VIEWER: 'info',
  }
  return typeMap[role] || 'info'
}

const getRoleText = (role: string) => {
  const textMap: Record<string, string> = {
    OWNER: '所有者',
    ADMIN: '管理员',
    MEMBER: '成员',
    VIEWER: '观察者',
  }
  return textMap[role] || role
}

const showAddDialog = async () => {
  memberForm.userId = ''
  memberForm.role = 'MEMBER'
  addDialogVisible.value = true
  // 加载用户列表
  await fetchUsers()
}

const fetchUsers = async () => {
  try {
    const response = await userApi.getUsers(0, 100)
    const result = handleApiResponse(response, '获取用户列表失败')
    if (result.success && result.data) {
      users.value = result.data.content || result.data || []
    }
  } catch (error) {
    console.error('获取用户列表失败:', error)
  }
}

const handleAddMember = async () => {
  if (!formRef.value) return

  const valid = await formRef.value.validate()
  if (!valid) return

  submitting.value = true

  try {
    const response = await projectApi.addMember(props.projectId, {
      userId: parseInt(memberForm.userId),
      role: memberForm.role
    })
    const result = handleApiResponse(response, '添加成员失败')
    if (result.success) {
      ElMessage.success('添加成员成功')
      addDialogVisible.value = false
      await fetchMembers()
      emit('refresh')
    } else {
      ElMessage.error(result.message || '添加成员失败')
    }
  } catch (error) {
    ElMessage.error('添加成员失败')
  } finally {
    submitting.value = false
  }
}

const getMemberUserId = (member: any): string => {
  // 后端返回的成员对象可能有多种格式
  const uid = member.userId ?? member.user?.id ?? member.id
  return uid?.toString() ?? ''
}

const startEditRole = (member: any) => {
  editingRoleId.value = member.id
  editRoleForm.role = member.role
}

const handleRoleChange = async (member: any) => {
  const userId = getMemberUserId(member)
  if (!userId) {
    ElMessage.error('无法获取用户ID')
    editingRoleId.value = null
    return
  }
  try {
    const response = await projectApi.updateMemberRole(props.projectId, userId, editRoleForm.role)
    const result = handleApiResponse(response, '更新角色失败')
    if (result.success) {
      ElMessage.success('角色更新成功')
      await fetchMembers()
      emit('refresh')
    } else {
      ElMessage.error(result.message || '更新角色失败')
    }
  } catch (error) {
    ElMessage.error('更新角色失败')
  } finally {
    editingRoleId.value = null
  }
}

const handleRemoveMember = async (member: any) => {
  try {
    const userId = getMemberUserId(member)
    if (!userId) {
      ElMessage.error('无法获取用户ID')
      return
    }
    const response = await projectApi.removeMember(props.projectId, userId)
    const result = handleApiResponse(response, '移除成员失败')
    if (result.success) {
      ElMessage.success('移除成员成功')
      await fetchMembers()
      emit('refresh')
    } else {
      ElMessage.error(result.message || '移除成员失败')
    }
  } catch (error) {
    ElMessage.error('移除成员失败')
  }
}

const fetchMembers = async () => {
  loading.value = true
  try {
    const response = await projectApi.getMembers(props.projectId)
    const result = handleApiResponse(response, '获取成员列表失败')
    if (result.success && result.data) {
      members.value = result.data || []
    }
  } catch (error) {
    ElMessage.error('获取成员列表失败')
  } finally {
    loading.value = false
  }
}

// 监听 projectId 变化
import { watch } from 'vue'
watch(() => props.projectId, fetchMembers, { immediate: true })
</script>

<style scoped>
.member-manager {
  padding: 20px;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header h2 {
  margin: 0;
  color: #333;
}
</style>
