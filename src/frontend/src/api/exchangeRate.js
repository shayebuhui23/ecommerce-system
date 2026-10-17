import request from './request'

export function fetchExchangeRates() {
  return request.get('/exchange-rate/list')
}

export function updateExchangeRate(id, rate) {
  return request.put(`/exchange-rate/${id}`, { rate })
}
