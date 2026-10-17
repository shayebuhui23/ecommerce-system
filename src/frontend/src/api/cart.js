import request from './request'

export function fetchCart() {
  return request.get('/cart')
}

export function addToCart(data) {
  return request.post('/cart/items', data)
}

export function updateCartQuantity(id, quantity) {
  return request.put(`/cart/items/${id}`, { quantity })
}

export function removeCartItem(id) {
  return request.delete(`/cart/items/${id}`)
}

export function clearCart() {
  return request.delete('/cart/items')
}
