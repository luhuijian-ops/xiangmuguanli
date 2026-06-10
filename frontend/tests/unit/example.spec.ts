import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '@/stores/user'

describe('user store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('应该正确初始化用户状态', () => {
    const userStore = useUserStore()
    expect(userStore.token).toBe(localStorage.getItem('token') || '')
    expect(userStore.userInfo).toBeNull()
  })

  it('isLoggedIn getter 应该正确工作', () => {
    const userStore = useUserStore()
    userStore.token = 'test-token'
    expect(userStore.isLoggedIn).toBe(true)

    userStore.token = ''
    expect(userStore.isLoggedIn).toBe(false)
  })
})
