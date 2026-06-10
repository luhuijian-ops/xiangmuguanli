import request from './request'

// 过滤空值、undefined、空字符串，避免后端 URL 参数解析失败
const cleanParams = (data: Record<string, any>): Record<string, any> => {
  const cleaned: Record<string, any> = {}
  for (const [key, value] of Object.entries(data)) {
    if (value !== '' && value !== null && value !== undefined && value !== 'NaN') {
      cleaned[key] = value
    }
  }
  return cleaned
}

export const taskApi = {
  // 获取任务列表 (按项目)
  getTasks: (projectId: number, params?: { page?: number; size?: number }) =>
    request.get('/api/v1/tasks', { params: { projectId, ...params } }),

  // 获取分配给我的任务
  getTasksByAssignee: (userId: string, params?: { page?: number; size?: number }) =>
    request.get(`/api/v1/tasks/assignee/${userId}`, { params }),

  // 获取任务详情
  getTask: (id: string) => request.get(`/api/v1/tasks/${id}`),

  // 创建任务
  createTask: (data: {
    projectId: number
    title: string
    description?: string
    assigneeId?: number
    priority?: number
    dueDate?: string
    startDate?: string
    tags?: string
    attachments?: string
    creatorId: number
    parentId?: number
    sortOrder?: number
  }) => request.post('/api/v1/tasks', null, { params: cleanParams(data) }),

  // 更新任务
  updateTask: (id: string, data: {
    title?: string
    description?: string
    assigneeId?: number
    status?: string
    priority?: number
    dueDate?: string
    startDate?: string
    tags?: string
    parentId?: number
    sortOrder?: number
  }) => request.put(`/api/v1/tasks/${id}`, null, { params: cleanParams(data) }),

  // 更新任务状态
  updateTaskStatus: (id: string, status: string) =>
    request.patch(`/api/v1/tasks/${id}/status`, null, { params: { status } }),

  // 删除任务
  deleteTask: (id: string) => request.delete(`/api/v1/tasks/${id}`),

  // 获取子任务
  getSubtasks: (id: string) => request.get(`/api/v1/tasks/${id}/subtasks`),

  // 设置父任务
  setParentTask: (id: string, parentId?: number) =>
    request.put(`/api/v1/tasks/${id}/parent`, null, { params: { parentId } }),

  // 添加依赖
  addDependency: (id: string, dependOnTaskId: number) =>
    request.post(`/api/v1/tasks/${id}/dependencies`, null, { params: { dependOnTaskId } }),

  // 移除依赖
  removeDependency: (id: string, dependOnTaskId: number) =>
    request.delete(`/api/v1/tasks/${id}/dependencies/${dependOnTaskId}`),

  // 更新排序
  updateSortOrder: (id: string, sortOrder: number) =>
    request.put(`/api/v1/tasks/${id}/sort-order`, null, { params: { sortOrder } }),

  // 批量更新排序
  batchUpdateSortOrder: (projectId: number, taskIds: number[]) =>
    request.post('/api/v1/tasks/batch/sort-order', taskIds, { params: { projectId } }),

  // 按项目和状态获取已排序任务
  getTasksByProjectAndStatusSorted: (projectId: number, status: string) =>
    request.get(`/api/v1/tasks/project/${projectId}/status/${status}/sorted`),
}
