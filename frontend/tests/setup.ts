import { vi } from 'vitest'
import { configDefaults, config } from '@vue/test-utils'
import * as ElementPlus from 'element-plus'

// Element Plus 组件全局注册
vi.stub('el-config-provider', {
  render: (props: any) => h('div', props),
})

// 模拟 Element Plus 组件
const ELEMENT_PLUS_COMPONENTS = [
  'ElButton',
  'ElForm',
  'ElFormItem',
  'ElInput',
  'ElSelect',
  'ElOption',
  'ElTable',
  'ElTableColumn',
  'ElPagination',
  'ElDialog',
  'ElCard',
  'ElTabs',
  'ElTabPane',
  'ElTag',
  'ElIcon',
  'ElPageHeader',
  'ElMessage',
  'ElMessageBox',
  'ElEmpty',
  'ElResult',
  'ElSkeleton',
  'ElRow',
  'ElCol',
  'ElUpload',
  'ElAvatar',
  'ElProgress',
  'ElDatePicker',
  'ElButtonGroup',
  'ElTimeline',
  'ElTimelineItem',
  'ElTooltip',
  'ElLink',
  'ElAlert',
  'ElBadge',
]

ELEMENT_PLUS_COMPONENTS.forEach(component => {
  vi.stub(component, new ElementPlus[component]())
})

// 全局配置
config.global.mocks = {
  $route: {
    path: '/',
    params: {},
    query: {},
  },
}

export default config
