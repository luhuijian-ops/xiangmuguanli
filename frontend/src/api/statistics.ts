import request from './request'

export const statisticsApi = {
  // 获取项目统计
  getProjectStatistics: (projectId: string, startDate?: string, endDate?: string) =>
    request.get(`/api/v1/statistics/project/${projectId}`, { params: { startDate, endDate } }),

  // 获取用户统计
  getUserStatistics: (userId: string) =>
    request.get(`/api/v1/statistics/user/${userId}`),

  // 获取仪表板统计
  getDashboardStatistics: () =>
    request.get('/api/v1/statistics/dashboard'),

  // 获取工时统计
  getWorkHourStatistics: (userId: string, startDate: string, endDate: string) =>
    request.get('/api/v1/statistics/work-hours', { params: { userId, startDate, endDate } }),
}
