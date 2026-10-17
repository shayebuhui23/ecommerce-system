<template>
  <div class="space-y-4">
    <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 class="text-[15px] font-medium text-slate-800">商品管理</h2>
          <p class="mt-1 text-xs text-slate-400">维护商品资料、SKU 与上下架状态；下架商品不会出现在客户商城中</p>
        </div>
        <button
          type="button"
          class="flex items-center gap-1.5 rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:-translate-y-0.5 hover:shadow-md hover:shadow-brand-600/30 active:translate-y-0"
          @click="openCreate"
        >
          <Plus class="h-3.5 w-3.5" />
          新增商品
        </button>
      </div>

      <div class="mt-4 flex flex-wrap items-center gap-2">
        <div class="flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50/60 px-3 py-1.5 transition-colors focus-within:border-brand-300 focus-within:bg-white">
          <Search class="h-3.5 w-3.5 text-slate-400" />
          <input
            v-model="query.keyword"
            type="text"
            placeholder="搜索商品名称 / 英文名 / SKU"
            class="w-56 border-0 bg-transparent p-0 text-xs text-slate-700 outline-none placeholder:text-slate-400"
            @keyup.enter="handleSearch"
          />
        </div>
        <el-select
          v-if="isAdmin"
          v-model="query.sellerId"
          placeholder="全部商家"
          clearable
          filterable
          size="default"
          class="!w-44"
          @change="handleSearch"
        >
          <el-option v-for="item in merchants" :key="item.id" :value="item.id" :label="item.nickname || item.username" />
        </el-select>
        <el-select v-model="query.categoryId" placeholder="全部分类" clearable size="default" class="!w-40">
          <el-option v-for="item in categoryOptions" :key="item.id" :value="item.id" :label="item.label" />
        </el-select>
        <el-select v-model="query.status" placeholder="全部状态" clearable class="!w-32">
          <el-option :value="1" label="已上架" />
          <el-option :value="0" label="已下架" />
        </el-select>
        <button
          type="button"
          class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:-translate-y-0.5 hover:shadow-md active:translate-y-0"
          @click="handleSearch"
        >
          查询
        </button>
        <button
          type="button"
          class="flex items-center gap-1.5 rounded-xl border border-slate-200 px-3 py-2 text-xs text-slate-500 transition-colors hover:bg-slate-50"
          @click="handleReset"
        >
          <RefreshCw class="h-3.5 w-3.5" />
          重置
        </button>
      </div>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-2">
      <el-table
        v-loading="loading"
        :data="rows"
        :header-cell-style="{ background: '#F8FAFC', color: '#64748B', fontWeight: 500, fontSize: '12px' }"
      >
        <el-table-column label="商品" min-width="230">
          <template #default="{ row }">
            <div class="flex items-center gap-3">
              <img
                v-if="row.imageUrl"
                :src="row.imageUrl"
                class="h-11 w-11 rounded-xl object-cover"
                @error="row.imageUrl = ''"
              />
              <span
                v-else
                class="flex h-11 w-11 items-center justify-center rounded-xl bg-gradient-to-br from-brand-100 to-cyan-100 text-sm text-brand-600"
              >
                {{ row.name.slice(0, 1) }}
              </span>
              <div class="min-w-0">
                <p class="truncate text-sm text-slate-700">{{ row.name }}</p>
                <p class="truncate text-[11px] text-slate-400">{{ row.nameEn || row.skuCode }}</p>
              </div>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="skuCode" label="SKU 编码" min-width="145" show-overflow-tooltip />
        <el-table-column prop="categoryName" label="分类" min-width="80" />
        <el-table-column v-if="isAdmin" prop="sellerName" label="所属商家" min-width="120" show-overflow-tooltip />
        <el-table-column label="售价" min-width="95">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-700">{{ row.currency }} {{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="库存" min-width="70">
          <template #default="{ row }">
            <span :class="row.stock > 0 ? 'text-slate-600' : 'text-rose-500'" class="text-xs">
              {{ row.stock > 0 ? row.stock : '缺货' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="78">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 text-xs" :class="row.status === 1 ? 'text-emerald-600' : 'text-slate-400'">
              <span class="h-1.5 w-1.5 rounded-full bg-current" />
              {{ row.status === 1 ? '已上架' : '已下架' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="178" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-1">
              <button type="button" class="rounded-lg px-2 py-1 text-xs text-slate-500 hover:bg-slate-100" @click="openEdit(row)">
                编辑
              </button>
              <button
                type="button"
                class="rounded-lg px-2 py-1 text-xs transition-colors"
                :class="row.status === 1 ? 'text-amber-600 hover:bg-amber-50' : 'text-emerald-600 hover:bg-emerald-50'"
                @click="toggleStatus(row)"
              >
                {{ row.status === 1 ? '下架' : '上架' }}
              </button>
              <button type="button" class="rounded-lg px-2 py-1 text-xs text-rose-500 hover:bg-rose-50" @click="remove(row)">
                删除
              </button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-10 text-xs text-slate-400">没有匹配的商品</div>
        </template>
      </el-table>

      <div class="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
        <p class="text-xs text-slate-400">共 {{ total }} 个商品</p>
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="sizes, prev, pager, next"
          background
          @size-change="loadProducts"
          @current-change="loadProducts"
        />
      </div>
    </section>

    <!-- 商家成本管理：仅商家可见，展示成本价与毛利信息 -->
    <section v-if="isMerchant" class="rounded-2xl border border-slate-200/70 bg-white p-6">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h3 class="text-[15px] font-medium text-slate-800">成本管理</h3>
          <p class="mt-1 text-xs text-slate-400">查看本店商品的成本价、单位毛利与库存占用成本，成本价可在编辑商品时调整</p>
        </div>
      </div>

      <div class="mt-4 grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <div v-for="item in costSummaryCards" :key="item.label" class="rounded-2xl bg-slate-50/80 px-4 py-3">
          <p class="text-[11px] text-slate-400">{{ item.label }}</p>
          <p class="mt-1 font-mono text-xl text-slate-800">{{ item.value }}</p>
        </div>
      </div>

      <el-table
        :data="costRows"
        class="mt-4"
        :header-cell-style="{ background: '#F8FAFC', color: '#64748B', fontWeight: 500, fontSize: '12px' }"
      >
        <el-table-column prop="name" label="商品" min-width="180" show-overflow-tooltip />
        <el-table-column prop="skuCode" label="SKU 编码" min-width="150" show-overflow-tooltip />
        <el-table-column label="成本价" min-width="95">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-700">CNY {{ row.cost.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="售价" min-width="95">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-700">CNY {{ Number(row.price).toFixed(2) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="单位毛利" min-width="95">
          <template #default="{ row }">
            <span class="font-mono text-xs" :class="row.profit >= 0 ? 'text-emerald-600' : 'text-rose-500'">
              {{ row.profit.toFixed(2) }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="毛利率" min-width="90">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-600">{{ row.margin.toFixed(1) }}%</span>
          </template>
        </el-table-column>
        <el-table-column label="库存成本" min-width="100">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-600">{{ row.stockCost.toFixed(2) }}</span>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-10 text-xs text-slate-400">暂无商品成本数据</div>
        </template>
      </el-table>
    </section>

    <ProductFormDrawer v-model="drawerVisible" :product-id="editingId" @saved="refreshAll" />
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, RefreshCw, Search } from 'lucide-vue-next'
import ProductFormDrawer from './components/ProductFormDrawer.vue'
import { deleteProduct, fetchProductPage, updateProductStatus } from '../../api/product'
import { fetchCategoryTree } from '../../api/category'
import { fetchUserPage } from '../../api/user'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.roleCode === 'ADMIN')
const isMerchant = computed(() => userStore.roleCode === 'STAFF')

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const categories = ref([])
const merchants = ref([])
const costProducts = ref([])
const drawerVisible = ref(false)
const editingId = ref(null)

const query = reactive({ pageNum: 1, pageSize: 10, keyword: '', categoryId: null, status: null, sellerId: null })

const categoryOptions = ref([])

async function loadMerchants() {
  if (!isAdmin.value) return
  const data = await fetchUserPage({ pageNum: 1, pageSize: 100, roleCode: 'STAFF' }).catch((error) => {
    console.error('加载商家列表失败', error)
    return null
  })
  if (data) merchants.value = data.records || []
}

async function loadCategories() {
  const data = await fetchCategoryTree().catch((error) => {
    console.error('加载分类失败', error)
    return []
  })
  categories.value = data
  const options = []
  data.forEach((parent) => {
    options.push({ id: parent.id, label: parent.name })
    ;(parent.children || []).forEach((child) => options.push({ id: child.id, label: `　└ ${child.name}` }))
  })
  categoryOptions.value = options
}

async function loadProducts() {
  loading.value = true
  const data = await fetchProductPage({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    keyword: query.keyword.trim() || undefined,
    categoryId: query.categoryId || undefined,
    status: query.status ?? undefined,
    sellerId: isAdmin.value ? query.sellerId || undefined : undefined,
    mine: !isAdmin.value
  }).catch((error) => {
    console.error('加载商品失败', error)
    return null
  })
  loading.value = false
  if (!data) return
  rows.value = data.records
  total.value = data.total
  loadCosts()
}

async function loadCosts() {
  if (!isMerchant.value) return
  const data = await fetchProductPage({ pageNum: 1, pageSize: 100, mine: true }).catch((error) => {
    console.error('加载成本数据失败', error)
    return null
  })
  if (data) costProducts.value = data.records || []
}

function refreshAll() {
  loadProducts()
}

const costRows = computed(() =>
  costProducts.value.map((product) => {
    const cost = Number(product.costPrice || 0)
    const price = Number(product.price || 0)
    const profit = price - cost
    const margin = price > 0 ? (profit / price) * 100 : 0
    return {
      ...product,
      cost,
      profit,
      margin,
      stockCost: cost * (product.stock || 0)
    }
  })
)

const costSummaryCards = computed(() => {
  const list = costRows.value
  const stockCost = list.reduce((sum, item) => sum + item.stockCost, 0)
  const soldProfit = list.reduce((sum, item) => sum + item.profit * (item.salesCount || 0), 0)
  const avgMargin = list.length
    ? list.reduce((sum, item) => sum + item.margin, 0) / list.length
    : 0
  return [
    { label: '商品数量', value: list.length },
    { label: '库存成本合计（CNY）', value: stockCost.toFixed(2) },
    { label: '已售毛利（CNY）', value: soldProfit.toFixed(2) },
    { label: '平均毛利率', value: `${avgMargin.toFixed(1)}%` }
  ]
})

function handleSearch() {
  query.pageNum = 1
  loadProducts()
}

function handleReset() {
  query.keyword = ''
  query.categoryId = null
  query.status = null
  query.sellerId = null
  query.pageNum = 1
  loadProducts()
}

function openCreate() {
  editingId.value = null
  drawerVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  drawerVisible.value = true
}

async function toggleStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  const action = nextStatus === 1 ? '上架' : '下架'
  const confirmed = await ElMessageBox.confirm(`确认${action}商品「${row.name}」？`, `${action}商品`, {
    confirmButtonText: action,
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await updateProductStatus(row.id, nextStatus)
    .then(() => true)
    .catch((error) => {
      console.error('更新商品状态失败', error)
      return false
    })
  if (!ok) return
  ElMessage.success(`商品已${action}`)
  loadProducts()
}

async function remove(row) {
  const confirmed = await ElMessageBox.confirm(
    `确认删除商品「${row.name}」？删除后客户将无法再看到该商品，历史订单不受影响。`,
    '删除商品',
    { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
  )
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await deleteProduct(row.id)
    .then(() => true)
    .catch((error) => {
      console.error('删除商品失败', error)
      return false
    })
  if (!ok) return
  ElMessage.success('商品已删除')
  loadProducts()
}

onMounted(() => {
  loadCategories()
  loadMerchants()
  loadProducts()
})
</script>
