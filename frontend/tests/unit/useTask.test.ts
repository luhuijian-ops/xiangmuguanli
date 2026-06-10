import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { taskApi } from '@/api'

describe('taskApi', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should have required methods', () => {
    expect(typeof taskApi.getTasks).toBe('function')
    expect(typeof taskApi.createTask).toBe('function')
    expect(typeof taskApi.updateTask).toBe('function')
    expect(typeof taskApi.deleteTask).toBe('function')
    expect(typeof taskApi.updateTaskStatus).toBe('function')
  })
})
