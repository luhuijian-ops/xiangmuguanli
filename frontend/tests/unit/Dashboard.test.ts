import { describe, it, expect, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createTestingPinia } from '@pinia/testing'
import Dashboard from '@/pages/dashboard/Dashboard.vue'

const testingPinia = createTestingPinia({ createApp: vi.fn() })

describe('Dashboard', () => {
  it('renders dashboard', () => {
    const wrapper = mount(Dashboard, {
      global: {
        plugins: [testingPinia],
        stubs: ['el-skeleton', 'el-row', 'el-col', 'el-card', 'el-icon'],
      },
    })

    expect(wrapper.find('.dashboard').exists()).toBe(true)
  })

  it('renders quick stats section', () => {
    const wrapper = mount(Dashboard, {
      global: {
        plugins: [testingPinia],
        stubs: ['el-skeleton', 'el-row', 'el-col', 'el-card', 'el-icon'],
      },
    })

    expect(wrapper.find('.stats-row').exists()).toBe(true)
  })
})
