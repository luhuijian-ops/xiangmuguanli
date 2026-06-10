<template>
  <div class="project-layout">
    <main class="project-main">
      <div class="header">
        <h1>项目列表</h1>
        <div class="header-actions">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索项目名称、编码"
            :prefix-icon="Search"
            clearable
            style="width: 260px"
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          />
          <el-button type="primary" :icon="Plus" @click="showCreateDialog">
            创建项目
          </el-button>
        </div>
      </div>

      <!-- 状态筛选与计数 -->
      <div class="filter-bar">
        <el-radio-group v-model="statusFilter" @change="handleFilterChange">
          <el-radio-button label="ALL">全部</el-radio-button>
          <el-radio-button label="ACTIVE">进行中</el-radio-button>
          <el-radio-button label="ARCHIVED">已归档</el-radio-button>
        </el-radio-group>
        <span class="filter-count">共 {{ filteredProjects.length }} 个项目</span>
      </div>

      <div v-if="error" class="error-message">
        <el-alert :title="error" type="error" show-icon :closable="false" />
      </div>

      <div v-if="loading" class="loading-container">
        <el-skeleton :rows="6" animated />
      </div>

      <div v-else-if="paginatedProjects.length > 0" class="table-wrapper">
        <el-table
          :data="paginatedProjects"
          stripe
          border
          style="width: 100%"
          size="default"
        >
          <el-table-column type="index" label="序号" width="60" align="center" />
          <el-table-column prop="code" label="项目编号" width="100" align="center" show-overflow-tooltip />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="statusTagType(row.status)" size="small">
                {{ statusLabel(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="项目名称" min-width="140" show-overflow-tooltip />
          <el-table-column prop="ownerName" label="项目负责人" width="110" align="center" />
          <el-table-column label="项目参与人" min-width="140" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.memberNames?.join('、') || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="项目等级" width="90" align="center">
            <template #default="{ row }">
              <el-tag :type="priorityTagType(row.priority)" size="small">
                {{ priorityLabel(row.priority) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="startDate" label="开始日期" width="110" align="center">
            <template #default="{ row }">
              {{ row.startDate || '-' }}
            </template>
          </el-table-column>
          <el-table-column prop="endDate" label="结束日期" width="110" align="center">
            <template #default="{ row }">
              {{ row.endDate || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="项目说明" min-width="160" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.description || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="项目附件" width="90" align="center">
            <template #default="{ row }">
              <span v-if="row.fileCount && row.fileCount > 0">{{ row.fileCount }} 个</span>
              <span v-else>-</span>
            </template>
          </el-table-column>
          <el-table-column label="项目备注" min-width="120" show-overflow-tooltip>
            <template #default="{ row }">
              {{ row.remarks || '-' }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right" align="center">
            <template #default="{ row }">
              <el-button v-if="row.status === 'ACTIVE'" link type="primary" size="small" @click="handleProjectEdit(row)">编辑</el-button>
              <el-button link type="primary" size="small" @click="handleProjectClick(row)">详情</el-button>
              <el-dropdown size="small" trigger="click">
                <el-button link type="primary" size="small">更多</el-button>
                <template #dropdown>
                  <el-dropdown-menu>
                    <el-dropdown-item v-if="row.status === 'ACTIVE'" @click="handleProjectArchive(row)">归档</el-dropdown-item>
                    <el-dropdown-item v-else-if="row.status === 'ARCHIVED'" @click="handleProjectUnarchive(row)">恢复</el-dropdown-item>
                    <el-dropdown-item divided @click="handleProjectDelete(row)" style="color: #f56c6c">删除</el-dropdown-item>
                  </el-dropdown-menu>
                </template>
              </el-dropdown>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <div v-else class="empty-state">
        <el-empty :description="emptyText">
          <el-button v-if="statusFilter === 'ALL'" type="primary" @click="showCreateDialog">
            创建第一个项目
          </el-button>
        </el-empty>
      </div>

      <!-- 分页 -->
      <div v-if="filteredProjects.length > pageSize" class="pagination-wrapper">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[8, 12, 24, 48]"
          layout="total, sizes, prev, pager, next"
          :total="filteredProjects.length"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </div>
    </main>

    <!-- 创建/编辑项目对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="editingProject ? '编辑项目' : '创建项目'"
      width="600px"
      :close-on-click-modal="false"
    >
      <project-form
        :project="editingProject"
        :loading="loading"
        @submit="handleProjectSubmit"
        @cancel="dialogVisible = false"
      />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type MessageBoxType } from 'element-plus'
import { Plus, Search } from '@element-plus/icons-vue'
import { useProject } from '@/composables/useProject'
import ProjectForm from '@/components/project/ProjectForm.vue'
import type { Project } from '@/types'

const router = useRouter()
const {
  projects,
  loading,
  error,
  fetchProjects,
  createProject,
  updateProject,
  deleteProject,
  archiveProject,
  unarchiveProject,
} = useProject()

const searchKeyword = ref('')
const dialogVisible = ref(false)
const editingProject = ref<Project | null>(null)
const statusFilter = ref<'ALL' | 'ACTIVE' | 'ARCHIVED'>('ALL')
const currentPage = ref(1)
const pageSize = ref(12)

onMounted(async () => {
  try {
    await fetchProjects({ size: 9999 })
  } catch (e) {
    console.error('加载项目列表失败:', e)
  }
})

// 统计数量
const activeCount = computed(() => projects.value.filter(p => p.status === 'ACTIVE').length)
const archivedCount = computed(() => projects.value.filter(p => p.status === 'ARCHIVED').length)

// 筛选后的项目列表
const filteredProjects = computed(() => {
  let list = projects.value

  // 按状态筛选
  if (statusFilter.value !== 'ALL') {
    list = list.filter(p => p.status === statusFilter.value)
  }

  // 按关键词搜索
  if (searchKeyword.value.trim()) {
    const keyword = searchKeyword.value.toLowerCase().trim()
    list = list.filter(p =>
      p.name.toLowerCase().includes(keyword) ||
      (p.description && p.description.toLowerCase().includes(keyword)) ||
      (p.code && p.code.toLowerCase().includes(keyword)) ||
      (p.ownerName && p.ownerName.toLowerCase().includes(keyword))
    )
  }

  return list
})

// 分页后的项目列表
const paginatedProjects = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  const end = start + pageSize.value
  return filteredProjects.value.slice(start, end)
})

const emptyText = computed(() => {
  if (statusFilter.value === 'ACTIVE') return '暂无进行中的项目'
  if (statusFilter.value === 'ARCHIVED') return '暂无已归档的项目'
  return '暂无项目'
})

const showCreateDialog = () => {
  editingProject.value = null
  dialogVisible.value = true
}

const handleProjectClick = (project: Project) => {
  router.push(`/projects/${project.id}`)
}

const handleProjectEdit = (project: Project) => {
  editingProject.value = project
  dialogVisible.value = true
}

const handleProjectDelete = async (project: Project) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除项目 "${project.name}" 吗？删除后不可恢复。`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning' as MessageBoxType,
      }
    )

    const result = await deleteProject(project.id)
    if (result.success) {
      ElMessage.success('项目删除成功')
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

const handleProjectArchive = async (project: Project) => {
  try {
    await ElMessageBox.confirm(
      `确定要归档项目 "${project.name}" 吗？归档后项目将变为只读。`,
      '确认归档',
      { confirmButtonText: '归档', cancelButtonText: '取消', type: 'warning' }
    )
    const result = await archiveProject(project.id)
    if (result.success) {
      ElMessage.success('项目已归档')
    } else {
      ElMessage.error(result.message || '归档失败')
    }
  } catch {
    // 取消
  }
}

const handleProjectUnarchive = async (project: Project) => {
  try {
    await ElMessageBox.confirm(
      `确定要恢复项目 "${project.name}" 吗？`,
      '确认恢复',
      { confirmButtonText: '恢复', cancelButtonText: '取消', type: 'info' }
    )
    const result = await unarchiveProject(project.id)
    if (result.success) {
      ElMessage.success('项目已恢复')
    } else {
      ElMessage.error(result.message || '恢复失败')
    }
  } catch {
    // 取消
  }
}

const handleProjectSubmit = async (data: Partial<Project>) => {
  console.log('[handleProjectSubmit] data:', data, 'editing:', editingProject.value?.id)
  let result
  if (editingProject.value) {
    result = await updateProject(editingProject.value.id, data)
    console.log('[handleProjectSubmit] update result:', result)
    if (result.success) {
      ElMessage.success('项目更新成功')
      dialogVisible.value = false
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  } else {
    result = await createProject(data)
    console.log('[handleProjectSubmit] create result:', result)
    if (result.success) {
      ElMessage.success('项目创建成功')
      dialogVisible.value = false
      // 刷新列表确保数据最新
      await fetchProjects()
    } else {
      ElMessage.error(result.message || '创建失败')
    }
  }
}

const handleSearch = () => {
  currentPage.value = 1
}

const handleFilterChange = () => {
  currentPage.value = 1
}

const handleSizeChange = (size: number) => {
  pageSize.value = size
  currentPage.value = 1
}

const handlePageChange = (page: number) => {
  currentPage.value = page
}

// 项目等级标签类型
const priorityTagType = (priority?: string) => {
  const map: Record<string, any> = {
    HIGH: 'danger',
    MEDIUM: 'warning',
    LOW: 'info',
  }
  return map[priority || ''] || 'info'
}

// 项目等级标签文本
const priorityLabel = (priority?: string) => {
  const map: Record<string, string> = {
    HIGH: '高',
    MEDIUM: '中',
    LOW: '低',
  }
  return map[priority || ''] || '-'
}

// 状态标签类型
const statusTagType = (status?: string) => {
  const map: Record<string, any> = {
    ACTIVE: 'success',
    ARCHIVED: 'info',
    DELETED: 'danger',
  }
  return map[status || ''] || 'info'
}

// 状态标签文本
const statusLabel = (status?: string) => {
  const map: Record<string, string> = {
    ACTIVE: '进行中',
    ARCHIVED: '已归档',
    DELETED: '已删除',
  }
  return map[status || ''] || status || '-'
}
</script>

<style scoped>
.project-layout {
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
  gap: 15px;
  align-items: center;
}

.filter-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 20px;
  padding: 12px 16px;
  background: #f8f9fa;
  border-radius: 8px;
}

.filter-count {
  color: #666;
  font-size: 0.9em;
}

.loading-container {
  padding: 20px;
}

.table-wrapper {
  background: white;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
}

.empty-state {
  padding: 50px;
  text-align: center;
}

.error-message {
  margin-bottom: 20px;
}

.pagination-wrapper {
  display: flex;
  justify-content: center;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

</style>
