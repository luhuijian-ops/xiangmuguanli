import { describe, it, expect } from 'vitest'
import { authApi } from '@/api'

describe('authApi', () => {
  it('should have required methods', () => {
    expect(typeof authApi.login).toBe('function')
    expect(typeof authApi.getWeChatAuthUrl).toBe('function')
    expect(typeof authApi.getDingTalkAuthUrl).toBe('function')
    expect(typeof authApi.refreshToken).toBe('function')
    expect(typeof authApi.getCurrentUser).toBe('function')
    expect(typeof authApi.register).toBe('function')
    expect(typeof authApi.wechatLogin).toBe('function')
    expect(typeof authApi.dingtalkLogin).toBe('function')
  })
})
