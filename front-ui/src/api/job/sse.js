import { axios } from '@/utils/request'

export function closeConnect (pageId) {
  return axios({
    url: `/api/sse/connect/closed/${pageId}`,
    method: 'GET'
  })
}
