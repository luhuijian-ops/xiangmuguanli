import request from './request'

export const activityApi = {
  // 获取项目的活动流
  getActivitiesByProject: (projectId: string, page = 0, pageSize = 20) =>
    request.get(`/api/v1/activities/project/${projectId}`, {
      params: { page, size: pageSize, sort: 'createdAt,desc' },
    }),

  // 获取用户的活动流
  getActivitiesByUser: (userId: string, page = 0, pageSize = 20) =>
    request.get(`/api/v1/activities/user/${userId}`, {
      params: { page, size: pageSize, sort: 'createdAt,desc' },
    }),

  // 获取实体的活动流
  getActivitiesByEntity: (entityType: string, entityId: string, page = 0, pageSize = 20) =>
    request.get(`/api/v1/activities/entity/${entityType}/${entityId}`, {
      params: { page, size: pageSize, sort: 'createdAt,desc' },
    }),
}
