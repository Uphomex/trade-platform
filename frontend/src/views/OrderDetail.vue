<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { getOrder, mockPay, cancelOrder, refundOrder, type OrderDetail } from '@/api/trade'

const route = useRoute()
const orderNo = route.params.orderNo as string
const detail = ref<OrderDetail | null>(null)
const userId = 1
const loading = ref(true)
const paying = ref(false)

async function load() {
  loading.value = true
  try {
    detail.value = await getOrder(orderNo)
  } catch (e: unknown) {
    ElMessage.error((e as Error).message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)

async function pay() {
  paying.value = true
  try {
    await mockPay(orderNo)
    ElMessage.success('已触发模拟支付，库存确认与发货由 MQ 异步处理，请稍后刷新')
    setTimeout(load, 1500)
  } catch (e: unknown) {
    ElMessage.error((e as Error).message || '支付失败')
  } finally {
    paying.value = false
  }
}

async function cancel() {
  try {
    await cancelOrder(orderNo, userId)
    ElMessage.success('已取消')
    await load()
  } catch (e: unknown) {
    ElMessage.error((e as Error).message || '取消失败')
  }
}

async function refund() {
  const refundNo = `RF-${Date.now()}`
  try {
    await refundOrder(orderNo, userId, refundNo)
    ElMessage.success('退款已处理（演示：同步退款 + 入库）')
    await load()
  } catch (e: unknown) {
    ElMessage.error((e as Error).message || '退款失败')
  }
}
</script>

<template>
  <div v-loading="loading">
    <h2>订单详情</h2>
    <template v-if="detail">
      <el-descriptions border :column="1">
        <el-descriptions-item label="订单号">{{ detail.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">{{ detail.status }}</el-descriptions-item>
        <el-descriptions-item label="金额">￥{{ detail.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="创建时间">{{ detail.createdAt }}</el-descriptions-item>
      </el-descriptions>
      <h3 style="margin-top: 24px">明细</h3>
      <el-table :data="detail.items" border>
        <el-table-column prop="productName" label="商品" />
        <el-table-column prop="skuId" label="SKU" />
        <el-table-column prop="quantity" label="数量" />
        <el-table-column prop="unitPrice" label="单价" />
      </el-table>
      <div style="margin-top: 16px">
        <el-button v-if="detail.status === 'PENDING_PAY'" type="primary" :loading="paying" @click="pay">
          模拟支付成功
        </el-button>
        <el-button v-if="detail.status === 'PENDING_PAY'" @click="cancel">取消订单</el-button>
        <el-button v-if="detail.status === 'COMPLETED'" type="warning" @click="refund">申请退款（演示）</el-button>
        <el-button @click="load">刷新状态</el-button>
      </div>
    </template>
  </div>
</template>
