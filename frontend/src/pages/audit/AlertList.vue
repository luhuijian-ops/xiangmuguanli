<template>
  <div class="alert-list-page">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>告警列表</span>
        </div>
      </template>

      <!-- 筛选条件 -->
      <el-form :inline="true" class="filter-form">
        <el-form-item label="告警类型">
          <el-select v-model="filters.type" placeholder="请选择" clearable>
            <el-option label="登录失败" value="LOGIN_FAILURE" />
            <el-option label="可疑活动" value="SUSPICIOUS_ACTIVITY" />
            <el-option label="权限拒绝" value="PERMISSION_DENIED" />
          </el-select>
        </el-form-item>
        <el-form-item label="严重程度">
          <el-select v-model="filters.severity" placeholder="请选择" clearable>
            <el-option label="低" value="LOW" />
            <el-option label="中" value="MEDIUM" />
            <el-option label="高" value="HIGH" />
            <el-option label="严重" value="CRITICAL" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="filters.resolved" placeholder="请选择" clearable>
            <el-option label="未处理" :value="false" />
            <el-option label="已处理" :value="true" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="loadAlerts">查询</el-button>
          <el-button @click="resetFilters">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 告警表格 -->
      <el-table :data="alerts" v-loading="loading" stripe>
        <el-table-column prop="createdAt" label="时间" width="180" />
        <el-table-column prop="type" label="类型" width="120">
          <template #default="{ row }">
            <el-tag>{{ typeLabels[row.type] || row.type }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="severity" label="严重程度" width="100">
          <template #default="{ row }">
            <el-tag :type="severityType[row.severity]">{{ severityLabels[row.severity] }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="userName" label="用户" width="120" />
        <el-table-column prop="message" label="消息" />
        <el-table-column prop="resolved" label="状态" width="80">
          <template #default="{ row }">
            <el-tag :type="row.resolved ? 'success' : 'danger'">
              {{ row.resolved ? '已处理' : '未处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              v-if="!row.resolved"
              type="primary"
              size="small"
              @click="handleResolve(row.id)"
            >
              标记已处理
            </el-button>
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
        @size-change="loadAlerts"
        @current-change="loadAlerts"
      />
    </el-card>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { alertApi } from '@/api'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const alerts = ref<any[]>([])
const pagination = reactive({
  page: 1,
  pageSize: 20,
  total: 0
})
const filters = reactive({
  type: '',
  severity: '',
  resolved: null as boolean | null
})

const typeLabels: Record<string, string> = {
  LOGIN_FAILURE: '登录失败',
  SUSPICIOUS_ACTIVITY: '可疑活动',
  PERMISSION_DENIED: '权限拒绝'
}

const severityLabels: Record<string, string> = {
  LOW: '低',
  MEDIUM: '中',
  HIGH: '高',
  CRITICAL: '严重'
}

const severityType: Record<string, string> = {
  LOW: 'info',
  MEDIUM: 'warning',
  HIGH: 'danger',
  CRITICAL: 'danger'
}

const loadAlerts = async () => {
  loading.value = true
  try {
    const params: any = {
      page: pagination.page,
      pageSize: pagination.pageSize
    }
    if (filters.type) {
      params.type = filters.type
    }
    if (filters.severity) {
      params.severity = filters.severity
    }
    if (filters.resolved !== null) {
      params.resolved = filters.resolved
    }

    const response = await alertApi.getAlerts(params)
    if (response && response.data && response.data.code === 200) {
      alerts.value = response.data.data.alerts
      pagination.total = response.data.data.total
    }
  } catch (error) {
    ElMessage.error('加载告警列表失败')
  } finally {
    loading.value = false
  }
}

const handleResolve = async (id: string) => {
  try {
    await ElMessageBox.confirm('确定要标记该告警为已处理吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })

    const response = await alertApi.resolveAlert(id)
    if (response && response.data && response.data.code === 200) {
      ElMessage.success('操作成功')
      loadAlerts()
    }
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('操作失败')
    }
  }
}

const resetFilters = () => {
  filters.type = ''
  filters.severity = ''
  filters.resolved = null
  pagination.page = 1
  loadAlerts()
}

onMounted(() => {
  loadAlerts()
})
</script>

<style scoped>
.alert-list-page {
  padding: 20px;
}
.filter-form {
  margin-bottom: 20px;
}
</style>
