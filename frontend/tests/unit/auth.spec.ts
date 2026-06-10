import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('user store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  describe('initialization', () => {
    it('应该从 localStorage 加载 token', () => {
      localStorage.setItem('token', 'saved-token')
      const userStore = useUserStore()
      expect(userStore.token).toBe('saved-token')
    })

    it('应该正确初始化用户状态', () => {
      const userStore = useUserStore()
      expect(userStore.token).toBe('')
      expect(userStore.userInfo).toBeNull()
      expect(userStore.permissions).toEqual([])
    })
  })

  describe('getters', () => {
    it('isLoggedIn 应该在存在 token 时返回 true', () => {
      const userStore = useUserStore()
      userStore.token = 'test-token'
      expect(userStore.isLoggedIn).toBe(true)
    })

    it('isLoggedIn 应该在没有 token 时返回 false', () => {
      const userStore = useUserStore()
      userStore.token = ''
      expect(userStore.isLoggedIn).toBe(false)
    })

    it('userId 应该返回用户 ID', () => {
      const userStore = useUserStore()
      userStore.userInfo = { id: 123, name: 'Test User', status: 'ACTIVE', createdAt: '' } as any
      expect(userStore.userId).toBe(123)
    })
  })

  describe('actions', () => {
    it('setUserInfo 应该正确设置用户信息', () => {
      const userStore = useUserStore()
      const mockUser = { id: 123, name: 'Test User', status: 'ACTIVE', createdAt: '' } as any
      userStore.setUserInfo(mockUser)
      expect(userStore.userInfo).toEqual(mockUser)
    })

    it('setToken 应该正确设置 token', () => {
      const userStore = useUserStore()
      const token = 'test-token'
      userStore.setToken(token)
      expect(userStore.token).toBe(token)
      expect(localStorage.getItem('token')).toBe(token)
    })

    it('logout 应该清除用户状态', () => {
      const userStore = useUserStore()
      userStore.setToken('test-token')
      userStore.logout()
      expect(userStore.token).toBe('')
      expect(userStore.userInfo).toBeNull()
      expect(localStorage.getItem('token')).toBeNull()
    })
  })
})
