import request from '@/utils/request'

const BASE_URL = '/api/cloude/music'

/** 搜索 */
export function search(keywords, type = 1, offset = 0, limit = 30) {
  return request({
    url: `${BASE_URL}/search`,
    method: 'get',
    params: { keywords, type, offset, limit },
  })
}

/** 热搜列表 */
export function searchHot() {
  return request({
    url: `${BASE_URL}/search/hot`,
    method: 'get',
  })
}
