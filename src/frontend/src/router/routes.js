import MainLayout from '../layouts/MainLayout.vue'

/**
 * 业务菜单配置。
 * meta.roles 为空表示三类角色均可见；存在时仅在服务端角色匹配的情况下渲染。
 * 注意：菜单过滤只是体验优化，真正的权限校验在后端接口上。
 */
export const menuRoutes = [
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('../views/DashboardView.vue'),
    meta: { title: '工作台', icon: 'LayoutDashboard', group: '概览' }
  },
  {
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/ProfileView.vue'),
    meta: { title: '个人资料', icon: 'UserRound', group: '概览' }
  },
  {
    path: '/admin/users',
    name: 'UserManage',
    component: () => import('../views/admin/UserManageView.vue'),
    meta: { title: '用户管理', icon: 'Users', group: '系统管理', roles: ['ADMIN'] }
  }
]

export const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/LoginView.vue'),
    meta: { title: '登录', public: true }
  },
  {
    path: '/',
    component: MainLayout,
    redirect: '/dashboard',
    children: menuRoutes
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/error/403.vue'),
    meta: { title: '无访问权限', public: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/dashboard'
  }
]

/**
 * 按角色过滤可见菜单。
 */
export function resolveMenus(roleCode) {
  return menuRoutes
    .filter((route) => !route.meta?.roles || route.meta.roles.includes(roleCode))
    .map((route) => ({
      path: route.path,
      title: route.meta.title,
      icon: route.meta.icon,
      group: route.meta.group
    }))
}
