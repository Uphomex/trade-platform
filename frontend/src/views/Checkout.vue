<script setup lang="ts">
import { computed, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { createOrder } from '@/api/trade'
import { useCartStore } from '@/stores/cart'

const router = useRouter()
const cart = useCartStore()
const userId = ref(1)
const remark = ref('')
const submitting = ref(false)

const total = computed(() =>
  cart.lines.reduce((s, l) => s + l.unitPrice * l.quantity, 0).toFixed(2)
)

async function submit() {
  if (!cart.lines.length) {
    ElMessage.warning('购物车为空')
    return
  }
  submitting.value = true
  try {
    const order = await createOrder(
      userId.value,
      cart.lines.map((l) => ({ skuId: l.skuId, quantity: l.quantity })),
      remark.value
    )
    cart.clear()
    ElMessage.success('下单成功')
    await router.push(`/orders/${order.orderNo}`)
  } catch (e: unknown) {
    ElMessage.error((e as Error).message || '下单失败')
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <h2>结算</h2>
  <el-form label-width="100px">
    <el-form-item label="演示用户 ID">
      <el-input-number v-model="userId" :min="1" />
    </el-form-item>
    <el-form-item label="备注">
      <el-input v-model="remark" />
    </el-form-item>
  </el-form>
  <el-table :data="cart.lines" border>
    <el-table-column prop="productName" label="商品" />
    <el-table-column prop="unitPrice" label="单价" />
    <el-table-column prop="quantity" label="数量" />
  </el-table>
  <p style="margin: 16px 0">合计：￥{{ total }}</p>
  <el-button type="primary" :loading="submitting" @click="submit">提交订单</el-button>
</template>
