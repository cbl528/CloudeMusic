<script setup>
import { ref, onMounted } from 'vue'
import { Icon } from '@iconify/vue'
import { getFavorites, removeFavorite } from '@/api/user'
import { useMusicStore } from '@/stores/music'
import { useAuthModal } from '@/composables/useAuthModal'
import { useToast } from '@/composables/useToast'

const musicStore = useMusicStore()
const { requireLogin } = useAuthModal()
const toast = useToast()

const favorites = ref([])
const loading = ref(true)

const isLoggedIn = () => !!localStorage.getItem('token')

onMounted(async () => {
  if (!isLoggedIn()) { loading.value = false; return }
  try {
    favorites.value = await getFavorites()
  } catch (_) {
    toast.error('获取收藏失败')
  } finally {
    loading.value = false
  }
})

function playFav(index) {
  if (!favorites.value.length) return
  const songs = favorites.value.map(f => ({
    id: f.songId,
    name: f.songName,
    cover: f.cover,
    duration: f.duration,
    artists: [{ name: f.artist }],
  }))
  musicStore.playMultiple(songs, index)
}

async function handleRemove(songId) {
  try {
    await removeFavorite(songId)
    favorites.value = favorites.value.filter(f => f.songId !== songId)
    toast.success('已取消收藏')
  } catch (_) {
    toast.error('操作失败')
  }
}

function formatDuration(dt) {
  if (!dt) return '--'
  const s = Math.floor(dt / 1000)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${String(sec).padStart(2, '0')}`
}

function onLoginClick() {
  requireLogin()
}
</script>

<template>
  <div class="page-favorites">
    <div class="page-header">
      <h2>我的收藏</h2>
      <p class="page-desc">收藏你喜欢的音乐内容</p>
    </div>

    <!-- 未登录提示 -->
    <div v-if="!isLoggedIn()" class="empty-state">
      <div class="empty-icon"><Icon icon="mdi:heart-outline" width="64" height="64" /></div>
      <p class="empty-text">请先登录</p>
      <p class="empty-desc">登录后可查看收藏的音乐</p>
      <button class="btn btn-primary" @click="onLoginClick">去登录</button>
    </div>

    <!-- 加载中 -->
    <div v-else-if="loading" class="empty-state">
      <p class="empty-text">加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!favorites.length" class="empty-state">
      <div class="empty-icon"><Icon icon="mdi:heart-outline" width="64" height="64" /></div>
      <p class="empty-text">还没有收藏的内容</p>
      <p class="empty-desc">去发现页找一些喜欢的音乐吧</p>
    </div>

    <!-- 收藏列表 -->
    <div v-else class="fav-list">
      <div class="list-header">
        <span class="col-index">#</span>
        <span class="col-title">音乐标题</span>
        <span class="col-artist">歌手</span>
        <span class="col-duration">时长</span>
        <span class="col-action">操作</span>
      </div>
      <div
        v-for="(fav, i) in favorites"
        :key="fav.id"
        class="song-row"
        :class="{ active: musicStore.currentSong?.id === fav.songId }"
        @click="playFav(i)"
      >
        <span class="col-index">{{ i + 1 }}</span>
        <span class="col-title">
          <img v-if="fav.cover" :src="fav.cover + '?param=32y32'" class="row-cover" />
          <span class="row-name" :class="{ highlight: musicStore.currentSong?.id === fav.songId }">{{ fav.songName }}</span>
        </span>
        <span class="col-artist">{{ fav.artist || '未知' }}</span>
        <span class="col-duration">{{ formatDuration(fav.duration) }}</span>
        <span class="col-action" @click.stop="handleRemove(fav.songId)">
          <button class="btn-del" title="取消收藏">✕</button>
        </span>
      </div>
      <div class="list-footer">—— 暂时没有更多了 ——</div>
    </div>
  </div>
</template>

<style scoped>
.page-favorites {
  padding: 32px 48px;
  width: 100%;
}

.page-header {
  margin-bottom: 20px;
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

.btn-primary {
  padding: 10px 28px;
  background: var(--color-primary);
  color: var(--text-white);
  border: none;
  border-radius: 22px;
  font-size: 14px;
  cursor: pointer;
  transition: var(--transition);
}
.btn-primary:hover {
  background: var(--color-primary-dark);
}

/* 空状态 */
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  text-align: center;
}
.empty-icon {
  color: #e0e0e0;
  margin-bottom: 16px;
}
.empty-text {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.empty-desc {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 20px;
}

/* 列表 */
.fav-list {
  width: 100%;
}

.list-header {
  display: flex;
  padding: 10px 16px;
  font-size: 12px;
  color: var(--text-light);
  border-bottom: 1px solid var(--border-color);
}

.song-row {
  display: flex;
  align-items: center;
  padding: 9px 16px;
  font-size: 13px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-light);
  transition: background 0.15s;
  cursor: pointer;
  border-radius: var(--radius-sm);
}
.song-row:hover {
  background: #f5f5f5;
}
.song-row.active {
  background: #fef0f0;
}

.col-index { width: 40px; flex-shrink: 0; text-align: center; }
.col-title {
  flex: 1; min-width: 0;
  display: flex; align-items: center; gap: 10px;
}
.col-artist { width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; }
.col-duration { width: 60px; text-align: right; flex-shrink: 0; }
.col-action { width: 50px; text-align: center; flex-shrink: 0; }

.row-cover {
  width: 32px; height: 32px; border-radius: 3px; object-fit: cover; flex-shrink: 0;
}
.row-name {
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.row-name.highlight {
  color: var(--color-primary); font-weight: 600;
}

.btn-del {
  background: none; border: none; cursor: pointer;
  color: var(--text-light); padding: 4px 8px; font-size: 13px;
  transition: color 0.2s;
}
.btn-del:hover {
  color: var(--color-primary);
}

.list-footer {
  text-align: center;
  padding: 24px 0 40px;
  font-size: 13px;
  color: var(--text-light);
  user-select: none;
}
</style>
