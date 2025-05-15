import { axios } from '@/utils/request'

export function pageQuery (params) {
  return axios({
    url: '/api/job/page',
    method: 'GET',
    params: params
  })
}
export function streamPageQuery (params) {
  return axios({
    url: '/api/stream/job/page',
    method: 'GET',
    params: params
  })
}
export function listQuery () {
  return axios({
    url: '/api/job/list',
    method: 'GET'
  })
}
export function delObj (jobId) {
  return axios({
    url: `/api/job/delete/${jobId}`,
    method: 'POST'
  })
}
export function streamDelObj (jobId) {
  return axios({
    url: `/api/stream/job/delete/${jobId}`,
    method: 'POST'
  })
}
export function exec (jobId) {
  return axios({
    url: `/api/job/exec/${jobId}`,
    method: 'POST'
  })
}

export function streamExec (jobId) {
  return axios({
    url: `/api/stream/job/exec/${jobId}`,
    method: 'POST'
  })
}
export function stop (jobId) {
  return axios({
    url: `/api/job/stop/${jobId}`,
    method: 'POST'
  })
}
export function streamStop (jobId) {
  return axios({
    url: `/api/stream/job/stop/${jobId}`,
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

export function streamAddObj (obj) {
  return axios({
    url: '/api/stream/job/create',
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

export function streamModifyObj (obj) {
  return axios({
    url: '/api/stream/job/modify',
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
