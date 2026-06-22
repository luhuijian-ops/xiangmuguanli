import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'

export function useAuth() {
  const userStore = useUserStore()
  const router = useRouter()
  const route = useRoute()

  /**
   * 处理登录
   */
  const handleLogin = async (code: string, platform: 'wechat' | 'dingtalk', state: string) => {
    const result = await userStore.login(code, platform, state)

    if (result.success) {
      // 跳转到重定向地址或首页
      const redirect = route.query.redirect as string
      router.push(redirect || '/')
    } else {
      // 登录失败处理
      console.error('登录失败:', result.message)
    }
  }

  /**
   * 处理登出
   */
  const handleLogout = async () => {
    await userStore.logout()
    router.push('/login')
  }

  /**
   * 解析 JWT Token 获取过期时间
   */
  const getTokenExpiration = (token: string): number | null => {
    try {
      const payload = token.split('.')[1]
      const decoded = JSON.parse(atob(payload))
      return decoded.exp ? decoded.exp * 1000 : null
    } catch {
      return null
    }
  }

  /**
   * 检查并刷新 Token
   */
  const checkAndRefreshToken = async () => {
    if (userStore.token) {
      const expiration = getTokenExpiration(userStore.token)
      if (expiration) {
        const now = Date.now()
        const fiveMinutes = 5 * 60 * 1000
        // Token 即将过期（5分钟内），尝试刷新
        if (expiration - now < fiveMinutes) {
          const result = await userStore.refreshToken()
          if (!result.success) {
            // 刷新失败，登出
            await handleLogout()
          }
        }
      }
    }
  }

  return {
    userStore,
    handleLogin,
    handleLogout,
    checkAndRefreshToken,
  }
}
