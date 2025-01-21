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

export function deleteMenu (menuId) {
  return request({
    url: '/system/menu/delete', // 在这里插入 menuId
    method: 'DELETE',
    params: { menuId }
  })
}
