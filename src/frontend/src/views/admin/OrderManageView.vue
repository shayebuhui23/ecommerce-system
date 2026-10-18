<template>
  <div class="space-y-4">
    <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 class="text-[15px] font-medium text-slate-800">订单管理</h2>
          <p class="mt-1 text-xs text-slate-400">按商家查看客户订单与履约状态，发货与售后由商家处理</p>
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
          <el-select v-if="isAdmin" v-model="sellerId" placeholder="全部商家" clearable filterable class="!w-44" @change="handleSearch">
            <el-option v-for="item in merchants" :key="item.id" :value="item.id" :label="item.nickname || item.username" />
          </el-select>
          <el-select v-model="status" placeholder="全部状态" clearable class="!w-36" @change="handleSearch">
            <el-option v-for="item in statusOptions" :key="item.value" :value="item.value" :label="item.label" />
          </el-select>
          <button
            type="button"
            class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:-translate-y-0.5 hover:shadow-md active:translate-y-0"
            @click="handleSearch"
          >
            查询
          </button>
        </div>
      </div>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-2">
      <el-table
        v-loading="loading"
        :data="rows"
        :header-cell-style="{ background: '#F8FAFC', color: '#64748B', fontWeight: 500, fontSize: '12px' }"
      >
        <el-table-column prop="orderNo" label="订单号" min-width="190" />
        <el-table-column label="客户用户名" min-width="120">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ row.customerUsername || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="件数" width="80">
          <template #default="{ row }">
            <span class="text-xs text-slate-600">{{ row.itemCount }} 件</span>
          </template>
        </el-table-column>
        <el-table-column label="应付金额" min-width="110">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-700">{{ money(row.currency, row.payAmount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <StatusTag :text="row.statusText" :tone="statusTone(row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="下单时间" min-width="160" />
        <el-table-column label="操作" :width="isAdmin ? 100 : 160" fixed="right" align="left" header-align="left">
          <template #default="{ row }">
            <div class="-ml-2 flex h-7 items-center gap-1">
              <button
                type="button"
                class="inline-flex h-7 items-center rounded-lg px-2 text-xs text-brand-600 transition-colors hover:bg-brand-50"
                @click="openDetail(row.id)"
              >
                详情
              </button>
              <!-- 管理员只查看详情，发货由商家在客户订单中完成 -->
              <button
                v-if="!isAdmin && row.status === 'PENDING_SHIPMENT'"
                type="button"
                class="inline-flex h-7 items-center rounded-lg px-2 text-xs text-emerald-600 transition-colors hover:bg-emerald-50"
                @click="openShip(row)"
              >
                发货
              </button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-10 text-xs text-slate-400">没有匹配的订单</div>
        </template>
      </el-table>

      <div class="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
        <p class="text-xs text-slate-400">共 {{ total }} 个订单</p>
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
    </section>

    <OrderDetailDrawer v-model="detailVisible" :order-id="detailId" @updated="loadOrders" />
    <ShipDialog v-model="shipVisible" :order="shippingOrder" @shipped="loadOrders" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { Search } from 'lucide-vue-next'
import { fetchOrderPage } from '../../api/order'
import { fetchUserPage } from '../../api/user'
import { useUserStore } from '../../stores/user'
import StatusTag from '../../components/StatusTag.vue'
import OrderDetailDrawer from '../orders/components/OrderDetailDrawer.vue'
import ShipDialog from './components/ShipDialog.vue'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.roleCode === 'ADMIN')

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const keyword = ref('')
const status = ref('')
const sellerId = ref(null)
const merchants = ref([])
const detailVisible = ref(false)
const detailId = ref(null)
const shipVisible = ref(false)
const shippingOrder = ref(null)
const query = reactive({ pageNum: 1, pageSize: 10 })

const statusOptions = [
  { label: '待付款', value: 'PENDING_PAYMENT' },
  { label: '待发货', value: 'PENDING_SHIPMENT' },
  { label: '已发货', value: 'SHIPPED' },
  { label: '已完成', value: 'COMPLETED' },
  { label: '已取消', value: 'CANCELLED' },
  { label: '售后处理中', value: 'AFTER_SALES' },
  { label: '已退款', value: 'REFUNDED' }
]

function money(currency, amount) {
  const symbol = currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(amount || 0).toFixed(2)}`
}

function statusTone(code) {
  const map = {
    PENDING_PAYMENT: 'amber',
    PENDING_SHIPMENT: 'blue',
    SHIPPED: 'sky',
    COMPLETED: 'emerald',
    CANCELLED: 'slate',
    AFTER_SALES: 'orange',
    REFUNDED: 'rose'
  }
  return map[code] || 'slate'
}

let loadSeq = 0

async function loadOrders() {
  const seq = ++loadSeq
  loading.value = true
  const data = await fetchOrderPage({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    status: status.value || undefined,
    keyword: keyword.value.trim() || undefined,
    sellerId: isAdmin.value ? sellerId.value || undefined : undefined
  }).catch((error) => {
    console.error('加载订单失败', error)
    return null
  })
  // 只采纳最后一次查询的结果，避免筛选/翻页连续操作时旧响应覆盖新列表
  if (seq !== loadSeq) return
  loading.value = false
  if (!data) return
  rows.value = data.records
  total.value = data.total
}

function handleSearch() {
  query.pageNum = 1
  loadOrders()
}

function openDetail(id) {
  detailId.value = id
  detailVisible.value = true
}

function openShip(row) {
  shippingOrder.value = row
  shipVisible.value = true
}

async function loadMerchants() {
  if (!isAdmin.value) return
  const data = await fetchUserPage({ pageNum: 1, pageSize: 100, roleCode: 'STAFF' }).catch((error) => {
    console.error('加载商家列表失败', error)
    return null
  })
  if (data) merchants.value = data.records || []
}

onMounted(() => {
  loadMerchants()
  loadOrders()
})
</script>
