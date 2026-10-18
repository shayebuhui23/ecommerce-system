<template>
  <el-dialog v-model="visible" title="填写退货物流" width="460px" align-center>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="pt-1">
      <div class="rounded-xl bg-slate-50 px-4 py-3 text-xs text-slate-500">
        售后单 {{ afterSale?.afterSaleNo }} · 请将商品寄回商家并填写真实物流信息
      </div>
      <el-form-item label="退货物流公司" prop="shippingCompany" class="mt-3">
        <el-input v-model="form.shippingCompany" maxlength="64" placeholder="例如 顺丰 / DHL / FedEx" />
      </el-form-item>
      <el-form-item label="退货物流单号" prop="trackingNo">
        <el-input v-model="form.trackingNo" maxlength="64" placeholder="请填写退货物流单号" />
      </el-form-item>
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
          {{ submitting ? '提交中…' : '提交退货物流' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { submitReturnShipment } from '../../../api/afterSale'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  afterSale: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'submitted'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const submitting = ref(false)
const formRef = ref()
const form = reactive({ shippingCompany: '', trackingNo: '' })

const rules = {
  shippingCompany: [{ required: true, message: '请填写退货物流公司', trigger: 'blur' }],
  trackingNo: [{ required: true, message: '请填写退货物流单号', trigger: 'blur' }]
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || !props.afterSale) return

  submitting.value = true
  const ok = await submitReturnShipment(props.afterSale.id, { ...form })
    .then(() => true)
    .catch((error) => {
      console.error('提交退货物流失败', error)
      return false
    })
  submitting.value = false
  if (!ok) return

  ElMessage.success('退货物流已提交，等待商家确认收货')
  visible.value = false
  emit('submitted')
}

watch(
  () => props.modelValue,
  (opened) => {
    if (opened) {
      form.shippingCompany = ''
      form.trackingNo = ''
      formRef.value?.clearValidate()
    }
  }
)
</script>
