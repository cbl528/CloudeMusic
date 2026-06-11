<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { getUserInfo, updateUserInfo, deleteAccount } from '@/api/user'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const toast = useToast()

const loading = ref(true)
const saving = ref(false)
const user = ref(null)

const editForm = ref({
  nickname: '',
  signature: '',
  avatar: '',
})

const avatarPreview = ref('')

onMounted(async () => {
  try {
    const data = await getUserInfo()
    user.value = data
    editForm.value.nickname = data.nickname || ''
    editForm.value.signature = data.signature || ''
    editForm.value.avatar = data.avatar || ''
    avatarPreview.value = data.avatar || ''
  } catch {
    toast.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
})

const hasChanges = computed(() => {
  if (!user.value) return false
  return (
    editForm.value.nickname !== (user.value.nickname || '') ||
    editForm.value.signature !== (user.value.signature || '') ||
    editForm.value.avatar !== (user.value.avatar || '')
  )
})

function changeAvatar() {
  const url = prompt('请输入头像图片链接', editForm.value.avatar || '')
  if (url !== null) {
    editForm.value.avatar = url.trim()
    avatarPreview.value = url.trim()
  }
}

async function handleSave() {
  if (!hasChanges.value) return
  saving.value = true
  try {
    const data = await updateUserInfo({
      nickname: editForm.value.nickname,
      signature: editForm.value.signature,
      avatar: editForm.value.avatar,
    })
    user.value = data
    toast.success('保存成功')
  } catch (e) {
    toast.error(e.message || '保存失败')
  } finally {
    saving.value = false
  }
}

function doLogout() {
  localStorage.removeItem('token')
  localStorage.removeItem('saved_credentials')
  toast.success('已退出登录')
  router.push('/')
}

function handleDeleteAccount() {
  const confirmed = confirm('确定要注销账号吗？此操作不可恢复！')
  if (!confirmed) return

  deleteAccount()
    .then(() => {
      toast.success('账号已注销')
      doLogout()
    })
    .catch((e) => {
      toast.error(e.message || '注销失败')
    })
}

function formatPhone(phone) {
  if (!phone) return '未绑定'
  return phone.replace(/(\d{3})\d{4}(\d{4})/, '$1****$2')
}
</script>

<template>
  <div class="page-profile">
    <div class="page-header">
      <h2>个人中心</h2>
      <p class="page-desc">管理你的个人信息</p>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <Icon icon="mdi:loading" width="32" height="32" class="spin" />
      <span>加载中...</span>
    </div>

    <!-- 个人信息卡片 -->
    <div v-else class="profile-main">
      <!-- 头像区域 -->
      <div class="avatar-section">
        <div class="avatar-wrap" @click="changeAvatar">
          <img
            v-if="avatarPreview"
            :src="avatarPreview"
            class="avatar-img"
            alt="头像"
            @error="avatarPreview = ''"
          />
          <div v-else class="avatar-placeholder">
            <Icon icon="mdi:account" width="40" height="40" />
          </div>
          <div class="avatar-overlay">
            <Icon icon="mdi:camera" width="22" height="22" />
            <span>更换头像</span>
          </div>
        </div>
      </div>

      <!-- 表单区域 -->
      <div class="form-card">
        <div class="form-row">
          <label class="form-label">昵 称</label>
          <div class="form-input-wrap">
            <input
              v-model="editForm.nickname"
              type="text"
              class="form-input"
              placeholder="请输入昵称"
              maxlength="20"
            />
            <span class="input-count">{{ editForm.nickname.length }}/20</span>
          </div>
        </div>

        <div class="form-row">
          <label class="form-label">个性签名</label>
          <div class="form-input-wrap">
            <textarea
              v-model="editForm.signature"
              class="form-textarea"
              placeholder="介绍一下自己..."
              rows="3"
              maxlength="100"
            ></textarea>
            <span class="input-count">{{ editForm.signature.length }}/100</span>
          </div>
        </div>

        <div class="form-row">
          <label class="form-label">手机号</label>
          <div class="form-value">{{ formatPhone(user?.phone) }}</div>
        </div>

        <div class="form-row">
          <label class="form-label">邮 箱</label>
          <div class="form-value">{{ user?.email || '未绑定' }}</div>
        </div>

        <div class="form-row">
          <label class="form-label">用户名</label>
          <div class="form-value">{{ user?.username }}</div>
        </div>

        <div class="form-row">
          <label class="form-label">注册时间</label>
          <div class="form-value">{{ user?.createTime }}</div>
        </div>
      </div>

      <!-- 保存按钮 -->
      <div class="save-bar">
        <button
          class="save-btn"
          :class="{ active: hasChanges }"
          :disabled="!hasChanges || saving"
          @click="handleSave"
        >
          {{ saving ? '保存中...' : '保 存' }}
        </button>
      </div>

      <!-- 账号管理 -->
      <div class="account-section">
        <h3 class="section-title">账号管理</h3>
        <div class="account-actions">
          <button class="action-btn" @click="doLogout">
            <Icon icon="mdi:logout" width="16" height="16" />
            <span>退出登录</span>
          </button>
          <button class="action-btn danger" @click="handleDeleteAccount">
            <Icon icon="mdi:account-remove-outline" width="16" height="16" />
            <span>注销账号</span>
          </button>
        </div>
        <p class="action-tip">退出登录后不会删除任何历史数据，下次登录可恢复正常使用</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-profile {
  padding: 32px 48px;
  width: 100%;
  max-width: 800px;
}

.page-header {
  margin-bottom: 32px;
}
.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
}
.page-desc {
  font-size: 13px;
  color: var(--text-light);
}

