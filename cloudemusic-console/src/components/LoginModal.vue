<script setup>
import { ref, watch } from 'vue'
import { login, register } from '@/api/user'

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['close', 'login-success'])

const activeTab = ref('login') // 'login' | 'register'

const loginForm = ref({
  username: '',
  password: '',
  remember: false,
})

const registerForm = ref({
  username: '',
  password: '',
  confirmPassword: '',
})

const loading = ref(false)
const errorMsg = ref('')

// 记住密码回填
const saved = localStorage.getItem('saved_credentials')
if (saved) {
  try {
    const c = JSON.parse(saved)
    loginForm.value.username = c.username || ''
    loginForm.value.password = c.password || ''
    loginForm.value.remember = !!(c.username && c.password)
  } catch { /* ignore */ }
}

// 打开弹窗时重置状态
watch(() => props.visible, (v) => {
  if (v) {
    activeTab.value = 'login'
    errorMsg.value = ''
  }
})

function onOverlayClick(e) {
  if (e.target === e.currentTarget) close()
}

function close() {
  emit('close')
  errorMsg.value = ''
}

async function handleLogin() {
  errorMsg.value = ''
  if (!loginForm.value.username || !loginForm.value.password) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  loading.value = true
  try {
    const token = await login({
      username: loginForm.value.username,
      password: loginForm.value.password,
    })
    localStorage.setItem('token', token)

    // 记住密码
    if (loginForm.value.remember) {
      localStorage.setItem('saved_credentials', JSON.stringify({
        username: loginForm.value.username,
        password: loginForm.value.password,
      }))
    } else {
      localStorage.removeItem('saved_credentials')
    }

    emit('login-success', { nickname: loginForm.value.username })
    close()
  } catch (e) {
    errorMsg.value = e.message || '登录失败，请检查用户名和密码'
  } finally {
    loading.value = false
  }
}

async function handleRegister() {
  errorMsg.value = ''
  const { username, password, confirmPassword } = registerForm.value
  if (!username || !password) {
    errorMsg.value = '请输入用户名和密码'
    return
  }
  if (password.length < 6) {
    errorMsg.value = '密码长度不能少于6位'
    return
  }
  if (password !== confirmPassword) {
    errorMsg.value = '两次密码输入不一致'
    return
  }
  loading.value = true
  try {
    await register({ username, password, nickname: username })
    // 注册成功 → 切回登录页并填充用户名
    loginForm.value.username = username
    loginForm.value.password = ''
    activeTab.value = 'login'
    errorMsg.value = '注册成功，请登录'
  } catch (e) {
    errorMsg.value = e.message || '注册失败'
  } finally {
    loading.value = false
  }
}

function onKeydown(e) {
  if (e.key === 'Escape') close()
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click="onOverlayClick" @keydown="onKeydown">
        <div class="modal-dialog">
          <button class="close-btn" @click="close">×</button>

          <!-- 标题区域 -->
          <div class="modal-header">
            <h3 class="modal-title">音乐的力量</h3>
            <p class="modal-subtitle">登录 CloudMusic 享受更多精彩</p>
          </div>

          <!-- 选项卡 -->
          <div class="tabs">
            <button
              class="tab"
              :class="{ active: activeTab === 'login' }"
              @click="activeTab = 'login'; errorMsg = ''"
            >登录</button>
            <button
              class="tab"
              :class="{ active: activeTab === 'register' }"
              @click="activeTab = 'register'; errorMsg = ''"
            >注册</button>
          </div>

          <!-- 错误/成功提示 -->
          <transition name="msg">
            <p v-if="errorMsg" class="form-message" :class="{ success: errorMsg === '注册成功，请登录' }">
              {{ errorMsg }}
            </p>
          </transition>

          <!-- 登录表单 -->
          <form v-if="activeTab === 'login'" class="form" @submit.prevent="handleLogin">
            <div class="input-group">
              <input
                v-model="loginForm.username"
                type="text"
                placeholder="用户名"
                autocomplete="username"
              />
            </div>
            <div class="input-group">
              <input
                v-model="loginForm.password"
                type="password"
                placeholder="密码"
                autocomplete="current-password"
              />
            </div>

            <div class="form-options">
              <label class="remember">
                <input v-model="loginForm.remember" type="checkbox" />
                <span>记住密码</span>
              </label>
              <a class="forgot-link" href="javascript:;" @click="$emit('close'); alert('请联系管理员重置密码')">忘记密码？</a>
            </div>

            <button type="submit" class="submit-btn" :disabled="loading">
              {{ loading ? '登录中...' : '登 录' }}
            </button>

            <div class="form-footer">
              <span>还没有账号？</span>
              <a href="javascript:;" class="switch-link" @click="activeTab = 'register'; errorMsg = ''">去注册</a>
            </div>
          </form>

          <!-- 注册表单 -->
          <form v-else class="form" @submit.prevent="handleRegister">
            <div class="input-group">
              <input
                v-model="registerForm.username"
                type="text"
                placeholder="用户名"
                autocomplete="username"
              />
            </div>
            <div class="input-group">
              <input
                v-model="registerForm.password"
                type="password"
                placeholder="密码（至少6位）"
                autocomplete="new-password"
              />
            </div>
            <div class="input-group">
              <input
                v-model="registerForm.confirmPassword"
                type="password"
                placeholder="确认密码"
                autocomplete="new-password"
              />
            </div>

            <button type="submit" class="submit-btn" :disabled="loading">
              {{ loading ? '注册中...' : '注 册' }}
            </button>

            <div class="form-footer">
              <span>已有账号？</span>
              <a href="javascript:;" class="switch-link" @click="activeTab = 'login'; errorMsg = ''">去登录</a>
            </div>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
/* 遮罩 */
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

/* 弹窗 */
.modal-dialog {
  position: relative;
  width: 420px;
  background: #fff;
  border-radius: 8px;
  padding: 40px 36px 32px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.2);
}

