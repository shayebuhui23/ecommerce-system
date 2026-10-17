import axios from 'axios'
import { ElMessage } from 'element-plus'
import { getToken, clearAuth } from '../utils/auth'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const token = getToken()
  if (token) {
    config.headers.Authorization = token
  }
  return config
})

request.interceptors.response.use(
  (response) => {
    const result = response.data
    if (!result || typeof result.code === 'undefined') {
      return result
    }
    if (result.code !== 200) {
      ElMessage.error(result.message || '请求处理失败')
      return Promise.reject(new Error(result.message || '请求处理失败'))
    }
    return result.data
  },
  (error) => {
    const status = error.response?.status
    const message = error.response?.data?.message

    if (status === 401) {
      clearAuth()
      ElMessage.error(message || '登录状态已失效，请重新登录')
      if (!window.location.pathname.startsWith('/login')) {
        window.location.assign('/login')
      }
    } else if (status === 403) {
      ElMessage.error(message || '没有权限执行该操作')
    } else {
      ElMessage.error(message || '网络异常，请稍后重试')
    }
    console.error('请求失败', error)
    return Promise.reject(error)
  }
)

export default request
