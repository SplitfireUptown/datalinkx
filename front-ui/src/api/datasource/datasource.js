import { axios } from '@/utils/request'

export function pageQuery (params) {
  return axios({
    url: '/api/ds/page',
    method: 'GET',
    params: params
  })
}
export function listQuery () {
  return axios({
    url: '/api/ds/list',
    method: 'GET'
  })
}

export function getObj (id) {
  return axios({
    url: `/api/ds/info/${id}`,
    method: 'get'
  })
}

export function putObj (obj) {
  return axios({
    url: '/api/ds/modify',
    method: 'POST',
    data: obj
  })
}
export function addObj (obj) {
  return axios({
    url: '/api/ds/create',
    method: 'POST',
    data: obj
  })
}
export function fetchTables (id) {
  return axios({
    url: `/api/ds/tables/${id}`,
    method: 'get'
  })
}
export function delObj (id) {
  return axios({
    url: `/api/ds/delete/${id}`,
    method: 'POST'
  })
}
export function getDsTbFieldsInfo (params) {
  return axios({
    url: '/api/ds/field/info',
    method: 'GET',
    params: params
  })
}
export function getDsGroup () {
  return axios({
    url: '/api/ds/group',
    method: 'GET'
  })
}
