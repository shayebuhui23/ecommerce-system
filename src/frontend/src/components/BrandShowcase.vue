<template>
  <div class="relative hidden flex-1 lg:block">
    <div class="mb-8 inline-flex items-center gap-2 rounded-full border border-white/15 bg-white/5 px-4 py-1.5 text-[11px] uppercase tracking-[0.24em] text-white/70">
      <span class="h-1.5 w-1.5 animate-pulse rounded-full bg-mint" />
      Cross-border commerce
    </div>

    <h1 class="max-w-xl text-[42px] font-semibold leading-[1.18] tracking-tight text-white">
      把每一笔跨境订单<br />
      跑成一条<span class="brand-gradient-text">清晰的流水线</span>
    </h1>

    <p class="mt-6 max-w-md text-[15px] leading-7 text-white/60">
      从商品目录、购物车到支付、发货与退款售后，一套后台串起整条履约链路。
      支持中英文商品资料、国际收货地址与币种换算，适配小型跨境外贸团队的日常运营。
    </p>

    <div class="mt-10 grid max-w-lg grid-cols-3 gap-4">
      <div
        v-for="(item, index) in highlights"
        :key="item.label"
        class="glass-panel animate-rise rounded-2xl px-4 py-4"
        :style="{ animationDelay: `${index * 90 + 160}ms` }"
      >
        <p class="font-mono text-xl text-white">{{ item.value }}</p>
        <p class="mt-1 text-xs text-white/55">{{ item.label }}</p>
      </div>
    </div>

    <div class="mt-10 max-w-md space-y-3">
      <div v-for="point in points" :key="point" class="flex items-start gap-3 text-sm text-white/65">
        <span class="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-gradient-to-r from-brand-300 to-mint" />
        {{ point }}
      </div>
    </div>

    <p class="mt-12 text-xs text-white/35">
      Software Engineering Practice · 单商家模式 · 模拟支付与模拟物流
    </p>
  </div>
</template>

<script setup>
const highlights = [
  { value: '17', label: '业务数据表' },
  { value: '3', label: '角色权限' },
  { value: 'CNY/USD', label: '模拟币种' }
]

const points = [
  '库存预扣与订单快照，保证历史订单金额不被改价影响',
  '订单状态机收敛状态流转，非法操作在后端直接拒绝',
  '退款审核与库存恢复分离，避免重复退款与重复回库'
]
</script>
