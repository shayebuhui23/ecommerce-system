<template>
  <div class="mx-auto max-w-4xl space-y-5">
    <section class="overflow-hidden rounded-3xl border border-slate-200/70 bg-white shadow-soft">
      <div class="relative h-28 bg-ink-800">
        <div class="absolute inset-0 bg-[radial-gradient(circle_at_20%_120%,rgba(99,102,241,0.55),transparent_55%),radial-gradient(circle_at_80%_-20%,rgba(6,182,212,0.35),transparent_50%)]" />
      </div>
      <div class="px-7 pb-7">
        <div class="-mt-9 flex flex-wrap items-end justify-between gap-4">
          <div class="flex items-end gap-4">
            <div class="flex h-[72px] w-[72px] items-center justify-center rounded-2xl border-4 border-white bg-gradient-to-br from-brand-500 to-cyan-500 text-2xl font-medium text-white shadow-lg">
              {{ userStore.displayName.slice(0, 1).toUpperCase() }}
            </div>
            <div class="pb-1">
              <p class="text-lg font-medium text-slate-800">{{ userStore.displayName }}</p>
              <p class="mt-0.5 font-mono text-xs text-slate-400">@{{ profile.username }}</p>
            </div>
          </div>
          <RoleTag :code="userStore.roleCode" :name="userStore.roleName" tone="light" />
        </div>

        <div class="mt-6 grid gap-4 sm:grid-cols-2">
          <div v-for="field in fields" :key="field.label" class="rounded-2xl bg-slate-50/80 px-4 py-3">
            <p class="text-[11px] text-slate-400">{{ field.label }}</p>
            <p class="mt-1 truncate text-sm text-slate-700">{{ field.value }}</p>
          </div>
        </div>

        <div class="mt-6 flex items-start gap-2 rounded-xl border border-amber-200/70 bg-amber-50/60 px-4 py-3 text-xs leading-5 text-amber-700">
          <Info class="mt-0.5 h-3.5 w-3.5 shrink-0" />
          骨架阶段仅提供只读资料展示，资料修改与收货地址管理将在商品与客户功能阶段开放。
        </div>
      </div>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-6">
      <h3 class="text-[15px] font-medium text-slate-800">角色说明</h3>
      <p class="mt-1 text-xs text-slate-400">{{ roleDescription }}</p>
      <div class="mt-4 grid gap-3 sm:grid-cols-3">
        <div
          v-for="role in roles"
          :key="role.code"
          class="rounded-xl border px-4 py-3 transition-all"
          :class="role.code === userStore.roleCode ? 'border-brand-300 bg-brand-50/60' : 'border-slate-100 bg-white'"
        >
          <RoleTag :code="role.code" :name="role.name" tone="light" />
          <p class="mt-2 text-[11px] leading-5 text-slate-500">{{ role.desc }}</p>
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Info } from 'lucide-vue-next'
import RoleTag from '../components/RoleTag.vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const profile = computed(() => userStore.userInfo || {})

const fields = computed(() => [
  { label: '用户名', value: profile.value.username || '—' },
  { label: '邮箱', value: profile.value.email || '—' },
  { label: '昵称', value: profile.value.nickname || '—' },
  { label: '所在国家或地区', value: profile.value.country || '—' },
  { label: '联系电话', value: profile.value.phone || '未填写' },
  { label: '账号状态', value: profile.value.status === 1 ? '正常启用' : '已禁用' }
])

const roles = [
  { code: 'ADMIN', name: '系统管理员', desc: '用户管理、基础配置、全量数据查询与统计' },
  { code: 'STAFF', name: '商家运营', desc: '商品、库存、发货履约与退款审核' },
  { code: 'CUSTOMER', name: '普通客户', desc: '浏览商品、下单支付、收货与售后申请' }
]

const roleDescription = computed(() => {
  const current = roles.find((role) => role.code === userStore.roleCode)
  return current ? current.desc : '角色信息加载中'
})
</script>
