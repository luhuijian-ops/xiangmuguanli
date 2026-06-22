<template>
  <div class="login-page">
    <div class="login-container">
      <h1>项目管理系统</h1>
      <p class="subtitle">欢迎使用项目管理系统</p>

      <!-- 用户名密码登录表单 -->
      <div v-if="loginMethod === 'password'" class="password-login">
        <el-form
          ref="loginFormRef"
          :model="loginForm"
          :rules="loginRules"
          class="login-form"
        >
          <el-form-item prop="username">
            <el-input
              v-model="loginForm.username"
              placeholder="用户名 / 邮箱"
              size="large"
              prefix-icon="User"
            />
          </el-form-item>
          <el-form-item prop="password">
            <el-input
              v-model="loginForm.password"
              type="password"
              placeholder="密码"
              size="large"
              prefix-icon="Lock"
              show-password
              @keyup.enter="handlePasswordLogin"
            />
          </el-form-item>
          <el-form-item>
            <el-button
              type="primary"
              size="large"
              :loading="loading"
              class="login-button"
              @click="handlePasswordLogin"
            >
              登录
            </el-button>
          </el-form-item>
        </el-form>

        <div class="login-divider">
          <span>其他登录方式</span>
        </div>

        <div class="login-buttons">
          <!-- 微信登录暂时关闭
          <button @click="handleWeChatLogin" class="login-btn wechat">
            <span>💬</span>
            <span>微信登录</span>
          </button>
          -->
          <button @click="handleDingTalkLogin" class="login-btn dingtalk">
            <span>⚙️</span>
            <span>钉钉登录</span>
          </button>
        </div>

        <div class="switch-login-method">
          <el-link type="primary" @click="loginMethod = 'qrcode'">
            返回扫码登录
          </el-link>
        </div>
      </div>

      <!-- 扫码登录 -->
      <div v-else class="qrcode-login">
        <div class="login-buttons">
          <button @click="loginMethod = 'password'" class="login-btn password">
            <span>🔐</span>
            <span>用户名密码登录</span>
          </button>
          <!-- 微信登录暂时关闭
          <button @click="handleWeChatLogin" class="login-btn wechat">
            <span>💬</span>
            <span>微信登录</span>
          </button>
          -->
          <button @click="handleDingTalkLogin" class="login-btn dingtalk">
            <span>⚙️</span>
            <span>钉钉登录</span>
          </button>
        </div>
      </div>

      <div v-if="errorMessage && loginMethod === 'password'" class="error-message">
        <el-alert type="error" :closable="false">{{ errorMessage }}</el-alert>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'
import type { FormInstance } from 'element-plus'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const loginMethod = ref<'password' | 'qrcode'>('password')
const loading = ref(false)
const errorMessage = ref('')
const loginFormRef = ref<FormInstance>()

const loginForm = reactive({
  username: '',
  password: ''
})

const loginRules = {
  username: [
    { required: true, message: '请输入用户名或邮箱', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

/**
 * 用户名密码登录
 */
const handlePasswordLogin = async () => {
  if (!loginFormRef.value) return

  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      errorMessage.value = ''

      try {
        const response = await authApi.login(loginForm.username, loginForm.password)

        if (response && response.data && response.data.code === 200) {
          // 更新userStore的token
          userStore.setToken(response.data.data.accessToken)
          userStore.setRefreshToken(response.data.data.refreshToken)

          // 直接使用API返回的用户信息
          userStore.setUserInfo(response.data.data.user)

          ElMessage.success('登录成功')

          // 跳转到重定向地址或首页
          const redirect = route.query.redirect as string
          const target = redirect || '/'

          // 使用 replace 而非 push，避免导航问题
          await router.replace(target)
        } else {
          errorMessage.value = response?.data?.message || '登录失败'
        }
      } catch (error: any) {
        console.error('登录失败:', error)
        errorMessage.value = error.response?.data?.message || '用户名或密码错误'
      } finally {
        loading.value = false
      }
    }
  })
}

/**
 * 微信登录
 */
const handleWeChatLogin = async () => {
  try {
    loading.value = true
    const redirectUri = window.location.origin + '/oauth/callback'
    const response = await authApi.getWeChatAuthUrl(redirectUri)

    if (response && response.data && response.data.code === 200) {
      sessionStorage.setItem('oauth_state', response.data.data.state)
      window.location.href = response.data.data.url
    } else {
      ElMessage.error('获取微信授权链接失败')
    }
  } catch (error) {
    console.error('获取微信授权链接失败:', error)
    ElMessage.error('获取微信授权链接失败')
  } finally {
    loading.value = false
  }
}

/**
 * 钉钉登录
 */
const handleDingTalkLogin = async () => {
  try {
    loading.value = true
    const redirectUri = window.location.origin + '/oauth/callback'
    const response = await authApi.getDingTalkAuthUrl(redirectUri)

    if (response && response.data && response.data.code === 200) {
      sessionStorage.setItem('oauth_state', response.data.data.state)
      window.location.href = response.data.data.url
    } else {
      ElMessage.error('获取钉钉授权链接失败')
    }
  } catch (error) {
    console.error('获取钉钉授权链接失败:', error)
    ElMessage.error('获取钉钉授权链接失败')
  } finally {
    loading.value = false
  }
}

/**
 * 组件卸载时清理
 */
onUnmounted(() => {
  // 清理工作
})
</script>

<style scoped>
.login-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 20px;
}

.login-container {
  background: white;
  padding: 50px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0,0,0,0.2);
  text-align: center;
  width: 100%;
  max-width: 450px;
}

h1 {
  color: #667eea;
  margin-bottom: 10px;
  font-size: 2em;
}

.subtitle {
  color: #666;
  margin-bottom: 30px;
  font-size: 1.1em;
}

.login-form {
  text-align: left;
}

.login-button {
  width: 100%;
}

.login-divider {
  margin: 25px 0;
  display: flex;
  align-items: center;
  color: #999;
  font-size: 0.9em;
}

.login-divider::before,
.login-divider::after {
  content: '';
  flex: 1;
  height: 1px;
  background: #ddd;
}

.login-divider span {
  padding: 0 15px;
}

.login-buttons {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.login-btn {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 15px 30px;
  border: none;
  border-radius: 8px;
  font-size: 1em;
  cursor: pointer;
  transition: all 0.3s;
  width: 100%;
  font-weight: 500;
}

.password {
  background: #667eea;
  color: white;
}

.wechat {
  background: #07c160;
  color: white;
}

.dingtalk {
  background: #2e82f7;
  color: white;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 5px 15px rgba(0,0,0,0.2);
}

.login-btn:active {
  transform: translateY(0);
}

.switch-login-method {
  margin-top: 20px;
}

.qrcode-container {
  margin-top: 30px;
  padding: 20px;
  background: #f8f9fa;
  border-radius: 8px;
}

.qrcode {
  width: 200px;
  height: 200px;
  background: white;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 15px;
  border: 2px dashed #ddd;
}

.qrcode-image img {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

.qrcode-placeholder {
  color: #999;
  font-size: 0.9em;
  text-align: center;
}

.tip {
  color: #666;
  font-size: 0.9em;
  margin: 10px 0 5px 0;
}

.status {
  color: #667eea;
  font-size: 0.95em;
  font-weight: 500;
  margin-bottom: 10px;
}

.error-message {
  margin-top: 20px;
}
</style>
