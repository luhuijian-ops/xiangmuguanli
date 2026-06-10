import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('useUserStore', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
  })

  describe('initialization', () => {
    it('应该正确初始化用户状态', () => {
      const userStore = useUserStore()
      expect(userStore.token).toBe('')
      expect(userStore.userInfo).toBeNull()
      expect(userStore.permissions).toEqual([])
    })

    it('应该从 localStorage 加载 token', () => {
      localStorage.setItem('token', 'saved-token')
      const userStore = useUserStore()
      expect(userStore.token).toBe('saved-token')
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
  })

  describe('actions', () => {
    it('setToken 应该正确设置 token', () => {
      const userStore = useUserStore()
      userStore.setToken('test-token')
      expect(userStore.token).toBe('test-token')
      expect(localStorage.getItem('token')).toBe('test-token')
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
