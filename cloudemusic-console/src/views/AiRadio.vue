<script setup>
import { ref, watch, onUnmounted, computed } from 'vue'
import { Icon } from '@iconify/vue'
import { useMusicStore } from '@/stores/music'

const musicStore = useMusicStore()

const loading = ref(false)
const error = ref('')
const excludedIds = ref([])

function currentArtistNames() {
  const s = musicStore.currentSong
  if (!s?.artists) return ''
  return s.artists.map(a => a.name || a).join(' / ')
}

function currentCover(param = '128y128') {
  const s = musicStore.currentSong
  if (!s?.cover) return ''
  return s.cover + '?param=' + param
}

const progressPercent = computed(() => {
  if (!musicStore.duration || musicStore.duration <= 0) return 0
  return Math.min(100, (musicStore.currentTime / musicStore.duration) * 100)
})

function formatTime(seconds) {
  if (!seconds || seconds <= 0) return '0:00'
  const m = Math.floor(seconds / 60)
  const s = Math.floor(seconds % 60)
  return `${m}:${String(s).padStart(2, '0')}`
}

async function fetchRecommendation() {
  if (!musicStore.currentSong) return
  loading.value = true
  error.value = ''
  await musicStore.requestAiRadioRecommendation(excludedIds.value)
  loading.value = false
  if (!musicStore.aiRadioRecommendation?.next_song) {
    error.value = '暂未找到情感匹配的歌曲，继续播放更多歌曲后重试'
  }
}

function toggleRadio() {
  musicStore.toggleAiRadio()
  if (!musicStore.aiRadioEnabled) {
    cancelSpeech()
    error.value = ''
    excludedIds.value = []
  } else {
    fetchRecommendation()
  }
}

function skipRecommendation() {
  const prev = musicStore.aiRadioRecommendation?.next_song
  if (prev?.id) excludedIds.value.push(prev.id)
  musicStore.aiRadioRecommendation = null
  fetchRecommendation()
}

function playNow() {
  cancelSpeech()
  musicStore.playAiRadioNext()
}

// ---- TTS ----
let currentUtterance = null

function cancelSpeech() {
  if (currentUtterance) {
    currentUtterance.onend = null
    currentUtterance.onerror = null
    currentUtterance = null
  }
  if (window.speechSynthesis) window.speechSynthesis.cancel()
}

function speakCommentary() {
  const commentary = musicStore.aiRadioRecommendation?.commentary
  if (!commentary || !window.speechSynthesis) {
    musicStore.playAiRadioNext()
    return
  }
  cancelSpeech()
  const utterance = new SpeechSynthesisUtterance(commentary)
  utterance.lang = 'zh-CN'
  utterance.rate = 1.15
  utterance.onend = () => { currentUtterance = null; musicStore.playAiRadioNext() }
  utterance.onerror = () => { currentUtterance = null; musicStore.playAiRadioNext() }
  currentUtterance = utterance
  window.speechSynthesis.speak(utterance)
}

watch(() => musicStore.aiRadioPending, (val) => {
  if (val && musicStore.aiRadioEnabled) speakCommentary()
})

watch(() => musicStore.currentSong?.id, (newId, oldId) => {
  if (musicStore.aiRadioEnabled && newId && newId !== oldId) {
    excludedIds.value = []
    fetchRecommendation()
  }
})

onUnmounted(() => cancelSpeech())
</script>

