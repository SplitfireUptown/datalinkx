import { axios } from '@/utils/request'

export function getSystemMonitor () {
  return axios({
    url: '/monitor/server/info',
    method: 'GET'
  })
}
