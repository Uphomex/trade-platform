# trade-platform

面向学习的「大厂风格」**单体**电商交易系统示例：一个 Spring Boot 进程内用 **包边界** 划分订单、支付、库存、履约；集成 **MySQL + Redis + RocketMQ + Seata AT**（本地 Docker Compose 一键起依赖）。

## 目录结构

```
trade-platform/
├── backend/                 # Spring Boot 3 + Java 17
├── frontend/                # Vue 3 + Vite + Element Plus
├── docker-compose.yml       # MySQL / Redis / RocketMQ / Seata
├── docs/                    # 中文学习文档（从 00 到 06）
└── README.md
```

## 快速运行

### 1. 启动基础设施

在项目根目录 `trade-platform/` 执行：

```powershell
docker compose up -d
```

首次启动 MySQL 会自动执行 `backend/src/main/resources/db/schema.sql` 建表与演示数据。

### 2. 启动后端

```powershell
cd backend
mvn spring-boot:run
```

默认端口 `8080`。需本机已安装 **JDK 17** 与 **Maven**。

若暂未启动 Seata TC，可使用（关闭 Seata 与 MQ 消费者相关能力会受限，仅建议排障）：

```powershell
mvn spring-boot:run -Dspring-boot.run.profiles=no-seata
```

（`application-no-seata.yml` 中 `seata.enabled=false`。若 RocketMQ 仍未就绪，请先 `docker compose up -d`。）

（如仍因 RocketMQ 连接失败，请先确保 `docker compose` 已启动 NameServer/Broker。）

### 3. 启动前端

```powershell
cd frontend
npm install
npm run dev
```

浏览器访问 `http://127.0.0.1:5173` ，通过 Vite 代理调用后端 `/api`。

## 演示流程建议

1. 在「商品」页加入购物车或直接购买 → **结算** 提交订单（状态 `PENDING_PAY`）。
2. 在「订单详情」点击 **模拟支付成功** → 走支付回调验签 + 幂等表；订单变 `PAID` 并发送 **RocketMQ** 消息。
3. MQ 消费者异步：确认库存（`ALLOCATING_STOCK`）→ mock 物流单（`SHIPPED`）→ `COMPLETED`。
4. 在「订单管理」筛选状态；**已完成**订单可点「申请退款」演示售后与库存回滚。

## 关键配置

| 环境变量 / 配置项 | 说明 |
|-------------------|------|
| `spring.datasource.*` | MySQL，默认 `127.0.0.1:3306/trade_db` |
| `spring.data.redis.*` | Redis 缓存、限流、秒杀 Lua 演示 |
| `rocketmq.name-server` | 默认 `127.0.0.1:9876` |
| `seata.service.grouplist.default` | Seata TC 地址，默认 `127.0.0.1:8091` |
| `trade.payment.callback-secret` | 模拟支付回调 HMAC 密钥 |

## 详细文档

见 `docs/` 目录（中文、循序渐进）：

- `00-项目总览与目录说明.md`
- `01-从0到1搭建步骤.md`
- `02-后端Spring与业务逻辑要点.md`
- `03-前端Vue语法与接口对接.md`
- `04-高并发与一致性设计说明.md`
- `05-Git与GitHub从本地到远程.md`
- `06-演进型微服务改造方案.md`

## 说明

- 本项目**不是**微服务多部署单元拆分，而是 **单应用 + 清晰包结构**，便于本地调试与阅读源码。
- 「支付网关」「物流承运商」均为 **可验签的 mock** 与 **mock 运单**，用于串联状态机与消息流。
