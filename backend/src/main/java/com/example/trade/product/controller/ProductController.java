package com.example.trade.product.controller;

import com.example.trade.common.api.ApiResponse;
import com.example.trade.product.service.ProductCatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductCatalogService productCatalogService;

    @GetMapping
    public ApiResponse<?> list() {
        return ApiResponse.ok(productCatalogService.listProducts());
    }
}
