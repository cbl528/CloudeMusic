import request from '@/utils/request'

const BASE_URL = '/api/cloude/music'

/** 所有榜单列表 */
export function getToplist() {
  return request({
    url: `${BASE_URL}/toplist`,
    method: 'get',
  })
}

/** 歌单详情（含歌曲列表） */
export function getPlaylistDetail(id) {
  return request({
    url: `${BASE_URL}/playlist/detail`,
    method: 'get',
    params: { id },
  })
}
