<script setup>
import { ref } from 'vue'

const keyword = ref('')
const searchType = ref('song')
const types = [
  { key: 'song', label: '歌曲' },
  { key: 'artist', label: '歌手' },
  { key: 'album', label: '专辑' },
]
</script>

<template>
  <div class="page-search">
    <div class="page-header">
      <h2>搜索</h2>
      <p class="page-desc">搜索音乐、歌手、专辑</p>
    </div>

    <!-- 搜索框 -->
    <div class="search-box">
      <div class="search-input-wrapper">
        <span class="search-icon">⌕</span>
        <input
          v-model="keyword"
          type="text"
          class="search-input"
          placeholder="输入关键词搜索..."
        />
        <button v-if="keyword" class="search-clear" @click="keyword = ''">✕</button>
      </div>
      <button class="search-btn">搜索</button>
    </div>

    <!-- 搜索类型 -->
    <div class="search-types">
      <span
        v-for="t in types"
        :key="t.key"
        class="type-tag"
        :class="{ active: searchType === t.key }"
        @click="searchType = t.key"
      >
        {{ t.label }}
      </span>
    </div>

    <!-- 搜索提示 -->
    <div class="search-placeholder">
      <p>请输入关键词搜索音乐</p>
      <div class="hot-tags">
        <span class="hot-tag">热门搜索</span>
        <span class="hot-tag">周杰伦</span>
        <span class="hot-tag">林俊杰</span>
        <span class="hot-tag">陈奕迅</span>
        <span class="hot-tag">邓紫棋</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-search {
  padding: 24px 32px;
  max-width: 800px;
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

.search-box {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
}
.search-input-wrapper {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}
.search-icon {
  position: absolute;
  left: 14px;
  font-size: 18px;
  color: var(--text-light);
}
.search-input {
  width: 100%;
  height: 44px;
  padding: 0 40px 0 42px;
  border: 2px solid var(--border-color);
  border-radius: 22px;
  font-size: 15px;
  outline: none;
  transition: var(--transition);
  background: var(--bg-card);
}
.search-input:focus {
  border-color: var(--color-primary);
}
.search-clear {
  position: absolute;
  right: 14px;
  background: none;
  border: none;
  font-size: 16px;
  color: var(--text-light);
  cursor: pointer;
}
.search-btn {
  height: 44px;
  padding: 0 28px;
  background: var(--color-primary);
  color: var(--text-white);
  border: none;
  border-radius: 22px;
  font-size: 15px;
  cursor: pointer;
  transition: var(--transition);
}
.search-btn:hover {
  background: var(--color-primary-dark);
}

.search-types {
  display: flex;
  gap: 10px;
  margin-bottom: 28px;
}
.type-tag {
  padding: 5px 16px;
  border-radius: 14px;
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: var(--transition);
}
.type-tag:hover,
.type-tag.active {
  color: var(--color-primary);
  border-color: var(--color-primary);
}

.search-placeholder {
  text-align: center;
  padding: 60px 0;
  color: var(--text-light);
}
.search-placeholder p {
  font-size: 15px;
  margin-bottom: 20px;
}

.hot-tags {
  display: flex;
  gap: 12px;
  justify-content: center;
  flex-wrap: wrap;
}
.hot-tag {
  padding: 6px 16px;
  border-radius: 16px;
  font-size: 13px;
  background: #f0f0f0;
  color: var(--text-secondary);
  cursor: pointer;
  transition: var(--transition);
}
.hot-tag:first-child {
  background: var(--color-primary);
  color: var(--text-white);
}
.hot-tag:hover {
  color: var(--color-primary);
}
</style>
