package com.example.trade.payment.controller;

import com.example.trade.common.api.ApiResponse;
import com.example.trade.payment.dto.PaymentCallbackRequest;
import com.example.trade.payment.service.PaymentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/callback")
    public ApiResponse<Void> callback(@Valid @RequestBody PaymentCallbackRequest req) {
        paymentService.handlePayCallback(req);
        return ApiResponse.ok();
    }
}
