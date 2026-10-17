<template>
  <div v-loading="loading" class="mx-auto max-w-5xl space-y-4">
    <section class="flex items-center justify-between rounded-2xl border border-slate-200/70 bg-white p-5">
      <div>
        <h2 class="text-[15px] font-medium text-slate-800">确认订单</h2>
        <p class="mt-1 text-xs text-slate-400">金额由后端重新计算，收货信息将保存为订单快照</p>
      </div>
      <router-link to="/cart" class="text-xs text-brand-600 hover:underline">返回购物车</router-link>
    </section>

    <div class="grid gap-4 lg:grid-cols-3">
      <div class="space-y-4 lg:col-span-2">
        <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
          <div class="flex items-center justify-between">
            <h3 class="text-sm text-slate-700">收货地址</h3>
            <router-link to="/addresses" class="text-[11px] text-brand-600 hover:underline">管理地址</router-link>
          </div>

          <div v-if="addresses.length" class="mt-3 space-y-2">
            <button
              v-for="address in addresses"
              :key="address.id"
              type="button"
              class="flex w-full items-start gap-3 rounded-xl border px-4 py-3 text-left transition-all"
              :class="
                selectedAddressId === address.id
                  ? 'border-brand-400 bg-brand-50/60 ring-1 ring-brand-200'
                  : 'border-slate-200 hover:border-brand-200'
              "
              @click="selectedAddressId = address.id"
            >
              <MapPin class="mt-0.5 h-4 w-4 shrink-0" :class="selectedAddressId === address.id ? 'text-brand-600' : 'text-slate-400'" />
              <div class="min-w-0 flex-1">
                <p class="text-sm text-slate-800">
                  {{ address.consignee }}
                  <span class="ml-2 font-mono text-[11px] text-slate-400">{{ address.phone }}</span>
                  <span v-if="address.isDefault === 1" class="ml-2 rounded-full bg-brand-100 px-2 py-0.5 text-[10px] text-brand-600">默认</span>
                </p>
                <p class="mt-1 text-xs leading-5 text-slate-500">{{ formatAddress(address) }}</p>
              </div>
            </button>
          </div>

          <div v-else class="mt-3 rounded-xl border border-dashed border-slate-200 px-4 py-6 text-center text-xs text-slate-400">
            还没有收货地址，先
            <router-link to="/addresses" class="text-brand-600 hover:underline">添加一个</router-link>
          </div>
        </section>

        <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
          <h3 class="text-sm text-slate-700">商品清单</h3>
          <div class="mt-3 space-y-3">
            <div v-for="item in items" :key="item.id" class="flex items-center gap-3">
              <img
                v-if="item.imageUrl"
                :src="item.imageUrl"
                class="h-12 w-12 rounded-lg object-cover"
                @error="item.imageUrl = ''"
              />
              <div v-else class="flex h-12 w-12 items-center justify-center rounded-lg bg-slate-100 text-xs text-slate-400">无图</div>
              <div class="min-w-0 flex-1">
                <p class="truncate text-xs text-slate-700">{{ item.productName }}</p>
                <p class="mt-0.5 text-[11px] text-slate-400">{{ formatMoney(item.currency, item.price) }} × {{ item.quantity }}</p>
              </div>
              <p class="font-mono text-xs text-slate-700">{{ formatMoney(item.currency, item.subtotal) }}</p>
            </div>
          </div>
        </section>
      </div>

      <section class="h-fit rounded-2xl border border-slate-200/70 bg-white p-5 lg:sticky lg:top-20">
        <h3 class="text-sm text-slate-700">金额明细</h3>
        <div class="mt-4 space-y-2 text-xs text-slate-500">
          <div class="flex justify-between">
            <span>商品金额</span>
            <span class="font-mono text-slate-700">{{ formatMoney(currency, totalAmount) }}</span>
          </div>
          <div class="flex justify-between">
            <span>运费</span>
            <span class="font-mono text-slate-700">{{ freightAmount === 0 ? '包邮' : formatMoney(currency, freightAmount) }}</span>
          </div>
          <p class="pt-1 text-[10px] text-slate-400">满 ¥500 包邮，未满收取 ¥20</p>
        </div>

        <div class="mt-4 flex items-end justify-between border-t border-slate-100 pt-4">
          <span class="text-xs text-slate-500">应付金额</span>
          <span class="font-mono text-xl text-brand-600">{{ formatMoney(currency, payAmount) }}</span>
        </div>

        <button
          type="button"
          class="mt-5 flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 py-3 text-sm text-white transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-brand-600/30 active:translate-y-0 disabled:cursor-not-allowed disabled:opacity-50"
          :disabled="submitting || !items.length || !selectedAddressId"
          @click="submit"
        >
          <ReceiptText class="h-4 w-4" />
          {{ submitting ? '提交中…' : '提交订单' }}
        </button>
        <p class="mt-2 text-center text-[10px] text-slate-400">提交后订单进入待付款状态，可在订单列表取消</p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { MapPin, ReceiptText } from 'lucide-vue-next'
import { fetchCart } from '../api/cart'
import { fetchAddressList } from '../api/address'
import { createOrder } from '../api/order'

const router = useRouter()
const loading = ref(false)
const submitting = ref(false)
const addresses = ref([])
const selectedAddressId = ref(null)
const items = ref([])
const currency = ref('CNY')
const totalAmount = ref(0)

const freightAmount = computed(() => (totalAmount.value >= 500 ? 0 : 20))
const payAmount = computed(() => Number(totalAmount.value) + freightAmount.value)

function formatMoney(code, amount) {
  const symbol = code === 'USD' ? '$' : '¥'
  return `${symbol}${Number(amount || 0).toFixed(2)}`
}

function formatAddress(address) {
  return [address.country, address.provinceState, address.city, address.postalCode, address.addressLine]
    .filter((part) => part)
    .join(' · ')
}

async function loadData() {
  loading.value = true
  const [cartData, addressData] = await Promise.all([
    fetchCart().catch((error) => {
      console.error('加载购物车失败', error)
      return null
    }),
    fetchAddressList().catch((error) => {
      console.error('加载地址失败', error)
      return []
    })
  ])
  loading.value = false

  if (cartData) {
    items.value = (cartData.items || []).filter((item) => item.available)
    currency.value = cartData.currency || 'CNY'
    totalAmount.value = Number(cartData.totalAmount || 0)
  }
  addresses.value = addressData || []
  const defaultAddress = addresses.value.find((item) => item.isDefault === 1)
  selectedAddressId.value = defaultAddress ? defaultAddress.id : addresses.value[0]?.id || null
}

async function submit() {
  if (!selectedAddressId.value || !items.value.length) return
  submitting.value = true
  const data = await createOrder({
    addressId: selectedAddressId.value,
    cartItemIds: items.value.map((item) => item.id),
    shippingMethod: 'EXPRESS'
  })
    .then((result) => result)
    .catch((error) => {
      console.error('创建订单失败', error)
      return null
    })
  submitting.value = false
  if (!data) return

  ElMessage.success(`订单已创建：${data.orderNo}`)
  router.replace('/orders')
}

onMounted(loadData)
</script>
