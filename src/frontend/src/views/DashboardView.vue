<template>
  <div class="space-y-5">
    <section class="relative overflow-hidden rounded-3xl bg-ink-800 px-8 py-8 text-white shadow-soft">
      <div class="pointer-events-none absolute inset-0">
        <div class="absolute -right-16 -top-24 h-72 w-72 rounded-full bg-brand-600/40 blur-[90px]" />
        <div class="absolute -bottom-28 left-1/3 h-64 w-64 rounded-full bg-cyan-500/20 blur-[100px]" />
      </div>

      <div class="relative flex flex-wrap items-start justify-between gap-6">
        <div>
          <p class="text-xs uppercase tracking-[0.2em] text-white/40">{{ today }}</p>
          <h2 class="mt-3 text-[26px] font-semibold leading-snug">
            {{ greeting }}，{{ userStore.displayName }}
            <span class="ml-1 text-white/50">/</span>
            <span class="brand-gradient-text">{{ userStore.roleName || '未分配角色' }}</span>
          </h2>
          <p class="mt-3 max-w-xl text-sm leading-6 text-white/55">
            阶段五已完成：退款售后、商品评价、币种汇率、经营统计与操作审计全部接入，系统进入测试与交付阶段。
          </p>
        </div>
        <div class="rounded-2xl border border-white/10 bg-white/5 px-5 py-4 backdrop-blur">
          <p class="text-[11px] uppercase tracking-[0.18em] text-white/35">当前阶段</p>
          <p class="mt-1 font-mono text-lg">阶段五 · 售后与扩展</p>
          <p class="mt-1 text-xs text-white/45">售后、评价、汇率、统计与审计</p>
        </div>
      </div>
    </section>

    <section class="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
      <article
        v-for="(item, index) in metrics"
        :key="item.label"
        class="animate-rise rounded-2xl border border-slate-200/70 bg-white p-5 shadow-[0_10px_30px_-24px_rgba(31,36,48,0.5)] transition-all duration-200 hover:-translate-y-0.5 hover:shadow-soft"
        :style="{ animationDelay: `${index * 70}ms` }"
      >
        <div class="flex items-center justify-between">
          <p class="text-xs text-slate-400">{{ item.label }}</p>
          <component :is="item.icon" class="h-4 w-4 text-brand-500" />
        </div>
        <p class="mt-3 font-mono text-2xl text-slate-800">{{ item.value }}</p>
        <p class="mt-1 text-[11px] text-slate-400">{{ item.hint }}</p>
      </article>
    </section>

    <section class="grid gap-4 lg:grid-cols-3">
      <div class="rounded-2xl border border-slate-200/70 bg-white p-6 lg:col-span-2">
        <div class="flex items-center justify-between">
          <div>
            <h3 class="text-[15px] font-medium text-slate-800">开发路线图</h3>
            <p class="mt-1 text-xs text-slate-400">按《初始规划.md》第七章分阶段交付，每阶段完成即可运行验收</p>
          </div>
          <span class="rounded-full bg-brand-50 px-3 py-1 text-[11px] text-brand-600">6 个阶段</span>
        </div>

        <ol class="mt-5 space-y-3">
          <li v-for="stage in stages" :key="stage.name" class="flex items-start gap-3 rounded-xl border border-slate-100 px-4 py-3">
            <span
              class="mt-0.5 flex h-6 w-6 shrink-0 items-center justify-center rounded-full text-[11px] font-mono"
              :class="stage.done ? 'bg-brand-600 text-white' : 'bg-slate-100 text-slate-400'"
            >
              {{ stage.index }}
            </span>
            <div class="min-w-0 flex-1">
              <p class="text-sm text-slate-700">{{ stage.name }}</p>
              <p class="mt-0.5 text-xs text-slate-400">{{ stage.desc }}</p>
            </div>
            <span
              class="shrink-0 rounded-full px-2.5 py-1 text-[10px]"
              :class="stage.done ? 'bg-emerald-50 text-emerald-600' : 'bg-slate-50 text-slate-400'"
            >
              {{ stage.done ? '已完成' : '待开始' }}
            </span>
          </li>
        </ol>
      </div>

      <div class="rounded-2xl border border-slate-200/70 bg-white p-6">
        <h3 class="text-[15px] font-medium text-slate-800">你的权限范围</h3>
        <p class="mt-1 text-xs text-slate-400">权限由后端校验，前端仅做展示</p>

        <div class="mt-4 rounded-xl bg-slate-50 p-4">
          <RoleTag :code="userStore.roleCode" :name="userStore.roleName" tone="light" />
          <ul class="mt-3 space-y-2">
            <li v-for="item in abilities" :key="item" class="flex items-start gap-2 text-xs leading-5 text-slate-500">
              <ShieldCheck class="mt-0.5 h-3.5 w-3.5 shrink-0 text-emerald-500" />
              {{ item }}
            </li>
          </ul>
        </div>

        <div class="mt-4 flex items-center gap-2 rounded-xl border border-dashed border-slate-200 px-4 py-3 text-xs text-slate-400">
          <Workflow class="h-3.5 w-3.5 text-brand-400" />
          商品、订单等模块将在后续阶段接入
          <ArrowUpRight class="ml-auto h-3.5 w-3.5" />
        </div>
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Boxes, Database, ShieldCheck, Users, Workflow, ArrowUpRight } from 'lucide-vue-next'
import RoleTag from '../components/RoleTag.vue'
import { useUserStore } from '../stores/user'

const userStore = useUserStore()

const today = new Date().toLocaleDateString('zh-CN', {
  year: 'numeric',
  month: 'long',
  day: 'numeric',
  weekday: 'long'
})

const greeting = computed(() => {
  const hour = new Date().getHours()
  if (hour < 6) return '夜深了'
  if (hour < 12) return '早上好'
  if (hour < 18) return '下午好'
  return '晚上好'
})

const metrics = [
  { label: '数据库表', value: '17', hint: '商品 / 订单 / 售后全覆盖', icon: Database },
  { label: '系统角色', value: '3', hint: '管理员、商家运营、普通客户', icon: Users },
  { label: '核心接口', value: '48', hint: '覆盖全部业务域', icon: Workflow },
  { label: '业务模块', value: '8 / 8', hint: '全部规划模块已上线', icon: Boxes }
]

const stages = [
  { index: 1, name: '项目初始化', desc: '工程、数据库、统一响应、登录与角色权限', done: true },
  { index: 2, name: '商品与客户功能', desc: '分类、商品、SKU、搜索与收货地址', done: true },
  { index: 3, name: '购物车与订单', desc: '购物车、下单、库存预扣与状态机', done: true },
  { index: 4, name: '支付与履约', desc: '模拟支付、发货、物流与确认收货', done: true },
  { index: 5, name: '售后与扩展', desc: '退款审核、评价、币种汇率与统计', done: true },
  { index: 6, name: '测试与交付', desc: '接口测试、缺陷修复与运行说明', done: false }
]

const abilityMap = {
  ADMIN: ['查看全量用户并启用/禁用账号', '维护商品分类与系统基础配置', '查询订单、支付与售后记录', '查看经营统计与操作日志'],
  STAFF: ['维护商品资料、SKU 与库存', '处理订单发货与物流信息', '审核客户退款申请', '查看商品销售与订单统计'],
  CUSTOMER: ['浏览商品、加入购物车并下单', '模拟支付、查看物流与确认收货', '对已完成订单申请退款', '维护个人资料与收货地址']
}

const abilities = computed(() => abilityMap[userStore.roleCode] || ['登录后可查看个人资料'])
</script>
