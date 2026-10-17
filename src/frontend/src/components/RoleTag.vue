<script setup>
import { computed } from 'vue'

const props = defineProps({
  code: { type: String, default: '' },
  name: { type: String, default: '' },
  tone: { type: String, default: 'dark' }
})

const themeMap = {
  ADMIN: {
    dark: 'border-amber-300/30 bg-amber-300/10 text-amber-200',
    light: 'border-amber-200 bg-amber-50 text-amber-700'
  },
  STAFF: {
    dark: 'border-cyan-300/30 bg-cyan-300/10 text-cyan-200',
    light: 'border-cyan-200 bg-cyan-50 text-cyan-700'
  },
  CUSTOMER: {
    dark: 'border-indigo-300/30 bg-indigo-300/10 text-indigo-200',
    light: 'border-indigo-200 bg-indigo-50 text-indigo-700'
  }
}

const label = computed(() => props.name || props.code || '未分配角色')

const classes = computed(() => {
  const theme = themeMap[props.code] || themeMap.CUSTOMER
  return theme[props.tone] || theme.dark
})
</script>

<template>
  <span
    class="inline-flex items-center gap-1.5 whitespace-nowrap rounded-full border px-2.5 py-1 text-xs font-medium tracking-wide"
    :class="classes"
  >
    <span class="h-1.5 w-1.5 rounded-full bg-current opacity-80" />
    {{ label }}
  </span>
</template>
