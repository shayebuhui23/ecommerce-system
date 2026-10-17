<template>
  <el-dialog v-model="visible" title="商品评价" width="440px" align-center>
    <div class="pt-1">
      <div class="flex items-center gap-3 rounded-xl bg-slate-50 px-4 py-3">
        <img v-if="item?.imageUrl" :src="item.imageUrl" class="h-10 w-10 rounded-lg object-cover" />
        <div v-else class="flex h-10 w-10 items-center justify-center rounded-lg bg-slate-200 text-[10px] text-slate-400">无图</div>
        <p class="text-sm text-slate-700">{{ item?.productName }}</p>
      </div>

      <div class="mt-4">
        <p class="text-xs text-slate-500">评分</p>
        <el-rate v-model="form.rating" :max="5" show-text :texts="['很差', '较差', '一般', '满意', '非常满意']" class="mt-1" />
      </div>

      <div class="mt-4">
        <p class="text-xs text-slate-500">评价内容</p>
        <el-input
          v-model="form.content"
          type="textarea"
          :rows="4"
          maxlength="500"
          show-word-limit
          placeholder="说说商品质量、物流速度或使用体验"
          class="mt-1"
        />
      </div>
      <p class="mt-2 text-[11px] text-slate-400">同一订单商品只能评价一次，提交后不可修改</p>
    </div>

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
          {{ submitting ? '提交中…' : '提交评价' }}
        </button>
      </div>
    </template>
  </el-dialog>
</template>

<script setup>
import { computed, reactive, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { createReview } from '../../../api/review'

const props = defineProps({
  modelValue: { type: Boolean, default: false },
  item: { type: Object, default: null }
})
const emit = defineEmits(['update:modelValue', 'reviewed'])

const visible = computed({
  get: () => props.modelValue,
  set: (value) => emit('update:modelValue', value)
})

const submitting = ref(false)
const form = reactive({ rating: 5, content: '' })

async function submit() {
  if (!props.item) return
  if (!form.rating) {
    ElMessage.warning('请选择评分')
    return
  }

  submitting.value = true
  const ok = await createReview({
    orderItemId: props.item.id,
    rating: form.rating,
    content: form.content || null
  })
    .then(() => true)
    .catch((error) => {
      console.error('提交评价失败', error)
      return false
    })
  submitting.value = false
  if (!ok) return

  ElMessage.success('评价已提交')
  visible.value = false
  emit('reviewed')
}

watch(
  () => props.modelValue,
  (opened) => {
    if (opened) {
      form.rating = 5
      form.content = ''
    }
  }
)
</script>
