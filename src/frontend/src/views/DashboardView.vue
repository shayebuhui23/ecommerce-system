<template>
  <div class="space-y-5">
    <div v-loading="loading" class="space-y-5">
      <p v-if="isMerchant" class="flex items-center gap-1.5 text-xs text-slate-400">
        <Store class="h-3.5 w-3.5" />
        以下数据仅统计本店商品与相关订单
      </p>

      <section
        class="grid gap-4 sm:grid-cols-2"
        :class="isMerchant ? 'xl:grid-cols-4' : 'xl:grid-cols-3'"
      >
        <article
          v-for="(item, index) in metrics"
          :key="item.label"
          class="animate-rise rounded-2xl border border-slate-200/70 bg-white p-5 shadow-[0_10px_30px_-24px_rgba(31,36,48,0.5)] transition-all duration-200 hover:-translate-y-0.5 hover:shadow-soft"
          :style="{ animationDelay: `${index * 70}ms` }"
        >
          <div class="flex items-center justify-between">
            <p class="text-xs text-slate-400">{{ item.label }}</p>
            <component :is="item.icon" class="h-4 w-4 text-brand-500" />
          </div>
          <p class="mt-3 font-mono text-2xl text-slate-800">{{ item.value }}</p>
          <p class="mt-1 text-[11px] text-slate-400">{{ item.hint }}</p>
        </article>
      </section>

      <section class="grid gap-4 lg:grid-cols-3">
        <div class="rounded-2xl border border-slate-200/70 bg-white lg:col-span-2">
          <div class="flex items-center justify-between px-6 pb-2 pt-6">
            <div>
              <h3 class="text-[15px] font-medium text-slate-800">最近订单</h3>
              <p class="mt-1 text-xs text-slate-400">最新 5 笔订单的履约状态</p>
            </div>
            <router-link
              to="/admin/orders"
              class="flex items-center gap-1 text-xs text-brand-600 transition-colors hover:text-brand-700"
            >
              全部订单
              <ArrowUpRight class="h-3.5 w-3.5" />
            </router-link>
          </div>

          <div v-if="recentOrders.length" class="mt-2">
            <div
              v-for="order in recentOrders"
              :key="order.id"
              class="flex flex-wrap items-center gap-3 border-t border-slate-100 px-6 py-3"
            >
              <div class="min-w-0 flex-1">
                <p class="truncate font-mono text-xs text-slate-700">{{ order.orderNo }}</p>
                <p class="mt-0.5 text-[11px] text-slate-400">
                  {{ order.consignee || '—' }} · {{ formatTime(order.createdAt) }}
                </p>
              </div>
              <span class="font-mono text-xs text-slate-700">{{ money(order.currency, order.payAmount) }}</span>
              <span class="shrink-0 rounded-full border px-2.5 py-1 text-[11px]" :class="statusClass(order.status)">
                {{ order.statusText || order.status }}
              </span>
            </div>
          </div>
          <div v-else class="flex flex-col items-center gap-2 py-14">
            <ClipboardList class="h-6 w-6 text-slate-300" />
            <p class="text-xs text-slate-400">暂时还没有订单</p>
          </div>
        </div>

        <div class="rounded-2xl border border-slate-200/70 bg-white p-6">
          <div class="flex items-center justify-between">
            <h3 class="text-[15px] font-medium text-slate-800">订单状态分布</h3>
            <router-link
              to="/admin/statistics"
              class="flex items-center gap-1 text-xs text-brand-600 transition-colors hover:text-brand-700"
            >
              经营统计
              <ArrowUpRight class="h-3.5 w-3.5" />
            </router-link>
          </div>
          <p class="mt-1 text-xs text-slate-400">共 {{ stats.orderTotal }} 笔订单</p>

          <div class="mt-4 space-y-3">
            <div v-for="item in statusRows" :key="item.label" class="flex items-center gap-3">
              <span class="w-16 shrink-0 text-xs text-slate-500">{{ item.label }}</span>
              <div class="h-2 flex-1 overflow-hidden rounded-full bg-slate-100">
                <div
                  class="h-full rounded-full bg-gradient-to-r from-brand-500 to-cyan-400"
                  :style="{ width: `${item.percent}%` }"
                />
              </div>
              <span class="w-10 shrink-0 text-right font-mono text-xs text-slate-600">{{ item.count }}</span>
            </div>
          </div>
        </div>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import {
  ArrowUpRight,
  Boxes,
  ClipboardList,
  RotateCcw,
  Store,
  Users,
  Wallet
} from 'lucide-vue-next'
import { fetchStatisticsOverview } from '../api/statistics'
import { fetchOrderPage } from '../api/order'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const loading = ref(false)
const stats = ref({
  productTotal: 0,
  productOnShelf: 0,
  orderTotal: 0,
  completedOrderCount: 0,
  completedAmount: 0,
  afterSaleTotal: 0,
  afterSalePending: 0,
  userTotal: 0,
  merchantTotal: 0,
  orderStatusCounts: {},
  topProducts: []
})
const recentOrders = ref([])

