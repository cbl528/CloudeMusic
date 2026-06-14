import request from '@/utils/request'

const BASE_URL = '/api/cloude/music/ai'

/**
 * AI 智能歌单生成
 * @param {string} keyword 心情/场景关键词
 * @returns {Promise<{playlist_name: string, description: string, songs: Array}>}
 */
export function generatePlaylist(keyword) {
  return request({
    url: `${BASE_URL}/playlist/generate`,
    method: 'post',
    data: { keyword },
    timeout: 45000,
  })
}

/**
 * AI DJ 情感注册：将歌曲歌词注册到情感向量库
 * @param {number|string} songId
 * @param {string} songName
 * @param {string} songArtists
 */
export function djRegister(songId, songName, songArtists) {
  return request({
    url: `${BASE_URL}/dj/register`,
    method: 'post',
    data: { songId, songName, songArtists },
    timeout: 15000,
  })
}

/**
 * AI DJ 推荐：获取情感接续推荐 + 解说词
 * @param {number|string} currentSongId
 * @param {string} currentSongName
 * @param {string} currentSongArtists
 * @param {number[]} recentIds 最近播放的歌曲 ID 列表
 * @returns {Promise<{commentary: string, next_song: {id, name, artists, cover, duration, reason}}>}
 */
export function djRecommend(currentSongId, currentSongName, currentSongArtists, recentIds = []) {
  return request({
    url: `${BASE_URL}/dj/recommend`,
    method: 'post',
    data: { currentSongId, currentSongName, currentSongArtists, recentIds },
    timeout: 30000,
  })
}
