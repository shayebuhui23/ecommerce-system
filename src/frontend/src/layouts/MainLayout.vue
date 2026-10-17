<template>
  <div class="min-h-screen bg-[#F5F7FB]">
    <aside
      class="fixed inset-y-0 left-0 z-30 flex flex-col bg-ink-800 shadow-[8px_0_30px_-24px_rgba(11,14,26,0.9)] transition-[width] duration-300"
      :class="collapsed ? 'w-[76px]' : 'w-[248px] max-lg:w-[76px]'"
    >
      <div class="flex h-[68px] items-center gap-3 border-b border-white/5 px-5">
        <div class="flex h-9 w-9 shrink-0 items-center justify-center rounded-xl bg-gradient-to-br from-brand-500 to-cyan-500 text-sm font-semibold text-white">
          E
        </div>
        <div v-if="!collapsed" class="min-w-0">
          <p class="truncate text-sm font-medium text-white">订单运营中枢</p>
          <p class="font-mono text-[10px] uppercase tracking-[0.18em] text-white/35">Ecommerce Ops</p>
        </div>
      </div>

      <nav class="flex-1 overflow-y-auto px-3 py-4">
        <div v-for="group in menuGroups" :key="group.name" class="mb-5">
          <p v-if="!collapsed" class="mb-2 px-3 text-[10px] uppercase tracking-[0.2em] text-white/25">
            {{ group.name }}
          </p>
          <router-link
            v-for="item in group.items"
            :key="item.path"
            :to="item.path"
            :title="item.title"
            class="group relative mb-1 flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm transition-all duration-200"
            :class="
              isActive(item.path)
                ? 'bg-gradient-to-r from-brand-600/30 to-transparent text-white'
                : 'text-white/55 hover:bg-white/5 hover:text-white/90'
            "
          >
            <span
              v-if="isActive(item.path)"
              class="absolute left-0 top-1/2 h-5 w-[3px] -translate-y-1/2 rounded-r-full bg-gradient-to-b from-brand-300 to-cyan-400"
            />
            <component :is="iconMap[item.icon] || LayoutDashboard" class="h-[18px] w-[18px] shrink-0" />
            <span v-if="!collapsed" class="truncate">{{ item.title }}</span>
          </router-link>
        </div>

        <div class="mb-5">
          <p v-if="!collapsed" class="mb-2 px-3 text-[10px] uppercase tracking-[0.2em] text-white/25">业务模块 · 建设中</p>
          <div
            v-for="item in pendingModules"
            :key="item.title"
            class="mb-1 flex cursor-not-allowed items-center gap-3 rounded-xl px-3 py-2.5 text-sm text-white/25"
            :title="`${item.title}（后续阶段开放）`"
          >
            <component :is="item.icon" class="h-[18px] w-[18px] shrink-0" />
            <span v-if="!collapsed" class="truncate">{{ item.title }}</span>
          </div>
        </div>
      </nav>

      <div class="border-t border-white/5 px-4 py-3">
        <p v-if="!collapsed" class="font-mono text-[10px] uppercase tracking-[0.18em] text-white/25">v1.0 · 骨架阶段</p>
        <p v-else class="text-center font-mono text-[10px] text-white/25">v1</p>
      </div>
    </aside>

    <div class="flex min-h-screen flex-col transition-[padding] duration-300" :class="collapsed ? 'pl-[76px]' : 'pl-[248px] max-lg:pl-[76px]'">
      <header class="sticky top-0 z-20 flex h-[68px] items-center justify-between gap-4 border-b border-slate-200/80 bg-white/85 px-6 backdrop-blur-md">
        <div class="flex min-w-0 items-center gap-3">
          <button
            type="button"
            class="flex h-9 w-9 items-center justify-center rounded-lg text-slate-500 transition-colors hover:bg-slate-100 hover:text-slate-800"
            :aria-label="collapsed ? '展开侧边栏' : '收起侧边栏'"
            @click="collapsed = !collapsed"
          >
            <PanelLeftOpen v-if="collapsed" class="h-4 w-4" />
            <PanelLeftClose v-else class="h-4 w-4" />
          </button>
          <div class="h-5 w-px bg-slate-200" />
          <div class="min-w-0">
            <p class="truncate text-[15px] font-medium text-slate-800">{{ currentTitle }}</p>
            <p class="truncate text-[11px] text-slate-400">{{ currentSubtitle }}</p>
          </div>
        </div>

        <div class="flex items-center gap-3">
          <RoleTag :code="userStore.roleCode" :name="userStore.roleName" tone="light" />
          <el-dropdown trigger="click" @command="handleCommand">
            <button
              type="button"
              class="flex items-center gap-2 rounded-full border border-slate-200 bg-white py-1 pl-1 pr-3 transition-all hover:border-brand-300 hover:shadow-sm"
            >
              <span class="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-cyan-500 text-xs font-medium text-white">
                {{ userStore.displayName.slice(0, 1).toUpperCase() }}
              </span>
              <span class="max-w-[120px] truncate text-xs text-slate-600">{{ userStore.displayName }}</span>
              <ChevronDown class="h-3.5 w-3.5 text-slate-400" />
            </button>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">
                  <UserRound class="mr-2 inline h-3.5 w-3.5" />个人资料
                </el-dropdown-item>
                <el-dropdown-item command="logout" divided>
                  <LogOut class="mr-2 inline h-3.5 w-3.5" />退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <main class="flex-1 px-6 py-6">
        <router-view v-slot="{ Component }">
          <transition name="page-fade" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>

      <footer class="px-6 pb-5 text-center text-[11px] text-slate-400">
        小型电商订单管理系统 · 课程实践项目 · 数据仅用于功能演示
      </footer>
    </div>
  </div>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  BarChart3,
  Boxes,
  ChevronDown,
  LayoutDashboard,
  LogOut,
  PanelLeftClose,
  PanelLeftOpen,
  ReceiptText,
  RotateCcw,
  ShoppingBag,
  Truck,
  UserRound,
  Users
} from 'lucide-vue-next'
import RoleTag from '../components/RoleTag.vue'
import { resolveMenus } from '../router/routes'
import { useUserStore } from '../stores/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const collapsed = ref(false)

