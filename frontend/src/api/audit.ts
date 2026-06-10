import request from './request'

const normalizePageParams = (params?: { page?: number; pageSize?: number }) => {
  if (!params) return undefined
  const apiParams: any = { ...params }
  if (apiParams.pageSize !== undefined && apiParams.size === undefined) {
    apiParams.size = apiParams.pageSize
    delete apiParams.pageSize
  }
  return apiParams
}

export const auditApi = {
  // 获取项目的活动日志
  getActivitiesByProject: (projectId: string, params?: { page?: number; pageSize?: number }) =>
    request.get(`/api/v1/audit/project/${projectId}`, { params: normalizePageParams(params) }),

  // 获取用户的活动日志
  getActivitiesByUser: (userId: string, params?: { page?: number; pageSize?: number }) =>
    request.get(`/api/v1/audit/user/${userId}`, { params: normalizePageParams(params) }),

  // 获取实体的活动日志
  getActivitiesByEntity: (entityType: string, entityId: string, params?: { page?: number; pageSize?: number }) =>
    request.get(`/api/v1/audit/entity/${entityType}/${entityId}`, { params: normalizePageParams(params) }),

  // 获取审计日志
  getAuditLogs: (params?: { page?: number; pageSize?: number; action?: string; startDate?: string; endDate?: string }) =>
    request.get('/api/v1/audit/logs', { params: normalizePageParams(params) }),
}
