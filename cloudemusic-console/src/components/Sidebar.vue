<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Icon } from '@iconify/vue'
import LoginModal from './LoginModal.vue'
import { useToast } from '@/composables/useToast'

const router = useRouter()
const route = useRoute()
const toast = useToast()

const isLoggedIn = ref(false)
const user = ref({
  nickname: '',
  avatar: '',
})
const showLoginModal = ref(false)

const showDropdown = ref(false)
const userSectionRef = ref(null)

function onClickOutside(e) {
  if (userSectionRef.value && !userSectionRef.value.contains(e.target)) {
    showDropdown.value = false
  }
}

onMounted(() => document.addEventListener('click', onClickOutside))
onUnmounted(() => document.removeEventListener('click', onClickOutside))

const navItems = [
  {
    group: '发现音乐',
    items: [
      { path: '/', label: '推荐', icon: 'mdi:compass-outline' },
      { path: '/toplist', label: '排行榜', icon: 'mdi:chart-line' },
      { path: '/artists', label: '歌手', icon: 'mdi:microphone' },
    ],
  },
  {
    group: '我的音乐',
    items: [
      { path: '/favorites', label: '收藏', icon: 'mdi:heart-outline' },
      { path: '/history', label: '播放历史', icon: 'mdi:history' },
    ],
  },
  {
    group: '其他',
    items: [
      { path: '/search', label: '搜索', icon: 'mdi:magnify' },
    ],
  },
]

function navigate(path) {
  router.push(path)
}

function isActive(path) {
  return route.path === path
}

function toggleDropdown() {
  if (isLoggedIn.value) {
    showDropdown.value = !showDropdown.value
  }
}

function handleAction(action) {
  showDropdown.value = false
  if (action === 'logout') {
    isLoggedIn.value = false
    user.value = { nickname: '', avatar: '' }
    localStorage.removeItem('token')
    localStorage.removeItem('saved_credentials')
    toast.success('已退出登录')
    router.push('/')
  } else if (action === 'login') {
    showLoginModal.value = true
  }
}

function onLoginSuccess(data) {
  isLoggedIn.value = true
  user.value = { nickname: data.nickname, avatar: '' }
  showLoginModal.value = false
}
</script>

<template>
  <aside class="sidebar">
    <div class="logo" @click="navigate('/')">
      <span class="logo-icon"><Icon icon="mdi:music-note" width="28" height="28" /></span>
      <span class="logo-text">CloudMusic</span>
    </div>

    <nav class="nav">
      <div v-for="group in navItems" :key="group.group" class="nav-group">
        <h3 class="nav-group-title">{{ group.group }}</h3>
        <div
          v-for="item in group.items"
          :key="item.path"
          class="nav-item"
          :class="{ active: isActive(item.path) }"
          @click="navigate(item.path)"
        >
          <span class="nav-icon"><Icon :icon="item.icon" width="16" height="16" /></span>
          <span class="nav-label">{{ item.label }}</span>
        </div>
      </div>
    </nav>

    <!-- 用户登录栏 -->
    <div ref="userSectionRef" class="user-section">
      <!-- 未登录：一行文字 -->
      <div v-if="!isLoggedIn" class="login-trigger" @click="handleAction('login')">
        <span class="login-trigger-text">请登录/注册</span>
      </div>

      <!-- 已登录 -->
      <div v-else class="user-card" @click="toggleDropdown">
        <div class="user-avatar">
          <span class="avatar-text">{{ user.nickname.charAt(0) }}</span>
        </div>
        <div class="user-info">
          <p class="user-name">{{ user.nickname }}</p>
          <p class="user-desc">查看个人主页</p>
        </div>
        <span class="arrow" :class="{ open: showDropdown }">▾</span>

        <!-- 下拉菜单 -->
        <Transition name="dropdown">
          <div v-if="showDropdown" class="dropdown-menu" @click.stop>
            <div class="dropdown-item" @click="navigate('/profile'); showDropdown = false">
              <span class="dropdown-icon"><Icon icon="mdi:account-outline" width="16" height="16" /></span>
              <span>个人中心</span>
            </div>
            <div class="dropdown-item" @click="navigate('/settings'); showDropdown = false">
              <span class="dropdown-icon"><Icon icon="mdi:cog-outline" width="16" height="16" /></span>
              <span>设置</span>
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item logout" @click="handleAction('logout')">
              <span class="dropdown-icon"><Icon icon="mdi:logout" width="16" height="16" /></span>
              <span>退出登录</span>
            </div>
          </div>
        </Transition>
      </div>
    </div>

    <!-- 登录/注册弹窗 -->
    <LoginModal
      :visible="showLoginModal"
      @close="showLoginModal = false"
      @login-success="onLoginSuccess"
    />
  </aside>
