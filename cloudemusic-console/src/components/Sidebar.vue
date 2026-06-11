<script setup>
import { useRouter, useRoute } from 'vue-router'

const router = useRouter()
const route = useRoute()

const navItems = [
  {
    group: '发现音乐',
    items: [
      { path: '/', label: '推荐', icon: '♡' },
      { path: '/toplist', label: '排行榜', icon: '♫' },
      { path: '/artists', label: '歌手', icon: '♪' },
    ],
  },
  {
    group: '我的音乐',
    items: [
      { path: '/favorites', label: '收藏', icon: '★' },
      { path: '/history', label: '播放历史', icon: '◷' },
    ],
  },
  {
    group: '其他',
    items: [
      { path: '/search', label: '搜索', icon: '⌕' },
    ],
  },
]

function navigate(path) {
  router.push(path)
}

function isActive(path) {
  return route.path === path
}
</script>

<template>
  <aside class="sidebar">
    <div class="logo" @click="navigate('/')">
      <span class="logo-icon">♪</span>
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
          <span class="nav-icon">{{ item.icon }}</span>
          <span class="nav-label">{{ item.label }}</span>
        </div>
      </div>
    </nav>
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
</style>
