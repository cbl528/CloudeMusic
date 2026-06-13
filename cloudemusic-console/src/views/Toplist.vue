<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getToplist, getPlaylistDetail } from '@/api/toplist'
import { useMusicStore } from '@/stores/music'

const router = useRouter()
const musicStore = useMusicStore()

const toplists = ref([])
const loading = ref(true)

function goPlaylist(id) {
  router.push(`/playlist/${id}`)
}

function playToplistTracks(tracks, index = 0) {
  if (tracks && tracks.length) {
    musicStore.playMultiple(tracks, index)
  }
}

function formatPlayCount(n) {
  if (!n) return ''
  if (n >= 100000000) return (n / 100000000).toFixed(1) + '亿'
  if (n >= 10000) return (n / 10000).toFixed(n >= 100000 ? 0 : 1) + '万'
  return String(n)
}

onMounted(async () => {
  try {
    const res = await getToplist()
    const list = res.list || []

    const details = await Promise.allSettled(
      list.slice(0, 6).map(tl => getPlaylistDetail(tl.id))
    )

    toplists.value = list.slice(0, 6).map((tl, i) => {
      const detail = details[i]
      const tracks = detail.status === 'fulfilled'
        ? (detail.value.playlist?.tracks || []).slice(0, 3)
        : []
      return { ...tl, tracks }
    })
  } catch (e) {
    console.error('获取排行榜数据失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-toplist">
    <div v-if="loading" class="loading-state">
      <span>加载中...</span>
    </div>

    <template v-else>
      <div class="page-header">
        <div class="header-top">
          <h2>排行榜</h2>
        </div>
        <p class="page-desc">各类音乐排行榜，发现热门好音乐</p>
      </div>

      <div class="toplist-list">
        <div
          v-for="tl in toplists"
          :key="tl.id"
          class="toplist-card"
          @click="goPlaylist(tl.id)"
        >
          <div class="card-left">
            <div class="card-cover">
              <img
                v-if="tl.coverImgUrl"
                :src="tl.coverImgUrl"
                :alt="tl.name"
                class="cover-img"
              />
              <div v-else class="cover-placeholder">{{ toplists.indexOf(tl) + 1 }}</div>
              <div class="cover-play-count">▶ {{ formatPlayCount(tl.playCount) }}</div>
              <div class="cover-mask">
                <span class="cover-play-icon">▶</span>
              </div>
            </div>
          </div>
          <div class="card-right">
            <div class="card-header">
              <h3 class="card-title">{{ tl.name }}</h3>
              <span class="card-more" @click.stop="goPlaylist(tl.id)">查看全部 &gt;</span>
            </div>
            <div class="card-songs">
              <div
                v-for="(song, i) in tl.tracks"
                :key="song.id"
                class="song-row"
                :class="{ active: musicStore.currentSong?.id === song.id }"
                @click.stop="playToplistTracks(tl.tracks, i)"
              >
                <span class="song-rank" :class="'rank-' + (i + 1)">{{ i + 1 }}</span>
                <span class="song-name">{{ song.name }}</span>
                <span class="song-artist">{{ song.ar?.[0]?.name || '未知' }}</span>
              </div>
              <div v-if="!tl.tracks.length" class="song-row empty">暂无歌曲</div>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page-toplist {
  padding: 32px 48px;
  width: 100%;
}

.loading-state {
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 100px 0;
  color: var(--text-light);
  font-size: 14px;
}

.page-header {
  margin-bottom: 28px;
}
.header-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 6px;
}
.header-top h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
}
.header-more {
  font-size: 13px;
  color: var(--text-light);
  cursor: pointer;
  transition: color 0.2s;
}
.header-more:hover {
  color: var(--color-primary);
}
.page-desc {
  font-size: 13px;
  color: var(--text-light);
}

/* ===== 排行榜列表 ===== */
.toplist-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.toplist-card {
  display: flex;
  height: 160px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  overflow: hidden;
  cursor: pointer;
  transition: box-shadow 0.25s, transform 0.25s;
}
.toplist-card:hover {
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.08);
  transform: translateY(-2px);
}

/* ---- 左侧封面 ---- */
.card-left {
  flex-shrink: 0;
}

.card-cover {
  position: relative;
  width: 160px;
  height: 160px;
  overflow: hidden;
  background: linear-gradient(135deg, var(--color-primary), #ff7e7e);
}

.cover-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  font-weight: 700;
  color: rgba(255, 255, 255, 0.6);
}

.cover-play-count {
  position: absolute;
  right: 6px;
  bottom: 6px;
  font-size: 11px;
  color: rgba(255, 255, 255, 0.9);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
  pointer-events: none;
}

.cover-mask {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s;
}
.card-cover:hover .cover-mask {
  opacity: 1;
}

.cover-play-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  padding-left: 3px;
  transition: transform 0.2s;
}
.card-cover:hover .cover-play-icon {
  transform: scale(1.05);
}

/* ---- 右侧信息 ---- */
.card-right {
  flex: 1;
  min-width: 0;
  padding: 14px 24px 10px;
  display: flex;
  flex-direction: column;
}

.card-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.card-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
}

.card-more {
  font-size: 12px;
  color: var(--text-light);
  cursor: pointer;
  transition: color 0.2s;
  flex-shrink: 0;
}
.card-more:hover {
  color: var(--color-primary);
}

/* ---- 歌曲列表 ---- */
.card-songs {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.song-row {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 5px 8px;
  border-radius: 4px;
  font-size: 13px;
  color: var(--text-secondary);
  transition: background 0.15s;
}
.song-row:hover {
  background: #f5f5f5;
}
.song-row.active {
  background: #fef0f0;
}

.song-rank {
  width: 24px;
  text-align: center;
  font-size: 14px;
  font-weight: 700;
  color: var(--text-light);
}
.song-rank.rank-1 {
  color: var(--color-primary);
  font-size: 16px;
}
.song-rank.rank-2 {
  color: #e66b6b;
  font-size: 15px;
}
.song-rank.rank-3 {
  color: #ff9d6e;
  font-size: 15px;
}

.song-name {
  flex: 1;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  color: var(--text-primary);
}
.song-row.active .song-name {
  color: var(--color-primary);
  font-weight: 600;
}

.song-artist {
  width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  text-align: right;
  color: var(--text-light);
  font-size: 12px;
}

.song-row.empty {
  color: var(--text-light);
  font-style: italic;
  justify-content: center;
  padding: 10px 0;
}
</style>
