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
    timeout: 45000, // AI 生成较慢，45s 超时
  })
}
