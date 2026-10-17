<template>
  <div class="space-y-4">
    <section class="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200/70 bg-white p-5">
      <div>
        <h2 class="text-[15px] font-medium text-slate-800">购物车</h2>
        <p class="mt-1 text-xs text-slate-400">金额以商品当前价格实时计算；下架或库存不足的商品无法结算</p>
      </div>
      <div class="flex items-center gap-3 text-xs text-slate-400">
        <span>共 {{ cart.totalQuantity || 0 }} 件</span>
        <button
          v-if="items.length"
          type="button"
          class="rounded-lg px-2 py-1 text-rose-500 transition-colors hover:bg-rose-50"
          @click="handleClear"
        >
          清空购物车
        </button>
      </div>
    </section>

    <section v-loading="loading" class="space-y-3">
      <article
        v-for="item in items"
        :key="item.id"
        class="flex flex-wrap items-center gap-4 rounded-2xl border bg-white p-4 transition-all"
        :class="item.available ? 'border-slate-200/70' : 'border-rose-100 bg-rose-50/30'"
      >
        <img
          v-if="item.imageUrl"
          :src="item.imageUrl"
          class="h-20 w-20 rounded-xl object-cover"
          @error="item.imageUrl = ''"
        />
        <div
          v-else
          class="flex h-20 w-20 items-center justify-center rounded-xl bg-gradient-to-br from-brand-100 to-cyan-100 text-xl font-semibold text-brand-500"
        >
          {{ item.productName.slice(0, 1) }}
        </div>

        <div class="min-w-[180px] flex-1">
          <p class="text-sm text-slate-800">{{ item.productName }}</p>
          <p class="mt-0.5 text-[11px] text-slate-400">{{ item.productNameEn }}</p>
          <p class="mt-2 font-mono text-sm text-brand-600">{{ formatMoney(item.currency, item.price) }}</p>
          <p v-if="!item.available" class="mt-1 text-[11px] text-rose-500">{{ item.unavailableReason }}</p>
        </div>

        <div class="flex items-center gap-4">
          <el-input-number
            v-model="item.quantity"
            :min="1"
            :max="Math.max(item.stock || 1, 1)"
            size="small"
            controls-position="right"
            class="!w-28"
            @change="handleQuantityChange(item)"
          />
          <p class="w-24 text-right font-mono text-sm text-slate-700">{{ formatMoney(item.currency, item.subtotal) }}</p>
          <button
            type="button"
            class="rounded-lg px-2 py-1 text-xs text-slate-400 transition-colors hover:bg-rose-50 hover:text-rose-500"
            @click="removeItem(item)"
          >
            删除
          </button>
        </div>
      </article>

      <div
        v-if="!items.length && !loading"
        class="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-slate-200 bg-white py-16"
      >
        <ShoppingCart class="h-7 w-7 text-slate-300" />
        <p class="text-sm text-slate-500">购物车还是空的</p>
        <router-link to="/shop" class="text-xs text-brand-600 hover:underline">去商品目录逛逛</router-link>
      </div>
    </section>

    <section v-if="items.length" class="sticky bottom-4 rounded-2xl border border-slate-200/70 bg-white/95 p-5 shadow-soft backdrop-blur">
      <div class="flex flex-wrap items-center justify-between gap-4">
        <div class="text-xs text-slate-400">
          可结算 {{ cart.availableCount || 0 }} 项，未通过校验的商品不会计入总额
        </div>
        <div class="flex items-center gap-4">
          <div class="text-right">
            <p class="text-[11px] text-slate-400">合计</p>
            <p class="font-mono text-xl text-brand-600">{{ formatMoney(cart.currency, cart.totalAmount) }}</p>
          </div>
          <button
            type="button"
            class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-6 py-2.5 text-sm text-white transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-brand-600/30 active:translate-y-0 disabled:cursor-not-allowed disabled:opacity-50"
            :disabled="!cart.availableCount"
            @click="goCheckout"
          >
            去结算
          </button>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ShoppingCart } from 'lucide-vue-next'
import { clearCart, fetchCart, removeCartItem, updateCartQuantity } from '../api/cart'

const router = useRouter()
const loading = ref(false)
const cart = ref({ items: [], totalAmount: 0, totalQuantity: 0, availableCount: 0, currency: 'CNY' })

const items = computed(() => cart.value.items || [])

function formatMoney(currency, amount) {
  const symbol = currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(amount || 0).toFixed(2)}`
}

async function loadCart() {
  loading.value = true
  const data = await fetchCart().catch((error) => {
    console.error('加载购物车失败', error)
    return null
  })
  loading.value = false
  if (data) cart.value = data
}

async function handleQuantityChange(item) {
  const ok = await updateCartQuantity(item.id, item.quantity)
    .then(() => true)
    .catch((error) => {
      console.error('修改数量失败', error)
      return false
    })
  loadCart()
  if (ok) ElMessage.success('数量已更新')
}

async function removeItem(item) {
  const confirmed = await ElMessageBox.confirm(`确认将「${item.productName}」移出购物车？`, '移除商品', {
    confirmButtonText: '移除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await removeCartItem(item.id)
    .then(() => true)
    .catch((error) => {
      console.error('移除失败', error)
      return false
    })
  if (!ok) return
  ElMessage.success('已移出购物车')
  loadCart()
}

async function handleClear() {
  const confirmed = await ElMessageBox.confirm('确认清空购物车？', '清空购物车', {
    confirmButtonText: '清空',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await clearCart()
    .then(() => true)
    .catch((error) => {
      console.error('清空失败', error)
      return false
    })
  if (!ok) return
  ElMessage.success('购物车已清空')
  loadCart()
}

function goCheckout() {
  router.push('/checkout')
}

onMounted(loadCart)
</script>
