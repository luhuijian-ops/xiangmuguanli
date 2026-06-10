import { describe, it, expect, beforeEach, vi } from 'vitest'

describe('project simple tests', () => {
  beforeEach(test => {
    // Clear localStorage before each test
    localStorage.clear()
  })

  describe('localStorage operations', test => {
    it('should save and read project data', test => {
      const testProject = { id: '123', name: 'Test Project', status: 'ACTIVE' }
      localStorage.setItem('project-test', JSON.stringify(testProject))

      const savedProject = localStorage.getItem('project-test')
      expect(savedProject).toBe(JSON.stringify(testProject))

      const parsedProject = savedProject ? JSON.parse(savedProject) : null
      expect(parsedProject).toEqual(testProject)
    })

    it('should delete project data', test => {
      localStorage.setItem('project-delete', 'data-to-delete')
      expect(localStorage.getItem('project-delete')).toBe('data-to-delete')

      localStorage.removeItem('project-delete')
      expect(localStorage.getItem('project-delete')).toBeNull()
    })

    it('should return null for non-existent data', test => {
      const nonExistent = localStorage.getItem('non-existent')
      expect(nonExistent).toBeNull()
    })
  })
})
