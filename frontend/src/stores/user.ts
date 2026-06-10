import { defineStore } from 'pinia'
import type { User } from '@/types'
import { authApi } from '@/api'

const USER_INFO_KEY = 'userInfo'

function getStoredUser(): User | null {
  try {
    const stored = localStorage.getItem(USER_INFO_KEY)
    return stored ? JSON.parse(stored) : null
  } catch {
    return null
  }
}

interface UserState {
  token: string
  refreshToken: string
  userInfo: User | null
  permissions: string[]
}

export const useUserStore = defineStore('user', {
  state: (): UserState => ({
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: getStoredUser(),
    permissions: [],
  }),

  getters: {
    isLoggedIn: (state) => !!state.token,
    userId: (state) => state.userInfo?.id || '',
    userName: (state) => state.userInfo?.name || '',
    userAvatar: (state) => state.userInfo?.avatar || '',
    isAdmin: (state) => state.userInfo?.isAdmin || false,
  },

  actions: {
    setUserInfo(user: User) {
      this.userInfo = user
      localStorage.setItem(USER_INFO_KEY, JSON.stringify(user))
    },

    setToken(token: string) {
      this.token = token
      localStorage.setItem('token', token)
    },

    setRefreshToken(token: string) {
      this.refreshToken = token
      localStorage.setItem('refreshToken', token)
    },

    async login(code: string, platform: 'wechat' | 'dingtalk') {
      try {
        const api = platform === 'wechat'
          ? authApi.wechatLogin
          : authApi.dingtalkLogin
        const response = await api(code)
        if (response && response.data && response.data.code === 200) {
          const data = response.data.data
          this.setToken(data.accessToken || data.token)
          this.setRefreshToken(data.refreshToken)
          this.setUserInfo(data.user)
          return { success: true, data }
        }
        return { success: false, message: response?.data?.message || '登录失败' }
      } catch (error: any) {
        return { success: false, message: error.response?.data?.message || '登录失败' }
      }
    },

    async refreshToken() {
      try {
        if (!this.refreshToken) {
          return { success: false, message: '没有刷新令牌' }
        }
        const response = await authApi.refreshToken(this.refreshToken)
        if (response && response.data && response.data.code === 200) {
          const data = response.data.data
          this.setToken(data.token)
          this.setRefreshToken(data.refreshToken)
          return { success: true, data }
        }
        return { success: false, message: response?.data?.message || '刷新失败' }
      } catch (error: any) {
        return { success: false, message: error.response?.data?.message || '刷新失败' }
      }
    },

    async fetchUserInfo() {
      try {
        const response = await authApi.getCurrentUser()
        if (response && response.data && response.data.code === 200) {
          this.setUserInfo(response.data.data)
          return response.data.data
        }
      } catch (error) {
        return null
      }
    },

    async logout() {
      // 清除本地存储
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
      localStorage.removeItem(USER_INFO_KEY)
      this.token = ''
      this.refreshToken = ''
      this.userInfo = null
      this.permissions = []
    },
  },
})
