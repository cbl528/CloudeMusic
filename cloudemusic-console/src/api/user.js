import request from '@/utils/request'

const BASE_URL = '/api/cloude/music/user'

/** 登录 */
export function login(data) {
  return request({
    url: `${BASE_URL}/login`,
    method: 'post',
    data,
  })
}

/** 注册 */
export function register(data) {
  return request({
    url: `${BASE_URL}/register`,
    method: 'post',
    data,
  })
}
