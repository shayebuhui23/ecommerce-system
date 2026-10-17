<template>
  <el-drawer v-model="visible" size="540px" :with-header="false">
    <div v-loading="loading" class="pb-4">
      <template v-if="product">
        <div class="relative h-60 overflow-hidden rounded-2xl bg-slate-100">
          <img
            v-if="product.imageUrl"
            :src="product.imageUrl"
            :alt="product.name"
            class="h-full w-full object-cover"
            @error="product.imageUrl = ''"
          />
          <div
            v-else
            class="flex h-full items-center justify-center bg-gradient-to-br from-brand-100 to-cyan-100 text-3xl font-semibold text-brand-500"
          >
            {{ product.name.slice(0, 1) }}
          </div>
          <span class="absolute left-4 top-4 rounded-full bg-ink-900/70 px-3 py-1 text-[11px] text-white backdrop-blur">
            {{ product.categoryName || '未分类' }}
          </span>
        </div>

        <div class="mt-5">
          <h2 class="text-xl font-semibold text-slate-800">{{ product.name }}</h2>
          <p class="mt-1 text-sm text-slate-400">{{ product.nameEn }}</p>

          <div class="mt-4 flex items-end justify-between rounded-2xl bg-slate-50 px-4 py-3">
            <div>
              <p class="text-[11px] text-slate-400">售价</p>
              <p class="font-mono text-2xl text-brand-600">{{ formatPrice(product) }}</p>
            </div>
            <div class="text-right">
              <p class="text-[11px] text-slate-400">库存 / 销量</p>
              <p class="mt-1 text-sm" :class="product.stock > 0 ? 'text-slate-600' : 'text-rose-500'">
                {{ product.stock > 0 ? `${product.stock} 件` : '暂时缺货' }} · 已售 {{ product.salesCount || 0 }}
              </p>
            </div>
          </div>

          <div class="mt-5">
            <p class="text-[11px] uppercase tracking-[0.18em] text-slate-400">商品描述</p>
            <p class="mt-2 text-sm leading-6 text-slate-600">{{ product.description || '商家暂未填写中文描述' }}</p>
            <p class="mt-2 text-xs leading-6 text-slate-400">{{ product.descriptionEn || 'No English description yet.' }}</p>
          </div>

          <div class="mt-5">
            <div class="flex items-center justify-between">
              <p class="text-[11px] uppercase tracking-[0.18em] text-slate-400">商品评价</p>
              <span class="text-[11px] text-slate-400">
                {{ summary.reviewCount }} 条 · 平均 {{ summary.averageRating || 0 }} 分
              </span>
            </div>
            <div v-if="reviews.length" class="mt-2 space-y-2">
              <div v-for="review in reviews" :key="review.id" class="rounded-xl border border-slate-100 px-3 py-2">
                <div class="flex items-center justify-between">
                  <span class="text-[11px] text-slate-500">{{ review.reviewer }}</span>
                  <el-rate :model-value="review.rating" disabled size="small" />
                </div>
                <p class="mt-1 text-xs leading-5 text-slate-600">{{ review.content || '该用户未填写评价内容' }}</p>
              </div>
            </div>
            <p v-else class="mt-2 rounded-xl bg-slate-50 px-3 py-2 text-xs text-slate-400">还没有评价，购买并完成订单后可以评价</p>
          </div>

          <div v-if="product.skus && product.skus.length" class="mt-5">
            <p class="text-[11px] uppercase tracking-[0.18em] text-slate-400">可选规格</p>
            <div class="mt-2 space-y-2">
              <div
                v-for="sku in product.skus"
                :key="sku.id"
                class="flex items-center justify-between rounded-xl border border-slate-200 px-3 py-2.5"
              >
                <div>
                  <p class="text-xs text-slate-700">{{ sku.specName || sku.skuCode }}</p>
                  <p class="font-mono text-[10px] text-slate-400">{{ sku.skuCode }}</p>
                </div>
                <div class="text-right">
                  <p class="font-mono text-xs text-slate-700">{{ product.currency }} {{ Number(sku.price).toFixed(2) }}</p>
                  <p class="text-[10px] text-slate-400">库存 {{ sku.stock }}</p>
                </div>
              </div>
            </div>
          </div>

          <div class="mt-6 flex items-start gap-2 rounded-xl border border-brand-100 bg-brand-50/70 px-4 py-3 text-xs leading-5 text-brand-700">
            <Info class="mt-0.5 h-3.5 w-3.5 shrink-0" />
            加入购物车后可前往结算，订单金额与库存会在下单时由后端重新校验。
          </div>

          <div class="mt-4 flex gap-2">
            <button
              type="button"
              class="flex-1 rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 py-3 text-sm text-white transition-all hover:-translate-y-0.5 hover:shadow-lg hover:shadow-brand-600/30 active:translate-y-0 disabled:cursor-not-allowed disabled:opacity-50"
              :disabled="product.stock === 0 || adding"
              @click="handleAddToCart"
            >
              {{ adding ? '正在加入…' : product.stock === 0 ? '暂时缺货' : '加入购物车' }}
            </button>
            <router-link
              to="/cart"
              class="flex items-center justify-center rounded-xl border border-slate-200 px-4 text-sm text-slate-600 transition-colors hover:border-brand-300 hover:text-brand-600"
            >
              去购物车
            </router-link>
          </div>
        </div>
      </template>

      <div v-else-if="!loading" class="flex flex-col items-center gap-3 py-20">
        <PackageX class="h-8 w-8 text-slate-300" />
        <p class="text-sm text-slate-500">商品信息加载失败或已下架</p>
      </div>
    </div>
  </el-drawer>
</template>

<script setup>
import { computed, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Info, PackageX } from 'lucide-vue-next'
import { fetchProductDetail } from '../../../api/product'
import { addToCart } from '../../../api/cart'
import { fetchProductReviews, fetchReviewSummary } from '../../../api/review'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  productId: { type: [Number, String], default: null }
})
const emit = defineEmits(['update:modelValue'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const loading = ref(false)
const adding = ref(false)
const product = ref(null)
const reviews = ref([])
const summary = ref({ reviewCount: 0, averageRating: 0 })

function formatPrice(item) {
  const symbol = item.currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(item.price).toFixed(2)}`
}

async function handleAddToCart() {
  if (!product.value) return
  adding.value = true
  const ok = await addToCart({ productId: product.value.id, quantity: 1 })
    .then(() => true)
    .catch((error) => {
      console.error('加入购物车失败', error)
      return false
    })
  adding.value = false
  if (!ok) return
  ElMessage.success('已加入购物车')
}

watch(
  () => props.modelValue,
  async (opened) => {
    if (!opened || !props.productId) return
    loading.value = true
    product.value = null
    reviews.value = []
    summary.value = { reviewCount: 0, averageRating: 0 }

    const data = await fetchProductDetail(props.productId).catch((error) => {
      console.error('加载商品详情失败', error)
      return null
    })
    product.value = data

    if (data) {
      const [reviewPage, reviewSummary] = await Promise.all([
        fetchProductReviews(props.productId, { pageNum: 1, pageSize: 3 }).catch(() => null),
        fetchReviewSummary(props.productId).catch(() => null)
      ])
      reviews.value = reviewPage?.records || []
      if (reviewSummary) summary.value = reviewSummary
    }
    loading.value = false
  }
)
</script>
