import { axios } from '@/utils/request'

export function getSystemMonitor () {
  return axios({
    url: '/api/monitor/server/info',
    method: 'GET'
  })
}
export function assetTotal () {
  return axios({
    url: '/api/monitor/asset/total',
    method: 'GET'
  })
}
export function assetJobStatus () {
  return axios({
    url: '/api/monitor/asset/job/status',
    method: 'GET'
  })
}
export function assetJobGroup () {
  return axios({
    url: '/api/monitor/asset/job/group',
    method: 'GET'
  })
}
