// 统一的API响应处理工具函数

export function handleApiResponse<T = any>(
  response: any,
  errorMessage: string = '操作失败'
): { success: boolean; data?: T; message?: string } {
  // 处理各种可能的响应格式
  if (!response) {
    return { success: false, message: '无响应数据' }
  }

  // 如果是 axios 响应对象，提取实际的响应数据
  let res = response
  if (typeof response === 'object' && 'data' in response && typeof response.status === 'number') {
    res = response.data
  }

  // 情况1: 标准格式 { code: 200, data: ... }
  if (typeof res === 'object' && 'code' in res) {
    if (res.code === 200) {
      const data = res.data
      return { success: true, data }
    } else {
      return { success: false, message: res.message || errorMessage }
    }
  }

  // 情况2: 直接返回数据 { data: ... }
  if (typeof res === 'object' && 'data' in res && !('code' in res)) {
    return { success: true, data: res.data }
  }

  // 情况3: 仅返回成功标识
  if (res === true || res === 'success') {
    return { success: true }
  }

  // 情况4: 其他情况，尝试将整个res作为data
  return { success: true, data: res }
}

export function isSuccessResponse(response: any): boolean {
  if (!response) return false

  // 如果是 axios 响应对象，提取实际的响应数据
  let res = response
  if (typeof response === 'object' && 'data' in response && typeof response.status === 'number') {
    res = response.data
  }

  if (typeof res === 'object') {
    return res.code === 200 || 'data' in res || res === true || res === 'success'
  }
  return false
}
