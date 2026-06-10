<template>
  <div class="user-statistics">
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="8" animated />
    </div>

    <div v-else-if="statistics">
      <!-- 概览卡片 -->
      <el-row :gutter="20" class="overview-cards">
        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#667eea">
                <Document />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.assignedTasks || 0 }}</div>
              <div class="stat-label">被分配任务</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#52c41a">
                <CircleCheck />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.createdTasks || 0 }}</div>
              <div class="stat-label">创建的任务</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="8">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#faad14">
                <Timer />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalWorkHours || 0 }}</div>
              <div class="stat-label">总工时</div>
            </div>
          </el-card>
        </el-col>
      </el-row>

      <!-- 图表区域 -->
      <el-row :gutter="20" class="charts-row">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>任务概览</span>
              </div>
            </template>
            <div ref="taskChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
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
      </el-row>

      <el-row :gutter="20" class="charts-row">
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>工时占比</span>
              </div>
            </template>
            <div ref="hoursChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>任务完成率</span>
              </div>
            </template>
            <div ref="gaugeChartRef" style="height: 300px"></div>
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
import { ref, watch, onMounted, onUnmounted } from 'vue'
import { Document, CircleCheck, Timer } from '@element-plus/icons-vue'
import { useStatistics } from '@/composables/useStatistics'
import { useUserStore } from '@/stores/user'
import * as echarts from 'echarts'

const userStore = useUserStore()
const { loading, fetchUserStatistics } = useStatistics()

const statistics = ref<any>(null)
const taskChartRef = ref()
const hoursChartRef = ref()
const statusChartRef = ref()
const gaugeChartRef = ref()
let taskChart: echarts.ECharts | null = null
let hoursChart: echarts.ECharts | null = null
let statusChart: echarts.ECharts | null = null
let gaugeChart: echarts.ECharts | null = null

const loadStatistics = async () => {
  const userId = userStore.userId
  if (!userId) {
    statistics.value = null
    return
  }

  const result = await fetchUserStatistics(userId.toString())
  if (result.success) {
    statistics.value = result.data
    setTimeout(() => {
      renderCharts()
    }, 100)
  }
}

const renderCharts = () => {
  if (!statistics.value) return

  // 任务概览柱状图
  if (taskChartRef.value) {
    if (!taskChart) {
      taskChart = echarts.init(taskChartRef.value)
    }
    taskChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['被分配任务', '创建的任务'],
      },
      yAxis: { type: 'value' },
      series: [
        {
          type: 'bar',
          data: [
            {
              value: statistics.value.assignedTasks || 0,
              itemStyle: { color: '#667eea' },
            },
            {
              value: statistics.value.createdTasks || 0,
              itemStyle: { color: '#52c41a' },
            },
          ],
          barWidth: '50%',
          label: {
            show: true,
            position: 'top',
          },
        },
      ],
    })
  }

  // 工时占比饼图
  if (hoursChartRef.value) {
    if (!hoursChart) {
      hoursChart = echarts.init(hoursChartRef.value)
    }
    const totalHours = statistics.value.totalWorkHours || 0
    hoursChart.setOption({
      tooltip: { trigger: 'item' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '70%'],
          data: [
            {
              name: '已记录工时',
              value: Number(totalHours) || 1,
              itemStyle: { color: '#faad14' },
            },
            {
              name: '未记录',
              value: Math.max(0, 40 - Number(totalHours)) || 0.1,
              itemStyle: { color: '#f0f0f0' },
            },
          ],
          label: {
            formatter: '{b}: {c}h',
          },
        },
      ],
    })
  }

  // 任务状态分布饼图
  if (statusChartRef.value) {
    if (!statusChart) {
      statusChart = echarts.init(statusChartRef.value)
    }
    const sb = statistics.value.statusBreakdown || {}
    statusChart.setOption({
      tooltip: { trigger: 'item' },
      legend: { orient: 'vertical', left: 'left' },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: [
            { name: '待处理', value: sb['TODO'] || 0, itemStyle: { color: '#909399' } },
            { name: '进行中', value: sb['DOING'] || 0, itemStyle: { color: '#e6a23c' } },
            { name: '已完成', value: sb['DONE'] || 0, itemStyle: { color: '#67c23a' } },
          ],
          emphasis: {
            itemStyle: {
              shadowBlur: 10,
              shadowOffsetX: 0,
              shadowColor: 'rgba(0, 0, 0, 0.5)',
            },
          },
        },
      ],
    })
  }

  // 完成率仪表盘
  if (gaugeChartRef.value) {
    if (!gaugeChart) {
      gaugeChart = echarts.init(gaugeChartRef.value)
    }
    const rate = statistics.value.completionRate || 0
    gaugeChart.setOption({
      series: [
        {
          type: 'gauge',
          startAngle: 180,
          endAngle: 0,
          min: 0,
          max: 100,
          splitNumber: 5,
          axisLine: {
            lineStyle: {
              width: 10,
              color: [
                [0.3, '#f5222d'],
                [0.7, '#faad14'],
                [1, '#52c41a'],
              ],
            },
          },
          pointer: {
            icon: 'path://M12.8,0.7l12,40.1H0.7L12.8,0.7z',
            length: '12%',
            width: 10,
            offsetCenter: [0, '-60%'],
            itemStyle: { color: 'auto' },
          },
          axisTick: { length: 12, lineStyle: { color: 'auto', width: 2 } },
          splitLine: { length: 20, lineStyle: { color: 'auto', width: 5 } },
          axisLabel: {
            color: '#464646',
            fontSize: 14,
            distance: -50,
            formatter: '{value}%',
          },
          title: { offsetCenter: [0, '-20%'], fontSize: 16 },
          detail: {
            fontSize: 30,
            offsetCenter: [0, '0%'],
            valueAnimation: true,
            formatter: '{value}%',
            color: 'auto',
          },
          data: [{ value: rate, name: '完成率' }],
        },
      ],
    })
  }
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', () => {
    taskChart?.resize()
    hoursChart?.resize()
    statusChart?.resize()
    gaugeChart?.resize()
  })
})

onUnmounted(() => {
  taskChart?.dispose()
  hoursChart?.dispose()
  statusChart?.dispose()
  gaugeChart?.dispose()
})

watch(
  () => userStore.userId,
  () => {
    loadStatistics()
  }
)
</script>

<style scoped>
.user-statistics {
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

.empty-state {
  padding: 50px;
  text-align: center;
}
</style>
