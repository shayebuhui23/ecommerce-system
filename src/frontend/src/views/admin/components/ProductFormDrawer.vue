<template>
  <el-drawer
    v-model="visible"
    :title="isEdit ? '编辑商品' : '新增商品'"
    size="660px"
    :close-on-click-modal="false"
  >
    <el-form ref="formRef" v-loading="loading" :model="form" :rules="rules" label-position="top">
      <div class="grid grid-cols-2 gap-x-4">
        <el-form-item label="所属分类" prop="categoryId" class="col-span-2">
          <el-select v-model="form.categoryId" class="w-full" placeholder="请选择商品分类">
            <el-option v-for="item in categoryOptions" :key="item.id" :value="item.id" :label="item.label" />
          </el-select>
        </el-form-item>

        <el-form-item label="商品 SKU 编码" prop="skuCode">
          <el-input v-model="form.skuCode" maxlength="64" placeholder="EC-HEADPHONE-001" />
        </el-form-item>
        <el-form-item label="基础币种" prop="currency">
          <el-select v-model="form.currency" class="w-full">
            <el-option value="CNY" label="CNY 人民币" />
            <el-option value="USD" label="USD 美元" />
          </el-select>
        </el-form-item>

        <el-form-item label="商品名称（中文）" prop="name" class="col-span-2">
          <el-input v-model="form.name" maxlength="128" placeholder="主动降噪无线耳机" />
        </el-form-item>
        <el-form-item label="商品名称（英文）" class="col-span-2">
          <el-input v-model="form.nameEn" maxlength="128" placeholder="Active Noise-Cancelling Headphones" />
        </el-form-item>

        <el-form-item label="售价" prop="price">
          <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="10" class="w-full" controls-position="right" />
        </el-form-item>
        <el-form-item label="库存" prop="stock">
          <el-input-number v-model="form.stock" :min="0" :precision="0" class="w-full" controls-position="right" />
        </el-form-item>

        <el-form-item label="商品图片地址">
          <el-input v-model="form.imageUrl" maxlength="255" placeholder="https://images.unsplash.com/photo-..." />
        </el-form-item>
        <el-form-item label="上架状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">上架</el-radio>
            <el-radio :label="0">下架</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="中文描述" class="col-span-2">
          <el-input v-model="form.description" type="textarea" :rows="3" maxlength="2000" show-word-limit />
        </el-form-item>
        <el-form-item label="英文描述" class="col-span-2">
          <el-input v-model="form.descriptionEn" type="textarea" :rows="3" maxlength="2000" show-word-limit />
        </el-form-item>
      </div>

      <div class="rounded-2xl border border-slate-200 p-4">
        <div class="flex items-start justify-between">
          <div>
            <p class="text-sm text-slate-700">商品规格 SKU</p>
            <p class="mt-0.5 text-[11px] text-slate-400">可选；SKU 编码全局唯一，留空则使用商品级价格与库存</p>
          </div>
          <button
            type="button"
            class="flex items-center gap-1 rounded-lg border border-slate-200 px-2.5 py-1.5 text-[11px] text-slate-600 transition-colors hover:border-brand-300 hover:text-brand-600"
            @click="addSku"
          >
            <Plus class="h-3 w-3" />
            添加规格
          </button>
        </div>

        <div v-if="form.skus.length" class="mt-3 space-y-2">
          <div v-for="(sku, index) in form.skus" :key="index" class="rounded-xl bg-slate-50 p-3">
            <div class="mb-2 flex items-center justify-between">
              <span class="font-mono text-[11px] text-slate-400">SKU {{ index + 1 }}</span>
              <button type="button" class="text-[11px] text-rose-500 hover:underline" @click="form.skus.splice(index, 1)">
                移除
              </button>
            </div>
            <div class="grid grid-cols-2 gap-2">
              <el-input v-model="sku.skuCode" size="small" placeholder="SKU 编码" />
              <el-input v-model="sku.specName" size="small" placeholder="规格，如 Color:Black" />
              <el-input-number v-model="sku.price" :min="0.01" :precision="2" size="small" controls-position="right" class="!w-full" />
              <el-input-number v-model="sku.stock" :min="0" :precision="0" size="small" controls-position="right" class="!w-full" />
            </div>
          </div>
        </div>
        <p v-else class="mt-3 rounded-xl bg-slate-50 px-3 py-2 text-xs text-slate-400">
          未添加规格，将直接使用商品级价格与库存
        </p>
      </div>
    </el-form>

    <template #footer>
      <div class="flex justify-end gap-2">
        <button
          type="button"
          class="rounded-xl border border-slate-200 px-4 py-2 text-xs text-slate-600 transition-colors hover:bg-slate-50"
          @click="visible = false"
        >
          取消
        </button>
        <button
          type="button"
          class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:shadow-md disabled:opacity-60"
          :disabled="submitting"
          @click="submit"
        >
          {{ submitting ? '保存中…' : '保存商品' }}
        </button>
      </div>
    </template>
  </el-drawer>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import { fetchCategoryTree } from '../../../api/category'
