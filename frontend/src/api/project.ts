import request from './request'

// 清理请求参数，移除空字符串、null、undefined，避免后端解析失败
const cleanParams = (data: Record<string, any>): Record<string, any> => {
  const cleaned: Record<string, any> = {}
  for (const [key, value] of Object.entries(data)) {
    if (value !== '' && value !== null && value !== undefined) {
      cleaned[key] = value
    }
  }
  return cleaned
}

export const projectApi = {
  // 获取项目列表（Spring Pageable 参数名：page / size / sort）
  getProjects: (params?: { page?: number; size?: number; pageSize?: number }) => {
    const apiParams: any = { ...params }
    // 兼容前端 pageSize 与后端 Spring Pageable 的 size 参数名差异
    if (apiParams.pageSize !== undefined && apiParams.size === undefined) {
      apiParams.size = apiParams.pageSize
      delete apiParams.pageSize
    }
    return request.get('/api/v1/projects', { params: apiParams })
  },

  // 获取当前用户参与的项目列表
  getMyProjects: (params?: { page?: number; size?: number; pageSize?: number }) => {
    const apiParams: any = { ...params }
    if (apiParams.pageSize !== undefined && apiParams.size === undefined) {
      apiParams.size = apiParams.pageSize
      delete apiParams.pageSize
    }
    return request.get('/api/v1/projects/my', { params: apiParams })
  },

  // 获取项目详情
  getProject: (id: number | string) => request.get(`/api/v1/projects/${id}`),

  // 按项目代码获取
  getProjectByCode: (code: string) => request.get(`/api/v1/projects/code/${code}`),

  // 按 owner 获取项目
  getProjectsByOwner: (ownerId: number | string, params?: { page?: number; size?: number }) =>
    request.get(`/api/v1/projects/owner/${ownerId}`, { params }),

  // 创建项目
  createProject: (data: {
    name: string
    description?: string
    code: string
    ownerId: number | string
    priority?: string
    budget?: number
    startDate?: string
    endDate?: string
    remarks?: string
  }) => request.post('/api/v1/projects', cleanParams(data)),

  // 更新项目
  updateProject: (id: number | string, data: {
    name?: string
    description?: string
    code?: string
    ownerId?: number | string
    priority?: string
    budget?: number
    startDate?: string
    endDate?: string
    status?: string
    remarks?: string
  }) => request.put(`/api/v1/projects/${id}`, cleanParams(data)),

  // 删除项目
  deleteProject: (id: number | string) => request.delete(`/api/v1/projects/${id}`),

  // 归档项目
  archiveProject: (id: number | string) => request.post(`/api/v1/projects/${id}/archive`),

  // 取消归档项目
  unarchiveProject: (id: number | string) => request.post(`/api/v1/projects/${id}/unarchive`),

  // 获取项目成员
  getMembers: (projectId: number | string) => request.get(`/api/v1/projects/${projectId}/members`),

  // 添加成员
  addMember: (projectId: number | string, data: { userId: number | string; role?: string }) =>
    request.post(`/api/v1/projects/${projectId}/members`, null, { params: cleanParams(data) }),

  // 移除成员
  removeMember: (projectId: number | string, userId: number | string) =>
    request.delete(`/api/v1/projects/${projectId}/members/${userId}`),

  // 更新成员角色
  updateMemberRole: (projectId: number | string, userId: number | string, role: string) =>
    request.put(`/api/v1/projects/${projectId}/members/${userId}`, null, { params: { role } }),
}
