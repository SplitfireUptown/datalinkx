import { axios } from '@/utils/request'

export function httpGo (params) {
  return axios({
    url: '/monitor/http/test',
    method: 'GET',
    params: params
  })
}
