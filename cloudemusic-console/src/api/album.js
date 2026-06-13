import request from '@/utils/request'

const BASE_URL = '/api/cloude/music'

/** 专辑详情（含歌曲列表） */
export function getAlbumDetail(id) {
  return request({
    url: `${BASE_URL}/album`,
    method: 'get',
    params: { id },
  })
}
