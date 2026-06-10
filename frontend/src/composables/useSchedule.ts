import { ref } from 'vue'
import { scheduleApi } from '@/api'
import { useUserStore } from '@/stores/user'
import type { Event } from '@/types'
import { handleApiResponse } from '@/utils/response'

export function useSchedule() {
  const schedules = ref<Event[]>([])
  const loading = ref(false)
  const errorMessage = ref('')

  // 获取日程列表（按用户）
  const fetchSchedules = async (params?: {
    start?: string
    end?: string
  }) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const userStore = useUserStore()
      const userId = userStore.userId as string
      const response = await scheduleApi.getEventsByUser(userId, params)
      const result = handleApiResponse(response, '获取日程列表失败')
      if (result.success && result.data) {
        const data = result.data
        schedules.value = Array.isArray(data) ? data : (data.content || [])
        return { success: true, data: schedules.value }
      } else {
        errorMessage.value = result.message
        return result
      }
    } catch (error: any) {
      console.error('获取日程列表失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  // 按项目获取日程
  const fetchSchedulesByProject = async (projectId: string) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await scheduleApi.getEventsByProject(projectId)
      const result = handleApiResponse(response, '获取项目日程失败')
      if (result.success && result.data) {
        const data = result.data
        schedules.value = Array.isArray(data) ? data : (data.content || [])
        return { success: true, data: schedules.value }
      } else {
        errorMessage.value = result.message
        return result
      }
    } catch (error: any) {
      console.error('获取项目日程失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  // 获取单个日程
  const fetchSchedule = async (id: string) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await scheduleApi.getEvent(id)
      const result = handleApiResponse(response, '获取日程失败')
      return result
    } catch (error: any) {
      console.error('获取日程失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  // 创建日程
  const createSchedule = async (data: Partial<Event>) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await scheduleApi.createEvent(data as any)
      const result = handleApiResponse(response, '创建日程失败')
      if (result.success && result.data) {
        const newSchedule = result.data
        schedules.value.unshift(newSchedule)
        return { success: true, data: newSchedule }
      } else {
        errorMessage.value = result.message
        return result
      }
    } catch (error: any) {
      console.error('创建日程失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  // 更新日程
  const updateSchedule = async (id: string, data: Partial<Event>) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await scheduleApi.updateEvent(id, data as any)
      const result = handleApiResponse(response, '更新日程失败')
      if (result.success && result.data) {
        const index = schedules.value.findIndex(s => s.id === id)
        if (index !== -1) {
          schedules.value[index] = { ...schedules.value[index], ...result.data }
        }
        return result
      } else {
        errorMessage.value = result.message
        return result
      }
    } catch (error: any) {
      console.error('更新日程失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  // 删除日程
  const deleteSchedule = async (id: string) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await scheduleApi.deleteEvent(id)
      const result = handleApiResponse(response, '删除日程失败')
      if (result.success) {
        schedules.value = schedules.value.filter(s => s.id !== id)
        return { success: true }
      } else {
        errorMessage.value = result.message
        return result
      }
    } catch (error: any) {
      console.error('删除日程失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  // 按日期范围获取日程
  const fetchSchedulesByDateRange = async (startDate: string, endDate: string) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const userStore = useUserStore()
      const userId = userStore.userId as string
      const response = await scheduleApi.getEventsByUser(userId, { start: startDate, end: endDate })
      const result = handleApiResponse(response, '获取日程失败')
      if (result.success && result.data) {
        const data = result.data
        const rangeSchedules = Array.isArray(data) ? data : (data.content || [])
        return { success: true, data: rangeSchedules }
      } else {
        errorMessage.value = result.message
        return result
      }
    } catch (error: any) {
      console.error('获取日程失败:', error)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  return {
    schedules,
    loading,
    error: errorMessage,
    fetchSchedules,
    fetchSchedulesByProject,
    fetchSchedule,
    createSchedule,
    updateSchedule,
    deleteSchedule,
    fetchSchedulesByDateRange,
  }
}
