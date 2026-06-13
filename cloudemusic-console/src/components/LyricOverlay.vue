<script setup>
import { ref, computed, watch, onMounted, onUnmounted, nextTick } from 'vue'
import { Icon } from '@iconify/vue'
import { useMusicStore } from '@/stores/music'
import { getLyric } from '@/api/song'

const emit = defineEmits(['close'])
const store = useMusicStore()

const loading = ref(false)
const noLyric = ref(false)
const lyrics = ref([])
const lyricContainer = ref(null)

// --- 进度条拖拽 ---
const progressRef = ref(null)
const isDragging = ref(false)
const dragTime = ref(0)

const displayCurrent = computed(() =>
  isDragging.value ? dragTime.value : store.currentTime
)
const progressPercent = computed(() => {
  const dur = store.duration
  if (!dur) return 0
  return Math.min(100, (displayCurrent.value / dur) * 100)
})

function formatTime(seconds) {
  if (!seconds || !isFinite(seconds)) return '00:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

function onProgressMouseDown(e) {
  isDragging.value = true
  updateDragTime(e)
  window.addEventListener('mousemove', onProgressMouseMove)
  window.addEventListener('mouseup', onProgressMouseUp)
}

function onProgressMouseMove(e) {
  updateDragTime(e)
}

function onProgressMouseUp(e) {
  window.removeEventListener('mousemove', onProgressMouseMove)
  window.removeEventListener('mouseup', onProgressMouseUp)
  isDragging.value = false
  store.seek(dragTime.value)
}

function updateDragTime(e) {
  const rect = progressRef.value?.getBoundingClientRect()
  if (!rect) return
  const ratio = Math.max(0, Math.min(1, (e.clientX - rect.left) / rect.width))
  dragTime.value = ratio * store.duration
}

// --- 音量 ---
const volumeShow = ref(false)
let volumeHideTimer = null

function onVolumeEnter() {
  clearTimeout(volumeHideTimer)
  volumeShow.value = true
}

function onVolumeLeave() {
  volumeHideTimer = setTimeout(() => {
    volumeShow.value = false
  }, 1000)
}

// --- 歌词 ---
watch(() => store.currentSong?.id, (newId) => {
  if (newId) fetchLyric(newId)
})

onMounted(() => {
  if (store.currentSong?.id) fetchLyric(store.currentSong.id)
})

onUnmounted(() => {
  window.removeEventListener('mousemove', onProgressMouseMove)
  window.removeEventListener('mouseup', onProgressMouseUp)
})

async function fetchLyric(id) {
  if (!id) { noLyric.value = true; return }
  loading.value = true
  noLyric.value = false
  lyrics.value = []
  try {
    const res = await getLyric(id)
    const lrcText = res.lrc?.lyric || ''
    if (!lrcText.trim()) {
      noLyric.value = true
      return
    }
    lyrics.value = parseLrc(lrcText)
    if (!lyrics.value.length) noLyric.value = true
  } catch (e) {
    console.error('获取歌词失败', e)
    noLyric.value = true
  } finally {
    loading.value = false
  }
}

function parseLrc(text) {
  const lines = text.split('\n')
  const result = []
  const regex = /\[(\d{2}):(\d{2})\.(\d{2,3})\](.*)/
  for (const line of lines) {
    const match = line.match(regex)
    if (match) {
      const min = parseInt(match[1])
      const sec = parseInt(match[2])
      const msStr = match[3].padEnd(3, '0')
      const ms = parseInt(msStr)
      const time = min * 60 + sec + ms / 1000
      const text = match[4].trim()
      if (text) result.push({ time, text })
    }
  }
  return result.sort((a, b) => a.time - b.time)
}

const currentLineIndex = computed(() => {
  const ct = store.currentTime
  const list = lyrics.value
  if (!list.length) return -1
  let idx = -1
  for (let i = 0; i < list.length; i++) {
    if (list[i].time <= ct) idx = i
    else break
  }
  return idx
})

watch(currentLineIndex, (idx) => {
  if (idx < 0) return
  nextTick(() => {
    const container = lyricContainer.value
    if (!container) return
    const activeEl = container.querySelector('.lyric-line.active')
    if (activeEl) {
      activeEl.scrollIntoView({ block: 'center', behavior: 'smooth' })
    }
  })
})

function onBackdropClick(e) {
  if (e.target === e.currentTarget) emit('close')
}

function onKeydown(e) {
  if (e.key === 'Escape') emit('close')
}

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
})
</script>