const iconMap = { LayoutDashboard, UserRound, Users }

const pendingModules = [
  { title: '商品与分类', icon: Boxes },
  { title: '购物车与订单', icon: ShoppingBag },
  { title: '支付与履约', icon: ReceiptText },
  { title: '物流跟踪', icon: Truck },
  { title: '售后与退款', icon: RotateCcw },
  { title: '经营统计', icon: BarChart3 }
]

const menuGroups = computed(() => {
  const menus = resolveMenus(userStore.roleCode)
  const map = new Map()
  menus.forEach((item) => {
    const key = item.group || '概览'
    if (!map.has(key)) map.set(key, [])
    map.get(key).push(item)
  })
  return Array.from(map, ([name, items]) => ({ name, items }))
})

const currentTitle = computed(() => route.meta.title || '工作台')
const currentSubtitle = computed(() => {
  const subtitleMap = {
    Dashboard: '订单履约与经营概览',
    Profile: '账号与角色信息',
    UserManage: '账号启用、禁用与角色查看'
  }
  return subtitleMap[route.name] || '跨境订单运营中枢'
})

function isActive(path) {
  return route.path === path
}

async function handleCommand(command) {
  if (command === 'profile') {
    router.push('/profile')
    return
  }
  if (command === 'logout') {
    const confirmed = await ElMessageBox.confirm('确认退出当前登录？', '退出登录', {
      confirmButtonText: '退出',
      cancelButtonText: '取消',
      type: 'warning'
    })
      .then(() => true)
      .catch(() => false)
    if (!confirmed) return

    await userStore.logout()
    ElMessage.success('已退出登录')
    router.replace('/login')
  }
}
</script>
