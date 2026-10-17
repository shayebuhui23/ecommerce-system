import { createRouter, createWebHistory } from 'vue-router'
import { routes } from './routes'
import { useUserStore } from '../stores/user'

const router = createRouter({
  history: createWebHistory(),
  routes,
  scrollBehavior: () => ({ top: 0 })
})

router.beforeEach(async (to) => {
  const userStore = useUserStore()

  if (to.meta.public) {
    if (to.path === '/login' && userStore.isLogin) {
      return { path: '/dashboard' }
    }
    return true
  }

  if (!userStore.isLogin) {
    return {
      path: '/login',
      query: to.fullPath === '/' ? {} : { redirect: to.fullPath }
    }
  }

  // 刷新页面后本地可能只有 token，需要补拉一次用户信息与角色
  if (!userStore.userInfo?.roleCode) {
    const info = await userStore.refreshUserInfo().catch(() => null)
    if (!info) {
      userStore.reset()
      return { path: '/login' }
    }
  }

  // 客户没有工作台，默认进入商品浏览
  if (to.name === 'Dashboard' && userStore.roleCode === 'CUSTOMER') {
    return { path: '/shop' }
  }

  const requiredRoles = to.meta.roles
  if (Array.isArray(requiredRoles) && requiredRoles.length && !requiredRoles.includes(userStore.roleCode)) {
    return { path: '/403' }
  }

  return true
})

router.afterEach((to) => {
  const userStore = useUserStore()
  let title = to.meta.title
  if (userStore.roleCode === 'STAFF' && to.name === 'Orders') title = '客户订单'
  if (userStore.roleCode === 'STAFF' && to.name === 'Addresses') title = '发货地址'
  document.title = title ? `${title} · 跨境订单运营中枢` : '跨境订单运营中枢'
})

export default router
