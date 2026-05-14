<script setup lang="ts">
import { RouterView } from 'vue-router'
</script>

<template>
  <el-container class="layout">
    <el-header class="header">
      <div class="brand">Trade Platform 学习演示</div>
      <el-menu mode="horizontal" router :ellipsis="false" class="nav">
        <el-menu-item index="/">商品</el-menu-item>
        <el-menu-item index="/cart">购物车</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
      </el-menu>
    </el-header>
    <el-main>
      <RouterView />
    </el-main>
  </el-container>
</template>

<style scoped>
.layout {
  min-height: 100vh;
}
.header {
  display: flex;
  align-items: center;
  gap: 24px;
  border-bottom: 1px solid #eee;
}
.brand {
  font-weight: 600;
}
.nav {
  flex: 1;
  border-bottom: none;
}
</style>
