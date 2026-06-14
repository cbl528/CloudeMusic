<script setup>
import { ref } from 'vue'
import { Icon } from '@iconify/vue'
import { generatePlaylist } from '@/api/ai'
import { useMusicStore } from '@/stores/music'
import { useToast } from '@/composables/useToast'

const musicStore = useMusicStore()
const toast = useToast()

const keyword = ref('')
const loading = ref(false)
const result = ref(null)
const error = ref('')

function normalizeForPlay(songs) {
  return songs.map(s => ({
    id: s.id,
    name: s.name,
    artists: s.artists ? s.artists.split('/').map(n => ({ name: n.trim() })) : [],
    cover: s.cover || '',
    duration: s.duration || 0,
  }))
}

function playAll(index) {
  if (!result.value?.songs?.length) return
  musicStore.playMultiple(normalizeForPlay(result.value.songs), index)
}

async function handleGenerate() {
  const kw = keyword.value.trim()
  if (!kw) {
    toast.warning('请输入心情或场景关键词')
    return
  }

  loading.value = true
  error.value = ''
  result.value = null

  try {
    const res = await generatePlaylist(kw)
    result.value = res
  } catch (e) {
    error.value = e?.response?.data?.message || e?.message || '生成失败，请稍后重试'
    toast.error(error.value)
  } finally {
    loading.value = false
  }
}

function formatDuration(dt) {
  if (!dt) return '--'
  const s = Math.floor(dt / 1000)
  const m = Math.floor(s / 60)
  const sec = s % 60
  return `${m}:${String(sec).padStart(2, '0')}`
}
</script>

<template>
  <div class="page-ai-playlist">
    <div class="page-header">
      <h2>AI 智能歌单</h2>
      <p class="page-desc">输入你的心情或场景，AI 为你精选最合适的音乐</p>
    </div>

    <!-- 输入区域 -->
    <div class="input-section">
      <div class="input-wrapper">
        <Icon icon="mdi:sparkles" width="22" height="22" class="input-icon" />
        <input
          v-model="keyword"
          class="keyword-input"
          placeholder="输入心情或场景，如「晚上跑步」「雨天发呆」「深夜学习」..."
          @keyup.enter="handleGenerate"
        />
        <button
          class="btn-generate"
          :disabled="loading || !keyword.trim()"
          @click="handleGenerate"
        >
          <Icon v-if="loading" icon="mdi:loading" width="18" class="spin" />
          <Icon v-else icon="mdi:auto-fix" width="18" />
          <span>{{ loading ? 'AI 思考中...' : '生成歌单' }}</span>
        </button>
      </div>
      <div class="hot-tags">
        <span class="tag-label">试试：</span>
        <span
          v-for="tag in ['晚上跑步', '雨天发呆', '深夜学习', '旅行路上', '失恋疗伤', '清晨起床']"
          :key="tag"
          class="hot-tag"
          @click="keyword = tag"
        >{{ tag }}</span>
      </div>
    </div>

    <!-- 加载动画 -->
    <div v-if="loading" class="loading-section">
      <div class="loading-card">
        <Icon icon="mdi:sparkles" width="48" class="loading-icon pulse" />
        <p class="loading-text">AI 正在搜索歌曲并为你策展...</p>
        <div class="loading-bar"><div class="loading-bar-inner" /></div>
      </div>
    </div>

    <!-- 错误提示 -->
    <div v-if="error && !loading" class="error-section">
      <Icon icon="mdi:alert-circle-outline" width="48" class="error-icon" />
      <p class="error-text">{{ error }}</p>
      <button class="btn btn-primary" @click="handleGenerate">重试</button>
    </div>

    <!-- 生成结果 -->
    <div v-if="result && !loading" class="result-section">
      <!-- 歌单头部卡片 -->
      <div class="playlist-header-card">
        <div class="playlist-cover-placeholder">
          <Icon icon="mdi:music-note" width="48" />
        </div>
        <div class="playlist-info">
          <h3 class="playlist-name">{{ result.playlist_name }}</h3>
          <p class="playlist-desc">{{ result.description }}</p>
          <div class="playlist-meta">
            <span class="meta-item">
              <Icon icon="mdi:music-note" width="14" />
              {{ result.songs?.length || 0 }} 首
            </span>
            <span class="meta-item">AI 生成</span>
          </div>
          <button class="btn-play-all" @click="playAll(0)">
            <Icon icon="mdi:play" width="20" />
            播放全部
          </button>
        </div>
      </div>

      <!-- 歌曲列表 -->
      <div class="song-list">
        <div class="list-header">
          <span class="col-index">#</span>
          <span class="col-title">音乐标题</span>
          <span class="col-artist">歌手</span>
          <span class="col-reason">AI 推荐理由</span>
          <span class="col-duration">时长</span>
        </div>
        <div
          v-for="(song, i) in result.songs"
          :key="song.id"
          class="song-row"
          :class="{ active: musicStore.currentSong?.id === song.id }"
          @click="playAll(i)"
        >
          <span class="col-index">{{ i + 1 }}</span>
          <span class="col-title">
            <img
              v-if="song.cover"
              :src="song.cover + '?param=32y32'"
              class="row-cover"
            />
            <span
              class="row-name"
              :class="{ highlight: musicStore.currentSong?.id === song.id }"
            >{{ song.name }}</span>
          </span>
          <span class="col-artist">{{ song.artists || '未知' }}</span>
          <span class="col-reason">
            <Icon icon="mdi:sparkles" width="12" class="reason-icon" />
            {{ song.reason }}
          </span>
          <span class="col-duration">{{ formatDuration(song.duration) }}</span>
        </div>
      </div>
    </div>

    <!-- 初始引导 -->
    <div v-if="!result && !loading && !error" class="guide-section">
      <div class="guide-card">
        <Icon icon="mdi:robot" width="56" class="guide-icon" />
        <h3>AI 智能歌单生成</h3>
        <p>输入一个心情或场景关键词，AI 会为你从海量歌曲中精选最合适的曲目，<br/>生成专属歌单并提供每首歌的入选理由。</p>
        <div class="guide-features">
          <div class="guide-feature">
            <Icon icon="mdi:search" width="20" />
            <span>AI 理解你的需求</span>
          </div>
          <div class="guide-feature">
            <Icon icon="mdi:music" width="20" />
            <span>网易云真实曲库</span>
          </div>
          <div class="guide-feature">
            <Icon icon="mdi:lightbulb" width="20" />
            <span>个性化推荐理由</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-ai-playlist {
  padding: 32px 48px;
  width: 100%;
}

