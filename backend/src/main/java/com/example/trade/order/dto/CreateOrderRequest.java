package com.example.trade.order.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;

@Data
public class CreateOrderRequest {
    @NotNull
    private Long userId;
    private String remark;
    @NotNull
    private List<Line> items;

    @Data
    public static class Line {
        @NotNull
        private Long skuId;
        @NotNull
        @Min(1)
        private Integer quantity;
    }
}
