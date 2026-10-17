import request from './request'

export function fetchProductPage(params) {
  return request.get('/product/page', { params })
}

export function fetchProductDetail(id) {
  return request.get(`/product/${id}`)
}

export function createProduct(data) {
  return request.post('/product', data)
}

export function updateProduct(id, data) {
  return request.put(`/product/${id}`, data)
}

export function updateProductStatus(id, status) {
  return request.put(`/product/${id}/status`, null, { params: { status } })
}

export function deleteProduct(id) {
  return request.delete(`/product/${id}`)
}
