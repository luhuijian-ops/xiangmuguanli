<template>
  <div class="oauth-callback-page">
    <div class="callback-container">
      <el-icon class="loading-icon" size="48" color="#667eea"><Loading /></el-icon>
      <h2>{{ statusText }}</h2>
      <p v-if="errorMessage" class="error">{{ errorMessage }}</p>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'
import { Loading } from '@element-plus/icons-vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const statusText = ref('正在处理登录...')
const errorMessage = ref('')

onMounted(async () => {
  const code = route.query.code as string
  const platform = route.query.platform as string

  if (!code) {
    statusText.value = '登录失败'
    errorMessage.value = '未获取到授权码，请返回重试'
    ElMessage.error('未获取到授权码')
    setTimeout(() => router.push('/login'), 2000)
    return
  }

  const state = route.query.state as string
  const expectedState = sessionStorage.getItem('oauth_state')
  sessionStorage.removeItem('oauth_state')
  if (!state || state !== expectedState) {
    statusText.value = '登录失败'
    errorMessage.value = 'OAuth state 校验失败，请返回重试'
    ElMessage.error('OAuth state 校验失败')
    setTimeout(() => router.push('/login'), 2000)
    return
  }

  // Determine platform from state first, fallback to query param for backward compatibility
  let loginPlatform: 'wechat' | 'dingtalk'
  if (state && state.startsWith('wechat')) {
    loginPlatform = 'wechat'
  } else if (state && state.startsWith('dingtalk')) {
    loginPlatform = 'dingtalk'
  } else if (platform === 'wechat' || platform === 'dingtalk') {
    loginPlatform = platform
  } else {
    statusText.value = '登录失败'
    errorMessage.value = '无法识别登录平台，请返回重试'
    ElMessage.error('无法识别登录平台')
    setTimeout(() => router.push('/login'), 2000)
    return
  }

  try {
    statusText.value = '正在登录...'
    const loginApi = loginPlatform === 'wechat'
      ? authApi.wechatLogin
      : authApi.dingtalkLogin

    const response = await loginApi(code, state)

    if (response && response.data && response.data.code === 200) {
      const data = response.data.data
      userStore.setToken(data.accessToken)
      userStore.setRefreshToken(data.refreshToken)
      userStore.setUserInfo(data.user)

      ElMessage.success('登录成功')
      const redirect = route.query.redirect as string
      router.replace(redirect || '/')
    } else {
      statusText.value = '登录失败'
      errorMessage.value = response?.data?.message || '登录失败，请返回重试'
      ElMessage.error(errorMessage.value)
      setTimeout(() => router.push('/login'), 2000)
    }
  } catch (error: any) {
    console.error('OAuth 登录失败:', error)
    statusText.value = '登录失败'
    errorMessage.value = error.response?.data?.message || '登录失败，请返回重试'
    ElMessage.error(errorMessage.value)
    setTimeout(() => router.push('/login'), 2000)
  }
})
</script>

<style scoped>
.oauth-callback-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.callback-container {
  background: white;
  padding: 50px;
  border-radius: 12px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
  text-align: center;
  width: 100%;
  max-width: 400px;
}

.loading-icon {
  animation: spin 1.5s linear infinite;
  margin-bottom: 20px;
}

@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

h2 {
  color: #333;
  margin-bottom: 10px;
  font-size: 1.3em;
}

.error {
  color: #f56c6c;
  margin-top: 10px;
}
</style>
