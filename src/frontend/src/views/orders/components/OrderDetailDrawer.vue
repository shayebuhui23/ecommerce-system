<template>
  <el-drawer v-model="visible" size="540px" :with-header="false">
    <div v-loading="loading" class="pb-4">
      <template v-if="order">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-[11px] uppercase tracking-[0.18em] text-slate-400">订单编号</p>
            <p class="mt-1 font-mono text-sm text-slate-800">{{ order.orderNo }}</p>
          </div>
          <span class="rounded-full border px-3 py-1 text-[11px]" :class="statusClass(order.status)">
            {{ order.statusText }}
          </span>
        </div>

        <div class="mt-5 rounded-2xl bg-slate-50 p-4">
          <p class="text-[11px] text-slate-400">收货信息</p>
          <p class="mt-1 text-sm text-slate-800">
            {{ order.consignee }}
            <span class="ml-2 font-mono text-[11px] text-slate-400">{{ order.phone }}</span>
          </p>
          <p class="mt-1 text-xs leading-5 text-slate-500">{{ fullAddress }}</p>
        </div>

        <div class="mt-5">
          <p class="text-[11px] uppercase tracking-[0.18em] text-slate-400">商品明细</p>
          <div class="mt-2 space-y-3">
            <div v-for="item in order.items" :key="item.id" class="flex items-center gap-3">
              <img
                v-if="item.imageUrl"
                :src="item.imageUrl"
                class="h-12 w-12 rounded-lg object-cover"
                @error="item.imageUrl = ''"
              />
              <div v-else class="flex h-12 w-12 items-center justify-center rounded-lg bg-slate-100 text-[10px] text-slate-400">无图</div>
              <div class="min-w-0 flex-1">
                <p class="truncate text-xs text-slate-700">{{ item.productName }}</p>
                <p class="mt-0.5 font-mono text-[10px] text-slate-400">{{ item.skuCode }}</p>
              </div>
              <div class="text-right">
                <p class="font-mono text-xs text-slate-700">{{ money(item.unitPrice) }}</p>
                <p class="text-[10px] text-slate-400">× {{ item.quantity }}</p>
                <button
                  v-if="canReview"
                  type="button"
                  class="mt-1 text-[10px] text-brand-600 hover:underline"
                  @click="openReview(item)"
                >
                  评价
                </button>
              </div>
            </div>
          </div>
          <p v-if="order.currency !== 'CNY'" class="mt-2 text-[10px] text-slate-400">
            商品单价以基础币种 CNY 记录，订单金额已按下单时汇率 {{ order.exchangeRate }} 折算
          </p>
        </div>

        <div class="mt-5 space-y-2 rounded-2xl border border-slate-100 p-4 text-xs">
          <div class="flex justify-between text-slate-500">
            <span>商品金额</span>
            <span class="font-mono text-slate-700">{{ money(order.totalAmount) }}</span>
          </div>
          <div class="flex justify-between text-slate-500">
            <span>运费</span>
            <span class="font-mono text-slate-700">{{ Number(order.freightAmount) === 0 ? '包邮' : money(order.freightAmount) }}</span>
          </div>
          <div class="flex justify-between border-t border-slate-100 pt-2">
            <span class="text-slate-600">应付金额</span>
            <span class="font-mono text-base text-brand-600">{{ money(order.payAmount) }}</span>
          </div>
        </div>

        <div v-if="afterSale" class="mt-4 rounded-2xl border px-4 py-3" :class="afterSaleClass">
          <div class="flex items-center justify-between">
            <p class="text-[11px] text-slate-400">售后申请 · {{ afterSale.afterSaleNo }}</p>
            <span class="text-[11px]">{{ afterSale.statusText }}</span>
          </div>
          <p class="mt-1 text-xs">原因：{{ afterSale.reason }}</p>
          <p v-if="afterSale.auditRemark" class="mt-1 text-[11px] opacity-80">商家意见：{{ afterSale.auditRemark }}</p>
          <p v-if="afterSale.refundedAt" class="mt-1 text-[11px] opacity-80">退款时间：{{ afterSale.refundedAt }}</p>
        </div>

        <div v-if="order.shipment" class="mt-4 rounded-2xl border border-brand-100 bg-brand-50/50 p-4">
          <div class="flex items-center justify-between">
            <p class="text-[11px] uppercase tracking-[0.18em] text-slate-400">物流信息</p>
            <span class="rounded-full border border-brand-200 bg-white px-2.5 py-0.5 text-[10px] text-brand-600">
              {{ order.shipment.statusText }}
            </span>
          </div>
          <p class="mt-2 text-sm text-slate-800">{{ order.shipment.shippingCompany }}</p>
          <p class="mt-0.5 font-mono text-xs text-slate-500">{{ order.shipment.trackingNo }}</p>
          <p class="mt-2 text-[11px] text-slate-500">
            {{ order.shipment.fromCountry }} → {{ order.shipment.toCountry }}
            <span class="mx-1">·</span>
            {{ shippingMethodText }}
          </p>
          <p v-if="order.shipment.estimatedDelivery" class="mt-1 text-[11px] text-slate-400">
            预计送达：{{ order.shipment.estimatedDelivery }}
          </p>
        </div>

        <div class="mt-5 space-y-2 text-[11px] text-slate-400">
          <p>创建时间：{{ order.createdAt }}</p>
          <p v-if="order.paidAt">支付时间：{{ order.paidAt }}</p>
          <p v-if="order.shippedAt">发货时间：{{ order.shippedAt }}</p>
          <p v-if="order.completedAt">完成时间：{{ order.completedAt }}</p>
          <p v-if="order.cancelledAt">取消时间：{{ order.cancelledAt }}</p>
        </div>

        <div class="mt-5 flex flex-wrap gap-2">
          <button
            v-if="showPay"
            type="button"
            class="flex-1 rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 py-2.5 text-sm text-white transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-brand-600/30 active:translate-y-0"
            @click="payVisible = true"
          >
            去支付
          </button>
          <button
            v-if="showReceive"
            type="button"
            class="flex-1 rounded-xl bg-gradient-to-r from-emerald-500 to-cyan-500 py-2.5 text-sm text-white transition-all hover:-translate-y-0.5 hover:shadow-lg active:translate-y-0"
            @click="handleReceive"
          >
            确认收货
          </button>
          <button
            v-if="canApplyRefund"
            type="button"
            class="flex-1 rounded-xl border border-amber-200 py-2.5 text-sm text-amber-600 transition-colors hover:bg-amber-50"
            @click="refundVisible = true"
          >
            申请退款
          </button>
          <button
            v-if="showCancel"
            type="button"
            class="flex-1 rounded-xl border border-rose-200 py-2.5 text-sm text-rose-500 transition-colors hover:bg-rose-50"
            @click="handleCancel"
          >
            取消订单
          </button>
        </div>

        <PayDialog v-model="payVisible" :order="order" @paid="handlePaid" />
        <AfterSaleDialog v-model="refundVisible" :order="order" @applied="handleApplied" />
        <ReviewDialog v-model="reviewVisible" :item="reviewItem" @reviewed="handleReviewed" />
      </template>

      <div v-else-if="!loading" class="py-20 text-center text-sm text-slate-400">订单信息加载失败</div>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { cancelOrder, fetchOrderDetail } from '../../../api/order'