.page-header {
  margin-bottom: 24px;
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

/* 输入区域 */
.input-section {
  margin-bottom: 28px;
}
.input-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
  background: var(--bg-content);
  border: 2px solid var(--border-color);
  border-radius: 24px;
  padding: 4px 4px 4px 20px;
  transition: border-color 0.3s;
}
.input-wrapper:focus-within {
  border-color: var(--color-primary);
}
.input-icon {
  color: var(--color-primary);
  flex-shrink: 0;
}
.keyword-input {
  flex: 1;
  border: none;
  outline: none;
  font-size: 15px;
  padding: 12px 0;
  background: transparent;
  color: var(--text-primary);
  min-width: 0;
}
.keyword-input::placeholder {
  color: var(--text-light);
}
.btn-generate {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 22px;
  background: var(--color-primary);
  color: var(--text-white);
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
  white-space: nowrap;
}
.btn-generate:hover:not(:disabled) {
  background: var(--color-primary-dark);
}
.btn-generate:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.hot-tags {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  padding: 0 4px;
}
.tag-label {
  font-size: 12px;
  color: var(--text-light);
}
.hot-tag {
  padding: 4px 14px;
  border: 1px solid var(--border-color);
  border-radius: 14px;
  font-size: 12px;
  color: var(--text-secondary);
  cursor: pointer;
  transition: all 0.2s;
}
.hot-tag:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: #fef0f0;
}

/* 加载 */
.loading-section {
  display: flex;
  justify-content: center;
  padding: 60px 0;
}
.loading-card {
  text-align: center;
  background: var(--bg-content);
  border-radius: var(--radius-md);
  padding: 48px 64px;
  box-shadow: var(--shadow-light);
}
.loading-icon {
  color: var(--color-primary);
  margin-bottom: 16px;
}
.loading-text {
  font-size: 15px;
  color: var(--text-secondary);
  margin-bottom: 20px;
}
.loading-bar {
  width: 240px;
  height: 3px;
  background: #f0f0f0;
  border-radius: 2px;
  overflow: hidden;
  margin: 0 auto;
}
.loading-bar-inner {
  width: 40%;
  height: 100%;
  background: var(--color-primary);
  border-radius: 2px;
  animation: barSlide 1.5s ease-in-out infinite;
}

