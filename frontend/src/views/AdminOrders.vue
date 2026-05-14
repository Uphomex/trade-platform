<script setup lang="ts">
import { onMounted, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { pageOrders, type OrderSummary } from '@/api/trade'

const router = useRouter()
const page = ref(1)
const size = ref(10)
const status = ref<string | undefined>(undefined)
const total = ref(0)
const records = ref<OrderSummary[]>([])
const loading = ref(false)

async function load() {
  loading.value = true
  try {
    const data = await pageOrders(page.value, size.value, status.value)
    records.value = data.records
    total.value = data.total
  } catch (e: unknown) {
    ElMessage.error((e as Error).message || '加载失败')
  } finally {
    loading.value = false
  }
}

onMounted(load)
watch([page, size, status], load)

function openDetail(row: OrderSummary) {
  router.push(`/orders/${row.orderNo}`)
}
</script>

<template>
  <h2>订单列表（管理台风格）</h2>
  <el-form inline>
    <el-form-item label="状态">
      <el-select v-model="status" clearable placeholder="全部" style="width: 200px">
        <el-option label="待支付" value="PENDING_PAY" />
        <el-option label="已支付" value="PAID" />
        <el-option label="配货中" value="ALLOCATING_STOCK" />
        <el-option label="已发货" value="SHIPPED" />
        <el-option label="已完成" value="COMPLETED" />
        <el-option label="已取消" value="CANCELLED" />
        <el-option label="退款中" value="REFUNDING" />
        <el-option label="已退款" value="REFUNDED" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="load">查询</el-button>
    </el-form-item>
  </el-form>
  <el-table v-loading="loading" :data="records" border @row-dblclick="openDetail">
    <el-table-column prop="orderNo" label="订单号" width="220" />
    <el-table-column prop="status" label="状态" width="160" />
    <el-table-column prop="totalAmount" label="金额" />
    <el-table-column prop="createdAt" label="创建时间" />
    <el-table-column label="操作" width="120">
      <template #default="{ row }">
        <el-button link type="primary" @click="openDetail(row)">详情</el-button>
      </template>
    </el-table-column>
  </el-table>
  <el-pagination
    v-model:current-page="page"
    v-model:page-size="size"
    :total="total"
    layout="total, prev, pager, next"
    style="margin-top: 16px"
  />
</template>
