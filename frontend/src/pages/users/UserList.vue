<template>
  <div class="user-management">
    <div class="page-header">
      <h2>用户管理</h2>
      <el-button type="primary" @click="showAddDialog">添加用户</el-button>
    </div>

    <!-- 搜索栏 -->
    <div class="search-bar">
      <el-input
        v-model="searchKeyword"
        placeholder="搜索用户名、姓名或邮箱"
        clearable
        style="width: 300px"
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
    </div>

    <!-- 用户列表 -->
    <el-table :data="users" v-loading="loading" stripe>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="username" label="用户名" width="150" />
      <el-table-column prop="name" label="姓名" width="150" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '正常' : '已禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="isAdmin" label="角色" width="100">
        <template #default="{ row }">
          <el-tag :type="row.isAdmin ? 'warning' : 'info'">
            {{ row.isAdmin ? '管理员' : '普通用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" fixed="right" width="280">
        <template #default="{ row }">
          <el-button link type="primary" @click="handleEdit(row)">编辑</el-button>
          <el-button link type="primary" @click="handleChangePassword(row)">改密</el-button>
          <el-button
            link
            :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
          <el-button link type="danger" @click="handleDelete(row)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 分页 -->
    <div class="pagination">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>

    <!-- 添加/编辑用户对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
      @close="handleDialogClose"
    >
      <el-form
        ref="userFormRef"
        :model="userForm"
        :rules="userRules"
        label-width="80px"
      >
        <el-form-item label="用户名" prop="username">
          <el-input
            v-model="userForm.username"
            :disabled="isEditMode"
            placeholder="请输入用户名"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="name">
          <el-input v-model="userForm.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="userForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item v-if="!isEditMode" label="密码" prop="password">
          <el-input
            v-model="userForm.password"
            type="password"
            show-password
            placeholder="请输入密码（至少6位）"
          />
        </el-form-item>
        <el-form-item label="管理员">
          <el-switch v-model="userForm.isAdmin" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>

    <!-- 修改密码对话框 -->
    <el-dialog
      v-model="passwordDialogVisible"
      title="修改密码"
      width="500px"
      @close="handlePasswordDialogClose"
    >
      <el-form
        ref="passwordFormRef"
        :model="passwordForm"
        :rules="passwordRules"
        label-width="80px"
      >
        <el-form-item label="用户">
          <el-input :value="currentUser?.username" disabled />
        </el-form-item>
        <el-form-item label="旧密码" prop="oldPassword">
          <el-input
            v-model="passwordForm.oldPassword"
            type="password"
            show-password
            placeholder="请输入旧密码"
          />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input
            v-model="passwordForm.newPassword"
            type="password"
            show-password
            placeholder="请输入新密码（至少6位）"
          />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input
            v-model="passwordForm.confirmPassword"
            type="password"
            show-password
            placeholder="请再次输入新密码"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="passwordDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handlePasswordSubmit" :loading="submitLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { userApi } from '@/api/user'
import { authApi } from '@/api'
import { formatDate } from '@/utils/date'
import type { FormInstance } from 'element-plus'

interface User {
  id: string
  username: string
  name: string
  email: string
  avatar?: string
  isAdmin: boolean
  status: 'ACTIVE' | 'INACTIVE' | 'DELETED'
  createdAt: string
  updatedAt: string
}

const users = ref<User[]>([])
const loading = ref(false)
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const searchKeyword = ref('')

const dialogVisible = ref(false)
const passwordDialogVisible = ref(false)
const isEditMode = ref(false)
const submitLoading = ref(false)
const currentUser = ref<User | null>(null)
const userFormRef = ref<FormInstance>()
const passwordFormRef = ref<FormInstance>()

const dialogTitle = ref('添加用户')

const userForm = reactive({
  username: '',
  name: '',
  email: '',
  password: '',
  isAdmin: false
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const userRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '用户名长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入旧密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule: any, value: string, callback: any) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

/**
 * 加载用户列表
 */
const loadUsers = async () => {
  loading.value = true
  try {
    const response = await userApi.getUsers(currentPage.value - 1, pageSize.value)
    if (response && response.data && response.data.code === 200) {
      users.value = response.data.data.content
      total.value = response.data.data.totalElements
    }
  } catch (error) {
    console.error('加载用户列表失败:', error)
    ElMessage.error('加载用户列表失败')
  } finally {
    loading.value = false
  }
}

/**
 * 搜索
 */
const handleSearch = () => {
  currentPage.value = 1
  loadUsers()
}

/**
 * 分页大小改变
 */
const handleSizeChange = (val: number) => {
  pageSize.value = val
  loadUsers()
}

/**
 * 页码改变
 */
const handlePageChange = (val: number) => {
  currentPage.value = val
  loadUsers()
}

/**
 * 显示添加对话框
 */
const showAddDialog = () => {
  isEditMode.value = false
  dialogTitle.value = '添加用户'
  userForm.username = ''
  userForm.name = ''
  userForm.email = ''
  userForm.password = ''
  userForm.isAdmin = false
  dialogVisible.value = true
}

/**
 * 编辑用户
 */
const handleEdit = (row: User) => {
  isEditMode.value = true
  dialogTitle.value = '编辑用户'
  userForm.username = row.username
  userForm.name = row.name
  userForm.email = row.email || ''
  userForm.password = ''
  userForm.isAdmin = row.isAdmin
  currentUser.value = row
  dialogVisible.value = true
}

/**
 * 修改密码
 */
const handleChangePassword = (row: User) => {
  currentUser.value = row
  passwordForm.oldPassword = ''
  passwordForm.newPassword = ''
  passwordForm.confirmPassword = ''
  passwordDialogVisible.value = true
}

/**
 * 切换用户状态
 */
const handleToggleStatus = async (row: User) => {
  const action = row.status === 'ACTIVE' ? '禁用' : '启用'
  try {
    await ElMessageBox.confirm(
      `确定要${action}用户 ${row.name} 吗？`,
      `确认${action}`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const api = row.status === 'ACTIVE' ? userApi.disableUser : userApi.enableUser
    const response = await api(row.id)

    if (response && response.data && response.data.code === 200) {
      ElMessage.success(`${action}成功`)
      loadUsers()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error(`${action}失败:`, error)
      ElMessage.error(`${action}失败`)
    }
  }
}

/**
 * 删除用户
 */
const handleDelete = async (row: User) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${row.name} 吗？删除后数据将无法恢复。`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    const response = await userApi.deleteUser(row.id)
    if (response && response.data && response.data.code === 200) {
      ElMessage.success('删除成功')
      loadUsers()
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('删除失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

/**
 * 提交表单
 */
const handleSubmit = async () => {
  if (!userFormRef.value) return

  await userFormRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (isEditMode.value && currentUser.value) {
          // 编辑模式
          const response = await userApi.updateUser(currentUser.value.id, {
            name: userForm.name,
            email: userForm.email,
            isAdmin: userForm.isAdmin
          })
          if (response && response.data && response.data.code === 200) {
            ElMessage.success('更新成功')
            dialogVisible.value = false
            loadUsers()
          }
        } else {
          // 添加模式
          const response = await authApi.register(
            userForm.username,
            userForm.password,
            userForm.email,
            userForm.name
          )
          if (response && response.data && response.data.code === 200) {
            ElMessage.success('添加成功')
            dialogVisible.value = false
            loadUsers()
          }
        }
      } catch (error: any) {
        console.error('操作失败:', error)
        ElMessage.error(error.response?.data?.message || '操作失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

/**
 * 提交密码修改
 */
const handlePasswordSubmit = async () => {
  if (!passwordFormRef.value) return

  await passwordFormRef.value.validate(async (valid) => {
    if (valid && currentUser.value) {
      submitLoading.value = true
      try {
        const response = await userApi.changePassword(
          currentUser.value.id,
          passwordForm.oldPassword,
          passwordForm.newPassword
        )
        if (response && response.data && response.data.code === 200) {
          ElMessage.success('密码修改成功')
          passwordDialogVisible.value = false
        }
      } catch (error: any) {
        console.error('密码修改失败:', error)
        ElMessage.error(error.response?.data?.message || '密码修改失败')
      } finally {
        submitLoading.value = false
      }
    }
  })
}

/**
 * 对话框关闭
 */
const handleDialogClose = () => {
  userFormRef.value?.resetFields()
}

/**
 * 密码对话框关闭
 */
const handlePasswordDialogClose = () => {
  passwordFormRef.value?.resetFields()
}

// 组件挂载时加载数据
onMounted(() => {
  loadUsers()
})
</script>

<style scoped>
.user-management {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  margin: 0;
}

.search-bar {
  display: flex;
  gap: 10px;
  margin-bottom: 20px;
}

.pagination {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>
