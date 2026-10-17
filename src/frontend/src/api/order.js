import request from './request'

export function createOrder(data) {
  return request.post('/order', data)
}

export function fetchOrderPage(params) {
  return request.get('/order/page', { params })
}

export function fetchOrderDetail(id) {
  return request.get(`/order/${id}`)
}

export function cancelOrder(id) {
  return request.put(`/order/${id}/cancel`)
}
