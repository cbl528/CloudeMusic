<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getAlbumDetail } from '@/api/album'
import { useMusicStore } from '@/stores/music'

const route = useRoute()
const router = useRouter()
const musicStore = useMusicStore()

const album = ref(null)
const songs = ref([])
const loading = ref(true)

function goBack() {
  if (window.history.length > 1) router.back()
  else router.push('/search')
}

function playAll() {
  if (songs.value.length) musicStore.playMultiple(songs.value, 0)
}

function playTrack(index) {
  if (songs.value.length) musicStore.playMultiple(songs.value, index)
}

function formatDuration(dt) {
  if (!dt) return '--'
  const s = Math.floor(dt / 1000)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${String(sec).padStart(2, '0')}`
}

function formatDate(ts) {
  if (!ts) return ''
  const d = new Date(ts)
  const y = d.getFullYear()
  const m = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${y}-${m}-${day}`
}

onMounted(async () => {
  const id = route.params.id
  if (!id) { router.replace('/search'); return }
  try {
    const res = await getAlbumDetail(id)
    album.value = res.album || null
    songs.value = res.songs || []
    if (!album.value) throw new Error('专辑不存在')
  } catch (e) {
    console.error('获取专辑详情失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-album-detail">
    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else-if="album">
      <div class="album-header" :style="{ background: `linear-gradient(180deg, rgba(236,65,65,0.1) 0%, var(--bg-content) 100%)` }">
        <button class="back-btn" @click="goBack">← 返回</button>
        <div class="header-content">
          <div class="cover-wrap">
            <img
              v-if="album.picUrl"
              :src="album.picUrl + '?param=160y160'"
              :alt="album.name"
              class="cover-img"
            />
            <div v-else class="cover-placeholder">♪</div>
          </div>
          <div class="header-info">
            <div class="info-tag">专辑</div>
            <h1 class="info-title">{{ album.name }}</h1>
            <div class="info-artist">
              <span class="artist-name">{{ album.artist?.name || '未知歌手' }}</span>
            </div>
            <div class="info-meta">
              <span>{{ formatDate(album.publishTime) }}</span>
              <span class="meta-divider">·</span>
              <span>{{ songs.length }} 首</span>
            </div>
            <div v-if="album.description" class="info-desc">
              <p>{{ album.description }}</p>
            </div>
            <button class="play-all-btn" @click="playAll">▶ 播放全部</button>
          </div>
        </div>
      </div>

      <div class="song-section">
        <div class="song-table-header">
          <span class="col-index">#</span>
          <span class="col-title">标题</span>
          <span class="col-artist">歌手</span>
          <span class="col-duration">时长</span>
        </div>
        <div
          v-for="(song, i) in songs"
          :key="song.id"
          class="song-row"
          :class="{ active: musicStore.currentSong?.id === song.id }"
          @click="playTrack(i)"
        >
          <span class="col-index">{{ i + 1 }}</span>
          <span class="col-title">
            <span v-if="song.al?.picUrl || album.picUrl" class="song-thumb">
              <img :src="(song.al?.picUrl || album.picUrl) + '?param=32y32'" class="thumb-img" />
            </span>
            <span class="song-name" :class="{ highlight: musicStore.currentSong?.id === song.id }">{{ song.name }}</span>
          </span>
          <span class="col-artist">{{ song.ar?.[0]?.name || '未知' }}</span>
          <span class="col-duration">{{ formatDuration(song.dt || song.duration) }}</span>
        </div>
        <div v-if="!songs.length" class="empty-tip">暂无歌曲</div>
      </div>
    </template>

    <div v-else class="loading-state">专辑不存在</div>
  </div>
</template>

<style scoped>
.page-album-detail {
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
  padding: 40px 0;
  color: var(--text-light);
  font-size: 14px;
}

/* ===== 头部 ===== */
.album-header {
  padding: 20px 48px 28px;
}

.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  margin-bottom: 16px;
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
  width: 160px;
  height: 160px;
  border-radius: var(--radius-md);
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.12);
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
  margin-bottom: 8px;
}

.info-artist {
  margin-bottom: 10px;
}
.artist-name {
  font-size: 14px;
  color: var(--text-link);
  cursor: pointer;
}
.artist-name:hover {
  color: var(--color-primary-dark);
}

.info-meta {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
}

.info-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
  margin-bottom: 16px;
  max-height: 48px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.info-desc p {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
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

/* ===== 歌曲列表 ===== */
.song-section {
  padding: 0 48px 40px;
}

.song-table-header {
  display: flex;
  align-items: center;
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

.song-thumb {
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

.col-duration {
  width: 60px;
  text-align: right;
  flex-shrink: 0;
}
</style>
