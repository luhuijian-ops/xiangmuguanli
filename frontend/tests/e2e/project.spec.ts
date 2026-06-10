import { test, expect } from '@playwright/test'

test.describe('项目模块 E2E 测试', test => {
  test.beforeEach(async ({ page }) => {
    await page.goto('http://localhost:3003')
  })

  test('应该能够访问项目列表页', async ({ page }) => {
    await page.goto('http://localhost:3003/projects')
    
    // 验证页面标题
    await expect(page.locator('h1')).toHaveText('项目列表')
    
    // 验证创建按钮存在
    await expect(page.locator('button:has-text("创建项目")')).toBeVisible()
  })

  test('应该能够显示搜索框', async ({ page }) => {
    await page.goto('http://localhost:3003/projects')
    
    // 验证搜索框存在
    await expect(page.locator('input[placeholder*="搜索项目"]')).toBeVisible()
  })
})
