import request from './request'

export function fetchUserPage(params) {
  return request.get('/user/page', { params })
}

export function updateUserStatus(data) {
  return request.put('/user/status', data)
}

export function updateProfile(data) {
  return request.put('/user/profile', data)
}
