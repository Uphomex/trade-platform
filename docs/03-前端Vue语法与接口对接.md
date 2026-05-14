# 03-前端 Vue 语法与接口对接

## 技术选型

- **Vue 3**：组合式 API（`<script setup>`）为主。  
- **Vite**：开发服务器与构建工具，开发态通过 `vite.config.ts` 把 `/api` 代理到后端 `8080`。  
- **Pinia**：购物车状态（`stores/cart.ts`）。  
- **Vue Router**：页面路由（`src/router/index.ts`）。  
- **Element Plus**：表格、表单、按钮、分页等 UI。  
- **Axios**：HTTP 客户端（`src/api/client.ts`）。

## 组合式 API 速览（读源码时对照）

在 `<script setup>` 中：

- `ref` / `computed`：声明响应式状态与派生值。  
- `onMounted`：页面首次挂载后请求后端列表。  
- `useRouter` / `useRoute`：编程式导航与读取路由参数（如订单号）。

## 与后端约定的响应格式

后端统一返回：

```json
{ "code": 0, "message": "OK", "data": ... }
```

`src/api/client.ts` 中响应拦截器约定：**`code !== 0` 视为业务失败**，抛出 `Error` 供页面 `ElMessage.error` 展示。

## 主要接口映射（`src/api/trade.ts`）

| 前端函数 | HTTP | 说明 |
|----------|------|------|
| `fetchProducts` | `GET /api/products` | 商品 + 默认 SKU + 可售库存 |
| `createOrder` | `POST /api/orders` | 提交购物车行 |
| `getOrder` | `GET /api/orders/{orderNo}` | 订单详情（走后端，可命中 Redis 缓存） |
| `mockPay` | `POST /api/orders/{orderNo}/mock-pay` | 本地一键模拟支付成功 |
| `pageOrders` | `GET /api/orders?page=&size=&status=` | 管理台列表 |
| `cancelOrder` | `POST /api/orders/{orderNo}/cancel?userId=` | 取消待支付订单 |
| `refundOrder` | `POST /api/orders/{orderNo}/refund?userId=` | 演示退款 |

## Element Plus 使用提示

- 表格：`el-table` + `el-table-column`。  
- 表单：`el-form` + `el-form-item` + `el-input` / `el-input-number`。  
- 反馈：`ElMessage.success / error` 做轻提示即可，演示项目不必上全局 Loading 状态机。

## 你可以做的练习

1. 在订单详情页增加 **自动轮询**（`setInterval`）直到状态变为 `COMPLETED`。  
2. 把「演示用户 ID」做成可持久化到 `localStorage` 的小设置页。  
3. 给 `axios` 增加请求 ID 头，便于和后端日志关联排查。
