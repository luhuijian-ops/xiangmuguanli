import { test, expect } from '@playwright/test'

test('debug homepage', async ({ page }) => {
  const errors: string[] = []
  page.on('console', msg => {
    if (msg.type() === 'error') {
      errors.push(msg.text())
    }
    console.log(`[${msg.type()}] ${msg.text()}`)
  })
  page.on('pageerror', err => {
    errors.push(err.message)
    console.log(`[PAGE ERROR] ${err.message}`)
  })

  await page.goto('http://localhost:6000/login')
  await page.waitForTimeout(2000)

  // Try login
  await page.fill('input[placeholder*="用户名"]', 'admin')
  await page.fill('input[type="password"]', 'admin123')
  await page.click('button:has-text("登录")')

  await page.waitForTimeout(3000)

  console.log('Console errors:', errors)
  expect(errors).toEqual([])
})
