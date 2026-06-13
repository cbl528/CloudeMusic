<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { Icon } from '@iconify/vue'
import { useMusicStore } from '@/stores/music'

const store = useMusicStore()

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

const volumeIcon = computed(() => {
  if (store.volume === 0) return 'mdi:volume-off'
  if (store.volume < 0.4) return 'mdi:volume-low'
  if (store.volume < 0.7) return 'mdi:volume-medium'
  return 'mdi:volume-high'
})

// --- 歌曲信息提取 ---
const artistName = computed(() => {
  const s = store.currentSong
  if (!s) return ''
  const list = s.artists || []
  return list.map(a => a.name).join(' / ') || '未知歌手'
})

onMounted(() => {
  window.addEventListener('keydown', onKeydown)
})

onUnmounted(() => {
  window.removeEventListener('keydown', onKeydown)
  window.removeEventListener('mousemove', onProgressMouseMove)
  window.removeEventListener('mouseup', onProgressMouseUp)
})

function onKeydown(e) {
  if (e.target.tagName === 'INPUT' || e.target.tagName === 'TEXTAREA') return
  if (e.code === 'Space') {
    e.preventDefault()
    store.togglePlay()
  }
}

function handlePlay() {
  store.togglePlay()
}
</script>

<template>
  <footer class="player-bar">
    <template v-if="!store.currentSong">
      <!-- 空状态 -->
      <div class="player-empty">
        <Icon icon="mdi:music-note" width="28" height="28" />
        <span>选择一首歌曲开始播放</span>
      </div>
    </template>

    <template v-else>
      <div class="player-inner">
        <!-- 左侧：歌曲信息 -->
        <div class="player-song">
          <div class="cover-wrap">
            <img
              v-if="store.currentSong.cover"
              :src="store.currentSong.cover"
              :alt="store.currentSong.name"
              class="cover-img"
            />
            <div v-else class="cover-placeholder">
              <Icon icon="mdi:music-note" width="24" height="24" />
            </div>
          </div>
          <div class="song-info">
            <p class="song-name ellipsis">{{ store.currentSong.name }}</p>
            <p class="song-artist ellipsis">{{ artistName }}</p>
          </div>
        </div>

        <!-- 中间：控制 + 进度 -->
        <div class="player-center">
          <div class="controls-top">
            <button
              class="ctrl-btn"
              :title="store.playModeLabel"
              @click="store.togglePlayMode()"
            >
              <Icon
                v-if="store.playMode === 'order'"
                icon="mdi:repeat"
                width="18"
                height="18"
              />
              <Icon
                v-else-if="store.playMode === 'random'"
                icon="mdi:shuffle"
                width="18"
                height="18"
              />
              <Icon v-else icon="mdi:repeat-once" width="18" height="18" />
            </button>
            <button class="ctrl-btn" title="上一首" @click="store.prev()">
              <Icon icon="mdi:skip-previous" width="22" height="22" />
            </button>
            <button
              class="ctrl-btn play-btn"
              :title="store.playing ? '暂停' : '播放'"
              :class="{ loading: store.loading }"
              @click="handlePlay"
            >
              <Icon
                v-if="store.loading"
                icon="mdi:loading"
                width="22"
                height="22"
                class="spin"
              />
              <Icon
                v-else-if="store.playing"
                icon="mdi:pause"
                width="22"
                height="22"
              />
              <Icon v-else icon="mdi:play" width="22" height="22" />
            </button>
            <button class="ctrl-btn" title="下一首" @click="store.next()">
              <Icon icon="mdi:skip-next" width="22" height="22" />
            </button>
          </div>

          <div class="progress-area">
            <span class="time">{{ formatTime(displayCurrent) }}</span>
            <div
              ref="progressRef"
              class="progress-bar"
              :class="{ dragging: isDragging }"
              @mousedown="onProgressMouseDown"
            >
              <div class="progress-track">
                <div
                  class="progress-current"
                  :style="{ width: progressPercent + '%' }"
                ></div>
              </div>
              <div
                class="progress-thumb"
                :style="{ left: progressPercent + '%' }"
              ></div>
            </div>
            <span class="time">{{ formatTime(store.duration) }}</span>
          </div>

          <!-- 错误提示 -->
          <p v-if="store.errorMsg" class="error-tip">{{ store.errorMsg }}</p>
        </div>

        <!-- 右侧：音量等 -->
        <div class="player-extra">
          <div
            class="volume-wrap"
            @mouseenter="onVolumeEnter"
            @mouseleave="onVolumeLeave"
          >
            <button class="ctrl-btn" title="音量" @click="store.setVolume(store.volume > 0 ? 0 : 0.7)">
              <Icon :icon="volumeIcon" width="20" height="20" />
            </button>
            <div v-show="volumeShow" class="volume-slider-wrap">
              <input
                type="range"
                class="volume-slider"
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
    </template>
  </footer>
