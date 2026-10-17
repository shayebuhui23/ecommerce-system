<template>
  <div class="space-y-4">
    <section class="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200/70 bg-white p-5">
      <div>
        <h2 class="text-[15px] font-medium text-slate-800">操作审计日志</h2>
        <p class="mt-1 text-xs text-slate-400">记录下单、支付、发货、退款等关键业务操作</p>
      </div>
      <el-select v-model="module" placeholder="全部模块" clearable class="!w-40" @change="handleSearch">
        <el-option value="ORDER" label="订单" />
        <el-option value="PAYMENT" label="支付" />
        <el-option value="SHIPMENT" label="发货物流" />
        <el-option value="AFTER_SALE" label="售后" />
      </el-select>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-2">
      <el-table
        v-loading="loading"
        :data="rows"
        :header-cell-style="{ background: '#F8FAFC', color: '#64748B', fontWeight: 500, fontSize: '12px' }"
      >
        <el-table-column prop="createdAt" label="时间" min-width="165" />
        <el-table-column prop="username" label="操作人" min-width="110" />
        <el-table-column label="模块" min-width="100">
          <template #default="{ row }">
            <span class="rounded-full bg-slate-100 px-2 py-0.5 text-[11px] text-slate-600">{{ moduleText(row.module) }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="action" label="操作" min-width="140" />
        <el-table-column prop="params" label="详情" min-width="240" show-overflow-tooltip />
        <template #empty>
          <div class="py-10 text-xs text-slate-400">暂无操作记录</div>
        </template>
      </el-table>

      <div class="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
        <p class="text-xs text-slate-400">共 {{ total }} 条记录</p>
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[20, 50, 100]"
          :total="total"
          layout="sizes, prev, pager, next"
          background
          @size-change="loadRows"
          @current-change="loadRows"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { fetchOperationLogs } from '../../api/operationLog'

const loading = ref(false)
const rows = ref([])
const total = ref(0)
const module = ref('')
const query = reactive({ pageNum: 1, pageSize: 20 })

function moduleText(code) {
  const map = { ORDER: '订单', PAYMENT: '支付', SHIPMENT: '发货物流', AFTER_SALE: '售后' }
  return map[code] || code
}

async function loadRows() {
  loading.value = true
  const data = await fetchOperationLogs({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    module: module.value || undefined
  }).catch((error) => {
    console.error('加载操作日志失败', error)
    return null
  })
  loading.value = false
  if (!data) return
  rows.value = data.records
  total.value = data.total
}

function handleSearch() {
  query.pageNum = 1
  loadRows()
}

onMounted(loadRows)
</script>
