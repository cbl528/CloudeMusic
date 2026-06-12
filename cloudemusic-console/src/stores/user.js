import { ref, computed } from 'vue'
import { defineStore } from 'pinia'
import { getUserInfo } from '@/api/user'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref(null) // { id, username, nickname, avatar, ... }
  const loaded = ref(false)  // 是否已完成 getUserInfo 恢复

  // 方便取用户信息
  const isLoggedIn = computed(() => !!token.value)
  const nickname = computed(() => userInfo.value?.nickname || userInfo.value?.username || '')
  const avatar = computed(() => userInfo.value?.avatar || '')
  const userId = computed(() => userInfo.value?.id || null)

  /** 登录成功后调用：存 token，拉取用户信息 */
  async function loginSuccess(jwt) {
    token.value = jwt
    localStorage.setItem('token', jwt)
    await fetchUserInfo()
  }

  /** 从后端拉取用户信息 */
  async function fetchUserInfo() {
    if (!token.value) return null
    try {
      const data = await getUserInfo()
      userInfo.value = data
      return data
    } catch {
      // token 无效，清理状态
      logout()
      return null
    } finally {
      loaded.value = true
    }
  }

  /** 退出登录 */
  function logout() {
    token.value = ''
    userInfo.value = null
    loaded.value = false
    localStorage.removeItem('token')
    localStorage.removeItem('saved_credentials')
  }

  /** 初始化：如果有 token 则自动恢复用户信息 */
  async function init() {
    if (token.value && !userInfo.value) {
      await fetchUserInfo()
    } else if (!token.value) {
      loaded.value = true
    }
  }

  return {
    token, userInfo, loaded,
    isLoggedIn, nickname, avatar, userId,
    loginSuccess, fetchUserInfo, logout, init,
  }
})
