<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { fetchProducts, type ProductItem } from '@/api/trade'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cart = useCartStore()
const products = ref<ProductItem[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    products.value = await fetchProducts()
  } catch (e: unknown) {
    ElMessage.error((e as Error).message || '加载失败')
  } finally {
    loading.value = false
  }
})

function addToCart(p: ProductItem) {
  cart.add({
    skuId: p.skuId,
    productName: p.name,
    unitPrice: p.price,
    quantity: 1,
  })
  ElMessage.success('已加入购物车')
}

function goCheckout(p: ProductItem) {
  cart.clear()
  cart.add({
    skuId: p.skuId,
    productName: p.name,
    unitPrice: p.price,
    quantity: 1,
  })
  router.push('/checkout')
}
</script>

<template>
  <div v-loading="loading">
    <h2>商品列表（演示数据）</h2>
    <el-row :gutter="16">
      <el-col v-for="p in products" :key="p.productId" :span="8" style="margin-bottom: 16px">
        <el-card shadow="hover">
          <img :src="p.coverUrl" style="width: 100%; height: 160px; object-fit: cover" />
          <h3>{{ p.name }}</h3>
          <p style="color: #666; font-size: 13px">{{ p.description }}</p>
          <p>SKU：{{ p.skuTitle }}，可售库存：{{ p.availableStock }}</p>
          <p style="font-size: 18px; font-weight: bold">￥{{ p.price }}</p>
          <el-space>
            <el-button type="primary" @click="addToCart(p)">加入购物车</el-button>
            <el-button @click="goCheckout(p)">直接购买</el-button>
          </el-space>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>
