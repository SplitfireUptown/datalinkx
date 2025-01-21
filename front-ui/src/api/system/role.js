import request from '@/utils/request'

export function getRoleList (parameter) {
  return request({
    url: '/system/role/list',
    method: 'GET',
    params: parameter
  })
}
