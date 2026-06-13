<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getPlaylistDetail } from '@/api/toplist'
import { useMusicStore } from '@/stores/music'
import FavoriteBtn from '@/components/FavoriteBtn.vue'

const route = useRoute()
const router = useRouter()
const musicStore = useMusicStore()

const playlist = ref(null)
const loading = ref(true)

const tracks = computed(() => playlist.value?.tracks || [])

function formatDuration(dt) {
  if (!dt) return '--'
  const s = Math.floor(dt / 1000)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${String(sec).padStart(2, '0')}`
}

function formatCount(n) {
  if (!n) return '0'
  if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(n >= 100000 ? 0 : 1) + '万'
  return String(n)
}

function playAll() {
  if (tracks.value.length) {
    musicStore.playMultiple(tracks.value, 0)
  }
}

function playTrack(index) {
  if (tracks.value.length) {
    musicStore.playMultiple(tracks.value, index)
  }
}

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/')
  }
}

onMounted(async () => {
  const id = route.params.id
  if (!id) {
    router.replace('/')
    return
  }
  try {
    const res = await getPlaylistDetail(id)
    playlist.value = res.playlist || null
    if (!playlist.value) throw new Error('歌单不存在')
  } catch (e) {
    console.error('获取歌单详情失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-playlist">
    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else-if="playlist">
      <!-- 顶部信息区 -->
      <div class="playlist-header" :style="{ background: `linear-gradient(180deg, rgba(236,65,65,0.12) 0%, var(--bg-content) 100%)` }">
        <button class="back-btn" @click="goBack">← 返回</button>
        <div class="header-content">
          <div class="cover-wrap">
            <img
              v-if="playlist.coverImgUrl"
              :src="playlist.coverImgUrl"
              :alt="playlist.name"
              class="cover-img"
            />
            <div v-else class="cover-placeholder">♪</div>
          </div>
          <div class="header-info">
            <div class="info-tag">歌单</div>
            <h1 class="info-title">{{ playlist.name }}</h1>
            <div class="info-creator">
              <img
                v-if="playlist.creator?.avatarUrl"
                :src="playlist.creator.avatarUrl"
                class="creator-avatar"
              />
              <span class="creator-name">{{ playlist.creator?.nickname || '未知' }}</span>
            </div>
            <div class="info-meta">
              <span class="meta-item">{{ formatCount(playlist.playCount) }} 次播放</span>
              <span class="meta-divider">·</span>
              <span class="meta-item">{{ playlist.trackCount }} 首</span>
            </div>
            <div v-if="playlist.tags?.length" class="info-tags">
              <span v-for="tag in playlist.tags" :key="tag" class="tag-chip">{{ tag }}</span>
            </div>
            <button class="play-all-btn" @click="playAll">
              <span class="play-all-icon">▶</span>
              播放全部
            </button>
          </div>
        </div>
      </div>

      <!-- 歌曲列表 -->
      <div class="song-section">
        <div class="song-table-header">
          <span class="col-index">#</span>
          <span class="col-title">标题</span>
          <span class="col-artist">歌手</span>
          <span class="col-fav"></span>
          <span class="col-album">专辑</span>
          <span class="col-duration">时长</span>
        </div>
        <div
          v-for="(song, i) in tracks"
          :key="song.id"
          class="song-row"
          :class="{ active: musicStore.currentSong?.id === song.id }"
          @click="playTrack(i)"
        >
          <span class="col-index">{{ i + 1 }}</span>
          <span class="col-title">
            <span v-if="song.al?.picUrl" class="song-cover-thumb">
              <img :src="song.al.picUrl + '?param=32y32'" class="thumb-img" />
            </span>
            <span class="song-name" :class="{ highlight: musicStore.currentSong?.id === song.id }">
              {{ song.name }}
            </span>
          </span>
          <span class="col-artist">{{ song.ar?.[0]?.name || '未知' }}</span>
          <span class="col-fav"><FavoriteBtn :song-id="song.id" :song-data="song" /></span>
          <span class="col-album">{{ song.al?.name || '未知' }}</span>
          <span class="col-duration">{{ formatDuration(song.dt || song.duration) }}</span>
        </div>
        <div v-if="!tracks.length" class="empty-tip">暂无歌曲</div>
      </div>
    </template>

    <div v-else class="loading-state">歌单不存在</div>
  </div>
</template>

<style scoped>
.page-playlist {
  width: 100%;
  min-height: 100%;
  background: var(--bg-content);
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 120px 0;
  color: var(--text-light);
  font-size: 14px;
}

.empty-tip {
  text-align: center;
  padding: 60px 0;
  color: var(--text-light);
  font-size: 14px;
}

/* ===== 顶部信息区 ===== */
.playlist-header {
  padding: 20px 48px 28px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  margin-bottom: 12px;
  background: none;
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  color: var(--text-secondary);
  font-size: 13px;
  cursor: pointer;
  transition: var(--transition);
}
.back-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

.header-content {
  display: flex;
  gap: 32px;
  align-items: flex-start;
}

.cover-wrap {
  width: 200px;
  height: 200px;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.15);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f0f0f0, #ddd);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #ccc;
}

.header-info {
  flex: 1;
  min-width: 0;
  padding-top: 8px;
}

.info-tag {
  display: inline-block;
  font-size: 11px;
  color: var(--color-primary);
  border: 1px solid var(--color-primary);
  border-radius: 3px;
  padding: 1px 6px;
  margin-bottom: 10px;
}

.info-title {
  font-size: 26px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 12px;
  line-height: 1.3;
}

.info-creator {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
}

.creator-avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  object-fit: cover;
}

.creator-name {
  font-size: 13px;
  color: var(--text-link);
  cursor: pointer;
}
.creator-name:hover {
  color: var(--color-primary-dark);
}

.info-meta {
  font-size: 12px;
  color: var(--text-light);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  margin-bottom: 16px;
}

.tag-chip {
  font-size: 12px;
  color: var(--text-secondary);
  background: #f5f5f5;
  border: 1px solid var(--border-color);
  border-radius: 12px;
  padding: 2px 12px;
}

.play-all-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 28px;
  background: var(--color-primary);
  color: var(--text-white);
  border: none;
  border-radius: 22px;
  font-size: 14px;
  cursor: pointer;
  transition: var(--transition);
}
.play-all-btn:hover {
  background: var(--color-primary-dark);
}

.play-all-icon {
  font-size: 14px;
}

/* ===== 歌曲列表 ===== */
.song-section {
  padding: 0 48px 40px;
}

.song-table-header {
  display: flex;
  align-items: center;
  padding: 12px 16px;
  font-size: 12px;
  color: var(--text-light);
  border-bottom: 1px solid var(--border-color);
}

.song-row {
  display: flex;
  align-items: center;
  padding: 10px 16px;
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

.col-index {
  width: 40px;
  text-align: center;
  flex-shrink: 0;
}

.col-title {
  flex: 1;
  min-width: 0;
  display: flex;
  align-items: center;
  gap: 10px;
}

.song-cover-thumb {
  width: 32px;
  height: 32px;
  border-radius: 3px;
  overflow: hidden;
  flex-shrink: 0;
}
.thumb-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.song-name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.song-name.highlight {
  color: var(--color-primary);
  font-weight: 600;
}

.col-artist {
  width: 140px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
}

.col-fav {
  width: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
}

.col-album {
  width: 160px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex-shrink: 0;
}

.col-duration {
  width: 60px;
  text-align: right;
  flex-shrink: 0;
}
</style>
