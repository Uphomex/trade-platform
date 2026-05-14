import { api, type ApiBody } from './client'

export interface ProductItem {
  productId: number
  name: string
  description: string
  price: number
  coverUrl: string
  skuId: number
  skuTitle: string
  availableStock: number
}

export async function fetchProducts() {
  const { data } = await api.get<ApiBody<ProductItem[]>>('/products')
  return data.data
}

export interface CreateOrderLine {
  skuId: number
  quantity: number
}

export async function createOrder(userId: number, items: CreateOrderLine[], remark?: string) {
  const { data } = await api.post<ApiBody<{ orderNo: string; id: number }>>('/orders', {
    userId,
    items,
    remark: remark ?? '',
  })
  return data.data
}

export interface OrderDetail {
  orderNo: string
  userId: number
  status: string
  totalAmount: number
  remark: string
  createdAt: string
  items: { skuId: number; productName: string; quantity: number; unitPrice: number }[]
}

export async function getOrder(orderNo: string) {
  const { data } = await api.get<ApiBody<OrderDetail>>(`/orders/${orderNo}`)
  return data.data
}

export async function mockPay(orderNo: string) {
  await api.post<ApiBody<null>>(`/orders/${orderNo}/mock-pay`)
}

export async function cancelOrder(orderNo: string, userId: number) {
  await api.post<ApiBody<null>>(`/orders/${orderNo}/cancel`, null, { params: { userId } })
}

export async function refundOrder(orderNo: string, userId: number, refundNo: string) {
  await api.post<ApiBody<null>>(`/orders/${orderNo}/refund`, { refundNo }, { params: { userId } })
}

export interface OrderSummary {
  orderNo: string
  status: string
  totalAmount: number
  createdAt: string
}

export async function pageOrders(page: number, size: number, status?: string) {
  const { data } = await api.get<ApiBody<{ records: OrderSummary[]; total: number; current: number; size: number }>>(
    '/orders',
    { params: { page, size, status } }
  )
  return data.data
}
