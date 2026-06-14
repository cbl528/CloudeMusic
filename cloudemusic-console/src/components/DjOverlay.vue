<script setup>
import { ref, watch, computed, onUnmounted } from 'vue'
import { useMusicStore } from '@/stores/music'

const store = useMusicStore()

const visible = ref(false)
const countdown = ref(0)
let countdownTimer = null

// 监听播放进度，超过 60% 且未触发推荐时请求 DJ
watch([() => store.currentTime, () => store.duration], ([time, dur]) => {
  if (!dur || dur <= 0) return
  if (time / dur > 0.6 && !visible.value && !store.djRecommendation) {
    store.requestDjRecommendation()
  }
})

// 当 DJ 推荐结果就绪（有解说词且有推荐歌曲）时显示浮层
watch(() => store.djRecommendation, (val) => {
  if (val?.commentary && val?.next_song) {
    visible.value = true
    startCountdown()
  }
})

function startCountdown() {
  stopCountdown()
  countdown.value = 10
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      stopCountdown()
      playNow()
    }
  }, 1000)
}

function stopCountdown() {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

function playNow() {
  stopCountdown()
  if (store.djRecommendation?.next_song) {
    const next = store.djRecommendation.next_song
    // 构造兼容 normalizeSong 格式的歌曲数据
    store.play({
      id: next.id,
      name: next.name,
      artists: next.artists ? next.artists.split('/').map(n => ({ name: n.trim() })) : [],
      cover: next.cover || '',
      duration: next.duration || 0,
    })
  }
  visible.value = false
}

function dismiss() {
  stopCountdown()
  visible.value = false
}

function formatArtists(artists) {
  if (!artists) return ''
  return artists.split('/').join(' / ')
}

onUnmounted(() => {
  stopCountdown()
})
</script>

<template>
  <Transition name="dj">
    <div v-if="visible && store.djRecommendation" class="dj-overlay">
      <div class="dj-avatar">
        <svg width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
          <circle cx="12" cy="12" r="10" />
          <path d="M12 16c-2.5 0-4-1.5-4-3s1.5-3 4-3 4 1.5 4 3-1.5 3-4 3z" />
          <line x1="12" y1="6" x2="12" y2="8" />
        </svg>
      </div>
      <div class="dj-body">
        <div class="dj-label">
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" class="dj-icon">
            <polygon points="5 3 19 12 5 21 5 3" />
          </svg>
          AI 电台
        </div>
        <p class="dj-commentary">{{ store.djRecommendation.commentary }}</p>
        <div class="dj-next">
          <span class="dj-next-label">接下来播放</span>
          <strong class="dj-next-name">{{ store.djRecommendation.next_song.name }}</strong>
          <span class="dj-next-artist">{{ formatArtists(store.djRecommendation.next_song.artists) }}</span>
        </div>
        <div class="dj-actions">
          <button class="dj-btn-play" @click="playNow">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="currentColor"><polygon points="5 3 19 12 5 21 5 3" /></svg>
            立即播放
          </button>
          <span v-if="countdown > 0" class="dj-countdown">{{ countdown }}s</span>
          <button class="dj-btn-skip" @click="dismiss">忽略</button>
        </div>
      </div>
    </div>
  </Transition>
</template>

<style scoped>
.dj-overlay {
  position: fixed;
  bottom: 80px;
  left: 240px;
  z-index: 1000;
  display: flex;
  align-items: flex-start;
  gap: 12px;
  max-width: 420px;
  padding: 16px;
  background: var(--bg-content, #fff);
  border: 1px solid var(--border-color, #e5e5e5);
  border-radius: 12px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
}

.dj-avatar {
  flex-shrink: 0;
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, var(--color-primary, #ec4141), #ff6b6b);
  border-radius: 50%;
  color: #fff;
}

.dj-body {
  flex: 1;
  min-width: 0;
}

.dj-label {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 12px;
  font-weight: 600;
  color: var(--color-primary, #ec4141);
  margin-bottom: 6px;
}

.dj-icon {
  margin-right: 2px;
}

.dj-commentary {
  margin: 0 0 8px;
  font-size: 13px;
  line-height: 1.6;
  color: var(--text-primary, #333);
}

.dj-next {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 4px 8px;
  padding: 8px 10px;
  background: var(--bg-secondary, #f5f5f5);
  border-radius: 8px;
  font-size: 12px;
  margin-bottom: 10px;
}

.dj-next-label {
  color: var(--text-secondary, #888);
}

.dj-next-name {
  color: var(--text-primary, #333);
}

.dj-next-artist {
  color: var(--text-secondary, #888);
}

.dj-actions {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dj-btn-play {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 6px 14px;
  font-size: 12px;
  font-weight: 500;
  color: #fff;
  background: var(--color-primary, #ec4141);
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: opacity 0.2s;
}

.dj-btn-play:hover {
  opacity: 0.85;
}

.dj-countdown {
  font-size: 12px;
  color: var(--text-secondary, #888);
  min-width: 24px;
  text-align: center;
}

.dj-btn-skip {
  padding: 4px 10px;
  font-size: 12px;
  color: var(--text-secondary, #888);
  background: transparent;
  border: 1px solid var(--border-color, #e5e5e5);
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
}

.dj-btn-skip:hover {
  color: var(--text-primary, #333);
  border-color: #ccc;
}

/* 进出动画 */
.dj-enter-active,
.dj-leave-active {
  transition: all 0.35s ease;
}

.dj-enter-from {
  opacity: 0;
  transform: translateX(-20px);
}

.dj-leave-to {
  opacity: 0;
  transform: translateX(-20px);
}
</style>