import { receiveOrder } from '../../../api/shipment'
import { fetchAfterSaleByOrder } from '../../../api/afterSale'
import PayDialog from './PayDialog.vue'
import AfterSaleDialog from './AfterSaleDialog.vue'
import ReviewDialog from './ReviewDialog.vue'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  orderId: { type: [Number, String], default: null }
})
const emit = defineEmits(['update:modelValue', 'updated'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const order = ref(null)
const afterSale = ref(null)
const payVisible = ref(false)
const refundVisible = ref(false)
const reviewVisible = ref(false)
const reviewItem = ref(null)

const fullAddress = computed(() => {
  if (!order.value) return ''
  return [order.value.country, order.value.provinceState, order.value.city, order.value.postalCode, order.value.addressLine]
    .filter((part) => part)
    .join(' · ')
})

const showCancel = computed(() => order.value?.status === 'PENDING_PAYMENT')
const showPay = computed(() => order.value?.status === 'PENDING_PAYMENT')
const showReceive = computed(() => order.value?.status === 'SHIPPED')

const canApplyRefund = computed(() => {
  const status = order.value?.status
  const refundable = ['PENDING_SHIPMENT', 'SHIPPED', 'COMPLETED'].includes(status)
  return refundable && !afterSale.value
})

const canReview = computed(() => order.value?.status === 'COMPLETED')

const shippingMethodText = computed(() => {
  const map = { EXPRESS: '国际快递', AIR: '空运', SEA: '海运' }
  return map[order.value?.shipment?.shippingMethod] || '国际快递'
})

const afterSaleClass = computed(() => {
  const map = {
    PENDING: 'border-amber-200 bg-amber-50/70 text-amber-700',
    REJECTED: 'border-rose-200 bg-rose-50/70 text-rose-600',
    REFUNDED: 'border-emerald-200 bg-emerald-50/70 text-emerald-700'
  }
  return map[afterSale.value?.status] || 'border-slate-200 bg-slate-50 text-slate-600'
})

function money(amount) {
  const symbol = order.value?.currency === 'USD' ? '$' : '¥'
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

async function loadDetail() {
  if (!props.orderId) return
  loading.value = true
  order.value = await fetchOrderDetail(props.orderId).catch((error) => {
    console.error('加载订单详情失败', error)
    return null
  })
  afterSale.value = await fetchAfterSaleByOrder(props.orderId).catch(() => null)
  loading.value = false
}

async function handleCancel() {
  const confirmed = await ElMessageBox.confirm('确认取消该订单？取消后库存将自动恢复。', '取消订单', {
    confirmButtonText: '取消订单',
    cancelButtonText: '再想想',
    type: 'warning'
  })
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await cancelOrder(order.value.id)
    .then(() => true)
    .catch((error) => {
      console.error('取消订单失败', error)
      return false
    })
  if (!ok) return

  ElMessage.success('订单已取消')
  await loadDetail()
  emit('updated')
}

async function handlePaid() {
  await loadDetail()
  emit('updated')
}

async function handleReceive() {
  const confirmed = await ElMessageBox.confirm('确认已经收到商品？确认后订单将标记为已完成。', '确认收货', {
    confirmButtonText: '确认收货',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await receiveOrder(order.value.id)
    .then(() => true)
    .catch((error) => {
      console.error('确认收货失败', error)
      return false
    })
  if (!ok) return

  ElMessage.success('已确认收货')
  await loadDetail()
  emit('updated')
}

async function handleApplied() {
  await loadDetail()
  emit('updated')
}

function openReview(item) {
  reviewItem.value = item
  reviewVisible.value = true
}

async function handleReviewed() {
  reviewItem.value = null
}

watch(
  () => props.modelValue,
  (opened) => {
    if (opened) {
      order.value = null
      afterSale.value = null
      loadDetail()
    }
  }
)
</script>
