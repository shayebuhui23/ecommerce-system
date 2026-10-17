<template>
  <el-dialog v-model="visible" title="订单发货" width="520px" align-center>
    <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="pt-1">
      <div class="rounded-xl bg-slate-50 px-4 py-3 text-xs leading-5 text-slate-500">
        <p>订单 {{ order?.orderNo }} · {{ order?.consignee }} · {{ order?.country }}</p>
        <p class="mt-1">收货地址：{{ fullReceiverAddress }}</p>
      </div>

      <div class="mt-3 grid grid-cols-2 gap-x-3">
        <el-form-item v-if="isMerchant" label="选择发货地址" class="col-span-2">
          <el-select v-model="form.addressId" class="w-full" placeholder="选择已保存的发货地址" @change="handleAddressChange">
            <el-option v-for="item in addresses" :key="item.id" :value="item.id" :label="addressLabel(item)" />
          </el-select>
          <p v-if="!addresses.length" class="mt-1 text-[11px] text-slate-400">
            尚未维护发货地址，可前往「发货地址」页面添加
          </p>
          <p v-else-if="selectedAddressText" class="mt-1 text-[11px] text-slate-400">
            发货地址：{{ selectedAddressText }}
          </p>
        </el-form-item>
        <el-form-item label="物流公司" prop="shippingCompany" class="col-span-2">
          <el-input v-model="form.shippingCompany" maxlength="64" placeholder="例如 DHL / FedEx / 顺丰" />
        </el-form-item>
        <el-form-item label="物流单号" prop="trackingNo" class="col-span-2">
          <el-input v-model="form.trackingNo" maxlength="64" placeholder="例如 DHL123456789CN" />
        </el-form-item>
        <el-form-item label="运输方式">
          <el-select v-model="form.shippingMethod" class="w-full">
            <el-option value="EXPRESS" label="国际快递" />
            <el-option value="AIR" label="空运" />
            <el-option value="SEA" label="海运" />
          </el-select>
        </el-form-item>
        <el-form-item label="预计送达日期">
          <el-date-picker
            v-model="form.estimatedDelivery"
            type="date"
            value-format="YYYY-MM-DD"
            placeholder="选择日期"
            class="!w-full"
          />
        </el-form-item>
        <el-form-item label="发货国家或地区">
          <el-input v-model="form.fromCountry" maxlength="64" placeholder="China" />
        </el-form-item>
        <el-form-item label="目的国家或地区">
          <el-input v-model="form.toCountry" maxlength="64" placeholder="United States" />
        </el-form-item>
      </div>
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
          {{ submitting ? '发货中…' : '确认发货' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { shipOrder } from '../../../api/shipment'
import { fetchAddressList } from '../../../api/address'
import { useUserStore } from '../../../stores/user'

const userStore = useUserStore()
const isMerchant = computed(() => userStore.roleCode === 'STAFF')

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  order: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'shipped'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const submitting = ref(false)
const formRef = ref()
const addresses = ref([])
const form = reactive({
  addressId: null,
  shippingCompany: '',
  trackingNo: '',
  shippingMethod: 'EXPRESS',
  fromCountry: 'China',
  toCountry: '',
  estimatedDelivery: null
})

const fullReceiverAddress = computed(() => {
  if (!props.order) return '—'
  return [props.order.country, props.order.provinceState, props.order.city, props.order.postalCode, props.order.addressLine]
    .filter(Boolean)
    .join(' · ')
})

const selectedAddress = computed(() => addresses.value.find((item) => item.id === form.addressId))

const selectedAddressText = computed(() => {
  const item = selectedAddress.value
  if (!item) return ''
  return [item.country, item.provinceState, item.city, item.postalCode, item.addressLine]
    .filter(Boolean)
    .join(' · ')
})

function addressLabel(item) {
  return [item.consignee, item.country, item.city].filter(Boolean).join(' · ')
}

function handleAddressChange(id) {
  const item = addresses.value.find((address) => address.id === id)
  if (item?.country) form.fromCountry = item.country
}

async function loadAddresses() {
  if (!isMerchant.value) return
  addresses.value = await fetchAddressList().catch((error) => {
    console.error('加载发货地址失败', error)
    return []
  })
  if (!form.addressId && addresses.value.length) {
    const preferred = addresses.value.find((item) => item.isDefault === 1) || addresses.value[0]
    form.addressId = preferred.id
    if (preferred.country) form.fromCountry = preferred.country
  }
}

const rules = {
  shippingCompany: [{ required: true, message: '请填写物流公司', trigger: 'blur' }],
  trackingNo: [{ required: true, message: '请填写物流单号', trigger: 'blur' }]
}

function reset(order) {
  Object.assign(form, {
    addressId: null,
    shippingCompany: '',
    trackingNo: '',
    shippingMethod: 'EXPRESS',
    fromCountry: 'China',
    toCountry: order?.country || '',
    estimatedDelivery: null
  })
  formRef.value?.clearValidate()
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid || !props.order) return

  submitting.value = true
  const payload = {
    shippingCompany: form.shippingCompany,
    trackingNo: form.trackingNo,
    shippingMethod: form.shippingMethod,
    fromCountry: form.fromCountry,
    toCountry: form.toCountry,
    estimatedDelivery: form.estimatedDelivery
  }
  const ok = await shipOrder(props.order.id, payload)
    .then(() => true)
    .catch((error) => {
      console.error('发货失败', error)
      return false
    })
  submitting.value = false
  if (!ok) return

  ElMessage.success('发货成功，订单已进入已发货状态')
  visible.value = false
  emit('shipped')
}

// 每次打开时重置表单，避免残留上一单的物流单号
watch(
  () => props.modelValue,
  (opened) => {
    if (!opened) return
    reset(props.order)
    loadAddresses()
  }
)
</script>
