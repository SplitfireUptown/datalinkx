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
    method: 'PUT',
    data
  })
}

export function createMenu (data) {
  return request({
    url: '/system/menu/insert',
    method: 'POST',
    data
  })
}

export function deleteMenu (data) {
  return request({
    url: '/system/menu/delete',
    method: 'DELETE',
    data
  })
}
