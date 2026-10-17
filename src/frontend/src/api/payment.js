import request from './request'

export function payOrder(data) {
  return request.post('/payment/pay', data)
}

export function fetchPaymentByOrder(orderId) {
  return request.get(`/payment/order/${orderId}`)
}

export function fetchPaymentPage(params) {
  return request.get('/payment/page', { params })
}
