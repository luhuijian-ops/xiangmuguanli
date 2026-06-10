import { ref } from 'vue'
import { statisticsApi } from '@/api'
import { handleApiResponse } from '@/utils/response'

export function useStatistics() {
  const loading = ref(false)
  const errorMessage = ref('')

  // 获取项目统计数据
  const fetchProjectStatistics = async (projectId: string) => {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await statisticsApi.getProjectStatistics(projectId)
      const result = handleApiResponse(response, '获取统计数据失败')
      if (result.success && result.data) {
        return result
      } else {
        errorMessage.value = result.message
        return result
      }
    } catch (error: any) {
      console.error('获取项目统计数据失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  // 获取用户统计数据
  const fetchUserStatistics = async (userId: string) => {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await statisticsApi.getUserStatistics(userId)
      const result = handleApiResponse(response, '获取统计数据失败')
      if (result.success && result.data) {
        return result
      } else {
        errorMessage.value = result.message
        return result
      }
    } catch (error: any) {
      console.error('获取用户统计数据失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  // 获取团队统计数据（使用仪表板统计作为后备）
  const fetchTeamStatistics = async (projectId: string) => {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await statisticsApi.getDashboardStatistics()
      const result = handleApiResponse(response, '获取统计数据失败')
      if (result.success && result.data) {
        return result
      } else {
        errorMessage.value = result.message
        return result
      }
    } catch (error: any) {
      console.error('获取团队统计数据失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  return {
    loading,
    error: errorMessage,
    fetchProjectStatistics,
    fetchUserStatistics,
    fetchTeamStatistics,
  }
}
