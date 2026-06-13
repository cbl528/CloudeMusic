<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getArtistDetail, getArtistAlbum, getArtistDesc } from '@/api/artist'
import { useMusicStore } from '@/stores/music'
import FavoriteBtn from '@/components/FavoriteBtn.vue'

const route = useRoute()
const router = useRouter()
const musicStore = useMusicStore()

const artist = ref(null)
const hotSongs = ref([])
const albums = ref([])
const introduction = ref([])
const loading = ref(true)

function goBack() {
  if (window.history.length > 1) {
    router.back()
  } else {
    router.push('/artists')
  }
}

function playAll() {
  if (hotSongs.value.length) {
    musicStore.playMultiple(hotSongs.value, 0)
  }
}

function playTrack(index) {
  if (hotSongs.value.length) {
    musicStore.playMultiple(hotSongs.value, index)
  }
}

function formatDuration(dt) {
  if (!dt) return '--'
  const s = Math.floor(dt / 1000)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${String(sec).padStart(2, '0')}`
}

function formatCount(n) {
  if (!n) return '0'
  if (n >= 100000) return (n / 10000).toFixed(n >= 100000 ? 0 : 1) + '万'
  return String(n)
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
  if (!id) {
    router.replace('/artists')
    return
  }
  try {
    const [detailRes, albumRes, descRes] = await Promise.all([
      getArtistDetail(id),
      getArtistAlbum(id),
      getArtistDesc(id),
    ])
    artist.value = detailRes.artist || null
    hotSongs.value = detailRes.hotSongs || []
    albums.value = albumRes.hotAlbums || []
    introduction.value = descRes.introduction || []
    if (!artist.value) throw new Error('歌手不存在')
  } catch (e) {
    console.error('获取歌手详情失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-artist-detail">
    <div v-if="loading" class="loading-state">加载中...</div>

    <template v-else-if="artist">
      <!-- 头部 -->
      <div class="artist-header" :style="{ background: `linear-gradient(180deg, rgba(236,65,65,0.08) 0%, var(--bg-content) 100%)` }">
        <button class="back-btn" @click="goBack">← 返回</button>
        <div class="header-content">
          <div class="avatar-wrap">
            <img
              v-if="artist.img1v1Url"
              :src="artist.img1v1Url + '?param=200y200'"
              :alt="artist.name"
              class="avatar-img"
            />
            <div v-else class="avatar-placeholder">{{ artist.name.charAt(0) }}</div>
          </div>
          <div class="header-info">
            <div class="info-tag">歌手</div>
            <h1 class="info-title">{{ artist.name }}</h1>
            <div class="info-alias" v-if="artist.alias?.length">{{ artist.alias.join(' / ') }}</div>
            <div class="info-meta">
              <span>{{ formatCount(artist.musicSize) }} 首歌曲</span>
              <span class="meta-divider">·</span>
              <span>{{ artist.albumSize }} 张专辑</span>
            </div>
            <div v-if="introduction.length" class="info-desc">
              <p>{{ introduction[0].txt }}</p>
            </div>
            <button class="play-all-btn" @click="playAll">▶ 播放热门50首</button>
          </div>
        </div>
      </div>

      <!-- 热门歌曲 -->
      <div class="section">
        <h3 class="section-title">热门歌曲 50 首</h3>
        <div class="song-table-header">
          <span class="col-index">#</span>
          <span class="col-title">标题</span>
          <span class="col-fav"></span>
          <span class="col-album">专辑</span>
          <span class="col-duration">时长</span>
        </div>
        <div
          v-for="(song, i) in hotSongs"
          :key="song.id"
          class="song-row"
          :class="{ active: musicStore.currentSong?.id === song.id }"
          @click="playTrack(i)"
        >
          <span class="col-index">{{ i + 1 }}</span>
          <span class="col-title">
            <span v-if="song.al?.picUrl" class="song-thumb">
              <img :src="song.al.picUrl + '?param=32y32'" class="thumb-img" />
            </span>
            <span class="song-name" :class="{ highlight: musicStore.currentSong?.id === song.id }">{{ song.name }}</span>
          </span>
          <span class="col-fav"><FavoriteBtn :song-id="song.id" :song-data="song" /></span>
          <span class="col-album">{{ song.al?.name || '未知' }}</span>
          <span class="col-duration">{{ formatDuration(song.dt || song.duration) }}</span>
        </div>
        <div v-if="!hotSongs.length" class="empty-tip">暂无热门歌曲</div>
      </div>

      <!-- 专辑 -->
      <div class="section">
        <h3 class="section-title">专辑</h3>
        <div v-if="albums.length" class="album-grid">
          <div
            v-for="album in albums"
            :key="album.id"
            class="album-card"
            @click="router.push(`/album/${album.id}`)"
          >
            <div class="album-cover">
              <img v-if="album.picUrl" :src="album.picUrl + '?param=160y160'" :alt="album.name" class="album-img" />
              <div v-else class="album-placeholder">♪</div>
            </div>
            <p class="album-name">{{ album.name }}</p>
            <p class="album-date">{{ formatDate(album.publishTime) }}</p>
          </div>
        </div>
        <div v-else class="empty-tip">暂无专辑</div>
      </div>
    </template>

    <div v-else class="loading-state">歌手不存在</div>
  </div>
</template>

<style scoped>
.page-artist-detail {
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
.artist-header {
  padding: 20px 48px 32px;
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
  gap: 36px;
  align-items: flex-start;
}

.avatar-wrap {
  width: 200px;
  height: 200px;
  border-radius: 50%;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 4px 24px rgba(0, 0, 0, 0.12);
  background: linear-gradient(135deg, #e8e8e8, #d0d0d0);
}

.avatar-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 64px;
  color: #ccc;
  font-weight: 700;
}

.header-info {
  flex: 1;
  min-width: 0;
  padding-top: 12px;
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
  font-size: 28px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}

.info-alias {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 10px;
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
  max-height: 60px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.info-desc p {
  display: -webkit-box;
  -webkit-line-clamp: 3;
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

/* ===== 通用区块 ===== */
.section {
  padding: 0 48px 40px;
}

.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 16px;
  position: relative;
  padding-left: 12px;
}
.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 3px;
  bottom: 3px;
  width: 3px;
  background: var(--color-primary);
  border-radius: 2px;
}

/* ===== 歌曲列表 ===== */
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

.col-fav {
  width: 36px;
  flex-shrink: 0;
  display: flex;
  align-items: center;
  justify-content: center;
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

.col-album {
  width: 180px;
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

/* ===== 专辑网格 ===== */
.album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 24px;
}

.album-card {
  cursor: pointer;
  transition: transform 0.25s;
}
.album-card:hover {
  transform: translateY(-4px);
}

.album-cover {
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: 10px;
  background: #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.album-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.album-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f0f0, #e0e0e0);
  color: #ccc;
  font-size: 36px;
}

.album-name {
  font-size: 13px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.album-date {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 2px;
}
</style>
