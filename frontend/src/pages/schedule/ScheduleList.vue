<template>
  <div class="schedule-list">
    <div class="header">
      <h1>日程管理</h1>
      <div class="header-actions">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索日程"
          :prefix-icon="Search"
          clearable
          style="width: 300px"
          @clear="handleSearch"
        />

        <el-button type="primary" @click="showCreateDialog" :icon="Plus">
          添加日程
        </el-button>

        <el-button @click="refreshSchedules" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="日历视图" name="calendar">
        <schedule-calendar
          ref="calendarRef"
          @date-click="handleDateClick"
          @event-click="handleEventClick"
        />
      </el-tab-pane>

      <el-tab-pane label="列表视图" name="list">
        <div v-if="loading" class="loading-container">
          <el-skeleton :rows="5" animated />
        </div>

        <div v-else class="schedules-container">
          <el-table :data="filteredSchedules" stripe style="width: 100%">
            <el-table-column prop="title" label="日程标题" width="200" />
            <el-table-column prop="location" label="地点" width="150">
              <template #default="{ row }">
                {{ row.location || '-' }}
              </template>
            </el-table-column>
            <el-table-column prop="startTime" label="开始时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.startTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="endTime" label="结束时间" width="180">
              <template #default="{ row }">
                {{ formatDateTime(row.endTime) }}
              </template>
            </el-table-column>
            <el-table-column prop="allDay" label="全天" width="80">
              <template #default="{ row }">
                <el-tag v-if="row.allDay" type="success" size="small">是</el-tag>
                <el-tag v-else type="info" size="small">否</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="description" label="备注">
              <template #default="{ row }">
                {{ row.description || '-' }}
              </template>
            </el-table-column>
            <el-table-column label="操作" width="180">
              <template #default="{ row }">
                <el-button-group>
                  <el-button size="small" @click="handleViewDetail(row)">
                    详情
                  </el-button>
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

        <div v-if="!loading && filteredSchedules.length === 0" class="empty-state">
          <el-empty description="暂无日程">
            <el-button type="primary" @click="showCreateDialog">创建第一个日程</el-button>
          </el-empty>
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 创建/编辑日程对话框 -->
    <el-dialog
      v-model="createDialogVisible"
      :title="editingSchedule ? '编辑日程' : '创建日程'"
      width="600px"
      :close-on-click-modal="false"
    >
      <schedule-form
        :schedule="editingSchedule"
        @submit="handleScheduleSubmit"
        @cancel="createDialogVisible = false"
      />
    </el-dialog>

    <!-- 日程详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="日程详情"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="selectedSchedule" class="schedule-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="标题">
            {{ selectedSchedule.title }}
          </el-descriptions-item>
          <el-descriptions-item label="地点">
            {{ selectedSchedule.location || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="开始时间">
            {{ formatDateTime(selectedSchedule.startTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="结束时间">
            {{ formatDateTime(selectedSchedule.endTime) }}
          </el-descriptions-item>
          <el-descriptions-item label="备注">
            {{ selectedSchedule.description || '-' }}
          </el-descriptions-item>
          <el-descriptions-item label="提醒时间(分钟)">
            {{ selectedSchedule.reminderMinutes || '-' }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type MessageBoxType } from 'element-plus'
import { Plus, Search, Refresh } from '@element-plus/icons-vue'
import { useSchedule } from '@/composables/useSchedule'
import { useUserStore } from '@/stores/user'
import type { Event } from '@/types'
import ScheduleCalendar from '@/components/schedule/ScheduleCalendar.vue'
import ScheduleForm from '@/components/schedule/ScheduleForm.vue'
import dayjs from 'dayjs'

const userStore = useUserStore()
const {
  schedules,
  loading,
  createSchedule,
  updateSchedule,
  deleteSchedule,
  fetchSchedules,
  fetchSchedulesByDateRange,
} = useSchedule()

const calendarRef = ref()
const searchKeyword = ref('')
const activeTab = ref('calendar')
const createDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const selectedSchedule = ref<Event | null>(null)
const editingSchedule = ref<Event | null>(null)

onMounted(() => {
  loadSchedules()
})

const filteredSchedules = computed(() => {
  let result = schedules.value

  // 按关键字搜索
  if (searchKeyword.value) {
    const keyword = searchKeyword.value.toLowerCase()
    result = result.filter(s =>
      s.title.toLowerCase().includes(keyword) ||
      (s.description && s.description.toLowerCase().includes(keyword)) ||
      (s.location && s.location.toLowerCase().includes(keyword))
    )
  }

  return result
})

const formatDateTime = (date: string) => {
  return dayjs(date).format('YYYY-MM-DD HH:mm')
}

const loadSchedules = () => {
  // 加载当前月份的日程
  const now = dayjs()
  const startDate = now.startOf('month').format('YYYY-MM-DD')
  const endDate = now.endOf('month').format('YYYY-MM-DD')
  fetchSchedulesByDateRange(startDate, endDate)
}

const showCreateDialog = () => {
  editingSchedule.value = null
  createDialogVisible.value = true
}

const refreshSchedules = () => {
  loadSchedules()
}

const handleSearch = () => {
  loadSchedules()
}

const handleViewDetail = (schedule: Schedule) => {
  selectedSchedule.value = schedule
  detailDialogVisible.value = true
}

const handleEdit = (schedule: Schedule) => {
  editingSchedule.value = schedule
  createDialogVisible.value = true
}

const handleDelete = async (schedule: Event) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除日程 "${schedule.title}" 吗`,
      '确认删除',
      {
        confirmButtonText: '删除',
        cancelButtonText: '取消',
        type: 'warning',
      }
    )

    const result = await deleteSchedule(schedule.id.toString())
    if (result.success) {
      ElMessage.success('日程删除成功')
      loadSchedules()
    } else {
      ElMessage.error(result.message || '删除失败')
    }
  } catch (error) {
    // 用户取消删除
  }
}

const handleScheduleSubmit = async (scheduleData: Partial<Event>) => {
  if (editingSchedule.value) {
    const result = await updateSchedule(editingSchedule.value!.id.toString(), scheduleData)
    if (result.success) {
      ElMessage.success('日程更新成功')
      createDialogVisible.value = false
      loadSchedules()
    } else {
      ElMessage.error(result.message || '更新失败')
    }
  } else {
    const result = await createSchedule({
      ...scheduleData,
      userId: userStore.userId as number
    } as any)
    if (result.success) {
      ElMessage.success('日程创建成功')
      createDialogVisible.value = false
      loadSchedules()
    } else {
      ElMessage.error(result.message || '创建失败')
    }
  }
}

const handleDateClick = (date: any) => {
  // 切换到列表视图并筛选
  activeTab.value = 'list'
  const dateStr = date.format('YYYY-MM-DD')
  searchKeyword.value = dateStr
}

const handleEventClick = (event: Event) => {
  handleViewDetail(event)
}
</script>

<style scoped>
.schedule-list {
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

.schedules-container {
  min-height: 400px;
}

.empty-state {
  padding: 50px;
  text-align: center;
}

.schedule-detail {
  padding: 20px;
}
</style>