</template>

<style scoped>
.sidebar {
  width: var(--sidebar-width);
  height: 100%;
  background: var(--bg-sidebar);
  border-right: 1px solid var(--border-color);
  display: flex;
  flex-direction: column;
  user-select: none;
  position: relative;
}

.logo {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 20px 20px 16px;
  cursor: pointer;
  border-bottom: 1px solid var(--border-light);
}
.logo-icon {
  font-size: 28px;
  color: var(--color-primary);
}
.logo-text {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  letter-spacing: 1px;
}

.nav {
  flex: 1;
  overflow-y: auto;
  padding: 12px 0;
}

.nav-group {
  margin-bottom: 8px;
}

.nav-group-title {
  padding: 10px 20px 6px;
  font-size: 12px;
  font-weight: 400;
  color: var(--text-light);
  letter-spacing: 2px;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 20px;
  margin: 2px 8px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition);
  color: var(--text-secondary);
}
.nav-item:hover {
  background: #eee;
  color: var(--text-primary);
}
.nav-item.active {
  background: var(--color-primary);
  color: var(--text-white);
}

.nav-icon {
  font-size: 16px;
  width: 20px;
  text-align: center;
}
.nav-label {
  font-size: 14px;
}

/* 未登录触发文字 */
.login-trigger {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px 10px;
  cursor: pointer;
  border-radius: var(--radius-md);
  transition: var(--transition);
}
.login-trigger:hover {
  background: #eee;
}
.login-trigger-text {
  font-size: 14px;
  color: var(--text-light);
  letter-spacing: 1px;
  transition: color 0.2s;
}
.login-trigger:hover .login-trigger-text {
  color: var(--color-primary);
}

/* 用户登录栏 */
.user-section {
  border-top: 1px solid var(--border-color);
  padding: 12px;
  flex-shrink: 0;
}

.user-card {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px;
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: var(--transition);
  position: relative;
}
.user-card:hover {
  background: #eee;
}

.user-avatar-placeholder,
.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  font-size: 16px;
}
.user-avatar-placeholder {
  background: #e0e0e0;
  color: var(--text-light);
}
.user-avatar {
  background: var(--color-primary);
  color: var(--text-white);
  font-weight: 600;
  font-size: 14px;
}

.user-info {
  flex: 1;
  min-width: 0;
}
.user-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.user-desc {
  font-size: 11px;
  color: var(--text-light);
  margin-top: 1px;
}

.arrow {
  font-size: 12px;
  color: var(--text-light);
  transition: transform 0.2s;
}
.arrow.open {
  transform: rotate(180deg);
}

/* 下拉菜单 */
.dropdown-menu {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 12px;
  right: 12px;
  background: var(--bg-content);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  box-shadow: 0 -4px 16px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  z-index: 200;
}

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 14px;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition);
}
.dropdown-item:hover {
  background: #f5f5f5;
  color: var(--text-primary);
}
.dropdown-item.logout:hover {
  color: var(--color-primary);
}

.dropdown-icon {
  font-size: 14px;
  width: 18px;
  text-align: center;
}

.dropdown-divider {
  height: 1px;
  background: var(--border-color);
  margin: 4px 0;
}

/* 下拉动画 */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: opacity 0.2s ease, transform 0.2s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(4px);
}
</style>