</template>

<style scoped>
.player-bar {
  height: var(--player-height);
  background: var(--bg-player);
  border-top: 1px solid var(--border-color);
  box-shadow: var(--shadow-player);
  flex-shrink: 0;
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
}

/* ---- 空状态 ---- */
.player-empty {
  display: flex;
  align-items: center;
  gap: 10px;
  color: var(--text-light);
  font-size: 14px;
}

/* ---- 主布局 ---- */
.player-inner {
  display: flex;
  align-items: center;
  width: 100%;
  height: 100%;
  padding: 0 24px;
  gap: 24px;
}

/* ---- 左侧歌曲信息 ---- */
.player-song {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 220px;
  flex-shrink: 0;
}

.cover-wrap {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  overflow: hidden;
  flex-shrink: 0;
  background: #f0f0f0;
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
  color: #ccc;
}

.song-info {
  flex: 1;
  min-width: 0;
}

.song-name {
  font-size: 14px;
  color: var(--text-primary);
}

.song-artist {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 2px;
}

.ellipsis {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ---- 中间控制区 ---- */
.player-center {
  flex: 1;
  max-width: 560px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.controls-top {
  display: flex;
  align-items: center;
  gap: 14px;
}

.ctrl-btn {
  background: none;
  border: none;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 4px;
  transition: var(--transition);
  line-height: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}
.ctrl-btn:hover {
  color: var(--text-primary);
}

.play-btn {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--text-white) !important;
  font-size: 16px;
}
.play-btn:hover {
  background: var(--color-primary-dark) !important;
}
.play-btn.loading {
  background: var(--color-primary-light) !important;
}

.spin {
  animation: rotate 1s linear infinite;
}
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}

/* ---- 进度条 ---- */
.progress-area {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  max-width: 500px;
}

.time {
  font-size: 11px;
  color: var(--text-light);
  min-width: 36px;
  text-align: center;
  font-variant-numeric: tabular-nums;
}

.progress-bar {
  flex: 1;
  height: 16px;
  display: flex;
  align-items: center;
  cursor: pointer;
  position: relative;
}

.progress-track {
  width: 100%;
  height: 4px;
  background: #e5e5e5;
  border-radius: 2px;
  overflow: hidden;
}

.progress-current {
  height: 100%;
  background: var(--color-primary);
  border-radius: 2px;
  transition: width 0.1s linear;
}

.progress-bar.dragging .progress-current {
  transition: none;
}

.progress-thumb {
  display: none;
}

/* ---- 错误提示 ---- */
.error-tip {
  font-size: 11px;
  color: var(--color-primary);
  margin-top: 2px;
}

/* ---- 右侧音量 ---- */
.player-extra {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 160px;
  flex-shrink: 0;
  justify-content: flex-end;
}

.volume-wrap {
  position: relative;
  display: flex;
  align-items: center;
}

.volume-slider-wrap {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 50%;
  transform: translateX(-50%);
  background: var(--bg-player);
  border: 1px solid var(--border-color);
  border-radius: var(--radius-md);
  padding: 8px 6px;
  box-shadow: var(--shadow-light);
  z-index: 200;
}

.volume-slider {
  writing-mode: vertical-lr;
  direction: rtl;
  width: 20px;
  height: 80px;
  cursor: pointer;
  accent-color: var(--color-primary);
}
</style>
