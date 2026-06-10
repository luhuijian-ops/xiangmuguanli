import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import ProjectCard from '@/components/project/ProjectCard.vue'
import type { Project } from '@/types'

const testingPinia = createTestingPinia({ createApp: vi.fn() })

const mockProject: Project = {
  id: 1,
  name: 'Test Project',
  status: 'ACTIVE',
  createdAt: new Date().toISOString(),
  ownerName: 'Test Owner',
}

describe('ProjectCard', () => {
  it('renders project information correctly', () => {
    const wrapper = mount(ProjectCard, {
      global: {
        plugins: [testingPinia],
        stubs: ['el-progress'],
      },
      props: {
        project: mockProject,
      },
    })

    expect(wrapper.text()).toContain('Test Project')
    expect(wrapper.text()).toContain('进行中')
  })

  it('emits edit event when edit button clicked', async () => {
    const wrapper = mount(ProjectCard, {
      global: {
        plugins: [testingPinia],
        stubs: ['el-progress'],
      },
      props: {
        project: mockProject,
      },
    })

    const editButton = wrapper.find('.action-btn.edit')
    expect(editButton.exists()).toBe(true)
    await editButton.trigger('click')

    expect(wrapper.emitted('edit')).toBeTruthy()
    expect(wrapper.emitted('edit')![0]).toEqual([mockProject])
  })

  it('emits delete event when delete button clicked', async () => {
    const wrapper = mount(ProjectCard, {
      global: {
        plugins: [testingPinia],
        stubs: ['el-progress'],
      },
      props: {
        project: mockProject,
      },
    })

    const deleteButton = wrapper.find('.action-btn.delete')
    expect(deleteButton.exists()).toBe(true)
    await deleteButton.trigger('click')

    expect(wrapper.emitted('delete')).toBeTruthy()
    expect(wrapper.emitted('delete')![0]).toEqual([mockProject])
  })
})
