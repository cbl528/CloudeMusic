<script setup>
import { ref, onMounted, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { getHistory, clearHistory } from '@/api/user'
import { useMusicStore } from '@/stores/music'
import { useAuthModal } from '@/composables/useAuthModal'
import { useToast } from '@/composables/useToast'

const musicStore = useMusicStore()
const { requireLogin } = useAuthModal()
const toast = useToast()

const history = ref([])
const loading = ref(true)

const isLoggedIn = () => !!localStorage.getItem('token')

onMounted(async () => {
  if (!isLoggedIn()) { loading.value = false; return }
  try {
    history.value = await getHistory()
  } catch (_) {
    toast.error('获取播放历史失败')
  } finally {
    loading.value = false
  }
})

// 按时间分组
const groups = computed(() => {
  const now = new Date()
  const today = new Date(now.getFullYear(), now.getMonth(), now.getDate())
  const yesterday = new Date(today.getTime() - 86400000)
  const weekStart = new Date(today.getTime() - today.getDay() * 86400000)

  const map = { today: [], yesterday: [], week: [], earlier: [] }

  for (const h of history.value) {
    const d = new Date(h.playedAt)
    if (d >= today) map.today.push(h)
    else if (d >= yesterday) map.yesterday.push(h)
    else if (d >= weekStart) map.week.push(h)
    else map.earlier.push(h)
  }

  const result = []
  if (map.today.length) result.push({ label: '今天', items: map.today })
  if (map.yesterday.length) result.push({ label: '昨天', items: map.yesterday })
  if (map.week.length) result.push({ label: '本周', items: map.week })
  if (map.earlier.length) result.push({ label: '更早', items: map.earlier })
  return result
})

function playHistory(index) {
  if (!history.value.length) return
  const songs = history.value.map(h => ({
    id: h.songId,
    name: h.songName,
    cover: h.cover,
    duration: h.duration,
    artists: [{ name: h.artist }],
  }))
  musicStore.playMultiple(songs, index)
}

async function handleClear() {
  try {
    await clearHistory()
    history.value = []
    toast.success('播放历史已清空')
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
  <div class="page-history">
    <div class="page-header">
      <h2>播放历史</h2>
      <p class="page-desc">查看你最近播放的音乐</p>
    </div>

    <!-- 未登录提示 -->
    <div v-if="!isLoggedIn()" class="empty-state">
      <div class="empty-icon"><Icon icon="mdi:history" width="64" height="64" /></div>
      <p class="empty-text">请先登录</p>
      <p class="empty-desc">登录后可查看播放历史</p>
      <button class="btn btn-primary" @click="onLoginClick">去登录</button>
    </div>

    <!-- 加载中 -->
    <div v-else-if="loading" class="empty-state">
      <p class="empty-text">加载中...</p>
    </div>

    <!-- 空状态 -->
    <div v-else-if="!history.length" class="empty-state">
      <div class="empty-icon"><Icon icon="mdi:history" width="64" height="64" /></div>
      <p class="empty-text">暂无播放记录</p>
      <p class="empty-desc">去听一些音乐吧</p>
    </div>

    <template v-else>
      <!-- 工具栏 -->
      <div class="toolbar">
        <span class="history-count">共 {{ history.length }} 首播放记录</span>
        <button class="btn-clear" @click="handleClear">清空播放历史</button>
      </div>

      <!-- 时间分组 -->
      <div v-for="group in groups" :key="group.label" class="time-group">
        <h3 class="time-label">{{ group.label }}</h3>
        <div
          v-for="(item, i) in group.items"
          :key="item.id"
          class="song-row"
          :class="{ active: musicStore.currentSong?.id === item.songId }"
          @click="playHistory(history.indexOf(item))"
        >
          <span class="col-index">{{ history.indexOf(item) + 1 }}</span>
          <span class="col-title">
            <img v-if="item.cover" :src="item.cover + '?param=32y32'" class="row-cover" />
            <span class="row-name" :class="{ highlight: musicStore.currentSong?.id === item.songId }">{{ item.songName }}</span>
          </span>
          <span class="col-artist">{{ item.artist || '未知' }}</span>
          <span class="col-duration">{{ formatDuration(item.duration) }}</span>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page-history {
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

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}
.history-count {
  font-size: 13px;
  color: var(--text-light);
}
.btn-clear {
  background: none;
  border: 1px solid var(--border-color);
  padding: 6px 16px;
  border-radius: var(--radius-md);
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition);
}
.btn-clear:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* 时间分组 */
.time-group {
  margin-bottom: 24px;
}
.time-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 4px;
}

/* 歌曲行 */
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

.row-cover {
  width: 32px; height: 32px; border-radius: 3px; object-fit: cover; flex-shrink: 0;
}
.row-name {
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.row-name.highlight {
  color: var(--color-primary); font-weight: 600;
}
</style>
