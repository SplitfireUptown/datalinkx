import { axios } from '@/utils/request'

export function pageQuery (params) {
  return axios({
    url: '/api/job/page',
    method: 'GET',
    params: params
  })
}
export function delObj (jobId) {
  return axios({
    url: `/api/job/delete/${jobId}`,
    method: 'POST'
  })
}
export function exec (jobId) {
  return axios({
    url: `/api/job/exec/${jobId}`,
    method: 'POST'
  })
}
export function addObj (obj) {
  return axios({
    url: '/api/job/create',
    method: 'POST',
    data: obj
  })
}

export function modifyObj (obj) {
  return axios({
    url: '/api/job/modify',
    method: 'POST',
    data: obj
  })
}

export function getObj (id) {
  return axios({
    url: `/api/job/info/${id}`,
    method: 'get'
  })
}
