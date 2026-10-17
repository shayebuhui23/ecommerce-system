<template>
  <div v-loading="loading" class="space-y-4">
    <!-- 毛利仪表盘：仅商家关注成本与毛利，管理员不展示 -->
    <section v-if="isMerchant" class="relative overflow-hidden rounded-3xl bg-ink-800 px-7 py-6 text-white shadow-soft">
      <div class="pointer-events-none absolute inset-0">
        <div class="absolute -right-16 -top-24 h-64 w-64 rounded-full bg-brand-600/40 blur-[90px]" />
        <div class="absolute -bottom-24 left-1/3 h-56 w-56 rounded-full bg-cyan-500/20 blur-[100px]" />
      </div>

      <div class="relative flex flex-wrap items-center gap-8">
        <div class="flex items-center gap-5">
          <div class="relative h-28 w-28 shrink-0 rounded-full" :style="donutStyle">
            <div class="absolute inset-2.5 flex flex-col items-center justify-center rounded-full bg-ink-800">
              <p class="font-mono text-xl leading-none">{{ marginPercent.toFixed(1) }}%</p>
              <p class="mt-1 text-[10px] text-white/45">毛利率</p>
            </div>
          </div>
          <div>
            <p class="text-[11px] uppercase tracking-[0.2em] text-white/40">Gross Profit</p>
            <p class="mt-1 font-mono text-2xl font-semibold">{{ money(data.grossProfit) }}</p>
            <p class="mt-1 text-xs text-white/50">已完成订单毛利（成交 {{ money(data.completedAmount) }} − 成本 {{ money(data.costAmount) }}）</p>
          </div>
        </div>

        <div class="grid min-w-[280px] flex-1 grid-cols-2 gap-3 sm:grid-cols-3">
          <div class="rounded-2xl border border-white/10 bg-white/5 px-4 py-3">
            <p class="text-[11px] text-white/45">成交金额</p>
            <p class="mt-1 font-mono text-lg">{{ money(data.completedAmount) }}</p>
            <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-white/10">
              <div class="h-full rounded-full bg-gradient-to-r from-brand-400 to-cyan-300" style="width: 100%" />
            </div>
          </div>
          <div class="rounded-2xl border border-white/10 bg-white/5 px-4 py-3">
            <p class="text-[11px] text-white/45">商品成本</p>
            <p class="mt-1 font-mono text-lg">{{ money(data.costAmount) }}</p>
            <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-white/10">
              <div class="h-full rounded-full bg-amber-300/80" :style="{ width: `${costPercent}%` }" />
            </div>
          </div>
          <div class="rounded-2xl border border-white/10 bg-white/5 px-4 py-3">
            <p class="text-[11px] text-white/45">毛利</p>
            <p class="mt-1 font-mono text-lg">{{ money(data.grossProfit) }}</p>
            <div class="mt-2 h-1.5 overflow-hidden rounded-full bg-white/10">
              <div class="h-full rounded-full bg-emerald-300/80" :style="{ width: `${marginPercent}%` }" />
            </div>
          </div>
        </div>
      </div>
    </section>

    <section class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <article
        v-for="(item, index) in metrics"
        :key="item.label"
        class="animate-rise rounded-2xl border border-slate-200/70 bg-white p-5 shadow-[0_10px_30px_-24px_rgba(31,36,48,0.5)]"
        :style="{ animationDelay: `${index * 60}ms` }"
      >
        <div class="flex items-center justify-between">
          <p class="text-xs text-slate-400">{{ item.label }}</p>
          <component :is="item.icon" class="h-4 w-4 text-brand-500" />
        </div>
        <p class="mt-3 font-mono text-2xl text-slate-800">{{ item.value }}</p>
        <p class="mt-1 text-[11px] text-slate-400">{{ item.hint }}</p>
      </article>
    </section>

    <section class="grid gap-4 lg:grid-cols-2">
      <div class="rounded-2xl border border-slate-200/70 bg-white p-6">
        <div class="flex items-center justify-between">
          <h3 class="text-[15px] font-medium text-slate-800">订单状态分布</h3>
          <span class="text-[11px] text-slate-400">共 {{ data.orderTotal }} 单</span>
        </div>
        <div class="mt-4 space-y-3">
          <div v-for="item in statusRows" :key="item.label" class="flex items-center gap-3">
            <span class="w-16 shrink-0 text-xs text-slate-500">{{ item.label }}</span>
            <div class="h-2 flex-1 overflow-hidden rounded-full bg-slate-100">
              <div class="h-full rounded-full bg-gradient-to-r from-brand-500 to-cyan-400" :style="{ width: `${item.percent}%` }" />
            </div>
            <span class="w-10 shrink-0 text-right font-mono text-xs text-slate-600">{{ item.count }}</span>
          </div>
        </div>
      </div>

      <div class="rounded-2xl border border-slate-200/70 bg-white p-6">
        <div class="flex items-center justify-between">
          <h3 class="text-[15px] font-medium text-slate-800">热门商品 Top 5</h3>
          <span class="text-[11px] text-slate-400">按累计下单件数</span>
        </div>
        <div class="mt-4 space-y-3">
          <div
            v-for="(item, index) in data.topProducts"
            :key="item.productId"
            class="flex items-center gap-3 rounded-xl border border-slate-100 px-3 py-2.5"
          >
            <span class="flex h-6 w-6 shrink-0 items-center justify-center rounded-full bg-slate-100 font-mono text-[11px] text-slate-500">
              {{ index + 1 }}
            </span>
            <p class="min-w-0 flex-1 truncate text-xs text-slate-700">{{ item.productName }}</p>
            <span class="font-mono text-xs text-brand-600">{{ item.quantity }} 件</span>
          </div>
          <p v-if="!data.topProducts.length" class="py-8 text-center text-xs text-slate-400">还没有成交数据</p>
        </div>
      </div>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-6">
      <h3 class="text-[15px] font-medium text-slate-800">统计口径</h3>
      <p class="mt-2 text-xs leading-6 text-slate-500">
        成交金额统一按订单下单时折算的基础币种（CNY）汇总，不同币种订单不会直接相加；
        商品单价以 CNY 记录，USD 订单在创建时保存汇率快照（{{ exchangeHint }}）。
        <template v-if="isMerchant">毛利 = 已完成订单成交金额 − 商品成本（数量 × 当前成本价），仅统计本店商品。</template>
      </p>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Boxes, CreditCard, PackageCheck, RotateCcw } from 'lucide-vue-next'
