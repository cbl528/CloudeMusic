<script setup>
const tabs = ['歌曲', '歌单', '专辑', '歌手']
</script>

<template>
  <div class="page-favorites">
    <div class="page-header">
      <h2>我的收藏</h2>
      <p class="page-desc">收藏你喜欢的音乐内容</p>
    </div>

    <!-- 分类标签 -->
    <div class="tabs">
      <span
        v-for="tab in tabs"
        :key="tab"
        class="tab"
        :class="{ active: tab === '歌曲' }"
      >
        {{ tab }}
      </span>
    </div>

    <!-- 收藏列表 -->
    <div class="empty-state">
      <div class="empty-icon">♡</div>
      <p class="empty-text">还没有收藏的内容</p>
      <p class="empty-desc">去发现页找一些喜欢的音乐吧</p>
      <button class="btn btn-primary">去发现音乐</button>
    </div>
  </div>
</template>

<style scoped>
.page-favorites {
  padding: 32px 48px;
  width: 100%;
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

.tabs {
  display: flex;
  gap: 4px;
  margin-bottom: 28px;
  border-bottom: 1px solid var(--border-color);
}
.tab {
  padding: 10px 20px;
  font-size: 14px;
  color: var(--text-secondary);
  cursor: pointer;
  border-bottom: 2px solid transparent;
  transition: var(--transition);
  margin-bottom: -1px;
}
.tab:hover {
  color: var(--text-primary);
}
.tab.active {
  color: var(--color-primary);
  border-bottom-color: var(--color-primary);
  font-weight: 600;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 80px 0;
  text-align: center;
}
.empty-icon {
  font-size: 64px;
  color: #e0e0e0;
  margin-bottom: 16px;
}
.empty-text {
  font-size: 16px;
  color: var(--text-secondary);
  margin-bottom: 6px;
}
.empty-desc {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 20px;
}
</style>
