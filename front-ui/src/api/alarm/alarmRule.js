import { axios } from '@/utils/request'

export function listQuery () {
  return axios({
    url: '/api/alarm/rule/list',
    method: 'GET'
  })
}
export function addObj (obj) {
  return axios({
    url: '/api/alarm/rule/create',
    method: 'POST',
    data: obj
  })
}
export function putObj (obj) {
  return axios({
    url: '/api/alarm/rule/modify',
    method: 'POST',
    data: obj
  })
}
export function getObj (id) {
  return axios({
    url: `/api/alarm/rule/info/${id}`,
    method: 'get'
  })
}
export function delObj (id) {
  return axios({
    url: `/api/alarm/rule/delete/${id}`,
    method: 'POST'
  })
}
export function shutdownObj (id) {
  return axios({
    url: `/api/alarm/rule/shutdown/${id}`,
    method: 'POST'
  })
}
