import request from '@/utils/request'

const BASE_URL = '/api/cloude/music'

/** 首页 Banner */
export function getBanner() {
  return request({
    url: `${BASE_URL}/banner`,
    method: 'get',
  })
}

/** 推荐歌单 */
export function getPersonalized(limit = 12) {
  return request({
    url: `${BASE_URL}/personalized`,
    method: 'get',
    params: { limit },
  })
}

/** 推荐新音乐 */
export function getNewSong(limit = 8) {
  return request({
    url: `${BASE_URL}/personalized/newsong`,
    method: 'get',
    params: { limit },
  })
}
