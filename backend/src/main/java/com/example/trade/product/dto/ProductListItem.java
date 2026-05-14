package com.example.trade.product.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class ProductListItem {
    private Long productId;
    private String name;
    private String description;
    private BigDecimal price;
    private String coverUrl;
    private Long skuId;
    private String skuTitle;
    private Integer availableStock;
}
