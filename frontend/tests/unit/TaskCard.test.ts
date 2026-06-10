import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import TaskCard from '@/components/task/TaskCard.vue'
import type { Task } from '@/types'

const testingPinia = createTestingPinia({ createApp: vi.fn() })

const mockTask: Task = {
  id: 1,
  title: 'Test Task',
  status: 'TODO',
  priority: 1,
  projectId: 1,
  createdBy: 1,
  createdAt: new Date().toISOString(),
}

describe('TaskCard', () => {
  it('renders task information correctly', () => {
    const wrapper = mount(TaskCard, {
      global: {
        plugins: [testingPinia],
        stubs: ['el-tag', 'el-avatar', 'el-icon'],
      },
      props: {
        task: mockTask,
      },
    })

    expect(wrapper.text()).toContain('Test Task')
    expect(wrapper.text()).toContain('P1')
  })

  it('emits click event when card clicked', async () => {
    const wrapper = mount(TaskCard, {
      global: {
        plugins: [testingPinia],
        stubs: ['el-tag', 'el-avatar', 'el-icon'],
      },
      props: {
        task: mockTask,
      },
    })

    await wrapper.find('.task-card').trigger('click')

    expect(wrapper.emitted('click')).toBeTruthy()
    expect(wrapper.emitted('click')![0]).toEqual([mockTask])
  })
})
