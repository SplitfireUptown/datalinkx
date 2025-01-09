import { axios } from '@/utils/request'
// 更新用户信息
export function updateUserInfo (parameter) {
  return axios({
    url: '/system/user',
    method: 'put',
    data: parameter
  })
}
// 获取用户详细信息
export function getUserInfo () {
  return axios({
    url: '/system/user/',
    method: 'get'
  })
}
// 获取指定用户信息
export function getUserInfoById (userId) {
  return axios({
    url: '/system/user/' + userId,
    method: 'get'
  })
}
