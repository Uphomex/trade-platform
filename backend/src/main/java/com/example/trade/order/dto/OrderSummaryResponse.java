package com.example.trade.order.dto;

import com.example.trade.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
public class OrderSummaryResponse {
    private String orderNo;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private Instant createdAt;
}
