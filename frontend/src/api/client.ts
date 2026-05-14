import axios from 'axios'

export const api = axios.create({
  baseURL: '/api',
  timeout: 30000,
})

api.interceptors.response.use(
  (res) => {
    const body = res.data
    if (body && typeof body.code === 'number' && body.code !== 0) {
      return Promise.reject(new Error(body.message || '业务错误'))
    }
    return res
  },
  (err) => {
    const msg =
      err.response?.data?.message ||
      (typeof err.response?.data === 'string' ? err.response.data : null) ||
      err.message
    return Promise.reject(new Error(msg || '网络错误'))
  }
)

export type ApiBody<T> = { code: number; message: string; data: T }
