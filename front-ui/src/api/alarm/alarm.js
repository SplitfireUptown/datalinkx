import { axios } from '@/utils/request'

export function listQuery () {
  return axios({
    url: '/api/alarm/component/list',
    method: 'GET'
  })
}
export function addObj (obj) {
  return axios({
    url: '/api/alarm/component/create',
    method: 'POST',
    data: obj
  })
}
export function putObj (obj) {
  return axios({
    url: '/api/alarm/component/modify',
    method: 'POST',
    data: obj
  })
}
export function getObj (id) {
  return axios({
    url: `/api/alarm/component/info/${id}`,
    method: 'get'
  })
}
export function delObj (id) {
  return axios({
    url: `/api/alarm/component/delete/${id}`,
    method: 'POST'
  })
}
export function listRuleQuery () {
  return axios({
    url: '/api/alarm/rule/list',
    method: 'GET'
  })
}
export function shutdownObj (id) {
  return axios({
    url: `/api/alarm/rule/shutdown/${id}`,
    method: 'POST'
  })
}
export function delRuleObj (id) {
  return axios({
    url: `/api/alarm/rule/delete/${id}`,
    method: 'POST'
  })
}
