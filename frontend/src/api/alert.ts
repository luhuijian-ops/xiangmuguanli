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

export const alertApi = {
  // 获取所有告警列表
  getAlerts: (params?: { page?: number; pageSize?: number }) =>
    request.get('/api/v1/alerts', { params: normalizePageParams(params) }),

  // 按类型获取告警
  getAlertsByType: (type: string, params?: { page?: number; pageSize?: number }) =>
    request.get(`/api/v1/alerts/type/${type}`, { params: normalizePageParams(params) }),

  // 获取未解决告警
  getUnresolvedAlerts: (params?: { page?: number; pageSize?: number }) =>
    request.get('/api/v1/alerts/unresolved', { params: normalizePageParams(params) }),

  // 解决告警
  resolveAlert: (id: string) => request.put(`/api/v1/alerts/${id}/resolve`),
}
