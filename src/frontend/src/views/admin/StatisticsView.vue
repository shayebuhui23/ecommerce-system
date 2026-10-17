<template>
  <div v-loading="loading" class="space-y-4">
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
      </p>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { Boxes, CreditCard, PackageCheck, RotateCcw } from 'lucide-vue-next'
import { fetchStatisticsOverview } from '../../api/statistics'

const loading = ref(false)
const data = ref({
  productTotal: 0,
  productOnShelf: 0,
  orderTotal: 0,
  completedOrderCount: 0,
  completedAmount: 0,
  afterSaleTotal: 0,
  afterSalePending: 0,
  userTotal: 0,
  orderStatusCounts: {},
  topProducts: []
})

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
