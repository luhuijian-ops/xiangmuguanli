import request from './request'

export const scheduleApi = {
  // 获取用户的日程列表
  getEventsByUser: (userId: string, params?: { start?: string; end?: string }) =>
    request.get(`/api/v1/schedule/user/${userId}`, { params }),

  // 获取项目的日程列表
  getEventsByProject: (projectId: string) =>
    request.get(`/api/v1/schedule/project/${projectId}`),

  // 获取日程详情
  getEvent: (id: string) => request.get(`/api/v1/schedule/${id}`),

  // 创建日程
  createEvent: (data: {
    title: string
    description?: string
    location?: string
    userId: number
    projectId?: number
    taskId?: number
    startTime: string
    endTime: string
    allDay?: boolean
    reminderMinutes?: number
    repeatRule?: string
  }) => request.post('/api/v1/schedule', null, { params: data }),

  // 更新日程
  updateEvent: (id: string, data: {
    title?: string
    description?: string
    location?: string
    startTime?: string
    endTime?: string
    allDay?: boolean
    reminderMinutes?: number
    repeatRule?: string
  }) => request.put(`/api/v1/schedule/${id}`, null, { params: data }),

  // 删除日程
  deleteEvent: (id: string) => request.delete(`/api/v1/schedule/${id}`),
}
