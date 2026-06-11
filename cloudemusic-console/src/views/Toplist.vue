<script setup>
const toplists = [
  { id: 1, name: '飙升榜', desc: '实时更新，热门飙升' },
  { id: 2, name: '热歌榜', desc: '最热歌曲排行榜' },
  { id: 3, name: '新歌榜', desc: '最新发布的歌曲' },
  { id: 4, name: '原创榜', desc: '原创音乐排行榜' },
  { id: 5, name: '欧美榜', desc: '欧美热门歌曲' },
  { id: 6, name: '华语榜', desc: '华语热门歌曲' },
]
</script>

<template>
  <div class="page-toplist">
    <div class="page-header">
      <h2>排行榜</h2>
      <p class="page-desc">各类音乐排行榜，发现热门好音乐</p>
    </div>

    <div class="toplist-grid">
      <div v-for="tl in toplists" :key="tl.id" class="toplist-card">
        <div class="toplist-cover">
          <div class="toplist-rank">{{ tl.id }}</div>
        </div>
        <div class="toplist-info">
          <h3 class="toplist-name">{{ tl.name }}</h3>
          <p class="toplist-desc">{{ tl.desc }}</p>
          <div class="toplist-songs">
            <p v-for="i in 3" :key="i" class="toplist-song">
              {{ i }}. 歌曲示例 {{ i }} — 歌手名
            </p>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-toplist {
  padding: 24px 32px;
  max-width: 1100px;
}

.page-header {
  margin-bottom: 28px;
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

.toplist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
}

.toplist-card {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: var(--bg-card);
  border-radius: var(--radius-md);
  border: 1px solid var(--border-color);
  transition: var(--transition);
  cursor: pointer;
}
.toplist-card:hover {
  box-shadow: var(--shadow-light);
  transform: translateY(-2px);
}

.toplist-cover {
  width: 100px;
  height: 100px;
  border-radius: var(--radius-sm);
  background: linear-gradient(135deg, var(--color-primary), #ff7e7e);
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.toplist-rank {
  font-size: 36px;
  font-weight: 700;
  color: var(--text-white);
  text-shadow: 1px 1px 4px rgba(0, 0, 0, 0.2);
}

.toplist-info {
  flex: 1;
  min-width: 0;
}
.toplist-name {
  font-size: 16px;
  font-weight: 700;
  margin-bottom: 4px;
}
.toplist-desc {
  font-size: 12px;
  color: var(--text-light);
  margin-bottom: 10px;
}
.toplist-songs {
  display: flex;
  flex-direction: column;
  gap: 4px;
}
.toplist-song {
  font-size: 12px;
  color: var(--text-secondary);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
