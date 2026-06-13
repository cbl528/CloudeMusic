<script setup>
import { ref, onMounted } from 'vue'
import { getTopArtists } from '@/api/artist'
import { useMusicStore } from '@/stores/music'

const musicStore = useMusicStore()
const artists = ref([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await getTopArtists(40)
    artists.value = res.artists || []
  } catch (e) {
    console.error('获取歌手列表失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-artists">
    <div class="page-header">
      <div class="header-left">
        <h2>热门歌手</h2>
        <p class="page-desc">发现你喜爱的音乐人</p>
      </div>
      <span class="artist-count">共 {{ artists.length }} 位</span>
    </div>

    <div v-if="loading" class="loading-state">
      <span>加载中...</span>
    </div>

    <div v-else-if="artists.length" class="artist-grid">
      <div
        v-for="artist in artists"
        :key="artist.id"
        class="artist-card"
        @click="musicStore.play(artist)"
      >
        <div class="artist-avatar">
          <img
            v-if="artist.img1v1Url"
            :src="artist.img1v1Url"
            :alt="artist.name"
            class="artist-img"
          />
          <div v-else class="avatar-placeholder">{{ artist.name.charAt(0) }}</div>
          <div class="avatar-mask">
            <span class="play-icon">▶</span>
          </div>
        </div>
        <p class="artist-name">{{ artist.name }}</p>
        <p class="artist-desc">{{ artist.albumSize || 0 }} 张专辑</p>
      </div>
    </div>

    <div v-else class="loading-state">暂无歌手数据</div>
  </div>
</template>

<style scoped>
.page-artists {
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

/* ===== 头部 ===== */
.page-header {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 28px;
}
.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  color: var(--text-primary);
  margin-bottom: 4px;
}
.page-desc {
  font-size: 13px;
  color: var(--text-light);
}
.artist-count {
  font-size: 12px;
  color: var(--text-light);
  padding-bottom: 2px;
}

/* ===== 歌手网格 ===== */
.artist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 28px 20px;
}

.artist-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.25s;
}
.artist-card:hover {
  transform: translateY(-6px);
}

/* ---- 头像 ---- */
.artist-avatar {
  position: relative;
  width: 130px;
  height: 130px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 12px;
  background: linear-gradient(135deg, #e8e8e8, #d0d0d0);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.artist-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s;
}
.artist-card:hover .artist-img {
  transform: scale(1.08);
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 40px;
  color: #ccc;
  font-weight: 700;
}

.avatar-mask {
  position: absolute;
  inset: 0;
  border-radius: 50%;
  background: rgba(0, 0, 0, 0.35);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.25s;
}
.artist-card:hover .avatar-mask {
  opacity: 1;
}

.play-icon {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  padding-left: 3px;
  transition: transform 0.2s;
}
.artist-card:hover .play-icon {
  transform: scale(1.1);
}

/* ---- 文字 ---- */
.artist-name {
  font-size: 14px;
  font-weight: 600;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.artist-desc {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 3px;
}
</style>
