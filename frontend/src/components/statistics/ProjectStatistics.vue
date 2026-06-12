<template>
  <div class="project-statistics">
    <div class="toolbar">
      <el-button type="success" :icon="Download" @click="exportReport" :disabled="!statistics">
        导出报表
      </el-button>
    </div>

    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="statistics">
      <!-- 概览卡片 -->
      <el-row :gutter="20" class="overview-cards">
        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#667eea">
                <Document />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalTasks || 0 }}</div>
              <div class="stat-label">总任务数</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#52c41a">
                <CircleCheck />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.doneTasks || 0 }}</div>
              <div class="stat-label">已完成</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#faad14">
                <Clock />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.doingTasks || 0 }}</div>
              <div class="stat-label">进行中</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#f5222d">
                <Clock />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.todoTasks || 0 }}</div>
              <div class="stat-label">待处理</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#ff4d4f">
                <Warning />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.overdueTasks || 0 }}</div>
              <div class="stat-label">延期任务</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="4">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#ff7875">
                <WarningFilled />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.overdueRate || 0 }}%</div>
              <div class="stat-label">延期率</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="charts-row">
        <!-- 任务状态分布 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>任务状态分布</span>
              </div>
            </template>
            <div ref="statusChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>

        <!-- 完成率趋势 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>任务完成率</span>
              </div>
            </template>
            <div class="completion-rate">
              <el-progress
                :percentage="completionRate"
                :stroke-width="20"
                :color="getProgressColor(completionRate)"
              />
              <div class="rate-text">
                {{ completionRate }}% 完成
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="charts-row">
        <!-- 优先级分布 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>优先级分布</span>
              </div>
            </template>
            <div ref="priorityChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>

        <!-- 成员贡献 -->
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>成员任务贡献</span>
              </div>
            </template>
            <div ref="memberChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="charts-row">
        <!-- 活动时间轴 -->
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>活动时间轴</span>
              </div>
            </template>
            <el-timeline v-if="recentActivity.length > 0">
              <el-timeline-item
                v-for="item in recentActivity"
                :key="item.id"
                :timestamp="formatActivityTime(item.createdAt)"
              >
                {{ item.userName || '系统' }} {{ actionText(item.action) }} {{ entityText(item.entityType) }} #{{ item.entityId }}
              </el-timeline-item>
            </el-timeline>
            <el-empty v-else description="暂无活动记录" />
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div v-else class="empty-state">
      <el-empty description="暂无统计数据">
        <el-button type="primary" @click="loadStatistics">加载数据</el-button>
      </el-empty>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { Document, CircleCheck, Clock, Download, Warning, WarningFilled } from '@element-plus/icons-vue'
import { useStatistics } from '@/composables/useStatistics'
import dayjs from 'dayjs'
import * as echarts from 'echarts'

interface Props {
  projectId?: string
  dateRange?: [string, string] | null
}

const props = defineProps<Props>()

const { loading, fetchProjectStatistics } = useStatistics()

const statistics = ref<any>(null)
const statusChartRef = ref()
const priorityChartRef = ref()
const memberChartRef = ref()
let statusChart: echarts.ECharts | null = null
let priorityChart: echarts.ECharts | null = null
let memberChart: echarts.ECharts | null = null

const completionRate = computed(() => {
  if (!statistics.value || statistics.value.totalTasks === 0) return 0
  return Math.round((statistics.value.doneTasks / statistics.value.totalTasks) * 100)
})

const recentActivity = computed(() => {
  return statistics.value?.recentActivity || []
})

const formatActivityTime = (time?: string) => {
  if (!time) return ''
  return dayjs(time).format('YYYY-MM-DD HH:mm')
}

const actionText = (action?: string) => {
  const map: Record<string, string> = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    STATUS_CHANGE: '变更状态',
    ASSIGN: '分配',
    COMMENT: '评论',
    UPLOAD: '上传',
  }
  return map[action || ''] || action || '操作'
}

const entityText = (entityType?: string) => {
  const map: Record<string, string> = {
    TASK: '任务',
    PROJECT: '项目',
    COMMENT: '评论',
    FILE: '文件',
    WORK_HOUR: '工时',
    EVENT: '日程',
    MILESTONE: '里程碑',
  }
  return map[entityType || ''] || entityType || '对象'
}

const loadStatistics = async () => {
  if (!props.projectId) {
    statistics.value = null
    return
  }

  const [startDate, endDate] = props.dateRange || [undefined, undefined]
  const result = await fetchProjectStatistics(props.projectId, startDate, endDate)
  if (result.success) {
    statistics.value = result.data
    setTimeout(() => {
      renderCharts()
    }, 100)
  }
}

const renderCharts = () => {
  renderStatusChart()
  renderPriorityChart()
  renderMemberChart()
}

