import { axios } from '@/utils/request'

export function copilotChat (params) {
  return axios({
    url: '/api/copilot/chat',
    method: 'GET',
    params: params
  })
}
export function copilotStreamChat (params) {
  return axios({
    url: '/api/copilot/stream/chat',
    method: 'GET',
    params: params
  })
}
