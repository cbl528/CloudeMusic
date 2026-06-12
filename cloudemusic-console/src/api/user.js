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

/** 获取当前用户信息 */
export function getUserInfo() {
  return request({
    url: `${BASE_URL}/info`,
    method: 'get',
  })
}

/** 更新用户信息（昵称/头像/个性签名） */
export function updateUserInfo(data) {
  return request({
    url: `${BASE_URL}/update`,
    method: 'put',
    data,
  })
}

/** 注销账号 */
export function deleteAccount() {
  return request({
    url: `${BASE_URL}/account`,
    method: 'delete',
  })
}
