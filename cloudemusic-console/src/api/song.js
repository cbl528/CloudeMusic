import request from '@/utils/request'

const BASE_URL = '/api/cloude/music'

/** 获取歌曲播放 URL */
export function getSongUrl(ids, br = 320000) {
  return request({
    url: `${BASE_URL}/song/url`,
    method: 'get',
    params: { ids, br },
  })
}

/** 获取歌曲详情 */
export function getSongDetail(ids) {
  return request({
    url: `${BASE_URL}/song/detail`,
    method: 'get',
    params: { ids },
  })
}

/** 获取歌词 */
export function getLyric(id) {
  return request({
    url: `${BASE_URL}/song/lyric`,
    method: 'get',
    params: { id },
  })
}
