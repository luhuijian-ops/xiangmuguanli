import { ref } from 'vue'
import { taskApi } from '@/api'
import type { Task } from '@/types'
import { handleApiResponse } from '@/utils/response'

export function useTask() {
  const tasks = ref<Task[]>([])
  const loading = ref(false)
  const error = ref<string>('')

  /**
   * 获取任务列表
   */
  const fetchTasks = async (projectId: string, params?: any) => {
    loading.value = true
    error.value = ''

    try {
      const response = await taskApi.getTasks(projectId, params)
      const result = handleApiResponse(response, '获取任务列表失败')
      if (result.success && result.data) {
        const data = result.data
        // 兼容 Spring Page / 直接数组 / 嵌套 data 等多种格式
        let list: Task[] = []
        if (Array.isArray(data)) {
          list = data
        } else if (data && Array.isArray(data.content)) {
          list = data.content
        } else if (data && Array.isArray(data.data)) {
          list = data.data
        }
        tasks.value = list
        return { success: true, data: tasks.value }
      } else {
        error.value = result.message
        return result
      }
    } catch (err) {
      console.error('获取任务列表失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取分配给我的任务
   */
  const fetchTasksByAssignee = async (userId: string, params?: any) => {
    loading.value = true
    error.value = ''

    try {
      const response = await taskApi.getTasksByAssignee(userId, params)
      const result = handleApiResponse(response, '获取任务列表失败')
      if (result.success && result.data) {
        const data = result.data
        // 兼容 Spring Page / 直接数组 / 嵌套 data 等多种格式
        let list: Task[] = []
        if (Array.isArray(data)) {
          list = data
        } else if (data && Array.isArray(data.content)) {
          list = data.content
        } else if (data && Array.isArray(data.data)) {
          list = data.data
        }
        tasks.value = list
        return { success: true, data: tasks.value }
      } else {
        error.value = result.message
        return result
      }
    } catch (err) {
      console.error('获取任务列表失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 按状态获取任务（看板）
   */
  const fetchTasksByStatus = async (projectId: string) => {
    loading.value = true
    error.value = ''

    try {
      const response = await taskApi.getTasksByStatus(projectId)
      const result = handleApiResponse(response, '获取看板数据失败')
      if (result.success && result.data) {
        return result
      } else {
        error.value = result.message
        return result
      }
    } catch (err) {
      console.error('获取看板数据失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取任务详情
   */
  const fetchTask = async (id: string) => {
    loading.value = true
    error.value = ''

    try {
      const response = await taskApi.getTask(id)
      const result = handleApiResponse(response, '获取任务详情失败')
      if (result.success && result.data) {
        return result
      } else {
        error.value = result.message
        return result
      }
    } catch (err) {
      console.error('获取任务详情失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建任务
   */
  const createTask = async (data: Partial<Task>) => {
    loading.value = true
    error.value = ''

    try {
      const response = await taskApi.createTask(data)
      const result = handleApiResponse(response, '创建任务失败')
      if (result.success && result.data) {
        const newTask = result.data
        tasks.value.unshift(newTask)
        return { success: true, data: newTask }
      } else {
        error.value = result.message
        return result
      }
    } catch (err) {
      console.error('创建任务失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新任务
   */
  const updateTask = async (id: string, data: Partial<Task>) => {
    loading.value = true
    error.value = ''

    try {
      const response = await taskApi.updateTask(id, data)
      const result = handleApiResponse(response, '更新任务失败')
      if (result.success && result.data) {
        const index = tasks.value.findIndex(t => t.id === id)
        if (index !== -1) {
          tasks.value[index] = { ...tasks.value[index], ...result.data }
        }
        return result
      } else {
        error.value = result.message
        return result
      }
    } catch (err) {
      console.error('更新任务失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新任务状态
   */
  const updateTaskStatus = async (taskId: string, status: string) => {
    loading.value = true
    error.value = ''

    try {
      const response = await taskApi.updateTaskStatus(taskId, status)
      const result = handleApiResponse(response, '更新任务状态失败')
      if (result.success && result.data) {
        const index = tasks.value.findIndex(t => t.id === taskId)
        if (index !== -1) {
          tasks.value[index].status = status as any
        }
        return result
      } else {
        error.value = result.message
        return result
      }
    } catch (err) {
      console.error('更新任务状态失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除任务
   */
  const deleteTask = async (id: string) => {
    loading.value = true
    error.value = ''

    try {
      const response = await taskApi.deleteTask(id)
      const result = handleApiResponse(response, '删除任务失败')
      if (result.success) {
        tasks.value = tasks.value.filter(t => t.id !== id)
        return { success: true }
      } else {
        error.value = result.message
        return result
      }
    } catch (err) {
      console.error('删除任务失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 批量更新排序
   */
  const batchUpdateSortOrder = async (projectId: number, taskIds: number[]) => {
    loading.value = true
    error.value = ''
    try {
      const response = await taskApi.batchUpdateSortOrder(projectId, taskIds)
      return handleApiResponse(response, '更新排序失败')
    } catch (err) {
      console.error('更新排序失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 设置父任务
   */
  const setParentTask = async (id: string, parentId?: number) => {
    loading.value = true
    error.value = ''
    try {
      const response = await taskApi.setParentTask(id, parentId)
      return handleApiResponse(response, '设置父任务失败')
    } catch (err) {
      console.error('设置父任务失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加任务依赖
   */
  const addDependency = async (id: string, dependOnTaskId: number) => {
    loading.value = true
    error.value = ''
    try {
      const response = await taskApi.addDependency(id, dependOnTaskId)
      return handleApiResponse(response, '添加依赖失败')
    } catch (err) {
      console.error('添加依赖失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 移除任务依赖
   */
  const removeDependency = async (id: string, dependOnTaskId: number) => {
    loading.value = true
    error.value = ''
    try {
      const response = await taskApi.removeDependency(id, dependOnTaskId)
      return handleApiResponse(response, '移除依赖失败')
    } catch (err) {
      console.error('移除依赖失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取子任务
   */
  const getSubtasks = async (id: string) => {
    loading.value = true
    error.value = ''
    try {
      const response = await taskApi.getSubtasks(id)
      return handleApiResponse(response, '获取子任务失败')
    } catch (err) {
      console.error('获取子任务失败:', err)
      error.value = '网络错误，请重试'
      return { success: false, message: error.value }
    } finally {
      loading.value = false
    }
  }

  return {
    tasks,
    loading,
    error,
    fetchTasks,
    fetchTasksByAssignee,
    fetchTasksByStatus,
    fetchTask,
    createTask,
    updateTask,
    updateTaskStatus,
    deleteTask,
    batchUpdateSortOrder,
    setParentTask,
    addDependency,
    removeDependency,
    getSubtasks,
  }
}
