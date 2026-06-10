import { test, expect } from '@playwright/test'

test('complete project management flow', async ({ page }) => {
  // 导航到登录页
  await page.goto('http://localhost:3001/login')
  await page.fillInput('input[type="text"]', 'testuser')
  await page.fillInput('input[type="password"]', 'testpass123')
  await page.click('button[type="submit"]')

  // 等待导航到首页
  await expect(page).toHaveURL(/http:\/\/localhost:3001\/$/)

  // 导航到项目页面
  await page.click('text=项目')
  await expect(page).toHaveURL(/projects/)

  // 查看项目详情
  await page.click('text=测试项目')
  await expect(page).toHaveURL(/projects\/\w+/)

  // 导航到项目任务
  await page.click('text=任务')
  await expect(page).toHaveURL(/tasks/)

  // 查看任务详情
  await page.click('text=测试任务')
  await expect(page).toHaveURL(/tasks\/\w+/)

  // 返回项目
  await page.click('text=返回项目')
  await expect(page).toHaveURL(/projects\/\w+/)
})
