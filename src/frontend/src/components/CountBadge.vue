<template>
  <span class="inline-flex items-center justify-center rounded-full text-center font-medium leading-none tabular-nums" :class="classes">
    {{ display }}
  </span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  value: { type: [Number, String], default: 0 },
  max: { type: Number, default: 99 },
  size: { type: String, default: 'sm' },
  tone: { type: String, default: 'rose' },
  ring: { type: String, default: 'none' }
})

const display = computed(() => {
  const count = Number(props.value || 0)
  if (!Number.isFinite(count) || count <= 0) return ''
  return count > props.max ? `${props.max}+` : String(count)
})

const sizeClass = computed(() => {
  const map = {
    xs: 'h-4 min-w-4 px-1 text-[10px]',
    sm: 'h-[18px] min-w-[18px] px-1.5 text-[11px]',
    md: 'h-5 min-w-5 px-1.5 text-[11px]'
  }
  return map[props.size] || map.sm
})

const toneClass = computed(() => {
  const map = {
    rose: 'bg-gradient-to-br from-rose-500 to-red-500 text-white shadow-[0_2px_6px_rgba(244,63,94,0.4)]',
    brand: 'bg-gradient-to-br from-brand-600 to-cyan-500 text-white shadow-[0_2px_6px_rgba(79,70,229,0.35)]',
    slate: 'bg-slate-500 text-white',
    light: 'bg-rose-50 text-rose-600'
  }
  return map[props.tone] || map.rose
})

const ringClass = computed(() => {
  const map = {
    ink: 'ring-2 ring-ink-800',
    white: 'ring-2 ring-white',
    none: ''
  }
  return map[props.ring] || ''
})

const classes = computed(() => [sizeClass.value, toneClass.value, ringClass.value])
</script>
