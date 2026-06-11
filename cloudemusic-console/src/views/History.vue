<script setup>
const timeRanges = ['今天', '昨天', '本周', '更早']
</script>

<template>
  <div class="page-history">
    <div class="page-header">
      <h2>播放历史</h2>
      <p class="page-desc">查看你最近播放的音乐</p>
    </div>

    <!-- 清空按钮 -->
    <div class="toolbar">
      <span class="history-count">共 0 首播放记录</span>
      <button class="btn" disabled>清空播放历史</button>
    </div>

    <!-- 时间分组 -->
    <div v-for="range in timeRanges" :key="range" class="time-group">
      <h3 class="time-label">{{ range }}</h3>

      <!-- 空状态 -->
      <div class="empty-history">
        <p>暂无播放记录</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-history {
  padding: 24px 32px;
  max-width: 900px;
}

.page-header {
  margin-bottom: 20px;
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

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border-color);
}
.history-count {
  font-size: 13px;
  color: var(--text-light);
}

.time-group {
  margin-bottom: 24px;
}
.time-label {
  font-size: 15px;
  font-weight: 600;
  color: var(--text-primary);
  padding-bottom: 10px;
  border-bottom: 1px solid var(--border-light);
  margin-bottom: 12px;
}

.empty-history {
  padding: 20px 0;
  text-align: center;
  color: var(--text-light);
  font-size: 14px;
}
</style>
