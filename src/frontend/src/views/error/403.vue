<template>
  <div class="flex min-h-screen items-center justify-center bg-[#F5F7FB] px-6">
    <div class="w-full max-w-md rounded-3xl border border-slate-200/70 bg-white p-10 text-center shadow-soft">
      <div class="mx-auto flex h-16 w-16 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 to-cyan-500 text-white">
        <ShieldAlert class="h-7 w-7" />
      </div>
      <p class="mt-5 font-mono text-[11px] uppercase tracking-[0.22em] text-slate-400">403 · Forbidden</p>
      <h1 class="mt-2 text-xl font-medium text-slate-800">没有权限访问该页面</h1>
      <p class="mt-2 text-sm leading-6 text-slate-500">
        当前账号的角色不具备该功能的使用权限。如需调整角色，请联系系统管理员。
      </p>
      <div class="mt-7 flex justify-center gap-3">
        <button
          type="button"
          class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-5 py-2.5 text-sm text-white transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-brand-600/30 active:translate-y-0"
          @click="router.replace('/dashboard')"
        >
          返回工作台
        </button>
        <button
          type="button"
          class="rounded-xl border border-slate-200 px-5 py-2.5 text-sm text-slate-600 transition-colors hover:border-slate-300 hover:bg-slate-50"
          @click="router.replace('/login')"
        >
          切换账号
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ShieldAlert } from 'lucide-vue-next'

const router = useRouter()
</script>
