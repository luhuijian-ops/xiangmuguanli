import request from './request'

export const authApi = {
  // 用户名密码登录
  login: (username: string, password: string) =>
    request.post('/api/v1/auth/login', { username, password }),

  // 用户注册
  register: (username: string, password: string, email?: string, name?: string) =>
    request.post('/api/v1/auth/register', { username, password, email, name }),

  // 获取微信登录授权 URL
  getWeChatAuthUrl: (redirectUri?: string) =>
    request.get('/api/v1/oauth/wechat/url', { params: { redirectUri } }),

  // 微信回调（后端直接回调时使用）
  weChatCallback: (code: string, state: string) => request.get(`/api/v1/oauth/wechat/callback?code=${code}&state=${state}`),

  // 获取钉钉登录授权 URL
  getDingTalkAuthUrl: (redirectUri?: string) =>
    request.get('/api/v1/oauth/dingtalk/url', { params: { redirectUri } }),

  // 钉钉回调（后端直接回调时使用）
  dingTalkCallback: (code: string, state: string) => request.get(`/api/v1/oauth/dingtalk/callback?code=${code}&state=${state}`),

  // 刷新 Token
  refreshToken: (refreshToken: string) => request.post('/api/v1/auth/refresh', { refreshToken }),

  // 微信直接登录（POST code + state 换 token）
  wechatLogin: (code: string, state: string) =>
    request.post('/api/v1/auth/wechat/login', { code, state }),

  // 钉钉直接登录（POST code + state 换 token）
  dingtalkLogin: (code: string, state: string) =>
    request.post('/api/v1/auth/dingtalk/login', { code, state }),

  // 获取当前用户信息
  getCurrentUser: () => request.get('/api/v1/auth/me'),
}
