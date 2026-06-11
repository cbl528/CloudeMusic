import axios from 'axios'

const service = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 15000,
})

// 请求拦截器 —— 自动携带 token
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error),
)

// 响应拦截器 —— 提取 data 字段，统一处理业务异常
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 200) {
      return res.data
    }
    // 业务错误（code !== 200）
    console.error(`请求出错 [${res.code}]: ${res.message}`)
    return Promise.reject(new Error(res.message || '请求失败'))
  },
  (error) => {
    if (error.response) {
      const { status } = error.response
      switch (status) {
        case 401:
          localStorage.removeItem('token')
          // TODO: 跳转登录页
          break
        case 403:
          console.error('没有权限')
          break
        case 404:
          console.error('请求的资源不存在')
          break
        case 500:
          console.error('服务器异常')
          break
        default:
          console.error(`请求失败 [${status}]`)
      }
    } else {
      console.error('网络异常，请检查网络连接')
    }
    return Promise.reject(error)
  },
)

export default service
