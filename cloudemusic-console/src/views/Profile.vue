<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter } from 'vue-router'
import { Icon } from '@iconify/vue'
import { updateUserInfo, deleteAccount } from '@/api/user'
import { useToast } from '@/composables/useToast'
import { useUserStore } from '@/stores/user'
import { storeToRefs } from 'pinia'

const router = useRouter()
const toast = useToast()
const userStore = useUserStore()
const { userInfo, loaded } = storeToRefs(userStore)

const loading = ref(true)
const saving = ref(false)
const user = ref(null)

const editForm = ref({
  nickname: '',
  signature: '',
  avatar: '',
})

const avatarPreview = ref('')

// 当 store 中数据加载完毕时填充表单
watch([userInfo, loaded], ([info, isLoaded]) => {
  if (info && isLoaded) {
    user.value = info
    editForm.value.nickname = info.nickname || ''
    editForm.value.signature = info.signature || ''
    editForm.value.avatar = info.avatar || ''
    avatarPreview.value = info.avatar || ''
    loading.value = false
  } else if (isLoaded && !info) {
    // 已加载但无数据（未登录），重新请求
    loadUserInfo()
  }
}, { immediate: true })

async function loadUserInfo() {
  loading.value = true
  try {
    const data = await userStore.fetchUserInfo()
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
}

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
    <!-- 页面标题 -->
    <div class="page-header">
      <h2 class="page-title">个人中心</h2>
    </div>

    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <Icon icon="mdi:loading" width="28" height="28" class="spin" />
      <span>加载中...</span>
    </div>

    <!-- 内容 -->
    <div v-else class="profile-content">
      <!-- 头像 + 基本信息卡片 -->
      <div class="profile-card profile-header-card">
        <div class="header-avatar-wrap" @click="changeAvatar" title="点击更换头像">
          <img
            v-if="avatarPreview"
            :src="avatarPreview"
            class="header-avatar"
            alt="头像"
            @error="avatarPreview = ''"
          />
          <div v-else class="header-avatar-placeholder">
            <Icon icon="mdi:account" width="44" height="44" />
          </div>
          <div class="header-avatar-overlay">
            <Icon icon="mdi:camera" width="20" height="20" />
          </div>
        </div>
        <div class="header-info">
          <div class="header-nickname">
            {{ user?.nickname || user?.username }}
          </div>
          <div class="header-signature">
            {{ user?.signature || '这个人很懒，什么都没写...' }}
          </div>
        </div>
      </div>

      <!-- 编辑表单卡片 -->
      <div class="profile-card">
        <div class="card-title">
          <Icon icon="mdi:account-edit-outline" width="18" height="18" />
          <span>编辑资料</span>
        </div>

        <div class="form-body">
          <div class="form-row">
            <label class="form-label">昵称</label>
            <div class="form-control">
              <input
                v-model="editForm.nickname"
                type="text"
                class="form-input"
                placeholder="请输入昵称"
                maxlength="20"
              />
              <span class="char-count">{{ editForm.nickname.length }}/20</span>
            </div>
          </div>

          <div class="form-row">
            <label class="form-label">个性签名</label>
            <div class="form-control">
              <textarea
                v-model="editForm.signature"
                class="form-textarea"
                placeholder="介绍一下自己..."
                rows="3"
                maxlength="100"
              ></textarea>
              <span class="char-count textarea-count">{{ editForm.signature.length }}/100</span>
            </div>
          </div>

          <div class="form-divider"></div>

          <div class="form-row form-row-readonly">
            <label class="form-label">手机号</label>
            <div class="form-readonly-value">
              <Icon icon="mdi:phone-outline" width="15" height="15" />
              <span>{{ formatPhone(user?.phone) }}</span>
            </div>
          </div>

          <div class="form-row form-row-readonly">
            <label class="form-label">邮箱</label>
            <div class="form-readonly-value">
              <Icon icon="mdi:email-outline" width="15" height="15" />
              <span>{{ user?.email || '未绑定' }}</span>
            </div>
          </div>

          <div class="form-row form-row-readonly">
            <label class="form-label">用户名</label>
            <div class="form-readonly-value">
              <Icon icon="mdi:account-outline" width="15" height="15" />
              <span>{{ user?.username }}</span>
            </div>
          </div>

          <div class="form-row form-row-readonly">
            <label class="form-label">注册时间</label>
            <div class="form-readonly-value">
              <Icon icon="mdi:calendar-outline" width="15" height="15" />
              <span>{{ user?.createTime }}</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 操作栏 -->
      <div class="action-bar">
        <button
          class="btn-save"
          :class="{ active: hasChanges }"
          :disabled="!hasChanges || saving"
          @click="handleSave"
        >
          <Icon v-if="saving" icon="mdi:loading" width="16" height="16" class="spin" />
          <Icon v-else icon="mdi:check" width="16" height="16" />
          <span>{{ saving ? '保存中...' : '保存' }}</span>
        </button>
      </div>

      <!-- 账号管理卡片 -->
      <div class="profile-card">
        <div class="card-title">
          <Icon icon="mdi:shield-account-outline" width="18" height="18" />
          <span>账号管理</span>
        </div>

        <div class="account-body">
          <div class="account-row">
            <div class="account-row-info">
              <Icon icon="mdi:logout" width="16" height="16" />
              <span>退出当前账号</span>
            </div>
            <button class="btn-outline" @click="doLogout">退出登录</button>
          </div>
          <div class="account-row account-row-danger">
            <div class="account-row-info">
              <Icon icon="mdi:alert-circle-outline" width="16" height="16" />
              <span>注销账号</span>
              <span class="account-row-tip">此操作不可恢复，所有数据将被永久删除</span>
            </div>
            <button class="btn-danger" @click="handleDeleteAccount">注销账号</button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-profile {
  padding: 28px 48px;
  width: 100%;
  max-width: 820px;
  margin: 0 auto;
}

/* 页面标题 */
.page-header {
  margin-bottom: 28px;
}
.page-title {
  font-size: 22px;
  font-weight: 700;
  color: var(--text-primary);
  position: relative;
  padding-left: 14px;
}
.page-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 3px;
  bottom: 3px;
  width: 3px;
  background: var(--color-primary);
  border-radius: 2px;
}

