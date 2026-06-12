<script setup>
import { ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { changePassword } from '@/api/user'
import { useToast } from '@/composables/useToast'

const toast = useToast()

const props = defineProps({
  visible: Boolean,
})

const emit = defineEmits(['close'])

const form = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const loading = ref(false)
const errorMsg = ref('')
const showOld = ref(false)
const showNew = ref(false)
const showConfirm = ref(false)

watch(() => props.visible, (v) => {
  if (v) {
    form.value = { oldPassword: '', newPassword: '', confirmPassword: '' }
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

function onKeydown(e) {
  if (e.key === 'Escape') close()
}

async function handleSubmit() {
  errorMsg.value = ''
  const { oldPassword, newPassword, confirmPassword } = form.value

  if (!oldPassword || !newPassword) {
    errorMsg.value = '请填写完整信息'
    return
  }
  if (newPassword.length < 6) {
    errorMsg.value = '新密码长度不能少于6位'
    return
  }
  if (newPassword !== confirmPassword) {
    errorMsg.value = '两次密码输入不一致'
    return
  }

  loading.value = true
  try {
    await changePassword({ oldPassword, newPassword })
    toast.success('密码修改成功')
    close()
  } catch (e) {
    errorMsg.value = e.message || '密码修改失败'
    toast.error(errorMsg.value)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <Teleport to="body">
    <Transition name="modal">
      <div v-if="visible" class="modal-overlay" @click="onOverlayClick" @keydown="onKeydown">
        <div class="modal-dialog">
          <button class="close-btn" @click="close">×</button>

          <!-- 标题 -->
          <div class="modal-header">
            <h3 class="modal-title">修改密码</h3>
            <p class="modal-subtitle">请设置一个新的密码</p>
          </div>

          <!-- 错误提示 -->
          <transition name="msg">
            <p v-if="errorMsg" class="form-message">{{ errorMsg }}</p>
          </transition>

          <!-- 表单 -->
          <form class="form" @submit.prevent="handleSubmit">
            <div class="input-wrapper">
              <Icon class="input-icon" icon="mdi:lock-outline" width="18" height="18" />
              <input
                v-model="form.oldPassword"
                :type="showOld ? 'text' : 'password'"
                placeholder="原密码"
                autocomplete="current-password"
              />
              <button type="button" class="pwd-toggle" @click="showOld = !showOld" tabindex="-1">
                <Icon :icon="showOld ? 'mdi:eye-off-outline' : 'mdi:eye-outline'" width="18" height="18" />
              </button>
            </div>

            <div class="input-wrapper">
              <Icon class="input-icon" icon="mdi:lock-outline" width="18" height="18" />
              <input
                v-model="form.newPassword"
                :type="showNew ? 'text' : 'password'"
                placeholder="新密码（至少6位）"
                autocomplete="new-password"
              />
              <button type="button" class="pwd-toggle" @click="showNew = !showNew" tabindex="-1">
                <Icon :icon="showNew ? 'mdi:eye-off-outline' : 'mdi:eye-outline'" width="18" height="18" />
              </button>
            </div>

            <div class="input-wrapper">
              <Icon class="input-icon" icon="mdi:lock-outline" width="18" height="18" />
              <input
                v-model="form.confirmPassword"
                :type="showConfirm ? 'text' : 'password'"
                placeholder="确认新密码"
                autocomplete="new-password"
              />
              <button type="button" class="pwd-toggle" @click="showConfirm = !showConfirm" tabindex="-1">
                <Icon :icon="showConfirm ? 'mdi:eye-off-outline' : 'mdi:eye-outline'" width="18" height="18" />
              </button>
            </div>

            <button type="submit" class="submit-btn" :disabled="loading">
              {{ loading ? '修改中...' : '确 认' }}
            </button>
          </form>
        </div>
      </div>
    </Transition>
  </Teleport>
</template>

<style scoped>
.modal-overlay {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-dialog {
  position: relative;
  width: 420px;
  background: #fff;
  border-radius: 8px;
  padding: 40px 36px 32px;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.2);
}

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

.form-message {
  text-align: center;
  font-size: 13px;
  color: var(--color-primary);
  margin-bottom: 16px;
}

.form {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.input-wrapper {
  position: relative;
  display: flex;
  align-items: center;
}
.input-wrapper .input-icon {
  position: absolute;
  left: 12px;
  color: #bbb;
  pointer-events: none;
  transition: color 0.2s;
}
.input-wrapper:focus-within .input-icon {
  color: var(--color-primary);
}
.input-wrapper input {
  width: 100%;
  height: 42px;
  padding: 0 38px 0 38px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  color: #333;
  outline: none;
  transition: border-color 0.2s;
}
.input-wrapper input::placeholder {
  color: #bbb;
}
.input-wrapper input:focus {
  border-color: var(--color-primary);
}

.pwd-toggle {
  position: absolute;
  right: 8px;
  width: 30px;
  height: 30px;
  border: none;
  background: none;
  color: #bbb;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: color 0.2s, background 0.2s;
}
.pwd-toggle:hover {
  color: #666;
  background: #f0f0f0;
}

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
