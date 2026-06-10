import request from './request'

export const fileApi = {
  // 上传文件
  upload: (
    file: File,
    params: {
      projectId?: number
      taskId?: number
      entity?: string
      uploadedById: number
    }
  ) => {
    const formData = new FormData()
    formData.append('file', file)
    if (params.projectId !== undefined) {
      formData.append('projectId', String(params.projectId))
    }
    if (params.taskId !== undefined) {
      formData.append('taskId', String(params.taskId))
    }
    if (params.entity) {
      formData.append('entity', params.entity)
    }
    formData.append('uploadedById', String(params.uploadedById))

    return request.post('/api/v1/files', formData)
  },

  // 获取项目附件
  getFilesByProject: (projectId: number | string) =>
    request.get(`/api/v1/files/project/${projectId}`),

  // 获取任务附件
  getFilesByTask: (taskId: number | string) =>
    request.get(`/api/v1/files/task/${taskId}`),

  // 删除文件
  delete: (id: number | string) => request.delete(`/api/v1/files/${id}`),
}
