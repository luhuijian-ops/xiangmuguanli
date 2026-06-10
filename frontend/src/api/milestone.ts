import request from './request'

export const milestoneApi = {
  // 创建里程碑
  createMilestone: (data: {
    projectId: number
    name: string
    description?: string
    dueDate: string
    orderIndex?: number
  }) => request.post('/api/v1/milestones', null, { params: data }),

  // 获取项目的里程碑列表
  getMilestonesByProject: (projectId: string) =>
    request.get(`/api/v1/milestones/project/${projectId}`),

  // 获取单个里程碑
  getMilestoneById: (id: string) =>
    request.get(`/api/v1/milestones/${id}`),

  // 更新里程碑
  updateMilestone: (id: string, data: {
    name?: string
    description?: string
    dueDate?: string
    status?: string
    orderIndex?: number
  }) => request.put(`/api/v1/milestones/${id}`, null, { params: data }),

  // 删除里程碑
  deleteMilestone: (id: string) =>
    request.delete(`/api/v1/milestones/${id}`),
}
