package com.example.trade.product.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.trade.inventory.entity.TradeSku;
import com.example.trade.inventory.mapper.TradeSkuMapper;
import com.example.trade.product.dto.ProductListItem;
import com.example.trade.product.entity.TradeProduct;
import com.example.trade.product.mapper.TradeProductMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCatalogService {

    private final TradeProductMapper productMapper;
    private final TradeSkuMapper skuMapper;

    public List<ProductListItem> listProducts() {
        List<TradeProduct> products = productMapper.selectList(Wrappers.<TradeProduct>lambdaQuery().orderByAsc(TradeProduct::getId));
        return products.stream().map(p -> {
            TradeSku sku = skuMapper.selectById(p.getDefaultSkuId());
            int available = sku == null ? 0 : sku.getTotalStock() - sku.getReservedStock();
            return ProductListItem.builder()
                    .productId(p.getId())
                    .name(p.getName())
                    .description(p.getDescription())
                    .price(p.getPrice())
                    .coverUrl(p.getCoverUrl())
                    .skuId(sku != null ? sku.getId() : null)
                    .skuTitle(sku != null ? sku.getTitle() : "")
                    .availableStock(available)
                    .build();
        }).collect(Collectors.toList());
    }
}
