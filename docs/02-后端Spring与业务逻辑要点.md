# 02-后端 Spring 与业务逻辑要点

## 分层与注解（结合本仓库代码）

| 概念 | 在本项目中的体现 |
|------|------------------|
| **启动类** | `TradeApplication`：`@SpringBootApplication` 扫描整个 `com.example.trade` |
| **Web 层** | `*Controller`：`@RestController` + `@RequestMapping("/api/...")` |
| **业务层** | `*Service`：`@Service`，封装用例（下单、支付、退款） |
| **持久层** | MyBatis-Plus：`Mapper` 接口继承 `BaseMapper<T>`，复杂 SQL 写在注解或 XML（本项目以注解为主） |
| **参数校验** | DTO 上使用 `jakarta.validation`（如 `@NotNull`），由 Spring 在进入 Controller 方法前校验 |
| **统一响应** | `ApiResponse<T>` + `GlobalExceptionHandler`：把业务异常转成可读 JSON |

## 订单状态机（与代码对应）

- **创建订单**（`OrderService#createOrder`）：状态 `PENDING_PAY`；同时对 SKU 做 **预占**（`reserved_stock` 增加）。  
- **支付回调**（`PaymentService#handlePayCallback`）：验签通过后，先插入 **支付幂等表**（唯一键防重），再调用 `OrderService#markPaidAndPublish` 将订单置为 `PAID` 并发送 MQ。  
- **异步履约**（`OrderPaidMqConsumer` → `OrderService#onOrderPaidMessage`）：  
  - `PAID` → `ALLOCATING_STOCK`：对每一行明细执行 **确认库存**（总库存与预占同时扣减，表示「成交出库」）。  
  - 调用 `FulfillmentService#mockShip` 写物流表，并把订单推到 `SHIPPED`。  
  - 再推到 `COMPLETED`。  
- **取消 / 超时**（`cancelByUser` / `OrderTimeoutJob`）：仅 `PENDING_PAY` 可关单，并对每一行 **释放预占**。  
- **退款**（`applyRefund`）：演示规则只允许从 `COMPLETED` 发起；先 `REFUNDING` 再 **库存回加**，最后 `REFUNDED`，并写入退款幂等记录。

## RocketMQ 在本项目中的角色

- **Topic `TRADE_ORDER_PAID`**：支付完成后投递，消费者做「重活」——库存确认 + 发货 + 完结，避免支付回调 HTTP 线程被拖慢。  
- **Topic `TRADE_SHIP_NOTIFY`**：在生成 mock 运单后投递一条通知，消费者当前仅打日志，便于你扩展「短信 / 推送 / BI」。

## Seata AT 模式（你要看什么表？）

- 全局事务入口：`OrderService#createOrder` 上的 `@GlobalTransactional`。  
- 参与分支的本地事务包括：插入订单、插入明细、更新 SKU 预占等。  
- MySQL 中关注表 **`undo_log`**：出现记录即表示 Seata 为回滚准备了前镜像与后镜像。

若 TC（`seata-server`）未启动，全局事务会失败——请先 `docker compose up -d`。

## 与「员工管理系统」的区别

本系统聚焦 **交易域**：订单、支付、库存、履约与消息；不包含人事、考勤、权限组织树等典型「员工管理」模块，避免干扰学习主线。
