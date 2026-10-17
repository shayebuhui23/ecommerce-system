<template>
  <div class="space-y-4">
    <section class="relative overflow-hidden rounded-3xl bg-ink-800 px-7 py-7 text-white">
      <div class="pointer-events-none absolute inset-0">
        <div class="absolute -right-10 -top-20 h-56 w-56 rounded-full bg-brand-600/40 blur-[80px]" />
        <div class="absolute -bottom-24 left-1/4 h-48 w-48 rounded-full bg-cyan-500/20 blur-[90px]" />
      </div>
      <div class="relative flex flex-wrap items-end justify-between gap-5">
        <div>
          <p class="text-[11px] uppercase tracking-[0.22em] text-white/40">Global Catalog</p>
          <h2 class="mt-2 text-2xl font-semibold">跨境商品目录</h2>
          <p class="mt-2 max-w-lg text-sm text-white/55">
            中英文商品资料与多币种标价，库存实时同步自商家后台。点击商品查看规格与详情。
          </p>
        </div>
        <div class="flex items-center gap-3">
          <div class="rounded-2xl border border-white/10 bg-white/5 px-5 py-3">
            <p class="font-mono text-xl">{{ total }}</p>
            <p class="mt-0.5 text-[11px] text-white/45">在售商品</p>
          </div>
          <div class="rounded-2xl border border-white/10 bg-white/5 px-5 py-3">
            <p class="font-mono text-xl">{{ categories.length }}</p>
            <p class="mt-0.5 text-[11px] text-white/45">商品分类</p>
          </div>
        </div>
      </div>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-4">
      <div class="flex flex-wrap items-center gap-2">
        <div class="flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50/60 px-3 py-1.5 transition-colors focus-within:border-brand-300 focus-within:bg-white">
          <Search class="h-3.5 w-3.5 text-slate-400" />
          <input
            v-model="keyword"
            type="text"
            placeholder="搜索商品名称或英文名"
            class="w-60 border-0 bg-transparent p-0 text-xs text-slate-700 outline-none placeholder:text-slate-400"
            @keyup.enter="handleSearch"
          />
        </div>
        <button
          type="button"
          class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:-translate-y-0.5 hover:shadow-md active:translate-y-0"
          @click="handleSearch"
        >
          搜索
        </button>
        <div class="ml-auto flex flex-wrap items-center gap-1.5">
          <button
            type="button"
            class="rounded-full px-3 py-1.5 text-xs transition-colors"
            :class="categoryId === null ? 'bg-brand-600 text-white' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
            @click="selectCategory(null)"
          >
            全部
          </button>
          <button
            v-for="item in categories"
            :key="item.id"
            type="button"
            class="rounded-full px-3 py-1.5 text-xs transition-colors"
            :class="categoryId === item.id ? 'bg-brand-600 text-white' : 'bg-slate-100 text-slate-500 hover:bg-slate-200'"
            @click="selectCategory(item.id)"
          >
            {{ item.name }}
          </button>
        </div>
      </div>
    </section>

    <section v-loading="loading" class="min-h-[240px]">
      <div v-if="rows.length" class="grid gap-4 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4">
        <button
          v-for="item in rows"
          :key="item.id"
          type="button"
          class="group overflow-hidden rounded-2xl border border-slate-200/70 bg-white text-left transition-all duration-300 hover:-translate-y-1 hover:border-brand-200 hover:shadow-soft"
          @click="openDetail(item.id)"
        >
          <div class="relative h-44 overflow-hidden bg-slate-100">
            <img
              v-if="item.imageUrl"
              :src="item.imageUrl"
              :alt="item.name"
              class="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
              @error="item.imageUrl = ''"
            />
            <div
              v-else
              class="flex h-full items-center justify-center bg-gradient-to-br from-brand-100 to-cyan-100 text-2xl font-semibold text-brand-500"
            >
              {{ item.name.slice(0, 1) }}
            </div>
            <span
              v-if="item.stock === 0"
              class="absolute left-3 top-3 rounded-full bg-rose-500/90 px-2.5 py-1 text-[10px] text-white"
            >
              暂时缺货
            </span>
          </div>
          <div class="p-4">
            <p class="line-clamp-1 text-sm text-slate-800">{{ item.name }}</p>
            <p class="mt-0.5 line-clamp-1 text-[11px] text-slate-400">{{ item.nameEn }}</p>
            <div class="mt-3 flex items-end justify-between">
              <p class="font-mono text-lg text-brand-600">{{ formatPrice(item) }}</p>
              <span class="text-[11px] text-slate-400">已售 {{ item.salesCount || 0 }}</span>
            </div>
          </div>
        </button>
      </div>

      <div v-else class="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-slate-200 bg-white py-16">
        <PackageX class="h-7 w-7 text-slate-300" />
        <p class="text-sm text-slate-500">没有找到匹配的商品</p>
        <p class="text-xs text-slate-400">试试更换关键词或切换其他分类</p>
      </div>
    </section>

    <div v-if="total > 0" class="flex justify-center pb-2">
      <el-pagination
        v-model:current-page="query.pageNum"
        v-model:page-size="query.pageSize"
        :page-sizes="[8, 12, 24]"
        :total="total"
        layout="sizes, prev, pager, next"
        background
        @size-change="loadProducts"
        @current-change="loadProducts"
      />
    </div>

    <ProductDetailDrawer v-model="detailVisible" :product-id="detailId" />
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { PackageX, Search } from 'lucide-vue-next'
import { fetchProductPage } from '../../api/product'
import { fetchCategoryTree } from '../../api/category'
import ProductDetailDrawer from './components/ProductDetailDrawer.vue'

const loading = ref(false)
const keyword = ref('')
const categoryId = ref(null)
const rows = ref([])
const total = ref(0)
const categories = ref([])
const detailVisible = ref(false)
const detailId = ref(null)
const query = reactive({ pageNum: 1, pageSize: 12 })

function formatPrice(item) {
  const symbol = item.currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(item.price).toFixed(2)}`
}

async function loadCategories() {
  const data = await fetchCategoryTree().catch((error) => {
    console.error('加载分类失败', error)
    return []
  })
  categories.value = data
}

async function loadProducts() {
  loading.value = true
  const data = await fetchProductPage({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    keyword: keyword.value.trim() || undefined,
    categoryId: categoryId.value || undefined
  }).catch((error) => {
    console.error('加载商品失败', error)
    return null
  })
  loading.value = false
  if (!data) return
  rows.value = data.records
  total.value = data.total
}

function handleSearch() {
  query.pageNum = 1
  loadProducts()
}

function selectCategory(id) {
  categoryId.value = id
  query.pageNum = 1
  loadProducts()
}

function openDetail(id) {
  detailId.value = id
  detailVisible.value = true
}

onMounted(() => {
  loadCategories()
  loadProducts()
})
</script>
