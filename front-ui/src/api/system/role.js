import request from '@/utils/request'

export function getRoleList (parameter) {
  return request({
    url: '/system/role/list',
    method: 'GET',
    params: parameter
  })
}

export function updateRole (parameter) {
  return request({
    url: '/system/role/update',
    method: 'PUT',
    data: parameter
  })
}

export function createRole (parameter) {
  return request({
    url: '/system/role/insert',
    method: 'POST',
    data: parameter
  })
}

export function deleteRole (data) {
  return request({
    url: '/system/role/delete',
    method: 'DELETE',
    data
  })
}

export function getAuthUserList (roleId) {
  return request({
    url: '/system/role/authUserList',
    method: 'GET',
    params: { roleId }
  })
}

export function createAuthRoleUserList (parameter) {
  return request({
    url: '/system/role/authUserList',
    method: 'POST',
    data: parameter
  })
}

export function getAuthMenuList (roleId) {
  return request({
    url: '/system/role/authMenuList',
    method: 'GET',
    params: { roleId }
  })
}

export function createAuthRoleMenuList (parameter) {
  return request({
    url: '/system/role/authMenuList',
    method: 'POST',
    data: parameter
  })
}
