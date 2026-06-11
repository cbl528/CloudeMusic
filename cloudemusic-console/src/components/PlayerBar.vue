<script setup>
import { Icon } from '@iconify/vue'
import { useMusicStore } from '@/stores/music'

const musicStore = useMusicStore()
</script>

<template>
  <footer class="player-bar">
    <div class="player-inner">
      <!-- 歌曲信息 -->
      <div class="player-song">
        <div class="cover-placeholder">
          <Icon icon="mdi:music-note" width="28" height="28" />
        </div>
        <div class="song-info">
          <p class="song-name">未在播放</p>
          <p class="song-artist">选择一首歌曲开始播放</p>
        </div>
        <button class="like-btn">
          <Icon icon="mdi:heart-outline" width="20" height="20" />
        </button>
      </div>

      <!-- 播放控制 -->
      <div class="player-controls">
        <div class="controls-top">
          <button class="ctrl-btn" title="切换播放模式" @click="musicStore.togglePlayMode()">
            <Icon v-if="musicStore.playMode === 'order'" icon="mdi:repeat" width="18" height="18" />
            <Icon v-else-if="musicStore.playMode === 'random'" icon="mdi:shuffle" width="18" height="18" />
            <Icon v-else icon="mdi:repeat-off" width="18" height="18" />
          </button>
          <button class="ctrl-btn" title="上一首">
            <Icon icon="mdi:skip-previous" width="20" height="20" />
          </button>
          <button class="ctrl-btn play-btn" title="播放/暂停">
            <Icon icon="mdi:play" width="20" height="20" />
          </button>
          <button class="ctrl-btn" title="下一首">
            <Icon icon="mdi:skip-next" width="20" height="20" />
          </button>
        </div>
        <div class="progress-area">
          <span class="time">00:00</span>
          <div class="progress-bar">
            <div class="progress-current" style="width: 0%"></div>
            <div class="progress-thumb" style="left: 0%"></div>
          </div>
          <span class="time">00:00</span>
        </div>
      </div>

      <!-- 右侧功能 -->
      <div class="player-extra">
        <button class="ctrl-btn" title="歌词">
          <Icon icon="mdi:file-text-outline" width="18" height="18" />
        </button>
        <button class="ctrl-btn" title="音量">
          <Icon icon="mdi:volume-high" width="18" height="18" />
        </button>
      </div>
    </div>
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
}

.player-inner {
  display: flex;
  align-items: center;
  height: 100%;
  padding: 0 24px;
  justify-content: space-between;
}

/* 左侧歌曲信息 */
.player-song {
  display: flex;
  align-items: center;
  gap: 12px;
  width: 240px;
}

.cover-placeholder {
  width: 48px;
  height: 48px;
  border-radius: var(--radius-sm);
  background: #e5e5e5;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 24px;
  color: var(--text-light);
  flex-shrink: 0;
}

.song-info {
  flex: 1;
  min-width: 0;
}
.song-name {
  font-size: 14px;
  color: var(--text-primary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.song-artist {
  font-size: 12px;
  color: var(--text-light);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.like-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: var(--text-light);
  padding: 4px;
  transition: var(--transition);
}
.like-btn:hover {
  color: var(--color-primary);
}

/* 播放控制 */
.player-controls {
  flex: 1;
  max-width: 600px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 6px;
}

.controls-top {
  display: flex;
  align-items: center;
  gap: 16px;
}

.ctrl-btn {
  background: none;
  border: none;
  font-size: 18px;
  cursor: pointer;
  color: var(--text-secondary);
  padding: 4px 6px;
  transition: var(--transition);
  line-height: 1;
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
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 16px;
}
.play-btn:hover {
  background: var(--color-primary-dark) !important;
}

.progress-area {
  display: flex;
  align-items: center;
  gap: 10px;
  width: 100%;
  max-width: 480px;
}

.time {
  font-size: 11px;
  color: var(--text-light);
  min-width: 36px;
  text-align: center;
}

.progress-bar {
  flex: 1;
  height: 4px;
  background: #e5e5e5;
  border-radius: 2px;
  position: relative;
  cursor: pointer;
}
.progress-current {
  height: 100%;
  background: var(--color-primary);
  border-radius: 2px;
  transition: width 0.2s;
}

/* 右侧 */
.player-extra {
  display: flex;
  align-items: center;
  gap: 8px;
  width: 240px;
  justify-content: flex-end;
}
</style>
