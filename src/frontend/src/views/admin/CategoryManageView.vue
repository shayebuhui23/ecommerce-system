<template>
  <div class="space-y-4">
    <section class="flex flex-wrap items-center justify-between gap-3 rounded-2xl border border-slate-200/70 bg-white p-5">
      <div>
        <h2 class="text-[15px] font-medium text-slate-800">分类管理</h2>
        <p class="mt-1 text-xs text-slate-400">两级分类结构；存在子分类或关联商品时不允许删除</p>
      </div>
      <button
        type="button"
        class="flex items-center gap-1.5 rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white transition-all hover:-translate-y-0.5 hover:shadow-md hover:shadow-brand-600/30 active:translate-y-0"
        @click="openCreate(0)"
      >
        <Plus class="h-3.5 w-3.5" />
        新增顶级分类
      </button>
    </section>

    <section class="rounded-2xl border border-slate-200/70 bg-white p-2">
      <el-table
        v-loading="loading"
        :data="tree"
        row-key="id"
        default-expand-all
        :tree-props="{ children: 'children' }"
        :header-cell-style="{ background: '#F8FAFC', color: '#64748B', fontWeight: 500, fontSize: '12px' }"
      >
        <el-table-column label="分类名称" min-width="260">
          <template #default="{ row }">
            <div class="flex items-center gap-2">
              <span
                class="flex h-6 w-6 items-center justify-center rounded-lg text-[10px]"
                :class="row.parentId === 0 ? 'bg-brand-50 text-brand-600' : 'bg-slate-100 text-slate-400'"
              >
                {{ row.parentId === 0 ? '级' : '子' }}
              </span>
              <span class="text-sm text-slate-700">{{ row.name }}</span>
              <span class="text-[11px] text-slate-400">{{ row.nameEn }}</span>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="sortOrder" label="排序" width="80" />
        <el-table-column label="状态" width="90">
          <template #default="{ row }">
            <span class="inline-flex items-center gap-1.5 text-xs" :class="row.status === 1 ? 'text-emerald-600' : 'text-slate-400'">
              <span class="h-1.5 w-1.5 rounded-full bg-current" />
              {{ row.status === 1 ? '启用' : '停用' }}
            </span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="{ row }">
            <div class="flex items-center gap-1">
              <button
                v-if="row.parentId === 0"
                type="button"
                class="rounded-lg px-2 py-1 text-xs text-brand-600 transition-colors hover:bg-brand-50"
                @click="openCreate(row.id)"
              >
                添加子分类
              </button>
              <button
                type="button"
                class="rounded-lg px-2 py-1 text-xs text-slate-500 transition-colors hover:bg-slate-100"
                @click="openEdit(row)"
              >
                编辑
              </button>
              <button
                type="button"
                class="rounded-lg px-2 py-1 text-xs text-rose-500 transition-colors hover:bg-rose-50"
                @click="remove(row)"
              >
                删除
              </button>
            </div>
          </template>
        </el-table-column>
        <template #empty>
          <div class="py-10 text-xs text-slate-400">还没有分类，先创建一个顶级分类</div>
        </template>
      </el-table>
    </section>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="440px" align-center>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="88px" class="pt-2">
        <el-form-item label="上级分类" prop="parentId">
          <el-select v-model="form.parentId" class="w-full" :disabled="Boolean(editingId && hasChildren)">
            <el-option :value="0" label="顶级分类" />
            <el-option v-for="item in parentOptions" :key="item.id" :value="item.id" :label="item.name" />
          </el-select>
        </el-form-item>
        <el-form-item label="分类名称" prop="name">
          <el-input v-model="form.name" maxlength="64" placeholder="例如：消费电子" />
        </el-form-item>
        <el-form-item label="英文名称" prop="nameEn">
          <el-input v-model="form.nameEn" maxlength="64" placeholder="Consumer Electronics" />
        </el-form-item>
        <el-form-item label="排序值" prop="sortOrder">
          <el-input-number v-model="form.sortOrder" :min="0" :max="999" controls-position="right" class="w-full" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :label="1">启用</el-radio>
            <el-radio :label="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <div class="flex justify-end gap-2">
          <button type="button" class="rounded-xl border border-slate-200 px-4 py-2 text-xs text-slate-600 hover:bg-slate-50" @click="dialogVisible = false">
            取消
          </button>
          <button
            type="button"
            class="rounded-xl bg-gradient-to-r from-brand-600 to-cyan-500 px-4 py-2 text-xs text-white hover:shadow-md disabled:opacity-60"
            :disabled="submitting"
            @click="submit"
          >
            {{ submitting ? '保存中…' : '保存' }}
          </button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus } from 'lucide-vue-next'
import { createCategory, deleteCategory, fetchCategoryTree, updateCategory } from '../../api/category'

const loading = ref(false)
const submitting = ref(false)
const tree = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const hasChildren = ref(false)
const formRef = ref()

const form = reactive({ parentId: 0, name: '', nameEn: '', sortOrder: 0, status: 1 })

const rules = {
  name: [{ required: true, message: '请输入分类名称', trigger: 'blur' }]
}

const parentOptions = computed(() =>
  tree.value.filter((item) => item.id !== editingId.value).map((item) => ({ id: item.id, name: item.name }))
)

async function loadTree() {
  loading.value = true
  const data = await fetchCategoryTree().catch((error) => {
    console.error('加载分类失败', error)
    return null
  })
  loading.value = false
  if (data) tree.value = data
}

function openCreate(parentId) {
  editingId.value = null
  hasChildren.value = false
  Object.assign(form, { parentId, name: '', nameEn: '', sortOrder: 0, status: 1 })
  dialogVisible.value = true
}

function openEdit(row) {
  editingId.value = row.id
  hasChildren.value = Array.isArray(row.children) && row.children.length > 0
  Object.assign(form, {
    parentId: row.parentId ?? 0,
    name: row.name,
    nameEn: row.nameEn || '',
    sortOrder: row.sortOrder ?? 0,
    status: row.status
  })
  dialogVisible.value = true
}

async function submit() {
  const valid = await formRef.value.validate().catch(() => false)
  if (!valid) return

  submitting.value = true
  const payload = { ...form, nameEn: form.nameEn || null }
  const ok = await (editingId.value ? updateCategory(editingId.value, payload) : createCategory(payload))
    .then(() => true)
    .catch((error) => {
      console.error('保存分类失败', error)
      return false
    })
  submitting.value = false
  if (!ok) return

  ElMessage.success(editingId.value ? '分类已更新' : '分类已创建')
  dialogVisible.value = false
  loadTree()
}

async function remove(row) {
  const confirmed = await ElMessageBox.confirm(
    `确认删除分类「${row.name}」？分类下存在商品时会被拒绝。`,
    '删除分类',
    { confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning' }
  )
    .then(() => true)
    .catch(() => false)
  if (!confirmed) return

  const ok = await deleteCategory(row.id)
    .then(() => true)
    .catch((error) => {
      console.error('删除分类失败', error)
      return false
    })
  if (!ok) return

  ElMessage.success('分类已删除')
  loadTree()
}

onMounted(loadTree)
</script>
