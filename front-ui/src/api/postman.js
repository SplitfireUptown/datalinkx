import { axios } from '@/utils/request'

export function httpGo (obj) {
  return axios({
    url: '/api/ds/http/test',
    method: 'POST',
    data: obj
  })
}