<template>
  <div class="page-ai-radio">
    <!-- Header -->
    <div class="page-header">
      <div>
        <h2>AI 电台</h2>
        <p class="page-desc">情感接续播放，AI 为你解说</p>
      </div>
      <button
        class="toggle-btn"
        :class="{ active: musicStore.aiRadioEnabled }"
        @click="toggleRadio"
      >
        <Icon :icon="musicStore.aiRadioEnabled ? 'mdi:radio' : 'mdi:radio-off'" width="20" />
        {{ musicStore.aiRadioEnabled ? '已开启' : '未开启' }}
      </button>
    </div>

    <!-- ====== 关闭状态 ====== -->
    <div v-if="!musicStore.aiRadioEnabled" class="off-content">
      <div class="hero-banner">
        <div class="hero-visual">
          <div class="hero-circle"></div>
          <Icon icon="mdi:radio" class="hero-icon" />
        </div>
        <h3>打开 AI 电台，让音乐更有温度</h3>
        <p>AI 会分析当前歌曲的歌词情感，推荐同类型的下一首，<br>并用温暖的人声为你朗读接续解说词。</p>
        <button class="btn-start" @click="toggleRadio">
          <Icon icon="mdi:power" width="18" />
          立即开启 AI 电台
        </button>
      </div>
      <div class="feature-grid">
        <div class="feature-card">
          <div class="feature-icon-box emotion">
            <Icon icon="mdi:waveform" width="24" />
          </div>
          <strong>歌词情感分析</strong>
          <span>分析歌词情感标签与强度</span>
        </div>
        <div class="feature-card">
          <div class="feature-icon-box match">
            <Icon icon="mdi:playlist-music" width="24" />
          </div>
          <strong>情感相似推荐</strong>
          <span>基于情感向量匹配同类型歌曲</span>
        </div>
        <div class="feature-card">
          <div class="feature-icon-box voice">
            <Icon icon="mdi:account-voice" width="24" />
          </div>
          <strong>AI 语音解说</strong>
          <span>浏览器 TTS 朗读接续解说词</span>
        </div>
      </div>
    </div>

    <!-- ====== 已开启，无播放歌曲 ====== -->
    <div v-else-if="!musicStore.currentSong" class="empty-state">
      <div class="empty-visual">
        <Icon icon="mdi:music-off" width="72" class="empty-icon" />
      </div>
      <p class="empty-text">请先播放一首歌曲</p>
      <p class="empty-desc">AI 电台需要正在播放的歌曲来进行情感分析</p>
    </div>

    <!-- ====== 已开启，有播放歌曲 ====== -->
    <template v-else>
      <!-- 当前歌曲（大卡片） -->
      <div class="now-playing-card">
        <div class="np-cover-wrap">
          <img v-if="currentCover()" :src="currentCover()" class="np-cover" />
          <div class="np-cover-overlay">
            <Icon icon="mdi:music-note" width="28" />
          </div>
        </div>
        <div class="np-info">
          <span class="np-badge">正在播放</span>
          <h3 class="np-name">{{ musicStore.currentSong.name }}</h3>
          <span class="np-artist">{{ currentArtistNames() }}</span>
          <div class="np-progress">
            <div class="np-time">
              <span>{{ formatTime(musicStore.currentTime) }}</span>
              <span>{{ formatTime(musicStore.duration) }}</span>
            </div>
            <div class="np-bar-bg">
              <div class="np-bar-fill" :style="{ width: progressPercent + '%' }"></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 加载中 -->
      <div v-if="loading" class="loading-section">
        <div class="loading-animation">
          <span class="dot-pulse"></span>
        </div>
        <p>AI 正在分析歌曲情感...</p>
      </div>

      <!-- 推荐结果 -->
      <div v-else-if="musicStore.aiRadioRecommendation?.next_song" class="result-area">
        <!-- 解说词 -->
        <div class="commentary-card">
          <Icon icon="mdi:format-quote-open" width="32" class="quote-icon" />
          <p>{{ musicStore.aiRadioRecommendation.commentary }}</p>
        </div>

        <!-- 情感接续指示 -->
        <div class="emotion-bridge">
          <span class="bridge-label">情感接续</span>
          <div class="bridge-line">
            <span class="bridge-dot start"></span>
            <span class="bridge-dash"></span>
            <span class="bridge-dot end"></span>
          </div>
          <div class="bridge-tags">
            <span class="tag from">当前歌曲</span>
            <Icon icon="mdi:arrow-right" width="16" />
            <span class="tag to">{{ musicStore.aiRadioRecommendation.next_song.reason || '情感相近' }}</span>
          </div>
        </div>

        <!-- 下一首 -->
        <div class="next-card">
          <div class="nc-cover-wrap">
            <img
              v-if="musicStore.aiRadioRecommendation.next_song.cover"
              :src="musicStore.aiRadioRecommendation.next_song.cover + '?param=80y80'"
              class="nc-cover"
            />
            <div v-else class="nc-cover-placeholder">
              <Icon icon="mdi:music" width="28" />
            </div>
          </div>
          <div class="nc-body">
            <span class="nc-label">接下来播放</span>
            <strong class="nc-name">{{ musicStore.aiRadioRecommendation.next_song.name }}</strong>
            <span class="nc-artist">{{ musicStore.aiRadioRecommendation.next_song.artists }}</span>
          </div>
          <div class="nc-actions">
            <button class="btn-play-now" @click="playNow">
              <Icon icon="mdi:play" width="16" />
              立即播放
            </button>
            <button class="btn-skip" @click="skipRecommendation">换一首</button>
          </div>
        </div>

        <!-- 状态栏 + TTS 动画 -->
        <div class="status-bar" :class="{ active: musicStore.aiRadioPending }">
          <div class="status-left">
            <div v-if="musicStore.aiRadioPending" class="equalizer">
              <span class="eq-bar" v-for="i in 4" :key="i"></span>
            </div>
            <Icon v-else icon="mdi:clock-outline" width="16" />
            <span v-if="musicStore.aiRadioPending">正在播放 AI 解说词...</span>
            <span v-else>当前歌曲播放结束后自动播放解说词</span>
          </div>
        </div>
      </div>

      <!-- 错误 -->
      <div v-else-if="error" class="error-block">
        <Icon icon="mdi:alert-circle-outline" width="40" />
        <p>{{ error }}</p>
        <button class="btn-retry" @click="fetchRecommendation">重试</button>
      </div>
    </template>
  </div>
