import { ref, computed } from 'vue'
import { projectApi } from '@/api'
import type { Project } from '@/types'
import { useProjectStore } from '@/stores/project'
import { useUserStore } from '@/stores/user'
import { handleApiResponse } from '@/utils/response'

export function useProject() {
  const projectStore = useProjectStore()
  const projects = ref<Project[]>([])
  const loading = ref(false)
  const errorMessage = ref('')

  /**
   * 获取项目列表
   */
  const fetchProjects = async (params?: any) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await projectApi.getMyProjects(params)
      const result = handleApiResponse(response, '获取项目列表失败')
      console.log('[fetchProjects] raw response:', response)
      console.log('[fetchProjects] parsed result:', result)
      if (result.success && result.data) {
        const data = result.data
        // 健壮性解析：兼容 Spring Page / 直接数组 / 嵌套 data 等多种格式
        let list: Project[] = []
        if (Array.isArray(data)) {
          list = data
        } else if (data && Array.isArray(data.content)) {
          list = data.content
        } else if (data && Array.isArray(data.data)) {
          list = data.data
        }
        projects.value = list
        projectStore.setProjectList(projects.value)
        console.log('[fetchProjects] loaded projects count:', list.length)
      } else {
        errorMessage.value = result.message || '获取项目列表失败'
        console.error('[fetchProjects] failed:', result.message)
      }
      return result
    } catch (err) {
      console.error('获取项目列表失败:', err)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 创建项目
   */
  const createProject = async (data: Partial<Project>) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const userStore = useUserStore()

      // 如果表单已提供 ownerId 则使用表单值，否则使用当前用户
      let ownerId = data.ownerId || userStore.userId

      // 如果用户信息未加载（如页面刷新后），先尝试获取
      if (!ownerId) {
        const userInfo = await userStore.fetchUserInfo()
        ownerId = userInfo?.id || userStore.userId
      }

      if (!ownerId) {
        errorMessage.value = '请先登录'
        return { success: false, message: errorMessage.value }
      }

      const projectData = { ...data, ownerId }

      const response = await projectApi.createProject(projectData as any)

      const result = handleApiResponse(response, '创建项目失败')

      if (result.success) {
        const newProject = result.data as Project
        if (newProject) {
          projects.value.unshift(newProject)
          projectStore.setProjectList(projects.value)
        }
      } else {
        errorMessage.value = result.message || '创建项目失败'
        console.error('[createProject] failed:', result.message)
      }
      return result
    } catch (err: any) {
      console.error('[createProject] exception:', err)
      // 尝试从后端响应中提取具体错误信息
      const backendMsg = err?.response?.data?.message
      const validationErrors = err?.response?.data?.data
      if (validationErrors && typeof validationErrors === 'object') {
        const messages = Object.entries(validationErrors).map(([k, v]) => `${k}: ${v}`).join(', ')
        errorMessage.value = messages || backendMsg || '请求参数验证失败'
      } else {
        errorMessage.value = backendMsg || '网络错误，请重试'
      }
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 更新项目
   */
  const updateProject = async (id: number | string, data: Partial<Project>) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await projectApi.updateProject(id, data)
      const result = handleApiResponse(response, '更新项目失败')
      if (result.success && result.data) {
        const numericId = typeof id === 'string' ? Number(id) : id
        const index = projects.value.findIndex(p => p.id === numericId)
        if (index !== -1) {
          projects.value[index] = { ...projects.value[index], ...result.data }
          projectStore.setProjectList(projects.value)
        }
      } else {
        errorMessage.value = result.message
      }
      return result
    } catch (err: any) {
      console.error('更新项目失败:', err)
      const backendMsg = err?.response?.data?.message
      errorMessage.value = backendMsg || '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 归档项目
   */
  const archiveProject = async (id: number | string) => {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await projectApi.archiveProject(id)
      const result = handleApiResponse(response, '归档项目失败')
      if (result.success && result.data) {
        const numericId = typeof id === 'string' ? Number(id) : id
        const index = projects.value.findIndex(p => p.id === numericId)
        if (index !== -1) {
          projects.value[index] = { ...projects.value[index], ...result.data }
          projectStore.setProjectList(projects.value)
        }
      } else {
        errorMessage.value = result.message
      }
      return result
    } catch (err: any) {
      console.error('归档项目失败:', err)
      const backendMsg = err?.response?.data?.message
      errorMessage.value = backendMsg || '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 取消归档项目
   */
  const unarchiveProject = async (id: number | string) => {
    loading.value = true
    errorMessage.value = ''
    try {
      const response = await projectApi.unarchiveProject(id)
      const result = handleApiResponse(response, '取消归档失败')
      if (result.success && result.data) {
        const numericId = typeof id === 'string' ? Number(id) : id
        const index = projects.value.findIndex(p => p.id === numericId)
        if (index !== -1) {
          projects.value[index] = { ...projects.value[index], ...result.data }
          projectStore.setProjectList(projects.value)
        }
      } else {
        errorMessage.value = result.message
      }
      return result
    } catch (err: any) {
      console.error('取消归档失败:', err)
      const backendMsg = err?.response?.data?.message
      errorMessage.value = backendMsg || '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 删除项目
   */
  const deleteProject = async (id: number | string) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await projectApi.deleteProject(id)
      const result = handleApiResponse(response, '删除项目失败')
      if (result.success) {
        const numericId = typeof id === 'string' ? Number(id) : id
        projects.value = projects.value.filter(p => p.id !== numericId)
        projectStore.setProjectList(projects.value)
      } else {
        errorMessage.value = result.message
      }
      return result
    } catch (err: any) {
      console.error('删除项目失败:', err)
      const backendMsg = err?.response?.data?.message
      errorMessage.value = backendMsg || '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取项目详情
   */
  const fetchProject = async (id: number | string) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await projectApi.getProject(id)
      const result = handleApiResponse(response, '获取项目详情失败')
      if (result.success && result.data) {
        projectStore.setCurrentProject(result.data)
      } else {
        errorMessage.value = result.message
      }
      return result
    } catch (err) {
      console.error('获取项目详情失败:', err)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 获取项目成员
   */
  const fetchMembers = async (projectId: number | string) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await projectApi.getMembers(projectId)
      const result = handleApiResponse(response, '获取项目成员失败')
      if (result.success && result.data) {
        return result
      } else {
        errorMessage.value = result.message
      }
      return result
    } catch (err) {
      console.error('获取项目成员失败:', err)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 添加项目成员
   */
  const addMember = async (projectId: number | string, data: any) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await projectApi.addMember(projectId, data)
      const result = handleApiResponse(response, '添加成员失败')
      if (result.success && result.data) {
        return result
      } else {
        errorMessage.value = result.message
      }
      return result
    } catch (err) {
      console.error('添加成员失败:', err)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  /**
   * 移除项目成员
   */
  const removeMember = async (projectId: number | string, userId: number | string) => {
    loading.value = true
    errorMessage.value = ''

    try {
      const response = await projectApi.removeMember(projectId, userId)
      const result = handleApiResponse(response, '移除成员失败')
      if (result.success) {
        return result
      } else {
        errorMessage.value = result.message
      }
      return result
    } catch (err) {
      console.error('移除成员失败:', err)
      errorMessage.value = '网络错误，请重试'
      return { success: false, message: errorMessage.value }
    } finally {
      loading.value = false
    }
  }

  // 计算属性
  const projectCount = computed(() => projects.value.length)
  const activeProjects = computed(() =>
    projects.value.filter(p => p.status === 'ACTIVE')
  )

  return {
    projects,
    loading,
    error: errorMessage,
    projectCount,
    activeProjects,
    fetchProjects,
    createProject,
    updateProject,
    deleteProject,
    archiveProject,
    unarchiveProject,
    fetchProject,
    fetchMembers,
    addMember,
    removeMember,
  }
}
