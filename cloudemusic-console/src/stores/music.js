import { ref, computed } from 'vue'
import { defineStore } from 'pinia'

export const useMusicStore = defineStore('music', () => {
  const playMode = ref('order') // order | random | single

  const playModeLabel = computed(() => {
    const map = { order: '顺序播放', random: '随机播放', single: '单曲循环' }
    return map[playMode.value]
  })

  function togglePlayMode() {
    const modes = ['order', 'random', 'single']
    const idx = modes.indexOf(playMode.value)
    playMode.value = modes[(idx + 1) % modes.length]
  }

  return { playMode, playModeLabel, togglePlayMode }
})
