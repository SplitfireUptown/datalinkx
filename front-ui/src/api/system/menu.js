import request from '@/utils/request'

export function getMenuList () {
  return request({
    url: '/system/menu/list',
    method: 'GET'
  })
}
