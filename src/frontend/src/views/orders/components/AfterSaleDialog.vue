<template>
  <el-dialog v-model="visible" title="申请退款" width="480px" align-center>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="pt-1">
      <div class="rounded-xl bg-slate-50 px-4 py-3 text-xs text-slate-500">
        订单 {{ order?.orderNo }} · 可退金额
        <span class="ml-1 font-mono text-sm text-brand-600">{{ money }}</span>
      </div>

      <el-form-item label="退款原因" prop="reason" class="mt-3">
        <el-select v-model="form.reason" class="w-full" placeholder="请选择退款原因" filterable allow-create>
          <el-option value="商品与描述不符" label="商品与描述不符" />
          <el-option value="尺寸或规格不合适" label="尺寸或规格不合适" />
          <el-option value="物流时间过长" label="物流时间过长" />
          <el-option value="不再需要该商品" label="不再需要该商品" />
        </el-select>
      </el-form-item>
      <el-form-item label="补充说明">
        <el-input v-model="form.description" type="textarea" :rows="3" maxlength="500" show-word-limit placeholder="可补充说明商品状态、物流情况等" />
      </el-form-item>

      <p class="rounded-xl border border-amber-200 bg-amber-50/60 px-3 py-2 text-[11px] leading-5 text-amber-700">
        首版为整单退款：商家审核通过后订单转为已退款，未发货订单会自动恢复库存。
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
import { computed, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { applyAfterSale } from '../../../api/afterSale'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'applied'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const submitting = ref(false)
const formRef = ref()
const form = reactive({ reason: '', description: '' })

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

  submitting.value = true
  const ok = await applyAfterSale({
    orderId: props.order.id,
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

  ElMessage.success('退款申请已提交，等待商家审核')
  visible.value = false
  emit('applied')
}
</script>
