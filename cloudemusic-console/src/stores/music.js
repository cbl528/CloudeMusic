import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getSongUrl, getSongDetail } from '@/api/song'
import { addHistory } from '@/api/user'
import { djRegister, djRecommend } from '@/api/ai'

function normalizeSong(s) {
  if (!s) return null
  const artists = s.artists || s.ar || (s.song?.artists) || []
  const album = s.album || s.al || null
  return {
    id: s.id,
    name: s.name || '未知歌曲',
    artists,
    album,
    cover:
      s.cover || s.picUrl || s.imageUrl || (s.al?.picUrl) || (s.album?.picUrl) || '',
    duration: s.duration || s.dt || 0,
  }
}

export const useMusicStore = defineStore('music', () => {
  // --- 状态 ---
  const playlist = ref([])
  const currentIndex = ref(-1)
  const playing = ref(false)
  const currentTime = ref(0)
  const duration = ref(0)
  const volume = ref(parseFloat(localStorage.getItem('volume') || '0.4'))
  const playMode = ref('order') // order | random | single
  const loading = ref(false)
  const errorMsg = ref('')

  // --- DJ 情感接续 ---
  const djRecommendation = ref(null)   // { commentary, next_song }
  const djLoading = ref(false)          // DJ 推荐加载中
  let djTriggeredSongId = null          // 当前已触发推荐的歌曲 ID（避免重复触发）

  // --- 从 localStorage 恢复播放状态 ---
  function restorePlayback() {
    try {
      const data = JSON.parse(localStorage.getItem('playback') || 'null')
      if (data?.playlist?.length && typeof data.currentIndex === 'number' && data.currentIndex >= 0) {
        playlist.value = data.playlist
        currentIndex.value = data.currentIndex
        if (data.currentTime) currentTime.value = data.currentTime
      }
    } catch (_) {}
  }
  restorePlayback()

  function savePlayback() {
    localStorage.setItem('playback', JSON.stringify({
      playlist: playlist.value,
      currentIndex: currentIndex.value,
      currentTime: currentTime.value,
    }))
  }

  // 页面关闭前保存最新进度
  if (typeof window !== 'undefined') {
    window.addEventListener('beforeunload', savePlayback)
  }

  // --- 计算属性 ---
  const currentSong = computed(() => {
    if (currentIndex.value < 0 || currentIndex.value >= playlist.value.length)
      return null
    return playlist.value[currentIndex.value]
  })

  const playModeLabel = computed(() => {
    const map = { order: '顺序播放', random: '随机播放', single: '单曲循环' }
    return map[playMode.value]
  })

  // --- Audio 单例 ---
  let audio = null

  function getAudio() {
    if (!audio) {
      audio = new Audio()
      audio.preload = 'auto'
      audio.volume = volume.value

      audio.addEventListener('timeupdate', () => {
        currentTime.value = audio.currentTime || 0
      })
      audio.addEventListener('loadedmetadata', () => {
        duration.value = audio.duration || 0
      })
      audio.addEventListener('play', () => {
        playing.value = true
      })
      audio.addEventListener('pause', () => {
        playing.value = false
      })
      audio.addEventListener('ended', () => {
        onEnded()
      })
      audio.addEventListener('error', () => {
        errorMsg.value = '音频加载失败'
        playing.value = false
      })
    }
    return audio
  }

  // --- 内部逻辑 ---
  function onEnded() {
    if (playMode.value === 'single') {
      audio.currentTime = 0
      audio.play()
    } else if (playMode.value === 'random') {
      playRandom()
    } else {
      // 顺序播放
      if (currentIndex.value < playlist.value.length - 1) {
        playByIndex(currentIndex.value + 1)
      } else {
        // 播完停止，回到开头
        currentIndex.value = -1
        playing.value = false
        currentTime.value = 0
      }
    }
  }

  async function playByIndex(index) {
    const list = playlist.value
    if (index < 0 || index >= list.length) return

    currentIndex.value = index
    savePlayback()
    // 重置 DJ 推荐状态，允许新歌曲触发推荐
    djRecommendation.value = null
    djTriggeredSongId = null
    const song = list[index]
    loading.value = true
    errorMsg.value = ''

    try {
      // 如果没有封面图，获取歌曲详情补充
      if (!song.cover) {
        try {
          const detailData = await getSongDetail(song.id)
          const detail = detailData.songs?.[0]
          if (detail) {
            song.cover = detail.al?.picUrl || detail.album?.picUrl || ''
          }
        } catch (_) { /* 封面获取失败不影响播放 */ }
      }

      const res = await getSongUrl(song.id)
      const items = res.data || []
      const item = Array.isArray(items) ? items[0] : null

      if (item && item.url) {
        const el = getAudio()
        el.src = item.url
        await el.play()
        // 记录播放历史（已登录时）
        recordHistory(song)
        // 静默注册 DJ 情感索引（fire-and-forget）
        registerDj(song)
      } else {
        errorMsg.value = '该歌曲暂无播放源'
        playing.value = false
      }
    } catch (e) {
      if (e.name !== 'AbortError') {
        errorMsg.value = '获取音频失败'
        playing.value = false
      }
    } finally {
      loading.value = false
    }
  }

  function playRandom() {
    const len = playlist.value.length
    if (len === 0) return
    if (len === 1) { playByIndex(0); return }
    let next
    do { next = Math.floor(Math.random() * len) }
    while (next === currentIndex.value)
    playByIndex(next)
  }

  // --- 对外 actions ---
  /** 播放单首歌曲 */
  function play(song) {
    const n = normalizeSong(song)
    if (!n) return
    playlist.value = [n]
    currentIndex.value = 0
    playByIndex(0)
  }

  /** 播放列表中的某首（或默认第一首） */
  function playMultiple(songs, index = 0) {
    if (!songs || !songs.length) return
    playlist.value = songs.map(normalizeSong).filter(Boolean)
    currentIndex.value = index
    playByIndex(index)
  }

  /** 切换播放/暂停 */
  function togglePlay() {
    const el = getAudio()
    if (!el.src) {
      // 恢复播放：有歌但未加载音频，重新加载
      if (currentSong.value) playByIndex(currentIndex.value)
      return
    }
    if (el.paused) el.play()
    else el.pause()
  }

  /** 暂停 */
  function pause() {
    if (audio && !audio.paused) audio.pause()
  }

  /** 下一首 */
  function next() {
    if (!playlist.value.length) return
    if (playMode.value === 'random') {
      playRandom()
    } else {
      const idx = currentIndex.value + 1
      playByIndex(idx < playlist.value.length ? idx : 0)
    }
  }

  /** 上一首 */
  function prev() {
    if (!playlist.value.length) return
    // 超过 3 秒则重播当前，否则切到上一首
    if (audio && audio.currentTime > 3) {
      audio.currentTime = 0
      return
    }
    const idx = currentIndex.value - 1
    playByIndex(idx >= 0 ? idx : playlist.value.length - 1)
  }

  /** 跳转到指定时间 */
  function seek(time) {
    if (audio) {
      audio.currentTime = time
      currentTime.value = time
    }
  }

  /** 设置音量 0-1 */
  function setVolume(v) {
    const val = Math.max(0, Math.min(1, v))
    volume.value = val
    localStorage.setItem('volume', val)
    if (audio) audio.volume = val
  }

  /** 静默注册 DJ 情感索引（fire-and-forget，失败不影响播放） */
  function registerDj(song) {
    if (!song?.id) return
    const artists = (song.artists || []).map(a => a.name || a).join(' / ')
    djRegister(song.id, song.name || '', artists).catch(_ => {})
  }

  /** 请求 AI DJ 推荐（播放进度超 60% 时调用） */
  async function requestDjRecommendation() {
    const song = currentSong.value
    if (!song || djLoading.value) return null

    // 同一首歌已触发过推荐
    if (djTriggeredSongId === song.id) return djRecommendation.value

    const artists = (song.artists || []).map(a => a.name || a).join(' / ')
    djLoading.value = true

    try {
      const result = await djRecommend(
        song.id,
        song.name || '',
        artists,
        [], // 暂不传 recent_ids，后续可扩展
      )
      djRecommendation.value = result
      djTriggeredSongId = song.id
      return result
    } catch (e) {
      // 静默失败（情感库不足等非关键错误）
      return null
    } finally {
      djLoading.value = false
    }
  }

  /** 记录播放历史（静默，失败不影响播放） */
  function recordHistory(song) {
    if (!localStorage.getItem('token')) return
    const artists = song.artists || []
    addHistory({
      songId: song.id,
      songName: song.name || '',
      artist: artists.map(a => a.name || a).join(' / '),
      cover: song.cover || '',
      duration: song.duration || 0,
    }).catch(_ => {})
  }

  /** 切换播放模式 */
  function togglePlayMode() {
    const modes = ['order', 'random', 'single']
    const idx = modes.indexOf(playMode.value)
    playMode.value = modes[(idx + 1) % modes.length]
  }

  return {
    playlist,
    currentIndex,
    currentSong,
    playing,
    currentTime,
    duration,
    volume,
    playMode,
    playModeLabel,
    loading,
    errorMsg,
    play,
    playMultiple,
    togglePlay,
    pause,
    next,
    prev,
    seek,
    setVolume,
    togglePlayMode,
    normalizeSong,
    djRecommendation,
    djLoading,
    requestDjRecommendation,
  }
})
