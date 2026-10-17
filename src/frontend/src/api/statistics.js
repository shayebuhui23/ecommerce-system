import request from './request'

export function fetchStatisticsOverview() {
  return request.get('/statistics/overview')
}