// 客户不使用工作台，路由已重定向到商品浏览
const isMerchant = computed(() => userStore.roleCode === 'STAFF')

const metrics = computed(() => {
  const items = [
    {
      label: '商品总数',
      value: stats.value.productTotal,
      hint: `在售 ${stats.value.productOnShelf} 个`,
      icon: Boxes
    }
  ]
  if (!isMerchant.value) {
    items.push(
      { label: '商家总数', value: stats.value.merchantTotal, hint: '已入驻的商家账号', icon: Store },
      { label: '用户总数', value: stats.value.userTotal, hint: '已注册的普通用户', icon: Users }
    )
  }
  items.push(
    {
      label: '订单总数',
      value: stats.value.orderTotal,
      hint: `已完成 ${stats.value.completedOrderCount} 单`,
      icon: ClipboardList
    },
    {
      label: '成交金额（CNY）',
      value: `¥${Number(stats.value.completedAmount || 0).toFixed(2)}`,
      hint: '仅统计已完成订单',
      icon: Wallet
    },
    {
      label: '待审核售后',
      value: stats.value.afterSalePending,
      hint: `累计售后 ${stats.value.afterSaleTotal} 条`,
      icon: RotateCcw
    }
  )
  return items
})

const statusRows = computed(() => {
  const counts = stats.value.orderStatusCounts || {}
  const total = Math.max(stats.value.orderTotal || 0, 1)
  return Object.entries(counts).map(([label, count]) => ({
    // 履约口径统一使用「打包中」，与商家实际操作一致
    label: label === '待发货' ? '打包中' : label,
    count,
    percent: Math.round((count / total) * 100)
  }))
})

function money(currency, amount) {
  const symbol = currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(amount || 0).toFixed(2)}`
}

function formatTime(value) {
  if (!value) return '—'
  return String(value).replace('T', ' ').slice(0, 16)
}

function statusClass(code) {
  const map = {
    PENDING_PAYMENT: 'border-amber-200 bg-amber-50 text-amber-700',
    PENDING_SHIPMENT: 'border-indigo-200 bg-indigo-50 text-indigo-600',
    SHIPPED: 'border-sky-200 bg-sky-50 text-sky-600',
    COMPLETED: 'border-emerald-200 bg-emerald-50 text-emerald-600',
    CANCELLED: 'border-slate-200 bg-slate-50 text-slate-500',
    AFTER_SALES: 'border-orange-200 bg-orange-50 text-orange-600',
    REFUNDED: 'border-rose-200 bg-rose-50 text-rose-600'
  }
  return map[code] || 'border-slate-200 bg-slate-50 text-slate-500'
}

async function loadDashboard() {
  loading.value = true
  const [overview, orderPage] = await Promise.all([
    fetchStatisticsOverview().catch((error) => {
      console.error('加载经营概览失败', error)
      return null
    }),
    fetchOrderPage({ pageNum: 1, pageSize: 5 }).catch((error) => {
      console.error('加载最近订单失败', error)
      return null
    })
  ])
  loading.value = false
  if (overview) stats.value = overview
  if (orderPage) recentOrders.value = orderPage.records || []
}

onMounted(loadDashboard)
</script>
