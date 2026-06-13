<script setup>
import { ref, onMounted } from 'vue'
import { getToplist, getPlaylistDetail } from '@/api/toplist'
import { useMusicStore } from '@/stores/music'

const musicStore = useMusicStore()

const toplists = ref([])
const loading = ref(true)

function playToplistTracks(tracks, index = 0) {
  if (tracks && tracks.length) {
    musicStore.playMultiple(tracks, index)
  }
}

onMounted(async () => {
  try {
    const res = await getToplist()
    const list = res.list || []

    // 给每个榜单加载前 3 首歌曲
    const details = await Promise.allSettled(
      list.slice(0, 6).map(tl => getPlaylistDetail(tl.id))
    )

    toplists.value = list.slice(0, 6).map((tl, i) => {
      const detail = details[i]
      const tracks = detail.status === 'fulfilled'
        ? (detail.value.playlist?.tracks || []).slice(0, 10)
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
        <h2>排行榜</h2>
        <p class="page-desc">各类音乐排行榜，发现热门好音乐</p>
      </div>

      <div class="toplist-grid">
        <div v-for="tl in toplists" :key="tl.id" class="toplist-card">
          <div class="toplist-cover" @click.stop="playToplistTracks(tl.tracks)">
            <img
              v-if="tl.coverImgUrl"
              :src="tl.coverImgUrl"
              :alt="tl.name"
              class="toplist-img"
            />
            <div v-else class="toplist-rank-placeholder">
              {{ toplists.indexOf(tl) + 1 }}
            </div>
            <div class="tl-play-overlay">▶ 播放全部</div>
          </div>
          <div class="toplist-info">
            <h3 class="toplist-name">{{ tl.name }}</h3>
            <p class="toplist-desc">{{ tl.description }}</p>
            <div class="toplist-songs">
              <p
                v-for="(song, i) in tl.tracks"
                :key="song.id"
                class="toplist-song"
                :class="{ active: musicStore.currentSong?.id === song.id }"
                @click.stop="musicStore.playMultiple(tl.tracks, i)"
              >
                <span class="song-idx">{{ i + 1 }}</span>
                {{ song.name }} — {{ song.ar?.[0]?.name || '未知' }}
              </p>
              <p v-if="!tl.tracks.length" class="toplist-song empty">暂无歌曲</p>
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
.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 6px;
}
.page-desc {
  font-size: 13px;
  color: var(--text-light);
}

.toplist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.toplist-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  transition: var(--transition);
  cursor: pointer;
}
.toplist-card:hover {
  box-shadow: var(--shadow-light);
  transform: translateY(-2px);
}

.toplist-cover {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  background: linear-gradient(135deg, var(--color-primary), #ff7e7e);
}
.toplist-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.toplist-rank-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 700;
  color: var(--text-white);
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.2);
}

.tl-play-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.4);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-white);
  font-size: 14px;
  opacity: 0;
  transition: var(--transition);
  cursor: pointer;
}
.toplist-cover:hover .tl-play-overlay {
  opacity: 1;
}

.toplist-info {
  flex: 1;
  min-width: 0;
}
.toplist-name {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 4px;
}
.toplist-desc {
  font-size: 12px;
  color: var(--text-light);
  margin-bottom: 10px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.toplist-songs {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.toplist-song {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.toplist-song:hover {
  color: var(--color-primary);
}
.toplist-song.active {
  color: var(--color-primary);
  font-weight: 600;
}
.toplist-song .song-idx {
  display: inline-block;
  width: 18px;
  text-align: right;
  margin-right: 4px;
}
.toplist-song.empty {
  color: var(--text-light);
  font-style: italic;
}
</style>
