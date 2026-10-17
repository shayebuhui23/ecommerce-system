<template>
  <div class="space-y-4">
    <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 class="text-[15px] font-medium text-slate-800">售后管理</h2>
          <p class="mt-1 text-xs text-slate-400">整单退款：审核通过后订单转已退款；未发货订单自动恢复库存</p>
        </div>
        <el-select v-model="status" placeholder="全部状态" clearable class="!w-36" @change="handleSearch">
          <el-option value="PENDING" label="待审核" />
          <el-option value="REFUNDED" label="已退款" />
          <el-option value="REJECTED" label="已驳回" />
        </el-select>
      </div>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-2">
      <el-table
        v-loading="loading"
        :data="rows"
        :header-cell-style="{ background: '#F8FAFC', color: '#64748B', fontWeight: 500, fontSize: '12px' }"
      >
        <el-table-column prop="afterSaleNo" label="售后单号" min-width="170" />
        <el-table-column prop="orderNo" label="订单号" min-width="180" />
        <el-table-column prop="reason" label="退款原因" min-width="160" show-overflow-tooltip />
        <el-table-column label="退款金额" min-width="110">
          <template #default="{ row }">
            <span class="font-mono text-xs text-slate-700">{{ money(row.currency, row.amount) }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="100">
          <template #default="{ row }">
            <span class="rounded-full border px-2.5 py-1 text-[11px]" :class="statusClass(row.status)">
              {{ row.statusText }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="110" fixed="right">
          <template #default="{ row }">
            <button
              v-if="row.status === 'PENDING'"
              type="button"
              class="rounded-lg px-2 py-1 text-xs text-brand-600 transition-colors hover:bg-brand-50"
              @click="openAudit(row)"
            >
              审核
            </button>
            <span v-else class="text-xs text-slate-300">已处理</span>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-10 text-xs text-slate-400">没有匹配的售后单</div>
        </template>
      </el-table>

      <div class="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
        <p class="text-xs text-slate-400">共 {{ total }} 条售后申请</p>
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="sizes, prev, pager, next"
          background
          @size-change="loadRows"
          @current-change="loadRows"
        />
      </div>
    </section>

    <el-dialog v-model="auditVisible" title="审核退款申请" width="460px" align-center>
      <div v-if="current" class="pt-1">
        <div class="rounded-xl bg-slate-50 px-4 py-3 text-xs text-slate-500">
          <p>售后单：{{ current.afterSaleNo }}</p>
          <p class="mt-1">订单号：{{ current.orderNo }}</p>
          <p class="mt-1">退款金额：<span class="font-mono text-brand-600">{{ money(current.currency, current.amount) }}</span></p>
          <p class="mt-1">申请原因：{{ current.reason }}</p>
          <p v-if="current.description" class="mt-1">补充说明：{{ current.description }}</p>
        </div>

        <el-form label-position="top" class="mt-3">
          <el-form-item label="审核意见">
            <el-input v-model="auditRemark" type="textarea" :rows="3" maxlength="500" placeholder="例如：同意退款 / 商品已使用，不符合退款条件" />
          </el-form-item>
        </el-form>

        <p class="rounded-xl border border-amber-200 bg-amber-50/60 px-3 py-2 text-[11px] leading-5 text-amber-700">
          通过后立即执行模拟退款，订单状态变为已退款；未发货订单库存会自动回补。
        </p>
      </div>

      <template #footer>
        <div class="flex justify-end gap-2">
          <button type="button" class="rounded-xl border border-slate-200 px-4 py-2 text-xs text-slate-600 hover:bg-slate-50" @click="auditVisible = false">
            取消
          </button>
          <button
            type="button"
            class="rounded-xl border border-rose-200 px-4 py-2 text-xs text-rose-500 hover:bg-rose-50 disabled:opacity-60"
            :disabled="submitting"
            @click="submitAudit(false)"
          >
            驳回
          </button>
          <button
            type="button"
            class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white hover:shadow-md disabled:opacity-60"
            :disabled="submitting"
            @click="submitAudit(true)"
          >
            {{ submitting ? '处理中…' : '通过并退款' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { auditAfterSale, fetchAfterSalePage } from '../../api/afterSale'

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const total = ref(0)
const status = ref('')
const auditVisible = ref(false)
const current = ref(null)
const auditRemark = ref('')
const query = reactive({ pageNum: 1, pageSize: 10 })

function money(currency, amount) {
  const symbol = currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(amount || 0).toFixed(2)}`
}

function statusClass(code) {
  const map = {
    PENDING: 'border-amber-200 bg-amber-50 text-amber-700',
    REJECTED: 'border-rose-200 bg-rose-50 text-rose-600',
    REFUNDED: 'border-emerald-200 bg-emerald-50 text-emerald-700'
  }
  return map[code] || 'border-slate-200 bg-slate-50 text-slate-500'
}

async function loadRows() {
  loading.value = true
  const data = await fetchAfterSalePage({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    status: status.value || undefined
  }).catch((error) => {
    console.error('加载售后列表失败', error)
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

function openAudit(row) {
  current.value = row
  auditRemark.value = ''
  auditVisible.value = true
}

async function submitAudit(approved) {
  if (!current.value) return
  submitting.value = true
  const ok = await auditAfterSale(current.value.id, { approved, auditRemark: auditRemark.value || null })
    .then(() => true)
    .catch((error) => {
      console.error('审核失败', error)
      return false
    })
  submitting.value = false
  if (!ok) return

  ElMessage.success(approved ? '已通过并完成退款' : '已驳回该申请')
  auditVisible.value = false
  loadRows()
}

onMounted(loadRows)
</script>