</template>

<style scoped>
.page-ai-radio {
  padding: 32px 48px 64px;
  width: 100%;
  max-width: 820px;
  margin: 0 auto;
}

/* ====== Header ====== */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 32px;
}
.page-header h2 {
  font-size: 24px;
  font-weight: 700;
  margin-bottom: 4px;
}
.page-desc {
  font-size: 13px;
  color: var(--text-light);
}

.toggle-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border: 2px solid var(--border-color);
  border-radius: 22px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  background: transparent;
  color: var(--text-secondary);
  transition: all 0.25s;
}
.toggle-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.toggle-btn.active {
  background: var(--color-primary);
  border-color: var(--color-primary);
  color: #fff;
}

/* ====== 关闭状态 ====== */
.off-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.hero-banner {
  text-align: center;
  padding: 60px 40px 48px;
  background: linear-gradient(135deg, #fef0f0 0%, #fff8f0 50%, #fff 100%);
  border-radius: 16px;
}
.hero-visual {
  position: relative;
  width: 96px;
  height: 96px;
  margin: 0 auto 20px;
  display: flex;
  align-items: center;
  justify-content: center;
}
.hero-circle {
  position: absolute;
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, var(--color-primary), #ff8a8a);
  opacity: 0.12;
  animation: heroPulse 3s ease-in-out infinite;
}
.hero-icon {
  font-size: 44px;
  color: var(--color-primary);
  position: relative;
}
.hero-banner h3 {
  font-size: 20px;
  font-weight: 700;
  margin-bottom: 10px;
}
.hero-banner p {
  font-size: 14px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 24px;
}
.btn-start {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 12px 32px;
  background: var(--color-primary);
  color: #fff;
  border: none;
  border-radius: 24px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: background 0.2s;
}
.btn-start:hover {
  background: var(--color-primary-dark);
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 20px;
}
.feature-card {
  display: flex;
  flex-direction: column;
  align-items: center;
  text-align: center;
  gap: 8px;
  padding: 28px 20px;
  background: var(--bg-content);
  border-radius: 12px;
  box-shadow: var(--shadow-light);
}
.feature-card strong {
  font-size: 14px;
  color: var(--text-primary);
}
.feature-card span {
  font-size: 12px;
  color: var(--text-light);
  line-height: 1.5;
}
.feature-icon-box {
  width: 48px;
  height: 48px;
  border-radius: 14px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 4px;
}
.feature-icon-box.emotion { background: #fef0f0; color: var(--color-primary); }
.feature-icon-box.match { background: #f0f7ff; color: #2979ff; }
.feature-icon-box.voice { background: #f5f0ff; color: #7c4dff; }

/* ====== 空状态（无歌曲） ====== */
.empty-state {
  text-align: center;
  padding: 100px 0;
}
.empty-visual {
  margin-bottom: 20px;
}
.empty-icon {
  color: #e0e0e0;
}
.empty-text {
  font-size: 17px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.empty-desc {
  font-size: 13px;
  color: var(--text-light);
}

/* ====== 当前歌曲卡片 ====== */
.now-playing-card {
  display: flex;
  gap: 24px;
  padding: 24px;
  background: var(--bg-content);
  border-radius: 14px;
  box-shadow: var(--shadow-light);
  margin-bottom: 24px;
}
.np-cover-wrap {
  position: relative;
  width: 128px;
  height: 128px;
  flex-shrink: 0;
  border-radius: 10px;
  overflow: hidden;
}
.np-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.np-cover-overlay {
  position: absolute;
  inset: 0;
  background: rgba(0,0,0,0.04);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: opacity 0.3s;
}
.np-cover-wrap:hover .np-cover-overlay {
  opacity: 1;
}
.np-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 4px;
  min-width: 0;
}
.np-badge {
  display: inline-block;
  width: fit-content;
  padding: 2px 10px;
  font-size: 11px;
  font-weight: 600;
  color: var(--color-primary);
  background: #fef0f0;
  border-radius: 10px;
  margin-bottom: 4px;
  letter-spacing: 0.5px;
}
.np-name {
  font-size: 20px;
  font-weight: 700;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.np-artist {
  font-size: 14px;
  color: var(--text-secondary);
  margin-bottom: 8px;
}
.np-progress {
  width: 100%;
}
.np-time {
  display: flex;
  justify-content: space-between;
  font-size: 11px;
  color: var(--text-light);
  margin-bottom: 4px;
}
.np-bar-bg {
  width: 100%;
  height: 3px;
  background: #eee;
  border-radius: 2px;
  overflow: hidden;
}
.np-bar-fill {
  height: 100%;
  background: var(--color-primary);
  border-radius: 2px;
  transition: width 0.5s linear;
}

/* ====== 加载 ====== */
.loading-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  padding: 48px 0;
  color: var(--text-secondary);
  font-size: 14px;
}
.loading-animation {
  display: flex;
  gap: 6px;
}
.dot-pulse {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--color-primary);
  animation: dotPulse 1.2s ease-in-out infinite;
  box-shadow: 0 0 0 0 rgba(236,65,65,0.4);
}

/* ====== 结果区域 ====== */
.result-area {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

/* 解说词 */
.commentary-card {
  position: relative;
  padding: 24px 28px 24px 56px;
  background: linear-gradient(135deg, #fef0f0 0%, #fff5f5 100%);
  border-radius: 12px;
  line-height: 1.9;
  font-size: 15px;
  color: var(--text-primary);
}
.quote-icon {
  position: absolute;
  left: 16px;
  top: 20px;
  color: var(--color-primary);
  opacity: 0.25;
}
.commentary-card p {
  margin: 0;
}

/* 情感接续 */
.emotion-bridge {
  text-align: center;
  padding: 8px 0;
}
.bridge-label {
  font-size: 11px;
  font-weight: 600;
  color: var(--text-light);
  letter-spacing: 2px;
  text-transform: uppercase;
}
.bridge-line {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin: 8px 0;
}
.bridge-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}
.bridge-dot.start { background: var(--color-primary); }
.bridge-dot.end { background: #ff8a8a; }
.bridge-dash {
  flex: 0 1 80px;
  height: 2px;
  background: linear-gradient(90deg, var(--color-primary), #ff8a8a);
  border-radius: 1px;
}
.bridge-tags {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  font-size: 12px;
  color: var(--text-light);
}
.tag {
  padding: 3px 12px;
  border-radius: 10px;
  font-size: 12px;
}
.tag.from {
  background: #fef0f0;
  color: var(--color-primary);
}
.tag.to {
  background: #fff0f0;
  color: #ff6b6b;
}

/* 下一首卡片 */
.next-card {
  display: flex;
  align-items: center;
  gap: 18px;
  padding: 20px 24px;
  background: var(--bg-content);
  border-radius: 12px;
  box-shadow: var(--shadow-light);
}
.nc-cover-wrap {
  width: 72px;
  height: 72px;
  flex-shrink: 0;
  border-radius: 8px;
  overflow: hidden;
}
.nc-cover {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.nc-cover-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f0f0;
  color: var(--text-light);
}
.nc-body {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 3px;
  min-width: 0;
}
.nc-label {
  font-size: 11px;
  color: var(--text-light);
  font-weight: 500;
}
.nc-name {
  font-size: 16px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.nc-artist {
  font-size: 13px;
  color: var(--text-secondary);
}

.nc-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-shrink: 0;
}
.btn-play-now {
  display: flex;
  align-items: center;
  gap: 4px;
  padding: 8px 18px;
  border: none;
  border-radius: 18px;
  background: var(--color-primary);
  font-size: 13px;
  font-weight: 500;
  color: #fff;
  cursor: pointer;
  transition: opacity 0.2s;
  white-space: nowrap;
}
.btn-play-now:hover {
  opacity: 0.85;
}
.btn-skip {
  padding: 8px 16px;
  border: 1px solid var(--border-color);
  border-radius: 18px;
  background: transparent;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap;
  flex-shrink: 0;
}
.btn-skip:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: #fef0f0;
}

/* ====== 状态栏 ====== */
.status-bar {
  display: flex;
  align-items: center;
  padding: 14px 20px;
  font-size: 13px;
  color: var(--text-light);
  background: var(--bg-content);
  border-radius: 10px;
}
.status-bar.active {
  color: var(--color-primary);
}
.status-left {
  display: flex;
  align-items: center;
  gap: 8px;
}

/* TTS 动态均衡器 */
.equalizer {
  display: flex;
  align-items: center;
  gap: 2px;
  height: 20px;
}
.eq-bar {
  width: 3px;
  background: var(--color-primary);
  border-radius: 2px;
  animation: eqWave 0.8s ease-in-out infinite alternate;
}
.eq-bar:nth-child(1) { height: 8px; animation-delay: 0s; }
.eq-bar:nth-child(2) { height: 14px; animation-delay: 0.15s; }
.eq-bar:nth-child(3) { height: 10px; animation-delay: 0.3s; }
.eq-bar:nth-child(4) { height: 16px; animation-delay: 0.45s; }

/* ====== 错误 ====== */
.error-block {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 60px 0;
  color: var(--text-secondary);
  font-size: 14px;
}
.btn-retry {
  padding: 8px 24px;
  border: 1px solid var(--border-color);
  border-radius: 18px;
  background: transparent;
  font-size: 13px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.btn-retry:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}

/* ====== 动画 ====== */
@keyframes heroPulse {
  0%, 100% { transform: scale(1); opacity: 0.12; }
  50% { transform: scale(1.15); opacity: 0.2; }
}
@keyframes dotPulse {
  0%, 100% { transform: scale(1); box-shadow: 0 0 0 0 rgba(236,65,65,0.4); }
  50% { transform: scale(1.3); box-shadow: 0 0 0 8px rgba(236,65,65,0); }
}
@keyframes eqWave {
  0% { transform: scaleY(0.4); }
  100% { transform: scaleY(1); }
}
</style>
