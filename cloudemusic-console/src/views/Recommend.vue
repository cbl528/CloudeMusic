<script setup>
const banners = [
  { id: 1, title: '发现好音乐', subtitle: '每一天都在这里' },
  { id: 2, title: '热门歌单推荐', subtitle: '为你精选' },
  { id: 3, title: '新歌首发', subtitle: '最新鲜的音乐' },
]

const playlists = Array.from({ length: 12 }, (_, i) => ({
  id: i + 1,
  name: `精品歌单 ${i + 1}`,
  playCount: Math.floor(Math.random() * 1000000),
  creator: `用户${i + 1}`,
}))
</script>

<template>
  <div class="page-recommend">
    <!-- Banner -->
    <section class="banner-section">
      <div class="banner-wrapper">
        <div
          v-for="banner in banners"
          :key="banner.id"
          class="banner-card"
          :style="{ background: `linear-gradient(135deg, var(--color-primary), var(--color-primary-dark))` }"
        >
          <div class="banner-content">
            <h2>{{ banner.title }}</h2>
            <p>{{ banner.subtitle }}</p>
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
      <div class="card-grid">
        <div v-for="pl in playlists" :key="pl.id" class="playlist-card">
          <div class="card-cover">
            <div class="card-cover-placeholder">♪</div>
            <span class="play-count">▶ {{ (pl.playCount / 10000).toFixed(0) }}万</span>
            <div class="card-play-overlay">▶</div>
          </div>
          <p class="card-name">{{ pl.name }}</p>
        </div>
      </div>
    </section>

    <!-- 最新音乐 -->
    <section class="section">
      <div class="section-header">
        <h3 class="section-title">最新音乐</h3>
        <a href="#" class="section-more">更多 &gt;</a>
      </div>
      <div class="card-grid">
        <div v-for="i in 8" :key="i" class="music-card">
          <div class="card-cover">
            <div class="card-cover-placeholder small">♪</div>
            <div class="card-play-overlay">▶</div>
          </div>
          <p class="card-name">最新歌曲 {{ i }}</p>
          <p class="card-artist">歌手 {{ i }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<style scoped>
.page-recommend {
  padding: 24px 32px;
  max-width: 1100px;
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
  display: flex;
  align-items: center;
  justify-content: center;
  cursor: pointer;
  transition: var(--transition);
}
.banner-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 16px rgba(236, 65, 65, 0.3);
}
.banner-content {
  text-align: center;
  color: var(--text-white);
}
.banner-content h2 {
  font-size: 22px;
  font-weight: 700;
  margin-bottom: 6px;
}
.banner-content p {
  font-size: 14px;
  opacity: 0.85;
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
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 20px;
}

.card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: 8px;
  cursor: pointer;
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