/* 错误 */
.error-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 60px 0;
  gap: 12px;
}
.error-icon {
  color: var(--text-light);
}
.error-text {
  font-size: 14px;
  color: var(--text-secondary);
}

/* 结果 - 歌单头部 */
.result-section {
  width: 100%;
}
.playlist-header-card {
  display: flex;
  gap: 28px;
  padding: 28px;
  background: linear-gradient(135deg, #fef0f0 0%, #fff 60%);
  border-radius: var(--radius-md);
  margin-bottom: 20px;
}
.playlist-cover-placeholder {
  width: 160px;
  height: 160px;
  background: linear-gradient(135deg, var(--color-primary-light), var(--color-primary));
  border-radius: var(--radius-sm);
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-white);
  flex-shrink: 0;
}
.playlist-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  gap: 8px;
}
.playlist-name {
  font-size: 22px;
  font-weight: 700;
}
.playlist-desc {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.6;
}
.playlist-meta {
  display: flex;
  gap: 16px;
  font-size: 12px;
  color: var(--text-light);
}
.meta-item {
  display: inline-flex;
  align-items: center;
  gap: 4px;
}
.btn-play-all {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 8px 24px;
  background: var(--color-primary);
  color: var(--text-white);
  border: none;
  border-radius: 20px;
  font-size: 14px;
  cursor: pointer;
  transition: background 0.2s;
  width: fit-content;
  margin-top: 4px;
}
.btn-play-all:hover {
  background: var(--color-primary-dark);
}

/* 结果 - 歌曲列表 */
.song-list {
  width: 100%;
}
.list-header {
  display: flex;
  padding: 10px 16px;
  font-size: 12px;
  color: var(--text-light);
  border-bottom: 1px solid var(--border-color);
}
.song-row {
  display: flex;
  align-items: center;
  padding: 9px 16px;
  font-size: 13px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-light);
  transition: background 0.15s;
  cursor: pointer;
  border-radius: var(--radius-sm);
}
.song-row:hover {
  background: #f5f5f5;
}
.song-row.active {
  background: #fef0f0;
}

.col-index { width: 40px; flex-shrink: 0; text-align: center; }
.col-title {
  flex: 1; min-width: 0;
  display: flex; align-items: center; gap: 10px;
}
.col-artist { width: 120px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; }
.col-reason {
  width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
  flex-shrink: 0; font-size: 12px; color: var(--color-primary-light);
}
.col-duration { width: 60px; text-align: right; flex-shrink: 0; }

.row-cover {
  width: 32px; height: 32px; border-radius: 3px; object-fit: cover; flex-shrink: 0;
}
.row-name {
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.row-name.highlight {
  color: var(--color-primary); font-weight: 600;
}
.reason-icon {
  vertical-align: -1px;
  margin-right: 2px;
}

/* 引导页面 */
.guide-section {
  display: flex;
  justify-content: center;
  padding: 40px 0;
}
.guide-card {
  text-align: center;
  background: var(--bg-content);
  border-radius: var(--radius-md);
  padding: 56px 72px;
  box-shadow: var(--shadow-light);
  max-width: 520px;
}
.guide-icon {
  color: var(--color-primary);
  margin-bottom: 16px;
}
.guide-card h3 {
  font-size: 18px;
  margin-bottom: 12px;
}
.guide-card p {
  font-size: 13px;
  color: var(--text-secondary);
  line-height: 1.8;
  margin-bottom: 28px;
}
.guide-features {
  display: flex;
  justify-content: center;
  gap: 32px;
}
.guide-feature {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
  font-size: 12px;
  color: var(--text-secondary);
}
.guide-feature svg {
  color: var(--color-primary);
}

/* 动画 */
.spin {
  animation: spin 1s linear infinite;
}
@keyframes spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
.pulse {
  animation: pulse 2s ease-in-out infinite;
}
@keyframes pulse {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.6; transform: scale(1.1); }
}
@keyframes barSlide {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(350%); }
}

</style>
