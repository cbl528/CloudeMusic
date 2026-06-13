<script setup>
import { ref, watch } from 'vue'
import { Icon } from '@iconify/vue'
import { checkFavorite, addFavorite, removeFavorite } from '@/api/user'
import { useAuthModal } from '@/composables/useAuthModal'
import { useToast } from '@/composables/useToast'

const props = defineProps({
  songId: { type: [Number, String], required: true },
  songData: { type: Object, default: null },
})

const { requireLogin } = useAuthModal()
const toast = useToast()

const favorited = ref(false)
const loading = ref(false)

// 是否已登录
function isLoggedIn() {
  return !!localStorage.getItem('token')
}

// 初始化检查收藏状态
async function initCheck() {
  if (!props.songId || !isLoggedIn()) return
  try {
    const res = await checkFavorite(props.songId)
    favorited.value = res.favorited === true
  } catch (_) { /* 忽略 */ }
}

// 监听 songId 变化（切歌时重新检查）
watch(() => props.songId, () => initCheck(), { immediate: true })

async function toggle() {
  if (!isLoggedIn()) {
    requireLogin()
    return
  }

  loading.value = true
  try {
    if (favorited.value) {
      await removeFavorite(props.songId)
      favorited.value = false
      toast.success('已取消收藏')
    } else {
      await addFavorite({
        songId: props.songId,
        songName: props.songData?.name || '',
        artist: formatArtist(props.songData),
        cover: props.songData?.cover || '',
        duration: props.songData?.duration || 0,
      })
      favorited.value = true
      toast.success('已收藏')
    }
  } catch (e) {
    toast.error('操作失败')
  } finally {
    loading.value = false
  }
}

function formatArtist(song) {
  if (!song) return ''
  const list = song.artists || song.ar || []
  return list.map(a => a.name || a).join(' / ')
}
</script>

<template>
  <button
    class="favorite-btn"
    :class="{ active: favorited, loading }"
    :title="favorited ? '取消收藏' : '收藏'"
    @click.stop="toggle"
  >
    <Icon
      v-if="loading"
      icon="mdi:loading"
      width="16"
      height="16"
      class="spin"
    />
    <Icon
      v-else
      :icon="favorited ? 'mdi:heart' : 'mdi:heart-outline'"
      width="16"
      height="16"
    />
  </button>
</template>

<style scoped>
.favorite-btn {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px;
  color: var(--text-light);
  transition: color 0.2s, transform 0.15s;
  line-height: 1;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}
.favorite-btn:hover {
  color: var(--color-primary);
  transform: scale(1.15);
}
.favorite-btn.active {
  color: var(--color-primary);
}

.spin {
  animation: rotate 0.8s linear infinite;
}
@keyframes rotate {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
