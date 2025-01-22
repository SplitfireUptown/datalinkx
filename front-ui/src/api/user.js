import request from '@/utils/request'
// 更新用户信息
export function updateUserInfo (parameter) {
  return request({
    url: '/system/user',
    method: 'put',
    data: parameter
  })
}
// 获取用户详细信息
export function getUserInfo () {
  return request({
    url: '/system/user/',
    method: 'get'
  })
}
// 获取用户详细列表
export function getUserList (parameter) {
  return request({
    url: '/system/user/list',
    method: 'get',
    params: parameter
  })
}
// 获取指定用户信息
export function getUserInfoById (userId) {
  return request({
    url: '/system/user/' + userId,
    method: 'get'
  })
}
// 修改用户密码
export function updateUserPwd (parameter) {
  return request({
    url: '/system/user/resetUserPwd',
    method: 'put',
    data: parameter
  })
}
