import request from './request'

export const statisticsApi = {
  // 获取项目统计
  getProjectStatistics: (projectId: string) =>
    request.get(`/api/v1/statistics/project/${projectId}`),

  // 获取用户统计
  getUserStatistics: (userId: string) =>
    request.get(`/api/v1/statistics/user/${userId}`),

  // 获取仪表板统计
  getDashboardStatistics: () =>
    request.get('/api/v1/statistics/dashboard'),
}
