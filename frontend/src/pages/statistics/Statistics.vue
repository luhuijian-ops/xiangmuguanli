<template>
  <div class="statistics">
    <div class="header">
      <h1>统计分析</h1>
      <div class="header-actions">
        <el-select
          v-model="selectedProjectId"
          placeholder="选择项目"
          clearable
          style="width: 200px"
          @change="handleProjectChange"
        >
          <el-option label="全部项目" value="" />
          <el-option
            v-for="project in projects"
            :key="project.id"
            :label="project.name"
            :value="project.id"
          />
        </el-select>

        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          value-format="YYYY-MM-DD"
          @change="handleDateChange"
        />

        <el-button type="primary" @click="refreshStatistics" :icon="Refresh">
          刷新
        </el-button>
      </div>
    </div>

    <el-tabs v-model="activeTab">
      <el-tab-pane label="项目统计" name="project">
        <project-statistics :project-id="selectedProjectId" :date-range="dateRange" />
      </el-tab-pane>

      <el-tab-pane label="个人统计" name="user">
        <user-statistics :date-range="dateRange" />
      </el-tab-pane>

      <el-tab-pane label="团队统计" name="team">
        <team-statistics :date-range="dateRange" />
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { Refresh } from '@element-plus/icons-vue'
import { useProject } from '@/composables/useProject'
import ProjectStatistics from '@/components/statistics/ProjectStatistics.vue'
import UserStatistics from '@/components/statistics/UserStatistics.vue'
import TeamStatistics from '@/components/statistics/TeamStatistics.vue'

const { projects, fetchProjects } = useProject()

const activeTab = ref('project')
const selectedProjectId = ref('')
const dateRange = ref<[string, string] | null>(null)

onMounted(async () => {
  await fetchProjects()
})

const refreshStatistics = () => {
  // 子组件会通过 watch 监听相关变化
}

const handleProjectChange = () => {
  refreshStatistics()
}

const handleDateChange = () => {
  refreshStatistics()
}
</script>

<style scoped>
.statistics {
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
</style>
