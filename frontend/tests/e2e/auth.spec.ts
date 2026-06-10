import { test, expect } from '@playwright/test'

test.describe('认证流程', () => {
  test.beforeEach(async ({ page }) => {
    // 导航到登录页
    await page.goto('http://localhost:5173/login')
  })

  test('应该显示登录页面', async ({ page }) => {
    // 验证页面标题
    await expect(page.locator('h1')).toHaveText('项目管理系统')

    // 验证登录按钮存在
    await expect(page.locator('.login-btn.wechat')).toBeVisible()
    await expect(page.locator('.login-btn.dingtalk')).toBeVisible()
  })

  test('点击微信登录应该跳转到微信授权页面', async ({ page }) => {
    // 点击微信登录按钮
    await page.click('.login-btn.wechat')

    // 等待页面跳转到微信授权页面
    await expect(page).toHaveURL(/open\.weixin\.qq\.com/)
  })

  test('点击钉钉登录应该跳转到钉钉授权页面', async ({ page }) => {
    // 点击钉钉登录按钮
    await page.click('.login-btn.dingtalk')

    // 等待页面跳转到钉钉授权页面
    await expect(page).toHaveURL(/login\.dingtalk\.com/)
  })

  test('OAuth回调页面应该处理登录', async ({ page }) => {
    // 导航到回调页面（模拟微信回调）
    await page.goto('http://localhost:5173/oauth/callback?code=invalid_test_code&platform=wechat')

    // 页面应该显示"正在登录..."或"登录失败"
    await expect(page.locator('h2')).toContainText(/正在登录|登录失败/)
  })
})
