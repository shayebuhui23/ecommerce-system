<template>
  <div class="space-y-4">
    <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 class="text-[15px] font-medium text-slate-800">我的订单</h2>
          <p class="mt-1 text-xs text-slate-400">下单后进入待付款状态，待付款订单可取消并自动恢复库存</p>
        </div>
        <div class="flex items-center gap-2">
          <div class="flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50/60 px-3 py-1.5 transition-colors focus-within:border-brand-300 focus-within:bg-white">
            <Search class="h-3.5 w-3.5 text-slate-400" />
            <input
              v-model="keyword"
              type="text"
              placeholder="搜索订单号"
              class="w-44 border-0 bg-transparent p-0 text-xs text-slate-700 outline-none placeholder:text-slate-400"
              @keyup.enter="handleSearch"
            />
          </div>
          <button
            type="button"
            class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:-translate-y-0.5 hover:shadow-md active:translate-y-0"
            @click="handleSearch"
          >
            查询
          </button>
        </div>
      </div>

      <div class="mt-4 flex flex-wrap gap-1.5">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          type="button"
          class="rounded-full px-3 py-1.5 text-xs transition-colors"
          :class="query.status === tab.value ? 'bg-brand-600 text-white' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
          @click="selectStatus(tab.value)"
        >
          {{ tab.label }}
        </button>
      </div>
    </section>

    <section v-loading="loading" class="space-y-3">
      <article
        v-for="order in rows"
        :key="order.id"
        class="rounded-2xl border border-slate-200/70 bg-white p-5 transition-all hover:-translate-y-0.5 hover:shadow-soft"
      >
        <div class="flex flex-wrap items-start justify-between gap-3">
          <div>
            <p class="font-mono text-xs text-slate-400">{{ order.orderNo }}</p>
            <p class="mt-1.5 text-sm text-slate-800">
              {{ order.itemCount }} 件商品
              <span class="ml-2 text-xs text-slate-400">{{ order.consignee }} · {{ order.country }}</span>
            </p>
          </div>
          <span class="rounded-full border px-3 py-1 text-[11px]" :class="statusClass(order.status)">
            {{ order.statusText }}
          </span>
        </div>

        <div class="mt-4 flex flex-wrap items-end justify-between gap-3">
          <p class="text-[11px] text-slate-400">{{ order.createdAt }}</p>
          <div class="flex items-center gap-4">
            <p class="font-mono text-lg text-brand-600">{{ money(order.currency, order.payAmount) }}</p>
            <button
              type="button"
              class="rounded-lg border border-slate-200 px-3 py-1.5 text-xs text-slate-600 transition-colors hover:border-brand-300 hover:text-brand-600"
              @click="openDetail(order.id)"
            >
              查看详情
            </button>
            <button
              v-if="order.status === 'PENDING_PAYMENT'"
              type="button"
              class="rounded-lg px-3 py-1.5 text-xs text-rose-500 transition-colors hover:bg-rose-50"
              @click="cancel(order)"
            >
              取消订单
            </button>
          </div>
        </div>
      </article>

      <div
        v-if="!rows.length && !loading"
        class="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-slate-200 bg-white py-16"
      >
        <ReceiptText class="h-7 w-7 text-slate-300" />
        <p class="text-sm text-slate-500">还没有订单</p>
        <router-link to="/shop" class="text-xs text-brand-600 hover:underline">去挑几件商品</router-link>
      </div>
    </section>

    <div v-if="total > 0" class="flex justify-center pb-2">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[10, 20, 50]"
        :total="total"
        layout="sizes, prev, pager, next"
        background
        @size-change="loadOrders"
        @current-change="loadOrders"
      />
    </div>

    <OrderDetailDrawer v-model="detailVisible" :order-id="detailId" @updated="loadOrders" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ReceiptText, Search } from 'lucide-vue-next'
import { cancelOrder, fetchOrderPage } from '../../api/order'
import OrderDetailDrawer from './components/OrderDetailDrawer.vue'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const keyword = ref('')
const detailVisible = ref(false)
const detailId = ref(null)
const query = reactive({ pageNum: 1, pageSize: 10, status: '' })

const tabs = [
  { label: '全部', value: '' },
  { label: '待付款', value: 'PENDING_PAYMENT' },
  { label: '待发货', value: 'PENDING_SHIPMENT' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' }
]

function money(currency, amount) {
  const symbol = currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(amount || 0).toFixed(2)}`
}

function statusClass(status) {
  const map = {
    PENDING_PAYMENT: 'border-amber-200 bg-amber-50 text-amber-700',
    PENDING_SHIPMENT: 'border-blue-200 bg-blue-50 text-blue-700',
    SHIPPED: 'border-indigo-200 bg-indigo-50 text-indigo-700',
    COMPLETED: 'border-emerald-200 bg-emerald-50 text-emerald-700',
    CANCELLED: 'border-slate-200 bg-slate-50 text-slate-500',
    AFTER_SALES: 'border-orange-200 bg-orange-50 text-orange-700',
    REFUNDED: 'border-rose-200 bg-rose-50 text-rose-700'
  }
  return map[status] || 'border-slate-200 bg-slate-50 text-slate-500'
}

async function loadOrders() {
  loading.value = true
  const data = await fetchOrderPage({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    status: query.status || undefined,
    keyword: keyword.value.trim() || undefined
  }).catch((error) => {
    console.error('加载订单失败', error)
    return null
  })
  loading.value = false
  if (!data) return
  rows.value = data.records
  total.value = data.total
}

function handleSearch() {
  query.pageNum = 1
  loadOrders()
}

function selectStatus(status) {
  query.status = status
  query.pageNum = 1
  loadOrders()
}

function openDetail(id) {
  detailId.value = id
  detailVisible.value = true
}

async function cancel(order) {
  const confirmed = await ElMessageBox.confirm('确认取消该订单？取消后库存将自动恢复。', '取消订单', {
    confirmButtonText: '取消订单',
    cancelButtonText: '再想想',
    type: 'warning'
  })
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await cancelOrder(order.id)
    .then(() => true)
    .catch((error) => {
      console.error('取消订单失败', error)
      return false
    })
  if (!ok) return
  ElMessage.success('订单已取消')
  loadOrders()
}

onMounted(loadOrders)
</script>
