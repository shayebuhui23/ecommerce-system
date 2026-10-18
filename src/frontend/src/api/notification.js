import request from './request'

export function fetchNotifications(params) {
  return request.get('/notification/list', { params })
}

export function fetchUnreadCount() {
  return request.get('/notification/unread-count')
}

export function markNotificationRead(id) {
  return request.put(`/notification/${id}/read`)
}

export function markAllNotificationsRead() {
  return request.put('/notification/read-all')
}