import { fetchStatisticsOverview } from '../../api/statistics'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const isMerchant = computed(() => userStore.roleCode === 'STAFF')

const loading = ref(false)
const data = ref({
  productTotal: 0,
  productOnShelf: 0,
  orderTotal: 0,
  completedOrderCount: 0,
  completedAmount: 0,
  costAmount: 0,
  grossProfit: 0,
  grossMargin: 0,
  afterSaleTotal: 0,
  afterSalePending: 0,
  userTotal: 0,
  orderStatusCounts: {},
  topProducts: []
})

function money(value) {
  return `¥${Number(value || 0).toFixed(2)}`
}

const marginPercent = computed(() => {
  const margin = Number(data.value.grossMargin || 0)
  return Math.max(0, Math.min(100, margin))
})

const costPercent = computed(() => {
  const revenue = Number(data.value.completedAmount || 0)
  if (revenue <= 0) return 0
  const cost = Number(data.value.costAmount || 0)
  return Math.max(0, Math.min(100, (cost / revenue) * 100))
})

const donutStyle = computed(() => ({
  background: `conic-gradient(#34d399 0% ${marginPercent.value}%, rgba(255,255,255,0.08) ${marginPercent.value}% 100%)`
}))

const metrics = computed(() => [
  { label: '商品总数', value: data.value.productTotal, hint: `上架 ${data.value.productOnShelf} 个`, icon: Boxes },
  { label: '订单总数', value: data.value.orderTotal, hint: `已完成 ${data.value.completedOrderCount} 单`, icon: PackageCheck },
  { label: '成交金额（CNY）', value: `¥${Number(data.value.completedAmount || 0).toFixed(2)}`, hint: '仅统计已完成订单', icon: CreditCard },
  { label: '售后申请', value: data.value.afterSaleTotal, hint: `待审核 ${data.value.afterSalePending} 条`, icon: RotateCcw }
])

const statusRows = computed(() => {
  const counts = data.value.orderStatusCounts || {}
  const total = Math.max(data.value.orderTotal, 1)
  return Object.entries(counts).map(([label, count]) => ({
    label,
    count,
    percent: Math.round((count / total) * 100)
  }))
})

const exchangeHint = computed(() => '1 CNY = 0.14 USD（可在汇率管理中调整）')

async function loadData() {
  loading.value = true
  const result = await fetchStatisticsOverview().catch((error) => {
    console.error('加载统计失败', error)
    return null
  })
  loading.value = false
  if (result) data.value = result
}

onMounted(loadData)
</script>
