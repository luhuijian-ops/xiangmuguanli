import request from './request'

export const workHourApi = {
  // 创建工时记录
  createWorkHour: (data: {
    projectId?: number
    taskId?: number
    hours: number
    date: string
    description?: string
  }) => request.post('/api/v1/work-hours', null, { params: data }),

  // 获取用户的工时记录
  getWorkHoursByUser: (userId: string, startDate: string, endDate: string) =>
    request.get(`/api/v1/work-hours/user/${userId}`, { params: { startDate, endDate } }),

  // 获取项目的工时记录
  getWorkHoursByProject: (projectId: string) =>
    request.get(`/api/v1/work-hours/project/${projectId}`),

  // 获取任务的工时记录
  getWorkHoursByTask: (taskId: string) =>
    request.get(`/api/v1/work-hours/task/${taskId}`),

  // 更新工时记录
  updateWorkHour: (id: string, data: {
    hours?: number
    date?: string
    description?: string
  }) => request.put(`/api/v1/work-hours/${id}`, null, { params: data }),

  // 删除工时记录
  deleteWorkHour: (id: string) =>
    request.delete(`/api/v1/work-hours/${id}`),
}
