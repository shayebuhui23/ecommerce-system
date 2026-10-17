<template>
  <div class="space-y-4">
    <section class="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200/70 bg-white p-5">
      <div>
        <h2 class="text-[15px] font-medium text-slate-800">收货地址</h2>
        <p class="mt-1 text-xs text-slate-400">按国际地址结构填写，支持国家/地区、省州、城市与邮编；下单时可保存为订单快照</p>
      </div>
      <button
        type="button"
        class="flex items-center gap-1.5 rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:-translate-y-0.5 hover:shadow-md hover:shadow-brand-600/30 active:translate-y-0"
        @click="openCreate"
      >
        <Plus class="h-3.5 w-3.5" />
        新增地址
      </button>
    </section>

    <section v-loading="loading" class="grid gap-3 lg:grid-cols-2">
      <article
        v-for="item in list"
        :key="item.id"
        class="group rounded-2xl border bg-white p-5 transition-all duration-200 hover:-translate-y-0.5 hover:shadow-soft"
        :class="item.isDefault === 1 ? 'border-brand-200 ring-1 ring-brand-100' : 'border-slate-200/70'"
      >
        <div class="flex items-start justify-between gap-3">
          <div class="flex items-center gap-2">
            <span class="flex h-8 w-8 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-cyan-500 text-xs text-white">
              {{ item.consignee.slice(0, 1).toUpperCase() }}
            </span>
            <div>
              <p class="text-sm text-slate-800">{{ item.consignee }}</p>
              <p class="font-mono text-[11px] text-slate-400">{{ item.phone || '未填写电话' }}</p>
            </div>
          </div>
          <span
            v-if="item.isDefault === 1"
            class="rounded-full border border-brand-200 bg-brand-50 px-2.5 py-1 text-[10px] text-brand-600"
          >
            默认地址
          </span>
        </div>

        <p class="mt-3 text-xs leading-5 text-slate-500">{{ formatAddress(item) }}</p>

        <div class="mt-4 flex items-center gap-1 border-t border-slate-100 pt-3">
          <button
            v-if="item.isDefault !== 1"
            type="button"
            class="rounded-lg px-2 py-1 text-xs text-brand-600 transition-colors hover:bg-brand-50"
            @click="markDefault(item)"
          >
            设为默认
          </button>
          <button type="button" class="rounded-lg px-2 py-1 text-xs text-slate-500 transition-colors hover:bg-slate-100" @click="openEdit(item)">
            编辑
          </button>
          <button type="button" class="rounded-lg px-2 py-1 text-xs text-rose-500 transition-colors hover:bg-rose-50" @click="remove(item)">
            删除
          </button>
        </div>
      </article>

      <div
        v-if="!list.length && !loading"
        class="flex flex-col items-center gap-3 rounded-2xl border border-dashed border-slate-200 bg-white py-16 lg:col-span-2"
      >
        <MapPin class="h-7 w-7 text-slate-300" />
        <p class="text-sm text-slate-500">还没有收货地址</p>
        <p class="text-xs text-slate-400">新增一个地址，后续下单时可以直接选用</p>
      </div>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑地址' : '新增地址'" width="520px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" class="pt-1">
        <div class="grid grid-cols-2 gap-x-3">
          <el-form-item label="收货人" prop="consignee">
            <el-input v-model="form.consignee" maxlength="64" placeholder="Alex Chen" />
          </el-form-item>
          <el-form-item label="联系电话" prop="phone">
            <el-input v-model="form.phone" maxlength="30" placeholder="+1 415 555 0132" />
          </el-form-item>
          <el-form-item label="国家或地区" prop="country" class="col-span-2">
            <el-input v-model="form.country" maxlength="64" placeholder="United States" />
          </el-form-item>
          <el-form-item label="省 / 州">
            <el-input v-model="form.provinceState" maxlength="64" placeholder="California" />
          </el-form-item>
          <el-form-item label="城市">
            <el-input v-model="form.city" maxlength="64" placeholder="San Francisco" />
          </el-form-item>
          <el-form-item label="邮政编码">
            <el-input v-model="form.postalCode" maxlength="20" placeholder="94107" />
          </el-form-item>
          <el-form-item label="设为默认">
            <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
          </el-form-item>
          <el-form-item label="详细地址" prop="addressLine" class="col-span-2">
            <el-input v-model="form.addressLine" type="textarea" :rows="2" maxlength="255" placeholder="1355 Market Street, Suite 900" />
          </el-form-item>
        </div>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <button type="button" class="rounded-xl border border-slate-200 px-4 py-2 text-xs text-slate-600 hover:bg-slate-50" @click="dialogVisible = false">
            取消
          </button>
          <button
            type="button"
            class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white hover:shadow-md disabled:opacity-60"
            :disabled="submitting"
            @click="submit"
          >
            {{ submitting ? '保存中…' : '保存地址' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { MapPin, Plus } from 'lucide-vue-next'
import {
  createAddress,
  deleteAddress,
  fetchAddressList,
  setDefaultAddress,
  updateAddress
} from '../api/address'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()

const form = reactive(createEmptyForm())

const rules = {
  consignee: [{ required: true, message: '请输入收货人', trigger: 'blur' }],
  country: [{ required: true, message: '请输入国家或地区', trigger: 'blur' }],
  addressLine: [{ required: true, message: '请输入详细地址', trigger: 'blur' }]
}

function createEmptyForm() {
  return {
    consignee: '',
    country: '',
    provinceState: '',
    city: '',
    postalCode: '',
    addressLine: '',
    phone: '',
    isDefault: 0
  }
}

function formatAddress(item) {
  return [item.country, item.provinceState, item.city, item.postalCode, item.addressLine]
    .filter((part) => part)
    .join(' · ')
}

async function loadList() {
  loading.value = true
  const data = await fetchAddressList().catch((error) => {
    console.error('加载地址失败', error)
    return []
  })
  loading.value = false
  list.value = data
}

function openCreate() {
  editingId.value = null
  Object.assign(form, createEmptyForm())
  formRef.value?.clearValidate()
  dialogVisible.value = true
}

function openEdit(item) {
  editingId.value = item.id
  Object.assign(form, {
    consignee: item.consignee,
    country: item.country,
    provinceState: item.provinceState || '',
    city: item.city || '',
    postalCode: item.postalCode || '',
    addressLine: item.addressLine,
    phone: item.phone || '',
    isDefault: item.isDefault
  })
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const payload = {
    ...form,
    provinceState: form.provinceState || null,
    city: form.city || null,
    postalCode: form.postalCode || null,
    phone: form.phone || null
  }

  submitting.value = true
  const ok = await (editingId.value ? updateAddress(editingId.value, payload) : createAddress(payload))
    .then(() => true)
    .catch((error) => {
      console.error('保存地址失败', error)
      return false
    })
  submitting.value = false
  if (!ok) return

  ElMessage.success(editingId.value ? '地址已更新' : '地址已保存')
  dialogVisible.value = false
  loadList()
}

async function markDefault(item) {
  const ok = await setDefaultAddress(item.id)
    .then(() => true)
    .catch((error) => {
      console.error('设置默认地址失败', error)
      return false
    })
  if (!ok) return
  ElMessage.success('已设为默认地址')
  loadList()
}

async function remove(item) {
  const confirmed = await ElMessageBox.confirm(`确认删除「${item.consignee}」的收货地址？`, '删除地址', {
    confirmButtonText: '删除',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await deleteAddress(item.id)
    .then(() => true)
    .catch((error) => {
      console.error('删除地址失败', error)
      return false
    })
  if (!ok) return
  ElMessage.success('地址已删除')
  loadList()
}

onMounted(loadList)
</script>
