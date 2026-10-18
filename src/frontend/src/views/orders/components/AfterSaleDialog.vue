<template>
  <el-dialog v-model="visible" :title="title" width="480px" align-center>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="pt-1">
      <div class="rounded-xl bg-slate-50 px-4 py-3 text-xs text-slate-500">
        订单 {{ order?.orderNo }} · 可退金额
        <span class="ml-1 font-mono text-sm text-brand-600">{{ money }}</span>
      </div>

      <el-form-item label="售后类型" class="mt-3">
        <div class="flex gap-2">
          <button
            v-for="item in typeOptions"
            :key="item.value"
            type="button"
            class="rounded-xl border px-3 py-1.5 text-xs transition-colors"
            :class="
              form.type === item.value
                ? 'border-brand-300 bg-brand-50 text-brand-600'
                : 'border-slate-200 text-slate-500 hover:border-brand-200'
            "
            @click="form.type = item.value"
          >
            {{ item.label }}
          </button>
        </div>
      </el-form-item>

      <el-form-item label="售后原因" prop="reason">
        <el-select v-model="form.reason" class="w-full" placeholder="请选择售后原因" filterable allow-create>
          <el-option value="商品与描述不符" label="商品与描述不符" />
          <el-option value="尺寸或规格不合适" label="尺寸或规格不合适" />
          <el-option value="物流时间过长" label="物流时间过长" />
          <el-option value="不再需要该商品" label="不再需要该商品" />
          <el-option value="未收到货" label="未收到货" />
          <el-option value="商品破损" label="商品破损" />
          <el-option value="退货退款" label="退货退款" />
        </el-select>
      </el-form-item>
      <el-form-item label="补充说明">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="可补充说明商品状态、物流情况等" />
      </el-form-item>

      <p class="rounded-xl border border-amber-200 bg-amber-50/60 px-3 py-2 text-[11px] leading-5 text-amber-700">
        {{
          form.type === 'RETURN_REFUND'
            ? '退货退款：商家同意后请寄回商品，商家确认收货后完成退款。'
            : '仅退款：商家审核通过后立即退款，未发货订单会自动恢复库存。'
        }}
      </p>
    </el-form>

    <template #footer>
      <div class="flex justify-end gap-2">
        <button type="button" class="rounded-xl border border-slate-200 px-4 py-2 text-xs text-slate-600 hover:bg-slate-50" @click="visible = false">
          取消
        </button>
        <button
          type="button"
          class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white hover:shadow-md disabled:opacity-60"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? '提交中…' : '提交申请' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { applyAfterSale } from '../../../api/afterSale'
import { receiveOrder } from '../../../api/shipment'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null },
  title: { type: String, default: '申请售后' },
  presetReason: { type: String, default: '' }
})
const emit = defineEmits(['update:modelValue', 'applied', 'received'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const submitting = ref(false)
const receiptConfirmed = ref(false)
const formRef = ref()
const form = reactive({ type: 'REFUND_ONLY', reason: '', description: '' })

const typeOptions = computed(() => {
  const status = props.order?.status
  if (status === 'PENDING_SHIPMENT') {
    return [{ value: 'REFUND_ONLY', label: '仅退款' }]
  }
  if (status === 'COMPLETED') {
    return [{ value: 'RETURN_REFUND', label: '退货退款' }]
  }
  return [
    { value: 'REFUND_ONLY', label: '仅退款（未收到/拒收）' },
    { value: 'RETURN_REFUND', label: '退货退款（已收到）' }
  ]
})

const rules = {
  reason: [{ required: true, message: '请选择或填写退款原因', trigger: 'change' }]
}

const money = computed(() => {
  const symbol = props.order?.currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(props.order?.payAmount || 0).toFixed(2)}`
})

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || !props.order) return

  // 未确认收货的订单需先确认收货，再按退货退款提交
  if (props.order.status === 'SHIPPED' && !receiptConfirmed.value) {
    const goReceive = await ElMessageBox.confirm(
      '您还未确认收货，请先确认收货后再申请退货退款。是否现在确认收货？',
      '请先确认收货',
      { confirmButtonText: '确认收货', cancelButtonText: '取消', type: 'warning' }
    )
      .then(() => true)
      .catch(() => false)
    if (!goReceive) return

    const received = await receiveOrder(props.order.id)
      .then(() => true)
      .catch((error) => {
        console.error('确认收货失败', error)
        return false
      })
    if (!received) return
    receiptConfirmed.value = true
    form.type = 'RETURN_REFUND'
    ElMessage.success('已确认收货，请继续提交退货退款申请')
    emit('received')
  }

  submitting.value = true
  const ok = await applyAfterSale({
    orderId: props.order.id,
    type: form.type,
    reason: form.reason,
    description: form.description || null
  })
    .then(() => true)
    .catch((error) => {
      console.error('提交退款申请失败', error)
      return false
    })
  submitting.value = false
  if (!ok) return

  ElMessage.success('售后申请已提交，等待商家审核')
  visible.value = false
  emit('applied')
}

// 打开时应用预设原因（如退货），未预设则清空
watch(
  () => props.modelValue,
  (opened) => {
    if (opened) {
      receiptConfirmed.value = false
      form.type = props.order?.status === 'COMPLETED' ? 'RETURN_REFUND' : 'REFUND_ONLY'
      form.reason = props.presetReason || ''
      form.description = ''
      formRef.value?.clearValidate()
    }
  }
)
</script>
