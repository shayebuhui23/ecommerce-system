<template>
  <div class="space-y-4">
    <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 class="text-[15px] font-medium text-slate-800">{{ pageTitle }}</h2>
          <p class="mt-1 text-xs text-slate-400">{{ pageHint }}</p>
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
          :class="isActiveTab(tab) ? 'bg-brand-600 text-white' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
          @click="selectTab(tab)"
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
          <StatusTag :text="displayStatus(order)" :tone="statusTone(order.status)" />
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
              v-if="!isMerchant && order.status === 'PENDING_PAYMENT'"
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
        <p class="text-sm text-slate-500">{{ isMerchant ? '还没有客户订单' : '还没有订单' }}</p>
        <router-link v-if="!isMerchant" to="/shop" class="text-xs text-brand-600 hover:underline">去挑几件商品</router-link>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ReceiptText, Search } from 'lucide-vue-next'
import { cancelOrder, fetchOrderPage } from '../../api/order'
import { useUserStore } from '../../stores/user'
import StatusTag from '../../components/StatusTag.vue'
import OrderDetailDrawer from './components/OrderDetailDrawer.vue'

const userStore = useUserStore()
const isMerchant = computed(() => userStore.roleCode === 'STAFF')

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const keyword = ref('')
const detailVisible = ref(false)
const detailId = ref(null)
const query = reactive({ pageNum: 1, pageSize: 10, status: '', afterSale: false })

const pageTitle = computed(() => (isMerchant.value ? '客户订单' : '我的订单'))
const pageHint = computed(() =>
  isMerchant.value
    ? '查看本店商品相关订单，已付款订单可直接发货'
    : '下单后进入待付款状态，待付款订单可取消并自动恢复库存'
)

const tabs = computed(() =>
  isMerchant.value
    ? [
        { label: '全部', value: '' },
        { label: '待处理', value: 'PENDING_SHIPMENT' },
        { label: '待付款', value: 'PENDING_PAYMENT' },
        { label: '已发货', value: 'SHIPPED' },
        { label: '已完成', value: 'COMPLETED' },
        { label: '已取消', value: 'CANCELLED' }
      ]
    : [
        { label: '全部', value: '' },
        { label: '待付款', value: 'PENDING_PAYMENT' },
        { label: '待发货', value: 'PENDING_SHIPMENT' },
        { label: '待收货', value: 'SHIPPED' },
        { label: '已完成', value: 'COMPLETED' },
        { label: '已取消', value: 'CANCELLED' },
        { label: '售后处理', value: '', afterSale: true }
      ]
)

function money(currency, amount) {
  const symbol = currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(amount || 0).toFixed(2)}`
}

function statusTone(status) {
  const map = {
    PENDING_PAYMENT: 'amber',
    PENDING_SHIPMENT: 'blue',
    SHIPPED: 'sky',
    COMPLETED: 'emerald',
    CANCELLED: 'slate',
    AFTER_SALES: 'orange',
    REFUNDED: 'rose'
  }
  return map[status] || 'slate'
}

function displayStatus(order) {
  if (isMerchant.value) return order.statusText
  if (order.status === 'SHIPPED') return '待收货'
  return order.statusText
}

async function loadOrders() {
  loading.value = true
  const data = await fetchOrderPage({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    status: query.afterSale ? undefined : query.status || undefined,
    afterSale: query.afterSale || undefined,
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

function isActiveTab(tab) {
  if (tab.afterSale) return query.afterSale
  return !query.afterSale && query.status === tab.value
}

function selectTab(tab) {
  query.status = tab.value
  query.afterSale = Boolean(tab.afterSale)
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
