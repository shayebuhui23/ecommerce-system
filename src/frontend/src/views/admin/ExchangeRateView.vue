<template>
  <div class="space-y-4">
    <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
      <h2 class="text-[15px] font-medium text-slate-800">汇率管理</h2>
      <p class="mt-1 text-xs text-slate-400">
        模拟汇率，不接实时外汇接口。调整后仅影响新订单，历史订单保留下单时的汇率快照。
      </p>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-2">
      <el-table
        v-loading="loading"
        :data="rows"
        :header-cell-style="{ background: '#F8FAFC', color: '#64748B', fontWeight: 500, fontSize: '12px' }"
      >
        <el-table-column label="币种对" min-width="160">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-700">{{ row.fromCurrency }} → {{ row.toCurrency }}</span>
          </template>
        </el-table-column>
        <el-table-column label="当前汇率" min-width="140">
          <template #default="{ row }">
            <span class="font-mono text-xs text-brand-600">{{ Number(row.rate).toFixed(6) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="生效时间" min-width="170">
          <template #default="{ row }">
            <span class="text-xs text-slate-500">{{ row.effectiveTime || row.updatedAt }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 text-xs" :class="row.status === 1 ? 'text-emerald-600' : 'text-slate-400'">
              <span class="h-1.5 w-1.5 rounded-full bg-current" />
              {{ row.status === 1 ? '启用' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="调整汇率" width="260" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <el-input-number
                v-model="row.editRate"
                :min="0.000001"
                :precision="6"
                :step="0.01"
                size="small"
                controls-position="right"
                class="!w-36"
              />
              <button
                type="button"
                class="rounded-lg bg-brand-600 px-2.5 py-1 text-[11px] text-white transition-colors hover:bg-brand-700"
                @click="save(row)"
              >
                保存
              </button>
            </div>
          </template>
        </el-table-column>
      </el-table>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
      <h3 class="text-sm text-slate-700">换算示例</h3>
      <p class="mt-2 text-xs leading-6 text-slate-500">
        商品价格以 CNY 记录。用户下单选择 USD 时，系统按「CNY → USD」汇率换算商品金额与运费，
        并保存汇率快照；后续调整汇率不会改变已创建订单的金额。
      </p>
    </section>
  </div>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { fetchExchangeRates, updateExchangeRate } from '../../api/exchangeRate'

const loading = ref(false)
const rows = ref([])

async function loadRows() {
  loading.value = true
  const data = await fetchExchangeRates().catch((error) => {
    console.error('加载汇率失败', error)
    return []
  })
  loading.value = false
  rows.value = (data || []).map((item) => ({ ...item, editRate: Number(item.rate) }))
}

async function save(row) {
  const ok = await updateExchangeRate(row.id, row.editRate)
    .then(() => true)
    .catch((error) => {
      console.error('更新汇率失败', error)
      return false
    })
  if (!ok) return
  ElMessage.success('汇率已更新，新订单将使用新汇率')
  loadRows()
}

onMounted(loadRows)
</script>
