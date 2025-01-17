import request from '@/utils/request'

export function getMenuList () {
  return request({
    url: '/system/menu/list',
    method: 'GET'
  })
}

export function updateMenu (data) {
  return request({
    url: '/system/menu/update',
    method: 'POST',
    data
  })
}
