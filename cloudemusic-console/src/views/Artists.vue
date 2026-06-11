<script setup>
const categories = ['华语', '欧美', '日本', '韩国', '其他']

const artists = Array.from({ length: 24 }, (_, i) => ({
  id: i + 1,
  name: `热门歌手 ${i + 1}`,
  desc: '知名音乐人',
}))
</script>

<template>
  <div class="page-artists">
    <div class="page-header">
      <h2>热门歌手</h2>
      <p class="page-desc">发现你喜爱的音乐人</p>
    </div>

    <!-- 分类标签 -->
    <div class="categories">
      <span
        v-for="cat in categories"
        :key="cat"
        class="category-tag"
        :class="{ active: cat === '华语' }"
      >
        {{ cat }}
      </span>
    </div>

    <!-- 歌手列表 -->
    <div class="artist-grid">
      <div v-for="artist in artists" :key="artist.id" class="artist-card">
        <div class="artist-avatar">
          <div class="avatar-placeholder">{{ artist.name.charAt(0) }}</div>
          <div class="artist-play-overlay">▶</div>
        </div>
        <p class="artist-name">{{ artist.name }}</p>
        <p class="artist-desc">{{ artist.desc }}</p>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-artists {
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

.categories {
  display: flex;
  gap: 12px;
  margin-bottom: 24px;
  flex-wrap: wrap;
}
.category-tag {
  padding: 6px 18px;
  border-radius: 20px;
  font-size: 13px;
  color: var(--text-secondary);
  background: var(--bg-card);
  border: 1px solid var(--border-color);
  cursor: pointer;
  transition: var(--transition);
}
.category-tag:hover {
  color: var(--color-primary);
  border-color: var(--color-primary);
}
.category-tag.active {
  background: var(--color-primary);
  color: var(--text-white);
  border-color: var(--color-primary);
}

.artist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 32px 24px;
}

.artist-card {
  text-align: center;
  cursor: pointer;
  transition: var(--transition);
}
.artist-card:hover {
  transform: translateY(-4px);
}

.artist-avatar {
  position: relative;
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 10px;
}
.avatar-placeholder {
  width: 100%;
  height: 100%;
  background: linear-gradient(135deg, #e8e8e8, #d0d0d0);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #bbb;
  font-weight: 700;
}

.artist-play-overlay {
  position: absolute;
  right: 4px;
  bottom: 4px;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--color-primary);
  color: var(--text-white);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  opacity: 0;
  transition: var(--transition);
}
.artist-card:hover .artist-play-overlay {
  opacity: 1;
}

.artist-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
}
.artist-desc {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 2px;
}
</style>
