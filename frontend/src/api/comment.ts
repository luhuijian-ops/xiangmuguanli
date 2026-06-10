import request from './request'

export const commentApi = {
  // 获取评论列表
  getComments: (entityType: string, entityId: string) =>
    request.get(`/api/v1/comments/${entityType}/${entityId}`),

  // 创建评论（后端使用 @RequestParam 接收查询参数）
  createComment: (data: any) =>
    request.post('/api/v1/comments', null, { params: data }),

  // 更新评论（后端使用 @RequestParam 接收查询参数）
  updateComment: (id: string, data: any) =>
    request.put(`/api/v1/comments/${id}`, null, { params: data }),

  // 删除评论
  deleteComment: (id: string, data?: { userId?: string | number }) =>
    request.delete(`/api/v1/comments/${id}`, { params: data }),

  // 获取回复列表
  getReplies: (commentId: string) =>
    request.get(`/api/v1/comments/${commentId}/replies`),
}
