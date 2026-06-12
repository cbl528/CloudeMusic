<script setup>
import { ref, onMounted } from 'vue'
import { getBanner, getPersonalized, getNewSong } from '@/api/recommend'

const banners = ref([])
const playlists = ref([])
const newSongs = ref([])
const loading = ref(true)

function formatPlayCount(count) {
  if (!count) return '0'
  if (count >= 10000) {
    return (count / 10000).toFixed(count >= 100000 ? 0 : 1) + '万'
  }
  return count
}

onMounted(async () => {
  try {
    const [bannerRes, plRes, songRes] = await Promise.all([
      getBanner(),
      getPersonalized(12),
      getNewSong(8),
    ])
    banners.value = bannerRes.banners || []
    playlists.value = plRes.result || []
    newSongs.value = songRes.result || []
  } catch (e) {
    console.error('获取推荐页数据失败', e)
  } finally {
    loading.value = false
  }
})
</script>

<template>
  <div class="page-recommend">
    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <span>加载中...</span>
    </div>

    <template v-else>
      <!-- Banner -->
      <section v-if="banners.length" class="banner-section">
        <div class="banner-wrapper">
          <div
            v-for="banner in banners"
            :key="banner.targetId || banner.pic"
            class="banner-card"
            :style="{ backgroundImage: `url(${banner.imageUrl || banner.pic})` }"
          >
            <div class="banner-content">
              <h2>{{ banner.typeTitle }}</h2>
            </div>
          </div>
        </div>
      </section>

      <!-- 推荐歌单 -->
      <section class="section">
        <div class="section-header">
          <h3 class="section-title">推荐歌单</h3>
          <a href="#" class="section-more">更多 &gt;</a>
        </div>
        <div v-if="playlists.length" class="card-grid">
          <div v-for="pl in playlists" :key="pl.id" class="playlist-card">
            <div class="card-cover">
              <img v-if="pl.picUrl" :src="pl.picUrl" :alt="pl.name" class="card-img" />
              <div v-else class="card-cover-placeholder">♪</div>
              <span class="play-count">▶ {{ formatPlayCount(pl.playCount) }}</span>
              <div class="card-play-overlay">▶</div>
            </div>
            <p class="card-name">{{ pl.name }}</p>
          </div>
        </div>
        <div v-else class="empty-tip">暂无推荐歌单</div>
      </section>

      <!-- 最新音乐 -->
      <section class="section">
        <div class="section-header">
          <h3 class="section-title">最新音乐</h3>
          <a href="#" class="section-more">更多 &gt;</a>
        </div>
        <div v-if="newSongs.length" class="card-grid">
          <div v-for="item in newSongs" :key="item.id" class="music-card">
            <div class="card-cover">
              <img v-if="item.picUrl" :src="item.picUrl" :alt="item.name" class="card-img" />
              <div v-else class="card-cover-placeholder small">♪</div>
              <div class="card-play-overlay">▶</div>
            </div>
            <p class="card-name">{{ item.name }}</p>
            <p class="card-artist">{{ item.song?.artists?.[0]?.name || '未知歌手' }}</p>
          </div>
        </div>
        <div v-else class="empty-tip">暂无新音乐</div>
      </section>
    </template>
  </div>
</template>

<style scoped>
.page-recommend {
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

.empty-tip {
  text-align: center;
  padding: 40px 0;
  color: var(--text-light);
  font-size: 14px;
}

/* Banner */
.banner-section {
  margin-bottom: 32px;
}
.banner-wrapper {
  display: flex;
  gap: 16px;
  overflow-x: auto;
  padding-bottom: 8px;
}
.banner-card {
  flex: 1;
  min-width: 260px;
  height: 160px;
  border-radius: var(--radius-md);
  background-size: cover;
  background-position: center;
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--transition);
  position: relative;
}
.banner-card::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  border-radius: var(--radius-md);
}
.banner-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.3);
}
.banner-content {
  position: relative;
  z-index: 1;
  text-align: center;
  color: var(--text-white);
}
.banner-content h2 {
  font-size: 22px;
  font-weight: 700;
  text-shadow: 0 2px 8px rgba(0, 0, 0, 0.5);
}

/* 通用区块 */
.section {
  margin-bottom: 36px;
}
.section-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.section-title {
  font-size: 18px;
  font-weight: 700;
  color: var(--text-primary);
  position: relative;
  padding-left: 12px;
}
.section-title::before {
  content: '';
  position: absolute;
  left: 0;
  top: 2px;
  bottom: 2px;
  width: 3px;
  background: var(--color-primary);
  border-radius: 2px;
}
.section-more {
  font-size: 13px;
  color: var(--text-light);
}
.section-more:hover {
  color: var(--color-primary);
}

/* 卡片网格 */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(170px, 1fr));
  gap: 24px;
}

.card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: 8px;
  cursor: pointer;
  background: #f0f0f0;
}

.card-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-cover-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #f0f0f0, #e0e0e0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 48px;
  color: #ccc;
}
.card-cover-placeholder.small {
  font-size: 36px;
}

.play-count {
  position: absolute;
  top: 6px;
  right: 8px;
  font-size: 11px;
  color: var(--text-white);
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.5);
}

.card-play-overlay {
  position: absolute;
  right: 8px;
  bottom: 8px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
  opacity: 0;
  transform: translateY(4px);
  transition: var(--transition);
}
.card-cover:hover .card-play-overlay {
  opacity: 1;
  transform: translateY(0);
}

.card-name {
  font-size: 13px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  line-height: 1.4;
}
.card-artist {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 2px;
}

.playlist-card .card-name {
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  white-space: normal;
}
</style>
