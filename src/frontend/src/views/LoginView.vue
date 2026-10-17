<template>
  <div class="relative min-h-screen overflow-hidden bg-ink-900">
    <div class="pointer-events-none absolute inset-0">
      <div class="animate-drift absolute -left-24 top-[-12%] h-[520px] w-[520px] rounded-full bg-brand-600/30 blur-[120px]" />
      <div
        class="animate-drift absolute bottom-[-18%] right-[-12%] h-[480px] w-[480px] rounded-full bg-cyan-500/20 blur-[130px]"
        style="animation-delay: -6s"
      />
      <div class="absolute inset-0 opacity-[0.14]" style="background-image: linear-gradient(rgba(255,255,255,.07) 1px, transparent 1px), linear-gradient(90deg, rgba(255,255,255,.07) 1px, transparent 1px); background-size: 64px 64px" />
    </div>

    <div class="relative z-10 mx-auto flex min-h-screen w-full max-w-[1280px] items-center gap-16 px-6 py-10 lg:px-12">
      <BrandShowcase />

      <section class="w-full animate-rise lg:max-w-[456px]">
        <div class="glass-panel rounded-[28px] p-7 shadow-glass sm:p-8">
          <div class="flex items-center justify-between">
            <div class="flex items-center gap-3">
              <div class="flex h-11 w-11 items-center justify-center rounded-2xl bg-gradient-to-br from-brand-500 to-cyan-500 text-lg font-semibold text-white shadow-lg shadow-brand-600/30">
                E
              </div>
              <div>
                <p class="text-sm font-medium text-white">订单运营中枢</p>
                <p class="font-mono text-[11px] uppercase tracking-[0.18em] text-white/40">Ecommerce Ops</p>
              </div>
            </div>
            <span class="rounded-full border border-white/10 px-2.5 py-1 text-[10px] uppercase tracking-widest text-white/40">v1.0</span>
          </div>

          <div class="mt-7 flex rounded-2xl bg-white/5 p-1">
            <button
              v-for="tab in tabs"
              :key="tab.value"
              type="button"
              class="flex-1 rounded-xl py-2 text-sm transition-all duration-200"
              :class="mode === tab.value ? 'bg-white/12 text-white shadow-sm' : 'text-white/50 hover:text-white/80'"
              @click="switchMode(tab.value)"
            >
              {{ tab.label }}
            </button>
          </div>

          <el-form
            v-if="mode === 'login'"
            ref="loginFormRef"
            :model="loginForm"
            :rules="loginRules"
            class="dark-form mt-6"
            label-position="top"
            @submit.prevent
          >
            <el-form-item prop="account">
              <el-input v-model="loginForm.account" size="large" placeholder="用户名或邮箱">
                <template #prefix><Mail class="h-4 w-4 text-white/35" /></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input
                v-model="loginForm.password"
                type="password"
                size="large"
                show-password
                placeholder="登录密码"
                @keyup.enter="handleLogin"
              >
                <template #prefix><Lock class="h-4 w-4 text-white/35" /></template>
              </el-input>
            </el-form-item>

            <button
              type="button"
              class="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-brand-600 via-brand-500 to-cyan-500 py-3 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-brand-600/40 active:translate-y-0 active:scale-[0.99] disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="loading"
              @click="handleLogin"
            >
              {{ loading ? '正在验证…' : '进入工作台' }}
              <ArrowRight v-if="!loading" class="h-4 w-4" />
            </button>
          </el-form>

          <el-form
            v-else
            ref="registerFormRef"
            :model="registerForm"
            :rules="registerRules"
            class="dark-form mt-6"
            label-position="top"
            @submit.prevent
          >
            <div class="grid grid-cols-2 gap-x-3">
              <el-form-item prop="username">
                <el-input v-model="registerForm.username" size="large" placeholder="用户名">
                  <template #prefix><User class="h-4 w-4 text-white/35" /></template>
                </el-input>
              </el-form-item>
              <el-form-item prop="nickname">
                <el-input v-model="registerForm.nickname" size="large" placeholder="昵称（可选）" />
              </el-form-item>
            </div>
            <el-form-item prop="email">
              <el-input v-model="registerForm.email" size="large" placeholder="邮箱">
                <template #prefix><Mail class="h-4 w-4 text-white/35" /></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="password">
              <el-input v-model="registerForm.password" type="password" size="large" show-password placeholder="设置密码（6~32 位）">
                <template #prefix><Lock class="h-4 w-4 text-white/35" /></template>
              </el-input>
            </el-form-item>
            <el-form-item prop="country">
              <el-input v-model="registerForm.country" size="large" placeholder="所在国家或地区（可选）">
                <template #prefix><Globe2 class="h-4 w-4 text-white/35" /></template>
              </el-input>
            </el-form-item>

            <button
              type="button"
              class="mt-2 flex w-full items-center justify-center gap-2 rounded-xl bg-gradient-to-r from-brand-600 via-brand-500 to-cyan-500 py-3 text-sm font-medium text-white transition-all duration-200 hover:-translate-y-0.5 hover:shadow-lg hover:shadow-brand-600/40 active:translate-y-0 active:scale-[0.99] disabled:cursor-not-allowed disabled:opacity-60"
              :disabled="loading"
              @click="handleRegister"
            >
              {{ loading ? '正在提交…' : '创建客户账号' }}
            </button>
            <p class="mt-3 text-center text-[11px] text-white/35">注册账号固定为普通客户角色，如需商家或管理员权限请联系系统管理员</p>
          </el-form>

          <div v-if="mode === 'login'" class="mt-6">
            <div class="flex items-center gap-3 text-[10px] uppercase tracking-[0.22em] text-white/30">
              <span class="h-px flex-1 bg-white/10" />
              演示账号一键填充
              <span class="h-px flex-1 bg-white/10" />
            </div>
            <div class="mt-3 space-y-2">
              <button
                v-for="demo in demoAccounts"
                :key="demo.account"
                type="button"
                class="flex w-full items-center justify-between rounded-xl border border-white/8 bg-white/[0.03] px-3 py-2.5 text-left transition-all duration-200 hover:border-brand-300/40 hover:bg-white/[0.06]"
                @click="fillDemo(demo)"
              >
                <RoleTag :code="demo.code" :name="demo.name" />
                <span class="font-mono text-[11px] text-white/40">{{ demo.account }} / 123456</span>
              </button>
            </div>
          </div>
        </div>

        <p class="mt-5 text-center text-[11px] text-white/25">
          软件工程课程期末综合实践 · 模拟支付与模拟物流，不接入真实支付与物流服务
        </p>
      </section>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowRight, Globe2, Lock, Mail, User } from 'lucide-vue-next'
