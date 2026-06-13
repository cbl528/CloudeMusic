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

/** 歌手详情 + 热门50首 */
export function getArtistDetail(id) {
  return request({
    url: `${BASE_URL}/artists`,
    method: 'get',
    params: { id },
  })
}

/** 歌手专辑列表 */
export function getArtistAlbum(id, limit = 30) {
  return request({
    url: `${BASE_URL}/artist/album`,
    method: 'get',
    params: { id, limit },
  })
}

/** 歌手描述/介绍 */
export function getArtistDesc(id) {
  return request({
    url: `${BASE_URL}/artist/desc`,
    method: 'get',
    params: { id },
  })
}