const renderStatusChart = () => {
  if (!statistics.value || !statusChartRef.value) return

  if (!statusChart) {
    statusChart = echarts.init(statusChartRef.value)
  }

  const chartData = [
    { name: '待处理', value: statistics.value.todoTasks || 0 },
    { name: '进行中', value: statistics.value.doingTasks || 0 },
    { name: '已完成', value: statistics.value.doneTasks || 0 },
  ]

  const option = {
    tooltip: {
      trigger: 'item',
    },
    legend: {
      orient: 'vertical',
      left: 'left',
    },
    series: [
      {
        name: '任务状态',
        type: 'pie',
        radius: '60%',
        data: chartData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)',
          },
        },
      },
    ],
  }

  statusChart.setOption(option)
}

const renderPriorityChart = () => {
  if (!statistics.value || !priorityChartRef.value) return

  if (!priorityChart) {
    priorityChart = echarts.init(priorityChartRef.value)
  }

  const pd = statistics.value.priorityDistribution || {}
  const option = {
    tooltip: { trigger: 'axis' },
    xAxis: {
      type: 'category',
      data: ['P0', 'P1', 'P2', 'P3', 'P4'],
    },
    yAxis: { type: 'value' },
    series: [
      {
        type: 'bar',
        data: [
          { value: pd['P0'] || 0, itemStyle: { color: '#f5222d' } },
          { value: pd['P1'] || 0, itemStyle: { color: '#fa541c' } },
          { value: pd['P2'] || 0, itemStyle: { color: '#faad14' } },
          { value: pd['P3'] || 0, itemStyle: { color: '#52c41a' } },
          { value: pd['P4'] || 0, itemStyle: { color: '#1890ff' } },
        ],
        barWidth: '50%',
        label: { show: true, position: 'top' },
      },
    ],
  }
  priorityChart.setOption(option)
}

const renderMemberChart = () => {
  if (!statistics.value || !memberChartRef.value) return

  if (!memberChart) {
    memberChart = echarts.init(memberChartRef.value)
  }

  const mc = statistics.value.memberContributions || {}
  const names = Object.keys(mc)
  const values = names.map(n => mc[n])

  if (names.length === 0) {
    memberChart.setOption({
      title: { text: '暂无成员任务数据', left: 'center', top: 'center', textStyle: { color: '#999' } },
      series: [],
    })
    return
  }

  const option = {
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
    xAxis: { type: 'value' },
    yAxis: {
      type: 'category',
      data: names.slice().reverse(),
    },
    series: [
      {
        type: 'bar',
        data: values.slice().reverse(),
        barWidth: '60%',
        itemStyle: { color: '#667eea', borderRadius: [0, 4, 4, 0] },
        label: { show: true, position: 'right' },
      },
    ],
  }
  memberChart.setOption(option)
}

const getProgressColor = (percentage: number) => {
  if (percentage >= 80) return '#52c41a'
  if (percentage >= 50) return '#faad14'
  return '#f5222d'
}

watch(
  () => [props.projectId, props.dateRange],
  () => {
    loadStatistics()
  },
  { immediate: true }
)

const exportReport = () => {
  if (!statistics.value) return

  const s = statistics.value
  const lines = [
    ['指标', '数值'],
    ['总任务数', s.totalTasks || 0],
    ['已完成', s.doneTasks || 0],
    ['进行中', s.doingTasks || 0],
    ['待处理', s.todoTasks || 0],
    ['延期任务', s.overdueTasks || 0],
    ['延期率', `${s.overdueRate || 0}%`],
    ['完成率', `${completionRate.value}%`],
    [],
    ['优先级', '数量'],
    ...Object.entries(s.priorityDistribution || {}).map(([k, v]) => [k, v]),
    [],
    ['成员', '任务数'],
    ...Object.entries(s.memberContributions || {}).map(([k, v]) => [k, v]),
  ]

  const csv = lines.map(row => row.map(cell => `"${String(cell).replace(/"/g, '""')}"`).join(',')).join('\n')
  const blob = new Blob(['﻿' + csv], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  link.href = URL.createObjectURL(blob)
  link.download = `项目统计报表_${props.projectId}_${dayjs().format('YYYY-MM-DD_HH-mm-ss')}.csv`
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  URL.revokeObjectURL(link.href)
}

onMounted(() => {
  window.addEventListener('resize', () => {
    statusChart?.resize()
    priorityChart?.resize()
    memberChart?.resize()
  })
})

onUnmounted(() => {
  statusChart?.dispose()
  priorityChart?.dispose()
  memberChart?.dispose()
})
</script>

<style scoped>
.project-statistics {
  padding: 10px 0;
}

.toolbar {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.overview-cards {
  margin-bottom: 20px;
}

.stat-card {
  display: flex;
  align-items: center;
}

.stat-icon {
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #333;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #666;
}

.charts-row {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.completion-rate {
  padding: 20px;
}

.rate-text {
  text-align: center;
  margin-top: 15px;
  font-size: 16px;
  font-weight: 500;
}

.empty-state {
  padding: 50px;
  text-align: center;
}
</style>
