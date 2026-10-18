<template>
  <div class="space-y-4">
    <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
      <div class="flex flex-wrap items-start justify-between gap-3">
        <div>
          <h2 class="text-[15px] font-medium text-slate-800">售后管理</h2>
          <p class="mt-1 text-xs text-slate-400">支持仅退款与退货退款：退货需买家寄回并由商家确认收货后完成退款</p>
        </div>
        <div class="flex items-center gap-2">
          <el-select v-if="isAdmin" v-model="sellerId" placeholder="全部商家" clearable filterable class="!w-44" @change="handleSearch">
            <el-option v-for="item in merchants" :key="item.id" :value="item.id" :label="item.nickname || item.username" />
          </el-select>
          <el-select v-model="status" placeholder="全部状态" clearable class="!w-40" @change="handleSearch">
            <el-option value="PENDING" label="待审核" />
            <el-option value="WAITING_RETURN" label="待买家寄回" />
            <el-option value="WAITING_RECEIPT" label="待商家收货" />
            <el-option value="REFUNDED" label="已完成" />
            <el-option value="REJECTED" label="已拒绝" />
          </el-select>
        </div>
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
            <StatusTag :text="row.statusText" :tone="statusTone(row.status)" />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="申请时间" min-width="160" />
        <el-table-column label="操作" width="180" fixed="right" align="left" header-align="left">
          <template #default="{ row }">
            <div class="-ml-2 flex h-7 items-center gap-1">
              <button
                type="button"
                class="inline-flex h-7 items-center rounded-lg px-2 text-xs text-slate-600 transition-colors hover:bg-slate-100"
                @click="openDetail(row)"
              >
                详情
              </button>
              <!-- 管理员只查看，审核与收货由商家处理 -->
              <button
                v-if="!isAdmin && row.status === 'PENDING'"
                type="button"
                class="inline-flex h-7 items-center rounded-lg px-2 text-xs text-brand-600 transition-colors hover:bg-brand-50"
                @click="openAudit(row)"
              >
                审核
              </button>
              <button
                v-else-if="!isAdmin && row.status === 'WAITING_RECEIPT'"
                type="button"
                class="inline-flex h-7 items-center rounded-lg px-2 text-xs text-emerald-600 transition-colors hover:bg-emerald-50"
                @click="handleConfirmReceipt(row)"
              >
                确认收货
              </button>
              <span
                v-else-if="row.status === 'PENDING'"
                class="inline-flex h-7 items-center px-2 text-xs text-amber-600"
              >
                待商家审核
              </span>
              <span
                v-else-if="row.status === 'WAITING_RETURN'"
                class="inline-flex h-7 items-center px-2 text-xs text-sky-600"
              >
                待买家寄回
              </span>
              <span
                v-else-if="row.status === 'WAITING_RECEIPT'"
                class="inline-flex h-7 items-center px-2 text-xs text-amber-600"
              >
                待商家确认
              </span>
              <span v-else class="inline-flex h-7 items-center px-2 text-xs text-slate-300">已处理</span>
            </div>
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

    <el-dialog v-model="auditVisible" title="审核售后申请" width="460px" align-center>
      <div v-if="current" class="pt-1">
        <div class="rounded-xl bg-slate-50 px-4 py-3 text-xs text-slate-500">
          <p>售后单：{{ current.afterSaleNo }}</p>
          <p class="mt-1">订单号：{{ current.orderNo }}</p>
          <p class="mt-1">售后类型：{{ current.typeText }}</p>
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
          {{
            current.type === 'RETURN_REFUND'
              ? '通过后进入「待买家寄回」，买家填写退货物流并由商家确认收货后才执行退款。'
              : '通过后立即执行模拟退款，订单状态变为已退款；未发货订单库存会自动回补。'
          }}
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
            {{ submitting ? '处理中…' : current?.type === 'RETURN_REFUND' ? '同意退货' : '同意退款' }}
          </button>
        </div>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="售后详情" width="480px" align-center>
      <div v-if="detailRow" class="space-y-2 text-xs text-slate-500">
        <div class="rounded-xl bg-slate-50 px-4 py-3">
          <p>售后单号：<span class="font-mono text-slate-700">{{ detailRow.afterSaleNo }}</span></p>
          <p class="mt-1">订单号：<span class="font-mono text-slate-700">{{ detailRow.orderNo }}</span></p>
          <p class="mt-1">售后类型：{{ detailRow.typeText }}</p>
          <p class="mt-1">处理状态：{{ detailRow.statusText }}</p>
          <p class="mt-1">退款金额：<span class="font-mono text-brand-600">{{ money(detailRow.currency, detailRow.amount) }}</span></p>
        </div>
        <div class="rounded-xl border border-slate-100 px-4 py-3">
          <p>申请原因：{{ detailRow.reason }}</p>
          <p class="mt-1">补充说明：{{ detailRow.description || '—' }}</p>
          <p class="mt-1">申请时间：{{ detailRow.createdAt || '—' }}</p>
        </div>
        <div v-if="detailRow.type === 'RETURN_REFUND'" class="rounded-xl border border-slate-100 px-4 py-3">
          <p>退货物流：{{ detailRow.returnShippingCompany || '—' }} {{ detailRow.returnTrackingNo || '' }}</p>
          <p class="mt-1">买家寄回时间：{{ detailRow.returnShippedAt || '—' }}</p>
          <p class="mt-1">商家收货时间：{{ detailRow.returnReceivedAt || '—' }}</p>
        </div>
        <div class="rounded-xl border border-slate-100 px-4 py-3">
          <p>商家意见：{{ detailRow.auditRemark || '—' }}</p>
          <p class="mt-1">审核时间：{{ detailRow.auditedAt || '—' }}</p>
          <p class="mt-1">退款时间：{{ detailRow.refundedAt || '—' }}</p>
        </div>
      </div>
      <template #footer>
        <div class="flex justify-end">
          <button type="button" class="rounded-xl border border-slate-200 px-4 py-2 text-xs text-slate-600 hover:bg-slate-50" @click="detailVisible = false">
            关闭
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { auditAfterSale, confirmReturnReceipt, fetchAfterSalePage } from '../../api/afterSale'
import { fetchUserPage } from '../../api/user'
import { useUserStore } from '../../stores/user'
import StatusTag from '../../components/StatusTag.vue'

