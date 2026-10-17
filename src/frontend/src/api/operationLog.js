import request from './request'

export function fetchOperationLogs(params) {
  return request.get('/operation-log/page', { params })
}
