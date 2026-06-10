import request from './request'

export const notificationApi = {
  // 获取用户的通知列表
  getNotifications: (userId: string, isRead?: boolean, page = 0, pageSize = 20) =>
    request.get(`/api/v1/notifications/user/${userId}`, {
      params: { isRead, page, size: pageSize, sort: 'createdAt,desc' },
    }),

  // 获取未读通知数量
  getUnreadCount: (userId: string) =>
    request.get(`/api/v1/notifications/user/${userId}/unread-count`),

  // 标记单条通知为已读
  markAsRead: (id: string, userId: string) =>
    request.put(`/api/v1/notifications/${id}/read`, null, { params: { userId } }),

  // 标记所有通知为已读
  markAllAsRead: (userId: string) =>
    request.put(`/api/v1/notifications/user/${userId}/read-all`),
}
