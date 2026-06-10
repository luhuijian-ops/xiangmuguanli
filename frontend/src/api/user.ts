import request from './request'

export const userApi = {
  // 获取用户列表（Spring Pageable 参数名：page / size，page 从 0 开始）
  getUsers: (page: number = 0, size: number = 10) =>
    request.get('/api/v1/users', { params: { page, size } }),

  // 获取单个用户
  getUserById: (id: string) => request.get(`/api/v1/users/${id}`),

  // 获取用户 by username
  getUserByUsername: (username: string) => request.get(`/api/v1/users/username/${username}`),

  // 更新用户信息
  updateUser: (id: string, data: { name?: string; email?: string; phone?: string; avatar?: string }) =>
    request.put(`/api/v1/users/${id}`, { params: data }),

  // 修改密码
  changePassword: (id: string, oldPassword: string, newPassword: string) =>
    request.put(`/api/v1/users/${id}/password`, null, { params: { oldPassword, newPassword } }),

  // 删除用户
  deleteUser: (id: string) => request.delete(`/api/v1/users/${id}`),

  // 启用用户
  enableUser: (id: string) => request.put(`/api/v1/users/${id}/enable`),

  // 禁用用户
  disableUser: (id: string) => request.put(`/api/v1/users/${id}/disable`),
}
