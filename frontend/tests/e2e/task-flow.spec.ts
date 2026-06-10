import { test, expect } from '@playwright/test'

test('complete task board drag and drop flow', async ({ page }) => {
  // 导航到任务页面
  await page.goto('http://localhost:3001/tasks')

  // 验证任务板显示
  await expect(page.locator('.task-board')).toBeVisible()

  // 测试拖拽任务到不同列
  const todoTask = page.locator('.todo-column .task-card').first()
  const doingColumn = page.locator('.doing-column')

  // 拖拽任务到"进行中"列
  await todoTask.dragTo(doingColumn)

  // 等待任务移动
  await expect(page.locator('.doing-column .task-card')).toHaveCount(1)

  // 测试任务状态更新
  await expect(doingColumn).toContainText('测试任务')

  // 测试任务详情对话框
  await page.click('.doing-column .task-card')
  await expect(page.locator('.task-detail-dialog')).toBeVisible()

  // 关闭对话框
  await page.click('.dialog-close-button')
  await expect(page.locator('.task-detail-dialog')).toBeHidden()
})
