<template>
  <div class="audit-log-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>审计日志</span>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filters.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
          />
        </el-form-item>
        <el-form-item label="操作类型">
          <el-select v-model="filters.action" placeholder="请选择" clearable>
            <el-option label="登录成功" value="LOGIN_SUCCESS" />
            <el-option label="登录失败" value="LOGIN_FAILURE" />
            <el-option label="任务创建" value="TASK_CREATE" />
            <el-option label="任务更新" value="TASK_UPDATE" />
            <el-option label="任务删除" value="TASK_DELETE" />
            <el-option label="项目创建" value="PROJECT_CREATE" />
            <el-option label="项目更新" value="PROJECT_UPDATE" />
            <el-option label="项目删除" value="PROJECT_DELETE" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadLogs">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 日志表格 -->
      <el-table :data="logs" v-loading="loading" stripe>
        <el-table-column prop="createdAt" label="时间" width="180">
          <template #default="{ row }">
            {{ formatDate(row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="action" label="操作" width="120">
          <template #default="{ row }">
            <el-tag>{{ row.action }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="详情" min-width="200">
          <template #default="{ row }">
            {{ getActionDescription(row) }}
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.pageSize"
        :total="pagination.total"
        :page-sizes="[10, 20, 50, 100]"
        layout="total, sizes, prev, pager, next"
        @size-change="loadLogs"
        @current-change="loadLogs"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { auditApi } from '@/api'
import { ElMessage } from 'element-plus'
import { formatDate } from '@/utils/date'

const loading = ref(false)
const logs = ref<any[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})
const filters = reactive({
  dateRange: [] as string[],
  action: ''
})

const loadLogs = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page - 1,
      pageSize: pagination.pageSize
    }
    if (filters.action) {
      params.action = filters.action
    }
    if (filters.dateRange && filters.dateRange.length === 2) {
      params.startDate = filters.dateRange[0]
      params.endDate = filters.dateRange[1]
    }

    const response = await auditApi.getAuditLogs(params)
    if (response && response.data && response.data.code === 200) {
      logs.value = response.data.data.content
      pagination.total = response.data.data.totalElements
    }
  } catch (error) {
    ElMessage.error('加载审计日志失败')
  } finally {
    loading.value = false
  }
}

const resetFilters = () => {
  filters.dateRange = []
  filters.action = ''
  pagination.page = 1
  loadLogs()
}

// 获取操作描述
const getActionDescription = (row: any): string => {
  const actionMap: Record<string, string> = {
    'LOGIN_SUCCESS': '用户登录成功',
    'LOGIN_FAILURE': '用户登录失败',
    'USER_REGISTER': '用户注册',
    'TASK_CREATE': '创建任务',
    'TASK_UPDATE': '更新任务',
    'TASK_DELETE': '删除任务',
    'PROJECT_CREATE': '创建项目',
    'PROJECT_UPDATE': '更新项目',
    'PROJECT_DELETE': '删除项目'
  }
  return actionMap[row.action] || row.action || '-'
}

onMounted(() => {
  loadLogs()
})
</script>

<style scoped>
.audit-log-page {
  padding: 20px;
}
.filter-form {
  margin-bottom: 20px;
}
</style>
