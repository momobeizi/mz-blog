import axios from 'axios'
import type { ApiResponse } from '@/types'

export const http = axios.create({
  baseURL: '/api/admin/v1',
  withCredentials: true,
  timeout: 20000,
  xsrfCookieName: 'XSRF-TOKEN',
  xsrfHeaderName: 'X-XSRF-TOKEN',
})

let csrfRequest: Promise<void> | null = null

function readCookie(name: string) {
  const prefix = `${name}=`
  return document.cookie.split('; ').find(item => item.startsWith(prefix))?.slice(prefix.length)
}

async function ensureCsrf() {
  if (readCookie('XSRF-TOKEN')) return
  if (!csrfRequest) {
    csrfRequest = http.get('/auth/csrf')
      .then(() => undefined)
      .finally(() => { csrfRequest = null })
  }
  await csrfRequest
}

http.interceptors.request.use(async config => {
  const method = config.method?.toLowerCase()
  if (method && ['post', 'put', 'patch', 'delete'].includes(method) && config.url !== '/auth/login') {
    await ensureCsrf()
    const token = readCookie('XSRF-TOKEN')
    if (token) config.headers.set('X-XSRF-TOKEN', decodeURIComponent(token))
  }
  return config
})

http.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && !location.pathname.endsWith('/login')) {
      location.replace('/admin/login')
    }
    return Promise.reject(error)
  },
)

export async function api<T>(promise: Promise<{ data: ApiResponse<T> }>): Promise<T> {
  const response = await promise
  if (response.data.code !== 0) {
    throw new Error(response.data.message || '请求失败')
  }
  return response.data.data
}

export function errorMessage(error: unknown): string {
  if (axios.isAxiosError(error)) {
    return (error.response?.data as ApiResponse<unknown> | undefined)?.message || error.message || '请求失败'
  }
  return error instanceof Error ? error.message : '请求失败'
}