import { createProduct, fetchProductDetail, updateProduct } from '../../../api/product'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  productId: { type: [Number, String], default: null }
})
const emit = defineEmits(['update:modelValue', 'saved'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const isEdit = computed(() => Boolean(props.productId))
const loading = ref(false)
const submitting = ref(false)
const formRef = ref()
const categoryOptions = ref([])

const form = reactive(createEmptyForm())

const rules = {
  categoryId: [{ required: true, message: '请选择商品分类', trigger: 'change' }],
  skuCode: [{ required: true, message: '请输入商品 SKU 编码', trigger: 'blur' }],
  name: [{ required: true, message: '请输入商品名称', trigger: 'blur' }],
  price: [{ required: true, message: '请输入售价', trigger: 'blur' }],
  stock: [{ required: true, message: '请输入库存', trigger: 'blur' }]
}

function createEmptyForm() {
  return {
    categoryId: null,
    skuCode: '',
    name: '',
    nameEn: '',
    description: '',
    descriptionEn: '',
    imageUrl: '',
    price: 0,
    currency: 'CNY',
    stock: 0,
    status: 0,
    skus: []
  }
}

function resetForm() {
  Object.assign(form, createEmptyForm())
  formRef.value?.clearValidate()
}

function addSku() {
  form.skus.push({ id: null, skuCode: '', specName: '', price: 1, stock: 0, status: 1 })
}

async function loadCategories() {
  const data = await fetchCategoryTree().catch((error) => {
    console.error('加载分类失败', error)
    return []
  })
  const options = []
  data.forEach((parent) => {
    options.push({ id: parent.id, label: parent.name })
    ;(parent.children || []).forEach((child) => {
      options.push({ id: child.id, label: `　└ ${child.name}` })
    })
  })
  categoryOptions.value = options
}

async function loadDetail() {
  if (!props.productId) return
  loading.value = true
  const data = await fetchProductDetail(props.productId).catch((error) => {
    console.error('加载商品详情失败', error)
    return null
  })
  loading.value = false
  if (!data) return

  Object.assign(form, {
    categoryId: data.categoryId,
    skuCode: data.skuCode,
    name: data.name,
    nameEn: data.nameEn || '',
    description: data.description || '',
    descriptionEn: data.descriptionEn || '',
    imageUrl: data.imageUrl || '',
    price: Number(data.price),
    currency: data.currency,
    stock: data.stock,
    status: data.status,
    skus: (data.skus || []).map((sku) => ({
      id: sku.id,
      skuCode: sku.skuCode,
      specName: sku.specName || '',
      price: Number(sku.price),
      stock: sku.stock,
      status: sku.status
    }))
  })
}

watch(
  () => props.modelValue,
  async (opened) => {
    if (!opened) return
    resetForm()
    await loadCategories()
    await loadDetail()
  }
)

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  const invalidSku = form.skus.find((sku) => !sku.skuCode.trim())
  if (invalidSku) {
    ElMessage.warning('请填写完整的 SKU 编码，或移除空行')
    return
  }

  const payload = {
    ...form,
    nameEn: form.nameEn || null,
    imageUrl: form.imageUrl || null,
    skus: form.skus.map((sku) => ({
      id: sku.id,
      skuCode: sku.skuCode.trim(),
      specName: sku.specName || null,
      price: sku.price,
      stock: sku.stock,
      status: sku.status ?? 1
    }))
  }

  submitting.value = true
  const ok = await (isEdit.value ? updateProduct(props.productId, payload) : createProduct(payload))
    .then(() => true)
    .catch((error) => {
      console.error('保存商品失败', error)
      return false
    })
  submitting.value = false
  if (!ok) return

  ElMessage.success(isEdit.value ? '商品已更新' : '商品已创建')
  visible.value = false
  emit('saved')
}
</script>
