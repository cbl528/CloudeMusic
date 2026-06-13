<script setup>
import { ref, watch, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { search, searchHot } from '@/api/search'
import { useMusicStore } from '@/stores/music'
import FavoriteBtn from '@/components/FavoriteBtn.vue'

const router = useRouter()
const musicStore = useMusicStore()

function goArtist(id) {
  router.push(`/artist/${id}`)
}

const keyword = ref('')
const searchType = ref('song')
const loading = ref(false)
const searched = ref(false)
const results = ref([])
const total = ref(0)
const hotTags = ref([])

const typeMap = { song: 1, artist: 100, album: 10 }
const types = [
  { key: 'song', label: '歌曲' },
  { key: 'artist', label: '歌手' },
  { key: 'album', label: '专辑' },
]

onMounted(async () => {
  try {
    const res = await searchHot()
    hotTags.value = res.result?.hots || []
  } catch (_) {}
})

// 切换搜索类型时重新搜索
watch(searchType, () => {
  if (keyword.value.trim()) doSearch()
})

async function doSearch() {
  const kw = keyword.value.trim()
  if (!kw) return

  loading.value = true
  searched.value = true
  try {
    const type = typeMap[searchType.value] || 1
    const res = await search(kw, type)
    // 搜索返回的结果字段取决于 type
    if (res.result?.songs) { results.value = res.result.songs; total.value = res.result.songCount || results.value.length }
    else if (res.result?.artists) { results.value = res.result.artists; total.value = res.result.artistCount || results.value.length }
    else if (res.result?.albums) { results.value = res.result.albums; total.value = res.result.albumCount || results.value.length }
    else results.value = []
  } catch (e) {
    console.error('搜索失败', e)
    results.value = []
  } finally {
    loading.value = false
  }
}

function onKeydown(e) {
  if (e.key === 'Enter') doSearch()
}

function onHotTagClick(kw) {
  keyword.value = kw
  doSearch()
}

function playSearchResult(index) {
  if (searchType.value === 'song' && results.value.length) {
    musicStore.playMultiple(results.value, index)
  }
}

function formatDuration(dt) {
  if (!dt) return ''
  const totalSeconds = Math.floor(dt / 1000)
  const min = Math.floor(totalSeconds / 60)
  const sec = totalSeconds % 60
  return `${min}:${String(sec).padStart(2, '0')}`
}
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
          @keydown="onKeydown"
        />
        <button v-if="keyword" class="search-clear" @click="keyword = ''">✕</button>
      </div>
      <button class="search-btn" :disabled="loading" @click="doSearch">
        {{ loading ? '搜索中...' : '搜索' }}
      </button>
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

    <!-- 加载中 -->
    <div v-if="loading" class="search-placeholder">搜索中...</div>

    <!-- 搜索结果 -->
    <div v-else-if="searched && results.length" class="search-results">
      <!-- 歌曲列表模式 -->
      <template v-if="searchType === 'song'">
        <div class="result-table-header">
          <span class="col-index">#</span>
          <span class="col-name">音乐标题</span>
          <span class="col-artist">歌手</span>
          <span class="col-fav"></span>
          <span class="col-album">专辑</span>
          <span class="col-duration">时长</span>
        </div>
        <div
          v-for="(song, i) in results"
          :key="song.id"
          class="result-row"
          @click="playSearchResult(i)"
        >
          <span class="col-index">{{ i + 1 }}</span>
          <span class="col-name">
            <span class="song-name">{{ song.name }}</span>
          </span>
          <span class="col-artist">{{ song.artists?.[0]?.name || '未知' }}</span>
          <span class="col-fav"><FavoriteBtn :song-id="song.id" :song-data="song" /></span>
          <span class="col-album">{{ song.album?.name || '未知' }}</span>
          <span class="col-duration">{{ formatDuration(song.duration) }}</span>
        </div>
      </template>

      <!-- 歌手网格 -->
      <template v-else-if="searchType === 'artist'">
        <div class="artist-grid">
          <div
            v-for="item in results"
            :key="item.id"
            class="artist-card"
            @click="goArtist(item.id)"
          >
            <div class="artist-cover">
              <img
                v-if="item.img1v1Url"
                :src="item.img1v1Url"
                :alt="item.name"
                class="artist-img"
              />
              <div v-else class="artist-placeholder">{{ item.name.charAt(0) }}</div>
            </div>
            <p class="artist-name">{{ item.name }}</p>
          </div>
        </div>
      </template>

      <!-- 专辑网格 -->
      <template v-else>
        <div class="album-grid">
          <div
            v-for="item in results"
            :key="item.id"
            class="album-card"
            @click="router.push('/album/' + item.id)"
          >
            <div class="album-cover">
              <img
                v-if="item.picUrl || item.artist?.img1v1Url"
                :src="item.picUrl || item.artist?.img1v1Url"
                :alt="item.name"
                class="album-img"
              />
              <div v-else class="album-placeholder">♪</div>
            </div>
            <p class="album-name">{{ item.name }}</p>
            <p class="album-artist">{{ item.artist?.name || '' }}</p>
          </div>
        </div>
      </template>
    </div>

    <!-- 无结果 -->
    <div v-else-if="searched && !results.length" class="search-placeholder">
      未找到"{{ keyword }}"的相关结果
    </div>

    <!-- 默认占位 -->
    <div v-else class="search-placeholder">
      <p>请输入关键词搜索音乐</p>
      <div v-if="hotTags.length" class="hot-tags">
        <span class="hot-tag label">热门搜索</span>
        <span
          v-for="tag in hotTags"
          :key="tag.first"
          class="hot-tag"
          @click="onHotTagClick(tag.first)"
        >
          {{ tag.first }}
        </span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.page-search {
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

.search-box {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  max-width: 600px;
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
  white-space: nowrap;
}
.search-btn:hover {
  background: var(--color-primary-dark);
}
.search-btn:disabled {
  opacity: 0.6;
  cursor: not-allowed;
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
.hot-tag.label {
  background: var(--color-primary);
  color: var(--text-white);
  cursor: default;
}
.hot-tag:hover:not(.label) {
  color: var(--color-primary);
}

/* 搜索结果 - 表格 */
.result-table-header {
  display: flex;
  padding: 10px 16px;
  font-size: 12px;
  color: var(--text-light);
  border-bottom: 1px solid var(--border-color);
}
.result-row {
  display: flex;
  padding: 10px 16px;
  font-size: 13px;
  color: var(--text-secondary);
  border-bottom: 1px solid var(--border-light);
  transition: var(--transition);
  cursor: pointer;
}
.result-row:hover {
  background: var(--bg-hover);
}
.col-index { width: 40px; flex-shrink: 0; }
.col-name { flex: 1; min-width: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.col-artist { width: 140px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; }
.col-fav { width: 36px; flex-shrink: 0; display: flex; align-items: center; justify-content: center; }
.col-album { width: 160px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex-shrink: 0; }
.col-duration { width: 60px; text-align: right; flex-shrink: 0; }

/* ===== 歌手网格（圆形） ===== */
.artist-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(140px, 1fr));
  gap: 32px 24px;
}
.artist-card {
  text-align: center;
  cursor: pointer;
  transition: transform 0.25s;
}
.artist-card:hover {
  transform: translateY(-4px);
}
.artist-cover {
  width: 120px;
  height: 120px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 10px;
  background: linear-gradient(135deg, #e8e8e8, #d0d0d0);
}
.artist-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.artist-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  color: #bbb;
  font-weight: 700;
}
.artist-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 500;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* ===== 专辑网格（方形） ===== */
.album-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
  gap: 28px 24px;
}
.album-card {
  cursor: pointer;
  transition: transform 0.25s;
}
.album-card:hover {
  transform: translateY(-4px);
}
.album-cover {
  width: 100%;
  aspect-ratio: 1;
  border-radius: var(--radius-md);
  overflow: hidden;
  margin-bottom: 10px;
  background: #f0f0f0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}
.album-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.album-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #f0f0f0, #e0e0e0);
  font-size: 36px;
  color: #ccc;
}
.album-name {
  font-size: 14px;
  color: var(--text-primary);
  font-weight: 600;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.album-artist {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 2px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
</style>
