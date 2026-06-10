import { describe, it, expect, beforeEach } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { projectApi } from '@/api'

describe('projectApi', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
  })

  it('should have required methods', () => {
    expect(typeof projectApi.getProjects).toBe('function')
    expect(typeof projectApi.createProject).toBe('function')
    expect(typeof projectApi.updateProject).toBe('function')
    expect(typeof projectApi.deleteProject).toBe('function')
    expect(typeof projectApi.getMembers).toBe('function')
  })
})
