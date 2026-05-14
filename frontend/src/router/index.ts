import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', component: () => import('@/views/ProductList.vue') },
    { path: '/cart', component: () => import('@/views/Cart.vue') },
    { path: '/checkout', component: () => import('@/views/Checkout.vue') },
    { path: '/orders/:orderNo', component: () => import('@/views/OrderDetail.vue') },
    { path: '/admin/orders', component: () => import('@/views/AdminOrders.vue') },
  ],
})

export default router
