import MainLayout from '../layouts/MainLayout.vue'

/**
 * 业务菜单配置。
 * meta.roles 为空表示三类角色均可见；存在时仅在服务端角色匹配的情况下渲染。
 * meta.hidden 为 true 的页面不进入侧边菜单（如结算页）。
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
    path: '/shop',
    name: 'Shop',
    component: () => import('../views/shop/ShopView.vue'),
    meta: { title: '商品浏览', icon: 'Store', group: '商城' }
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('../views/CartView.vue'),
    meta: { title: '购物车', icon: 'ShoppingCart', group: '商城' }
  },
  {
    path: '/checkout',
    name: 'Checkout',
    component: () => import('../views/CheckoutView.vue'),
    meta: { title: '确认订单', icon: 'ReceiptText', group: '商城', hidden: true }
  },
  {
    path: '/orders',
    name: 'Orders',
    component: () => import('../views/orders/OrdersView.vue'),
    meta: { title: '我的订单', icon: 'ClipboardList', group: '我的' }
  },
  {
    path: '/addresses',
    name: 'Addresses',
    component: () => import('../views/AddressView.vue'),
    meta: { title: '收货地址', icon: 'MapPin', group: '我的' }
  },
  {
    path: '/admin/products',
    name: 'ProductManage',
    component: () => import('../views/admin/ProductManageView.vue'),
    meta: { title: '商品管理', icon: 'Package', group: '商家管理', roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/admin/categories',
    name: 'CategoryManage',
    component: () => import('../views/admin/CategoryManageView.vue'),
    meta: { title: '分类管理', icon: 'FolderTree', group: '商家管理', roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/admin/orders',
    name: 'OrderManage',
    component: () => import('../views/admin/OrderManageView.vue'),
    meta: { title: '订单管理', icon: 'ClipboardList', group: '商家管理', roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/admin/after-sales',
    name: 'AfterSaleManage',
    component: () => import('../views/admin/AfterSaleManageView.vue'),
    meta: { title: '售后管理', icon: 'RotateCcw', group: '商家管理', roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/admin/statistics',
    name: 'Statistics',
    component: () => import('../views/admin/StatisticsView.vue'),
    meta: { title: '经营统计', icon: 'BarChart3', group: '商家管理', roles: ['ADMIN', 'STAFF'] }
  },
  {
    path: '/admin/users',
    name: 'UserManage',
    component: () => import('../views/admin/UserManageView.vue'),
    meta: { title: '用户管理', icon: 'Users', group: '系统管理', roles: ['ADMIN'] }
  },
  {
    path: '/admin/exchange-rates',
    name: 'ExchangeRates',
    component: () => import('../views/admin/ExchangeRateView.vue'),
    meta: { title: '汇率管理', icon: 'Coins', group: '系统管理', roles: ['ADMIN'] }
  },
  {
    path: '/admin/operation-logs',
    name: 'OperationLogs',
    component: () => import('../views/admin/OperationLogView.vue'),
    meta: { title: '操作日志', icon: 'ScrollText', group: '系统管理', roles: ['ADMIN'] }
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
 * 按角色过滤可见菜单，并排除隐藏页面。
 */
export function resolveMenus(roleCode) {
  return menuRoutes
    .filter((route) => !route.meta?.hidden)
    .filter((route) => !route.meta?.roles || route.meta.roles.includes(roleCode))
    .map((route) => ({
      path: route.path,
      title: route.meta.title,
      icon: route.meta.icon,
      group: route.meta.group
    }))
}