/* 关闭按钮 */
.close-btn {
  position: absolute;
  top: 12px;
  right: 16px;
  width: 32px;
  height: 32px;
  border: none;
  background: none;
  font-size: 22px;
  color: #999;
  cursor: pointer;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: all 0.2s;
}
.close-btn:hover {
  background: #f0f0f0;
  color: #333;
}

/* 标题 */
.modal-header {
  text-align: center;
  margin-bottom: 28px;
}
.modal-title {
  font-size: 22px;
  font-weight: 700;
  color: #333;
  letter-spacing: 2px;
}
.modal-subtitle {
  font-size: 13px;
  color: #999;
  margin-top: 6px;
}

/* 选项卡 */
.tabs {
  display: flex;
  border-bottom: 1px solid #e5e5e5;
  margin-bottom: 20px;
}
.tab {
  flex: 1;
  height: 40px;
  border: none;
  background: none;
  font-size: 15px;
  color: #666;
  cursor: pointer;
  position: relative;
  transition: color 0.2s;
}
.tab:hover {
  color: #333;
}
.tab.active {
  color: var(--color-primary);
  font-weight: 600;
}
.tab.active::after {
  content: '';
  position: absolute;
  bottom: -1px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 2px;
  background: var(--color-primary);
  border-radius: 1px;
}

/* 提示消息 */
.form-message {
  text-align: center;
  font-size: 13px;
  color: var(--color-primary);
  margin-bottom: 12px;
}
.form-message.success {
  color: #67c23a;
}

/* 表单 */
.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-group input {
  width: 100%;
  height: 42px;
  padding: 0 14px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  outline: none;
  transition: border-color 0.2s;
}
.input-group input::placeholder {
  color: #bbb;
}
.input-group input:focus {
  border-color: var(--color-primary);
}

/* 选项行 */
.form-options {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 13px;
}
.remember {
  display: flex;
  align-items: center;
  gap: 5px;
  color: #666;
  cursor: pointer;
}
.remember input[type="checkbox"] {
  accent-color: var(--color-primary);
}
.forgot-link {
  color: var(--text-link);
  font-size: 13px;
}
.forgot-link:hover {
  text-decoration: underline;
}

/* 提交按钮 */
.submit-btn {
  width: 100%;
  height: 42px;
  border: none;
  border-radius: 21px;
  background: var(--color-primary);
  color: #fff;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 4px;
  cursor: pointer;
  transition: background 0.2s;
}
.submit-btn:hover {
  background: var(--color-primary-dark);
}
.submit-btn:disabled {
  background: #ccc;
  cursor: not-allowed;
}

/* 底部切换 */
.form-footer {
  text-align: center;
  font-size: 13px;
  color: #999;
}
.switch-link {
  color: var(--text-link);
  font-size: 13px;
}
.switch-link:hover {
  text-decoration: underline;
}

/* 过渡动画 */
.modal-enter-active,
.modal-leave-active {
  transition: opacity 0.25s ease;
}
.modal-enter-active .modal-dialog,
.modal-leave-active .modal-dialog {
  transition: transform 0.25s ease;
}
.modal-enter-from,
.modal-leave-to {
  opacity: 0;
}
.modal-enter-from .modal-dialog {
  transform: scale(0.95);
}
.modal-leave-to .modal-dialog {
  transform: scale(0.95);
}

.msg-enter-active,
.msg-leave-active {
  transition: opacity 0.2s;
}
.msg-enter-from,
.msg-leave-to {
  opacity: 0;
}
</style>
