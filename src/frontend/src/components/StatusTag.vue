<template>
  <span
    class="inline-flex items-center justify-center whitespace-nowrap rounded-full border leading-none"
    :class="[sizeClass, toneClass]"
  >
    {{ text }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  text: { type: [String, Number], default: '' },
  tone: { type: String, default: 'slate' },
  size: { type: String, default: 'sm' }
})

const sizeClass = computed(() => {
  const map = {
    xs: 'h-5 px-2 text-[10px]',
    sm: 'h-6 px-2.5 text-[11px]',
    md: 'h-7 px-3 text-xs'
  }
  return map[props.size] || map.sm
})

const toneClass = computed(() => {
  const map = {
    slate: 'border-slate-200 bg-slate-50 text-slate-500',
    amber: 'border-amber-200 bg-amber-50 text-amber-700',
    blue: 'border-blue-200 bg-blue-50 text-blue-700',
    sky: 'border-sky-200 bg-sky-50 text-sky-600',
    indigo: 'border-indigo-200 bg-indigo-50 text-indigo-600',
    emerald: 'border-emerald-200 bg-emerald-50 text-emerald-600',
    rose: 'border-rose-200 bg-rose-50 text-rose-600',
    orange: 'border-orange-200 bg-orange-50 text-orange-600',
    violet: 'border-violet-200 bg-violet-50 text-violet-600',
    brand: 'border-brand-200 bg-brand-50 text-brand-600'
  }
  return map[props.tone] || map.slate
})
</script>