/* 加载中 */
.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 100px 0;
  color: var(--text-light);
  font-size: 14px;
}
.spin {
  animation: rotate 1s linear infinite;
}
@keyframes rotate {
  to { transform: rotate(360deg); }
}

/* ---------- 卡片通用 ---------- */
.profile-card {
  background: #fff;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  overflow: hidden;
}

/* ---------- 头像卡片 ---------- */
.profile-header-card {
  display: flex;
  align-items: center;
  gap: 24px;
  padding: 32px 36px;
  margin-bottom: 20px;
  background: linear-gradient(135deg, #fafafa, #fff);
}

.header-avatar-wrap {
  position: relative;
  width: 88px;
  height: 88px;
  border-radius: 50%;
  overflow: hidden;
  cursor: pointer;
  flex-shrink: 0;
  border: 3px solid #f0f0f0;
  transition: border-color 0.25s;
}
.header-avatar-wrap:hover {
  border-color: var(--color-primary);
}

.header-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.header-avatar-placeholder {
  width: 100%;
  height: 100%;
  background: #eee;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #ccc;
}

.header-avatar-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
  opacity: 0;
  transition: opacity 0.25s;
}
.header-avatar-wrap:hover .header-avatar-overlay {
  opacity: 1;
}

.header-info {
  flex: 1;
  min-width: 0;
}
.header-nickname {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 6px;
}
.header-signature {
  font-size: 13px;
  color: var(--text-light);
  line-height: 1.5;
}

/* ---------- 表单卡片 ---------- */
.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-light);
  background: #fafafa;
}

.form-body {
  padding: 8px 0;
}

.form-row {
  display: flex;
  align-items: flex-start;
  padding: 14px 24px;
}

.form-label {
  width: 76px;
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 36px;
  flex-shrink: 0;
}

.form-control {
  flex: 1;
  position: relative;
}

.form-input {
  width: 100%;
  max-width: 360px;
  height: 36px;
  padding: 0 12px;
  border: 1px solid #ddd;
  border-radius: var(--radius-sm);
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
  max-width: 460px;
  padding: 8px 12px;
  border: 1px solid #ddd;
  border-radius: var(--radius-sm);
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

.char-count {
  position: absolute;
  right: 8px;
  bottom: -18px;
  font-size: 11px;
  color: var(--text-light);
}
.textarea-count {
  right: 12px;
  bottom: -18px;
}

.form-divider {
  height: 1px;
  background: var(--border-light);
  margin: 4px 24px;
}

/* 只读行 */
.form-row-readonly {
  padding: 12px 24px;
}

.form-readonly-value {
  display: flex;
  align-items: center;
  gap: 6px;
  line-height: 36px;
  font-size: 14px;
  color: var(--text-secondary);
}
.form-readonly-value svg {
  color: var(--text-light);
  flex-shrink: 0;
}

/* ---------- 保存按钮 ---------- */
.action-bar {
  display: flex;
  justify-content: center;
  padding: 24px 0 28px;
}

.btn-save {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  width: 220px;
  height: 42px;
  border: none;
  border-radius: 21px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 2px;
  cursor: pointer;
  transition: all 0.25s;
  background: #ddd;
  color: #fff;
}
.btn-save.active {
  background: linear-gradient(135deg, var(--color-primary), var(--color-primary-dark));
  box-shadow: 0 4px 14px rgba(236, 65, 65, 0.35);
}
.btn-save.active:hover {
  box-shadow: 0 6px 20px rgba(236, 65, 65, 0.45);
  transform: translateY(-1px);
}
.btn-save:disabled {
  cursor: not-allowed;
}

/* ---------- 账号管理 ---------- */
.account-body {
  padding: 8px 0;
}

.account-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 24px;
}
.account-row + .account-row {
  border-top: 1px solid var(--border-light);
}

.account-row-info {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: var(--text-secondary);
}
.account-row-info svg {
  color: var(--text-light);
  flex-shrink: 0;
}

.account-row-tip {
  font-size: 12px;
  color: var(--text-light);
  margin-left: 4px;
}

.btn-outline {
  display: inline-flex;
  align-items: center;
  padding: 7px 18px;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-sm);
  background: #fff;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.btn-outline:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.btn-danger {
  display: inline-flex;
  align-items: center;
  padding: 7px 18px;
  border: 1px solid var(--color-primary);
  border-radius: var(--radius-sm);
  background: #fff;
  font-size: 13px;
  color: var(--color-primary);
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
}
.btn-danger:hover {
  background: #fef2f2;
}

/* ---------- 账号管理危险行 ---------- */
.account-row-danger .account-row-info svg {
  color: var(--color-primary);
}
</style>