import BrandShowcase from '../components/BrandShowcase.vue'
import RoleTag from '../components/RoleTag.vue'
import { useUserStore } from '../stores/user'
import { register as registerApi } from '../api/auth'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const mode = ref('login')
const loading = ref(false)
const loginFormRef = ref()
const registerFormRef = ref()

const tabs = [
  { label: '登录', value: 'login' },
  { label: '注册', value: 'register' }
]

const demoAccounts = [
  { code: 'ADMIN', name: '系统管理员', account: 'admin' },
  { code: 'STAFF', name: '商家运营', account: 'staff' },
  { code: 'CUSTOMER', name: '普通客户', account: 'customer' }
]

const loginForm = reactive({ account: '', password: '' })
const registerForm = reactive({ username: '', email: '', password: '', nickname: '', country: '' })

const loginRules = {
  account: [{ required: true, message: '请输入用户名或邮箱', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { pattern: /^[A-Za-z0-9_]{3,50}$/, message: '3~50 位字母、数字或下划线', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请设置密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度需在 6~32 位之间', trigger: 'blur' }
  ]
}

function switchMode(next) {
  mode.value = next
}

function fillDemo(demo) {
  mode.value = 'login'
  loginForm.account = demo.account
  loginForm.password = '123456'
}

async function handleLogin() {
  const valid = await loginFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const data = await userStore
    .login({ account: loginForm.account, password: loginForm.password })
    .catch((error) => {
      console.error('登录失败', error)
      return null
    })
  loading.value = false
  if (!data) return

  ElMessage.success(`欢迎回来，${data.userInfo.nickname || data.userInfo.username}`)
  const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/dashboard'
  router.replace(redirect)
}

async function handleRegister() {
  const valid = await registerFormRef.value.validate().catch(() => false)
  if (!valid) return

  loading.value = true
  const ok = await registerApi({ ...registerForm })
    .then(() => true)
    .catch((error) => {
      console.error('注册失败', error)
      return false
    })
  loading.value = false
  if (!ok) return

  ElMessage.success('注册成功，请使用新账号登录')
  loginForm.account = registerForm.username
  loginForm.password = ''
  mode.value = 'login'
}
</script>
