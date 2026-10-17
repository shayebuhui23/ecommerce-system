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
            购物车与下单功能将在下一阶段开放，当前可先浏览商品资料与规格信息。
          </div>

          <button
            type="button"
            disabled
            class="mt-4 w-full cursor-not-allowed rounded-xl bg-slate-200 py-3 text-sm text-slate-500"
          >
            加入购物车 · 下一阶段开放
          </button>
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
import { Info, PackageX } from 'lucide-vue-next'
import { fetchProductDetail } from '../../../api/product'

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
const product = ref(null)

function formatPrice(item) {
  const symbol = item.currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(item.price).toFixed(2)}`
}

watch(
  () => props.modelValue,
  async (opened) => {
    if (!opened || !props.productId) return
    loading.value = true
    product.value = null
    const data = await fetchProductDetail(props.productId).catch((error) => {
      console.error('加载商品详情失败', error)
      return null
    })
    loading.value = false
    product.value = data
  }
)
</script>