<template>
  <div class="lyric-overlay" @click="onBackdropClick">
    <button class="close-btn" @click="emit('close')">✕</button>

    <!-- 主区域：封面 + 歌词 -->
    <div class="lyric-main">
      <!-- 左侧封面 -->
      <div class="lyric-left" v-if="store.currentSong">
        <div class="cover-wrap">
          <img
            v-if="store.currentSong.cover"
            :src="store.currentSong.cover + '?param=220y220'"
            :alt="store.currentSong.name"
            class="cover-img"
          />
          <div v-else class="cover-placeholder">
            <Icon icon="mdi:music-note" width="48" height="48" />
          </div>
        </div>
        <p class="meta-name">{{ store.currentSong.name }}</p>
        <p class="meta-artist">{{ store.currentSong.artists?.map?.(a => a.name).join(' / ') || '' }}</p>
      </div>

      <!-- 右侧歌词 -->
      <div class="lyric-right">
        <div v-if="loading" class="lyric-status">歌词加载中...</div>
        <div v-else-if="noLyric" class="lyric-status">暂无歌词</div>
        <div v-else ref="lyricContainer" class="lyric-list">
          <div
            v-for="(line, i) in lyrics"
            :key="i"
            class="lyric-line"
            :class="{ active: i === currentLineIndex, passed: i < currentLineIndex }"
          >
            {{ line.text }}
          </div>
        </div>
      </div>
    </div>

    <!-- 底部播放控制栏 -->
    <div class="lyric-controls" @click.stop>
      <!-- 进度条 -->
      <div class="ctrl-progress">
        <span class="ctrl-time">{{ formatTime(displayCurrent) }}</span>
        <div
          ref="progressRef"
          class="ctrl-progress-bar"
          :class="{ dragging: isDragging }"
          @mousedown="onProgressMouseDown"
        >
          <div class="ctrl-progress-track">
            <div class="ctrl-progress-current" :style="{ width: progressPercent + '%' }"></div>
          </div>
          <div class="ctrl-progress-thumb" :style="{ left: progressPercent + '%' }"></div>
        </div>
        <span class="ctrl-time">{{ formatTime(store.duration) }}</span>
      </div>

      <!-- 按钮 -->
      <div class="ctrl-buttons">
        <button class="ctrl-btn" :title="store.playModeLabel" @click="store.togglePlayMode()">
          <Icon v-if="store.playMode === 'order'" icon="mdi:repeat" width="20" height="20" />
          <Icon v-else-if="store.playMode === 'random'" icon="mdi:shuffle" width="20" height="20" />
          <Icon v-else icon="mdi:repeat-once" width="20" height="20" />
        </button>
        <button class="ctrl-btn" title="上一首" @click="store.prev()">
          <Icon icon="mdi:skip-previous" width="28" height="28" />
        </button>
        <button
          class="ctrl-btn ctrl-play"
          :class="{ loading: store.loading }"
          :title="store.playing ? '暂停' : '播放'"
          @click="store.togglePlay()"
        >
          <Icon v-if="store.loading" icon="mdi:loading" width="28" height="28" class="spin" />
          <Icon v-else-if="store.playing" icon="mdi:pause-circle" width="40" height="40" />
          <Icon v-else icon="mdi:play-circle" width="40" height="40" />
        </button>
        <button class="ctrl-btn" title="下一首" @click="store.next()">
          <Icon icon="mdi:skip-next" width="28" height="28" />
        </button>
        <!-- 音量 -->
        <div
          class="lyric-volume-wrap"
          @mouseenter="onVolumeEnter"
          @mouseleave="onVolumeLeave"
        >
          <button class="ctrl-btn" title="音量" @click="store.setVolume(store.volume > 0 ? 0 : 0.7)">
            <Icon v-if="store.volume === 0" icon="mdi:volume-off" width="20" height="20" />
            <Icon v-else icon="mdi:volume-high" width="20" height="20" />
          </button>
          <div v-show="volumeShow" class="lyric-volume-slider-wrap">
            <input
              type="range"
              class="lyric-volume-slider"
              min="0"
              max="1"
              step="0.05"
              :value="store.volume"
              @input="store.setVolume(Number($event.target.value))"
            />
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.lyric-overlay {
  position: fixed;
  inset: 0;
  z-index: 1000;
  background: rgba(0, 0, 0, 0.87);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  backdrop-filter: blur(12px);
  user-select: none;
}

