import request from '@/utils/request'

const BASE_URL = '/api/cloude/music'

/** 热门歌手 */
export function getTopArtists(limit = 24, offset = 0) {
  return request({
    url: `${BASE_URL}/top/artists`,
    method: 'get',
    params: { limit, offset },
  })
}

/** 分类歌手列表 */
export function getArtistList(cat, limit = 24, offset = 0) {
  return request({
    url: `${BASE_URL}/artist/list`,
    method: 'get',
    params: { cat, limit, offset },
  })
}
