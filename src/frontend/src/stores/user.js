import { defineStore } from 'pinia'
import { login as loginApi, logout as logoutApi, getCurrentUser } from '../api/auth'
import { getToken, setToken, getUserInfo, setUserInfo, clearAuth } from '../utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userInfo: getUserInfo()
  }),

  getters: {
    isLogin: (state) => Boolean(state.token),
    roleCode: (state) => state.userInfo?.roleCode || '',
    roleName: (state) => state.userInfo?.roleName || '',
    displayName: (state) => state.userInfo?.nickname || state.userInfo?.username || '未登录'
  },

  actions: {
    async login(payload) {
      const data = await loginApi(payload)
      this.token = data.token
      this.userInfo = data.userInfo
      setToken(data.token)
      setUserInfo(data.userInfo)
      return data
    },

    async refreshUserInfo() {
      const data = await getCurrentUser()
      this.userInfo = data
      setUserInfo(data)
      return data
    },

    async logout() {
      try {
        await logoutApi()
      } catch (error) {
        console.error('退出登录接口调用失败', error)
      }
      this.reset()
    },

    reset() {
      this.token = ''
      this.userInfo = null
      clearAuth()
    }
  }
})
