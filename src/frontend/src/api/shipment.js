import request from './request'

export function shipOrder(orderId, data) {
  return request.post(`/order/${orderId}/ship`, data)
}

export function fetchShipment(orderId) {
  return request.get(`/order/${orderId}/shipment`)
}

export function receiveOrder(orderId) {
  return request.post(`/order/${orderId}/receive`)
}