/* 加载中 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 80px 0;
  color: var(--text-light);
  font-size: 14px;
}
.spin {
  animation: rotate 1s linear infinite;
}
@keyframes rotate {
  to { transform: rotate(360deg); }
}

/* 头像区域 */
.avatar-section {
  display: flex;
  justify-content: center;
  margin-bottom: 36px;
}
.avatar-wrap {
  position: relative;
  width: 100px;
  height: 100px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  border: 3px solid #f0f0f0;
  transition: border-color 0.2s;
}
.avatar-wrap:hover {
  border-color: var(--color-primary);
}
.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #bbb;
}
.avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 4px;
  color: #fff;
  font-size: 12px;
  opacity: 0;
  transition: opacity 0.25s;
}
.avatar-wrap:hover .avatar-overlay {
  opacity: 1;
}

/* 表单卡片 */
.form-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 8px 0;
  margin-bottom: 24px;
}

.form-row {
  display: flex;
  align-items: flex-start;
  padding: 16px 24px;
  border-bottom: 1px solid var(--border-light);
}
.form-row:last-child {
  border-bottom: none;
}

.form-label {
  width: 80px;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 36px;
  flex-shrink: 0;
}

.form-input-wrap {
  flex: 1;
  position: relative;
}

.form-input {
  width: 100%;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
  transition: border-color 0.2s;
}
.form-input:focus {
  border-color: var(--color-primary);
}
.form-input::placeholder {
  color: #bbb;
}

.form-textarea {
  width: 100%;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: 4px;
  font-size: 14px;
  color: var(--text-primary);
  outline: none;
  resize: none;
  font-family: inherit;
  line-height: 1.5;
  transition: border-color 0.2s;
}
.form-textarea:focus {
  border-color: var(--color-primary);
}
.form-textarea::placeholder {
  color: #bbb;
}

.input-count {
  position: absolute;
  right: 10px;
  bottom: -20px;
  font-size: 12px;
  color: var(--text-light);
}

.form-value {
  line-height: 36px;
  font-size: 14px;
  color: var(--text-primary);
}

/* 保存按钮 */
.save-bar {
  display: flex;
  justify-content: center;
  margin-bottom: 40px;
}
.save-btn {
  width: 200px;
  height: 40px;
  border: none;
  border-radius: 20px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 3px;
  cursor: pointer;
  transition: all 0.2s;
  background: #ddd;
  color: #fff;
}
.save-btn.active {
  background: var(--color-primary);
  color: #fff;
}
.save-btn.active:hover {
  background: var(--color-primary-dark);
}
.save-btn:disabled {
  cursor: not-allowed;
}

/* 账号管理 */
.account-section {
  border-top: 1px solid var(--border-color);
  padding-top: 24px;
}
.section-title {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-secondary);
  margin-bottom: 16px;
}
.account-actions {
  display: flex;
  gap: 12px;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 18px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  background: #fff;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.action-btn:hover {
  border-color: var(--text-light);
  color: var(--text-primary);
}
.action-btn.danger {
  color: var(--color-primary);
  border-color: var(--color-primary);
}
.action-btn.danger:hover {
  background: #fef2f2;
}
.action-tip {
  margin-top: 12px;
  font-size: 12px;
  color: var(--text-light);
  line-height: 1.5;
}
</style>
