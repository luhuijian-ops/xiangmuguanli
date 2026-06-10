<template>
  <div class="team-statistics">
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
                <User />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalProjects || 0 }}</div>
              <div class="stat-label">项目总数</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#52c41a">
                <Document />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalTasks || 0 }}</div>
              <div class="stat-label">任务总数</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#faad14">
                <User />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ statistics.totalUsers || 0 }}</div>
              <div class="stat-label">用户总数</div>
            </div>
          </el-card>
        </el-col>

        <el-col :span="6">
          <el-card class="stat-card">
            <div class="stat-icon">
              <el-icon :size="30" color="#13c2c2">
                <TrendCharts />
              </el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-value">{{ projectCount || 0 }}</div>
              <div class="stat-label">我的项目</div>
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
                <span>数据概览</span>
              </div>
            </template>
            <div ref="overviewChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>资源分布</span>
              </div>
            </template>
            <div ref="resourceChartRef" style="height: 300px"></div>
          </el-card>
        </el-col>
      </el-row>

      <el-row :gutter="20" class="charts-row">
        <el-col :span="24">
          <el-card>
            <template #header>
              <div class="card-header">
                <span>近7天任务趋势</span>
              </div>
            </template>
            <div ref="trendChartRef" style="height: 320px"></div>
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
import { ref, onMounted, onUnmounted } from 'vue'
import { User, Document, TrendCharts } from '@element-plus/icons-vue'
import { useStatistics } from '@/composables/useStatistics'
import { useProject } from '@/composables/useProject'
import * as echarts from 'echarts'

const { loading, fetchTeamStatistics } = useStatistics()
const { projects, fetchProjects } = useProject()

const statistics = ref<any>(null)
const projectCount = ref(0)
const overviewChartRef = ref()
const resourceChartRef = ref()
const trendChartRef = ref()
let overviewChart: echarts.ECharts | null = null
let resourceChart: echarts.ECharts | null = null
let trendChart: echarts.ECharts | null = null

const loadStatistics = async () => {
  // 获取仪表板统计数据
  const result = await fetchTeamStatistics('')
  if (result.success) {
    statistics.value = result.data
    setTimeout(() => {
      renderCharts()
    }, 100)
  }

  // 获取我的项目数
  await fetchProjects()
  projectCount.value = projects.value.length
}

const renderCharts = () => {
  if (!statistics.value) return

  // 数据概览柱状图
  if (overviewChartRef.value) {
    if (!overviewChart) {
      overviewChart = echarts.init(overviewChartRef.value)
    }
    overviewChart.setOption({
      tooltip: { trigger: 'axis' },
      xAxis: {
        type: 'category',
        data: ['项目总数', '任务总数', '用户总数'],
      },
      yAxis: { type: 'value' },
      series: [
        {
          type: 'bar',
          data: [
            {
              value: statistics.value.totalProjects || 0,
              itemStyle: { color: '#667eea' },
            },
            {
              value: statistics.value.totalTasks || 0,
              itemStyle: { color: '#52c41a' },
            },
            {
              value: statistics.value.totalUsers || 0,
              itemStyle: { color: '#faad14' },
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

  // 资源分布饼图
  if (resourceChartRef.value) {
    if (!resourceChart) {
      resourceChart = echarts.init(resourceChartRef.value)
    }
    resourceChart.setOption({
      tooltip: { trigger: 'item' },
      legend: {
        orient: 'vertical',
        left: 'left',
      },
      series: [
        {
          type: 'pie',
          radius: '60%',
          data: [
            {
              name: '项目',
              value: statistics.value.totalProjects || 0,
              itemStyle: { color: '#667eea' },
            },
            {
              name: '任务',
              value: statistics.value.totalTasks || 0,
              itemStyle: { color: '#52c41a' },
            },
            {
              name: '用户',
              value: statistics.value.totalUsers || 0,
              itemStyle: { color: '#faad14' },
            },
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

  // 近7天任务趋势折线图
  if (trendChartRef.value) {
    if (!trendChart) {
      trendChart = echarts.init(trendChartRef.value)
    }
    const wt = statistics.value.weeklyTrend || { dates: [], created: [], completed: [] }
    trendChart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['新建任务', '完成任务'] },
      grid: { left: '3%', right: '4%', bottom: '3%', containLabel: true },
      xAxis: {
        type: 'category',
        boundaryGap: false,
        data: wt.dates || [],
      },
      yAxis: { type: 'value' },
      series: [
        {
          name: '新建任务',
          type: 'line',
          smooth: true,
          data: wt.created || [],
          itemStyle: { color: '#667eea' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(102,126,234,0.3)' },
                { offset: 1, color: 'rgba(102,126,234,0.05)' },
              ],
            },
          },
        },
        {
          name: '完成任务',
          type: 'line',
          smooth: true,
          data: wt.completed || [],
          itemStyle: { color: '#52c41a' },
          areaStyle: {
            color: {
              type: 'linear',
              x: 0, y: 0, x2: 0, y2: 1,
              colorStops: [
                { offset: 0, color: 'rgba(82,196,26,0.3)' },
                { offset: 1, color: 'rgba(82,196,26,0.05)' },
              ],
            },
          },
        },
      ],
    })
  }
}

onMounted(() => {
  loadStatistics()
  window.addEventListener('resize', () => {
    overviewChart?.resize()
    resourceChart?.resize()
    trendChart?.resize()
  })
})

onUnmounted(() => {
  overviewChart?.dispose()
  resourceChart?.dispose()
  trendChart?.dispose()
})
</script>

<style scoped>
.team-statistics {
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
