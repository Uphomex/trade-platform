package com.example.trade.order.domain;

/**
 * 订单状态机（简化说明见 docs/02）。
 */
public enum OrderStatus {
    PENDING_PAY,
    PAID,
    ALLOCATING_STOCK,
    SHIPPED,
    COMPLETED,
    CANCELLED,
    REFUNDING,
    REFUNDED
}
