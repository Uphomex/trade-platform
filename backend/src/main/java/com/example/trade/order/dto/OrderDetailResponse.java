package com.example.trade.order.dto;

import com.example.trade.order.domain.OrderStatus;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Data
@Builder
public class OrderDetailResponse {
    private String orderNo;
    private Long userId;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private String remark;
    private Instant createdAt;
    private List<Item> items;

    @Data
    @Builder
    public static class Item {
        private Long skuId;
        private String productName;
        private int quantity;
        private BigDecimal unitPrice;
    }
}
