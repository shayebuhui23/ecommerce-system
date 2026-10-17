<template>
  <el-dialog v-model="visible" title="模拟支付" width="420px" align-center>
    <div class="pt-1">
      <div class="rounded-2xl bg-slate-50 px-4 py-4 text-center">
        <p class="text-[11px] text-slate-400">订单 {{ order?.orderNo }}</p>
        <p class="mt-2 font-mono text-3xl text-brand-600">{{ money }}</p>
        <p class="mt-1 text-[11px] text-slate-400">模拟支付，不接入真实支付网关</p>
      </div>

      <div class="mt-4 space-y-2 text-xs text-slate-500">
        <div class="flex justify-between">
          <span>支付方式</span>
          <span class="text-slate-700">模拟支付（MOCK）</span>
        </div>
        <div class="flex justify-between">
          <span>支付金额</span>
          <span class="font-mono text-slate-700">{{ money }}</span>
        </div>
      </div>

      <p class="mt-4 rounded-xl border border-amber-200 bg-amber-50/60 px-3 py-2 text-[11px] leading-5 text-amber-700">
        支付成功后订单进入待发货状态。为便于演示异常分支，可点击「模拟支付失败」，订单状态不会改变。
      </p>
    </div>

    <template #footer>
      <div class="flex justify-end gap-2">
        <button
          type="button"
          class="rounded-xl border border-slate-200 px-4 py-2 text-xs text-slate-500 transition-colors hover:bg-slate-50"
          :disabled="submitting"
          @click="handlePay(false)"
        >
          模拟支付失败
        </button>
        <button
          type="button"
          class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:shadow-md disabled:opacity-60"
          :disabled="submitting"
          @click="handlePay(true)"
        >
          {{ submitting ? '支付中…' : '模拟支付成功' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { payOrder } from '../../../api/payment'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'paid'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const submitting = ref(false)

const money = computed(() => {
  const symbol = props.order?.currency === 'USD' ? '$' : '¥'
  return `${symbol}${Number(props.order?.payAmount || 0).toFixed(2)}`
})

async function handlePay(success) {
  if (!props.order) return
  submitting.value = true
  const data = await payOrder({
    orderId: props.order.id,
    amount: Number(props.order.payAmount),
    success
  })
    .then((result) => result)
    .catch((error) => {
      console.error('支付失败', error)
      return null
    })
  submitting.value = false
  if (!data) return

  if (data.status === 1) {
    ElMessage.success('支付成功，订单已进入待发货')
    visible.value = false
  } else {
    ElMessage.warning(data.failReason || '模拟支付失败，订单状态未改变')
  }
  emit('paid', data)
}
</script>
