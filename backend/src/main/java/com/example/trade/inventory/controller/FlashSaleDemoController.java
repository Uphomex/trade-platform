package com.example.trade.inventory.controller;

import com.example.trade.common.api.ApiResponse;
import com.example.trade.inventory.service.FlashSaleRedisService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/demo/flash")
@RequiredArgsConstructor
public class FlashSaleDemoController {

    private final FlashSaleRedisService flashSaleRedisService;

    @PostMapping("/init")
    public ApiResponse<String> init(@RequestParam String key, @RequestParam long stock) {
        flashSaleRedisService.initStock(key, stock);
        return ApiResponse.ok("OK");
    }

    @PostMapping("/try")
    public ApiResponse<Long> tryDecr(@RequestParam String key) {
        return ApiResponse.ok(flashSaleRedisService.tryDecr(key));
    }

    @GetMapping("/peek")
    public ApiResponse<String> peek(@RequestParam String key) {
        return ApiResponse.ok(flashSaleRedisService.getStock(key));
    }
}
