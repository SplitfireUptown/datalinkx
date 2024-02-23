import { axios } from '@/utils/request'

export function pageQuery (params) {
  return axios({
    url: '/api/job/relation/page',
    method: 'GET',
    params: params
  })
}

export function delObj (relationId) {
  return axios({
    url: `/api/job/delete_relation/${relationId}`,
    method: 'POST'
  })
}

export function relationInfo (relationId) {
  return axios({
    url: `/api/job/relation_blood/info/${relationId}`,
    method: 'GET'
  })
}

export function addObj (obj) {
  return axios({
    url: '/api/job/relation/create',
    method: 'POST',
    data: obj
  })
}
