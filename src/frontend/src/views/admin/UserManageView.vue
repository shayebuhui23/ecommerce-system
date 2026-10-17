<template>
  <div class="space-y-4">
    <section class="rounded-2xl border border-slate-200/70 bg-white p-5">
      <div class="flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 class="text-[15px] font-medium text-slate-800">用户管理</h2>
          <p class="mt-1 text-xs text-slate-400">查看全部账号、启用或禁用账号；禁用后该账号无法继续调用受保护接口</p>
        </div>
        <div class="flex items-center gap-2">
          <div class="flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-50/60 px-3 py-1.5 transition-colors focus-within:border-brand-300 focus-within:bg-white">
            <Search class="h-3.5 w-3.5 text-slate-400" />
            <input
              v-model="keyword"
              type="text"
              placeholder="搜索用户名 / 昵称 / 邮箱"
              class="w-52 border-0 bg-transparent p-0 text-xs text-slate-700 outline-none placeholder:text-slate-400"
              @keyup.enter="handleSearch"
            />
          </div>
          <button
            type="button"
            class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:-translate-y-0.5 hover:shadow-md hover:shadow-brand-600/30 active:translate-y-0"
            @click="handleSearch"
          >
            查询
          </button>
          <button
            type="button"
            class="flex items-center gap-1.5 rounded-xl border border-slate-200 px-3 py-2 text-xs text-slate-500 transition-colors hover:bg-slate-50"
            @click="handleReset"
          >
            <RefreshCw class="h-3.5 w-3.5" />
            重置
          </button>
        </div>
      </div>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-2">
      <el-table v-loading="loading" :data="rows" style="width: 100%" :header-cell-style="{ background: '#F8FAFC', color: '#64748B', fontWeight: 500, fontSize: '12px' }">
        <el-table-column prop="username" label="用户名" min-width="130">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span class="flex h-7 w-7 items-center justify-center rounded-full bg-gradient-to-br from-brand-500 to-cyan-500 text-[11px] text-white">
                {{ (row.nickname || row.username).slice(0, 1).toUpperCase() }}
              </span>
              <span class="text-xs text-slate-700">{{ row.username }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="nickname" label="昵称" min-width="110" />
        <el-table-column prop="email" label="邮箱" min-width="180" />
        <el-table-column label="角色" min-width="110">
          <template #default="{ row }">
            <RoleTag :code="row.roleCode" :name="row.roleName" tone="light" />
          </template>
        </el-table-column>
        <el-table-column label="状态" min-width="90">
          <template #default="{ row }">
            <span
              class="inline-flex items-center gap-1.5 text-xs"
              :class="row.status === 1 ? 'text-emerald-600' : 'text-slate-400'"
            >
              <span class="h-1.5 w-1.5 rounded-full bg-current" />
              {{ row.status === 1 ? '启用' : '已禁用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="注册时间" min-width="160" />
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="{ row }">
            <button
              type="button"
              class="rounded-lg px-2.5 py-1 text-xs transition-colors"
              :class="
                row.id === currentUserId
                  ? 'cursor-not-allowed text-slate-300'
                  : row.status === 1
                    ? 'text-rose-500 hover:bg-rose-50'
                    : 'text-emerald-600 hover:bg-emerald-50'
              "
              :disabled="row.id === currentUserId"
              @click="toggleStatus(row)"
            >
              {{ row.status === 1 ? '禁用' : '启用' }}
            </button>
          </template>
        </el-table-column>
        <template #empty>
          <div class="flex flex-col items-center gap-2 py-10 text-xs text-slate-400">
            <ShieldAlert class="h-5 w-5 text-slate-300" />
            没有匹配的用户
          </div>
        </template>
      </el-table>

      <div class="flex flex-wrap items-center justify-between gap-3 px-4 py-3">
        <p class="text-xs text-slate-400">共 {{ total }} 个账号</p>
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="sizes, prev, pager, next"
          background
          @size-change="loadUsers"
          @current-change="loadUsers"
        />
      </div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshCw, Search, ShieldAlert } from 'lucide-vue-next'
import RoleTag from '../../components/RoleTag.vue'
import { fetchUserPage, updateUserStatus } from '../../api/user'
import { useUserStore } from '../../stores/user'

const userStore = useUserStore()
const currentUserId = computed(() => userStore.userInfo?.id)

const loading = ref(false)
const keyword = ref('')
const rows = ref([])
const total = ref(0)
const query = reactive({ pageNum: 1, pageSize: 10 })

async function loadUsers() {
  loading.value = true
  const data = await fetchUserPage({
    pageNum: query.pageNum,
    pageSize: query.pageSize,
    keyword: keyword.value.trim() || undefined
  }).catch((error) => {
    console.error('加载用户列表失败', error)
    return null
  })
  loading.value = false
  if (!data) return
  rows.value = data.records
  total.value = data.total
}

function handleSearch() {
  query.pageNum = 1
  loadUsers()
}

function handleReset() {
  keyword.value = ''
  query.pageNum = 1
  loadUsers()
}

async function toggleStatus(row) {
  const nextStatus = row.status === 1 ? 0 : 1
  const action = nextStatus === 1 ? '启用' : '禁用'

  const confirmed = await ElMessageBox.confirm(
    `确认${action}用户「${row.username}」？${nextStatus === 0 ? '禁用后该账号将无法调用受保护接口。' : ''}`,
    `${action}账号`,
    { confirmButtonText: action, cancelButtonText: '取消', type: 'warning' }
  )
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await updateUserStatus({ userId: row.id, status: nextStatus })
    .then(() => true)
    .catch((error) => {
      console.error('更新用户状态失败', error)
      return false
    })
  if (!ok) return

  ElMessage.success(`已${action}用户「${row.username}」`)
  loadUsers()
}

onMounted(loadUsers)
</script>
