import { axios } from '@/utils/request'

export function pageQuery (params) {
  return axios({
    url: '/api/job/log/page',
    method: 'GET',
    params: params
  })
}
