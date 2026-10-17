import request from './request'

export function createReview(data) {
  return request.post('/review', data)
}

export function fetchProductReviews(productId, params) {
  return request.get(`/review/product/${productId}`, { params })
}

export function fetchReviewSummary(productId) {
  return request.get(`/review/product/${productId}/summary`)
}
