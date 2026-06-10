<template>
  <div class="project-statistics">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="statistics">
      <!-- 概览卡片 -->
      <el-row :gutter="20" class="overview-cards">
        <el-col :span="6">
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

        <el-col :span="6">
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

        <el-col :span="6">
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

        <el-col :span="6">
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
import { Document, CircleCheck, Clock } from '@element-plus/icons-vue'
import { useStatistics } from '@/composables/useStatistics'
import dayjs from 'dayjs'
import * as echarts from 'echarts'

interface Props {
  projectId?: string
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

const loadStatistics = async () => {
  if (!props.projectId) {
    statistics.value = null
    return
  }

  const result = await fetchProjectStatistics(props.projectId)
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
  () => props.projectId,
  () => {
    loadStatistics()
  },
  { immediate: true }
)

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
