<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { getBanner, getPersonalized, getNewSong } from '@/api/recommend'
import { useMusicStore } from '@/stores/music'

const musicStore = useMusicStore()

const banners = ref([])
const playlists = ref([])
const newSongs = ref([])
const loading = ref(true)

// 轮播（3 张堆叠效果）
const currentSlide = ref(0)
let slideTimer = null

const prevIndex = computed(() =>
  currentSlide.value === 0 ? banners.value.length - 1 : currentSlide.value - 1
)
const nextIndex = computed(() =>
  currentSlide.value === banners.value.length - 1 ? 0 : currentSlide.value + 1
)

function startSlide() {
  slideTimer = setInterval(() => {
    if (banners.value.length) {
      currentSlide.value = (currentSlide.value + 1) % banners.value.length
    }
  }, 4000)
}

function stopSlide() {
  if (slideTimer) {
    clearInterval(slideTimer)
    slideTimer = null
  }
}

function goToSlide(index) {
  if (index === currentSlide.value) return
  currentSlide.value = index
  stopSlide()
  startSlide()
}

function playAllNewSongs(index) {
  musicStore.playMultiple(newSongs.value, index)
}

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
    if (banners.value.length) startSlide()
  } catch (e) {
    console.error('获取推荐页数据失败', e)
  } finally {
    loading.value = false
  }
})

onUnmounted(() => {
  stopSlide()
})
</script>

<template>
  <div class="page-recommend">
    <!-- 加载中 -->
    <div v-if="loading" class="loading-state">
      <span>加载中...</span>
    </div>

    <template v-else>
      <!-- Banner 堆叠轮播 -->
      <section v-if="banners.length" class="carousel-section"
        @mouseenter="stopSlide"
        @mouseleave="startSlide"
      >
        <div class="carousel-stage">
          <div
            v-for="(banner, i) in banners"
            :key="banner.targetId || i"
            class="carousel-slide"
            :class="{ prev: i === prevIndex, current: i === currentSlide, next: i === nextIndex }"
            :style="{ backgroundImage: `url(${banner.imageUrl || banner.pic})` }"
          >
            <div class="slide-overlay"></div>
            <div class="slide-content">
              <h2>{{ banner.typeTitle }}</h2>
            </div>
          </div>
        </div>

        <!-- 指示点 -->
        <div class="carousel-dots">
          <span
            v-for="(banner, i) in banners"
            :key="i"
            class="dot"
            :class="{ active: i === currentSlide }"
            @click="goToSlide(i)"
          ></span>
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
          <div
            v-for="(item, i) in newSongs"
            :key="item.id"
            class="music-card"
            @click="playAllNewSongs(i)"
          >
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

/* ========== 堆叠轮播 ========== */
.carousel-section {
  margin-bottom: 36px;
  position: relative;
}

.carousel-stage {
  position: relative;
  height: 380px;
  overflow: hidden;
}

.carousel-slide {
  position: absolute;
  top: 50%;
  height: 80%;
  border-radius: 10px;
  background-size: cover;
  background-position: center;
  transition: all 0.6s cubic-bezier(0.4, 0, 0.2, 1);
  opacity: 0;
  pointer-events: none;
  overflow: hidden;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

/* 当前（中间，最大最前） */
.carousel-slide.current {
  left: 20%;
  width: 60%;
  z-index: 3;
  transform: translateY(-50%) scale(1);
  opacity: 1;
  pointer-events: auto;
  box-shadow: 0 8px 30px rgba(0, 0, 0, 0.25);
}

/* 前一张（左侧后方） */
.carousel-slide.prev {
  left: -5%;
  width: 34%;
  z-index: 2;
  transform: translateY(-50%) scale(0.85);
  opacity: 0.5;
  pointer-events: none;
}

/* 后一张（右侧后方） */
.carousel-slide.next {
  right: -5%;
  width: 34%;
  z-index: 2;
  transform: translateY(-50%) scale(0.85);
  opacity: 0.5;
  pointer-events: none;
}

.slide-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0, 0, 0, 0.3);
  border-radius: 10px;
}

.slide-content {
  position: relative;
  z-index: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  height: 100%;
  padding: 20px;
}

.slide-content h2 {
  font-size: 30px;
  font-weight: 700;
  color: var(--text-white);
  text-shadow: 0 2px 12px rgba(0, 0, 0, 0.5);
  text-align: center;
}

.carousel-dots {
  display: flex;
  justify-content: center;
  gap: 8px;
  margin-top: 14px;
}

.dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #ddd;
  cursor: pointer;
  transition: all 0.3s;
}

.dot.active {
  background: var(--color-primary);
  width: 24px;
  border-radius: 4px;
}

/* ========== 通用区块 ========== */
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

/* ========== 卡片网格 ========== */
.card-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 28px 24px;
}

.card-cover {
  position: relative;
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: 10px;
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
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.9);
  color: var(--color-primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  opacity: 0;
  transform: translateY(4px);
  transition: var(--transition);
}

.card-cover:hover .card-play-overlay {
  opacity: 1;
  transform: translateY(0);
}

.card-name {
  font-size: 14px;
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