.close-btn {
  position: absolute;
  top: 20px;
  right: 24px;
  background: none;
  border: none;
  color: rgba(255, 255, 255, 0.5);
  font-size: 24px;
  cursor: pointer;
  transition: color 0.2s;
  line-height: 1;
  padding: 8px;
  z-index: 10;
}
.close-btn:hover {
  color: #fff;
}

/* ---- 主区域：左封面 + 右歌词 ---- */
.lyric-main {
  flex: 1;
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: center;
  width: 100%;
  max-width: 1050px;
  padding: 80px 48px 20px;
  min-height: 0;
  gap: 60px;
}

/* 左侧封面 */
.lyric-left {
  display: flex;
  flex-direction: column;
  align-items: center;
  flex-shrink: 0;
  width: 220px;
}

.cover-wrap {
  width: 200px;
  height: 200px;
  border-radius: 12px;
  overflow: hidden;
  flex-shrink: 0;
  box-shadow: 0 8px 40px rgba(0, 0, 0, 0.5);
  margin-bottom: 16px;
  background: rgba(255, 255, 255, 0.06);
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
  color: rgba(255, 255, 255, 0.3);
}

.meta-name {
  font-size: 16px;
  color: #fff;
  font-weight: 600;
  margin-bottom: 4px;
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.meta-artist {
  font-size: 13px;
  color: rgba(255, 255, 255, 0.45);
  text-align: center;
  width: 100%;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 右侧歌词 */
.lyric-right {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  max-width: 600px;
  align-self: stretch;
}

.lyric-status {
  color: rgba(255, 255, 255, 0.4);
  font-size: 15px;
}

.lyric-list {
  width: 100%;
  max-height: 55vh;
  overflow-y: auto;
  padding: 8px 0 8px 24px;
  scroll-behavior: smooth;
  scrollbar-width: none;
}
.lyric-list::-webkit-scrollbar {
  display: none;
}

.lyric-line {
  font-size: 16px;
  line-height: 2.6;
  color: rgba(255, 255, 255, 0.3);
  text-align: center;
  transition: color 0.35s, font-size 0.3s;
  padding: 3px 0;
}
.lyric-line.passed {
  color: rgba(255, 255, 255, 0.45);
}
.lyric-line.active {
  color: #fff;
  font-size: 19px;
  font-weight: 600;
}

/* ---- 底部控制栏 ---- */
.lyric-controls {
  width: 100%;
  max-width: 680px;
  padding: 0 48px 32px;
  flex-shrink: 0;
}

/* 进度条 */
.ctrl-progress {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 18px;
}
.ctrl-time {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.5);
  min-width: 36px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}
.ctrl-progress-bar {
  flex: 1;
  height: 20px;
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
}
.ctrl-progress-track {
  width: 100%;
  height: 4px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 2px;
  overflow: hidden;
}
.ctrl-progress-current {
  height: 100%;
  background: var(--color-primary);
  border-radius: 2px;
  transition: width 0.1s linear;
}
.ctrl-progress-bar.dragging .ctrl-progress-current {
  transition: none;
}
.ctrl-progress-thumb {
  position: absolute;
  top: 50%;
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: var(--color-primary);
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.15s;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.3);
}
.ctrl-progress-bar:hover .ctrl-progress-thumb {
  opacity: 1;
}
.ctrl-progress-bar.dragging .ctrl-progress-thumb {
  opacity: 1;
}

/* 按钮行 */
.ctrl-buttons {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 20px;
}

.ctrl-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: rgba(255, 255, 255, 0.6);
  padding: 4px;
  transition: color 0.2s;
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ctrl-btn:hover {
  color: #fff;
}

.ctrl-play {
  color: rgba(255, 255, 255, 0.9);
}
.ctrl-play:hover {
  color: #fff;
}
.ctrl-play.loading {
  opacity: 0.7;
}

.spin {
  animation: rotate 1s linear infinite;
}
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* 音量滑块 */
.lyric-volume-wrap {
  position: relative;
  display: flex;
  align-items: center;
}
.lyric-volume-slider-wrap {
  position: absolute;
  bottom: calc(100% + 12px);
  left: 50%;
  transform: translateX(-50%);
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(255, 255, 255, 0.12);
  border-radius: 8px;
  padding: 8px 6px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
  z-index: 10;
}
.lyric-volume-slider {
  writing-mode: vertical-lr;
  direction: rtl;
  width: 20px;
  height: 80px;
  cursor: pointer;
  accent-color: var(--color-primary);
}
</style>
