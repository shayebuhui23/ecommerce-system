import request from './request'

export function fetchCategoryTree() {
  return request.get('/category/tree')
}

export function createCategory(data) {
  return request.post('/category', data)
}

export function updateCategory(id, data) {
  return request.put(`/category/${id}`, data)
}

export function deleteCategory(id) {
  return request.delete(`/category/${id}`)
}
