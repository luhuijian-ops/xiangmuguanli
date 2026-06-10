import { test, expect } from '@playwright/test'

test('complete authentication flow', async ({ page }) => {
  // 导航到登录页
  await page.goto('http://localhost:3001/login')

  // �填写登录表单
  await page.fillInput('input[type="text"]', 'testuser')
  await page.fillInput('input[type="password"]', 'testpass123')
  await page.click('button[type="submit"]')

  // 等待导航到首页
  await expect(page).toHaveURL(/http:\/\/localhost:3001\/$/)

  // 验证用户已登录
  await expect(page.locator('text=testuser')).toBeVisible()

  // 导航到项目页面
  await page.click('text=项目')
  await expect(page).toHaveURL(/projects/)

  // 创建新项目
  await page.click('button:has-text("新建项目")')
  await page.fillInput('input[placeholder*="项目名称"]', 'E2E 测试项目')
  await page.fillInput('textarea[placeholder*="项目描述"]', '这是一个用于 Playwright E2E 测试的后端项目')
  await page.click('button:has-text("提交")')

  // 等待项目创建成功
  await expect(page.locator('text=E2.测试项目')).toBeVisible()

  // 导航到任务列表
  await page.click('text=任务')
  await expect(page).toHaveURL(/tasks/)
})
