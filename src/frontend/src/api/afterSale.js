import request from './request'

export function applyAfterSale(data) {
  return request.post('/after-sale', data)
}

export function auditAfterSale(id, data) {
  return request.put(`/after-sale/${id}/audit`, data)
}

export function fetchAfterSaleByOrder(orderId) {
  return request.get(`/after-sale/order/${orderId}`)
}

export function fetchAfterSalePage(params) {
  return request.get('/after-sale/page', { params })
}

export function submitReturnShipment(id, data) {
  return request.put(`/after-sale/${id}/return-shipment`, data)
}

export function confirmReturnReceipt(id) {
  return request.put(`/after-sale/${id}/confirm-receipt`)
}
