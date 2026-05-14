<script setup lang="ts">
import { useRouter } from 'vue-router'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cart = useCartStore()
</script>

<template>
  <h2>购物车</h2>
  <el-table v-if="cart.lines.length" :data="cart.lines" border>
    <el-table-column prop="productName" label="商品" />
    <el-table-column prop="unitPrice" label="单价" />
    <el-table-column prop="quantity" label="数量" />
    <el-table-column label="小计">
      <template #default="{ row }">
        {{ (row.unitPrice * row.quantity).toFixed(2) }}
      </template>
    </el-table-column>
  </el-table>
  <el-empty v-else description="购物车为空" />
  <div style="margin-top: 16px">
    <el-button type="primary" :disabled="!cart.lines.length" @click="router.push('/checkout')">去结算</el-button>
    <el-button @click="router.push('/')">继续购物</el-button>
  </div>
</template>