const userStore = useUserStore()
const isAdmin = computed(() => userStore.roleCode === 'ADMIN')

const loading = ref(false)
const submitting = ref(false)
const rows = ref([])
const total = ref(0)
const status = ref('')
const sellerId = ref(null)
const merchants = ref([])
const auditVisible = ref(false)
const detailVisible = ref(false)
const detailRow = ref(null)
const current = ref(null)
const auditRemark = ref('')
const query = reactive({ pageNum: 1, pageSize: 10 })

function money(currency, amount) {
  const symbol = currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(amount || 0).toFixed(2)}`
}

function statusTone(code) {
  const map = {
    PENDING: 'amber',
    WAITING_RETURN: 'sky',
    WAITING_RECEIPT: 'indigo',
    REJECTED: 'rose',
    REFUNDED: 'emerald'
  }
  return map[code] || 'slate'
}

async function loadRows() {
  loading.value = true
  const data = await fetchAfterSalePage({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    status: status.value || undefined,
    sellerId: isAdmin.value ? sellerId.value || undefined : undefined
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

function openDetail(row) {
  detailRow.value = row
  detailVisible.value = true
}

async function handleConfirmReceipt(row) {
  const confirmed = await ElMessageBox.confirm(
    `确认已收到售后退货商品 ${row.afterSaleNo}？确认后将完成退款并恢复库存。`,
    '确认收货并退款',
    { confirmButtonText: '确认收货', cancelButtonText: '取消', type: 'warning' }
  )
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await confirmReturnReceipt(row.id)
    .then(() => true)
    .catch((error) => {
      console.error('确认收货失败', error)
      return false
    })
  if (!ok) return
  ElMessage.success('已确认收货并完成退款')
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

  ElMessage.success(
    approved
      ? current.value?.type === 'RETURN_REFUND'
        ? '已同意退货，等待买家寄回'
        : '已通过并完成退款'
      : '已拒绝该申请'
  )
  auditVisible.value = false
  loadRows()
}

async function loadMerchants() {
  if (!isAdmin.value) return
  const data = await fetchUserPage({ pageNum: 1, pageSize: 100, roleCode: 'STAFF' }).catch((error) => {
    console.error('加载商家列表失败', error)
    return null
  })
  if (data) merchants.value = data.records || []
}

onMounted(() => {
  loadMerchants()
  loadRows()
})
</script>
