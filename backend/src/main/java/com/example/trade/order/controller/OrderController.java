package com.example.trade.order.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.trade.common.api.ApiResponse;
import com.example.trade.order.domain.OrderStatus;
import com.example.trade.order.dto.CreateOrderRequest;
import com.example.trade.order.dto.RefundRequest;
import com.example.trade.order.entity.TradeOrder;
import com.example.trade.order.service.OrderService;
import com.example.trade.payment.dto.PaymentCallbackRequest;
import com.example.trade.payment.service.PaymentService;
import com.example.trade.payment.service.PaymentSignUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final PaymentService paymentService;
    private final PaymentSignUtil paymentSignUtil;

    @PostMapping
    public ApiResponse<TradeOrder> create(@Valid @RequestBody CreateOrderRequest req) {
        return ApiResponse.ok(orderService.createOrder(req));
    }

    @GetMapping("/{orderNo}")
    public ApiResponse<?> detail(@PathVariable String orderNo) {
        return ApiResponse.ok(orderService.getOrderDetail(orderNo));
    }

    @GetMapping
    public ApiResponse<?> page(
            @RequestParam(defaultValue = "1") long page,
            @RequestParam(defaultValue = "10") long size,
            @RequestParam(required = false) OrderStatus status) {
        Page<?> p = orderService.pageOrders(page, size, status);
        return ApiResponse.ok(p);
    }

    @PostMapping("/{orderNo}/cancel")
    public ApiResponse<Void> cancel(@PathVariable String orderNo, @RequestParam long userId) {
        orderService.cancelByUser(orderNo, userId);
        return ApiResponse.ok();
    }

    @PostMapping("/{orderNo}/refund")
    public ApiResponse<Void> refund(
            @PathVariable String orderNo,
            @RequestParam long userId,
            @Valid @RequestBody RefundRequest body) {
        orderService.applyRefund(orderNo, userId, body.getRefundNo());
        return ApiResponse.ok();
    }

    /**
     * 本地演示：模拟支付成功（内部走与网关回调相同的验签逻辑）。
     */
    @PostMapping("/{orderNo}/mock-pay")
    public ApiResponse<Void> mockPay(@PathVariable String orderNo) {
        var detail = orderService.getOrderDetail(orderNo);
        String payId = "MOCK-" + UUID.randomUUID();
        long ts = System.currentTimeMillis();
        String amountStr = detail.getTotalAmount().stripTrailingZeros().toPlainString();
        String sign = paymentSignUtil.sign(orderNo, payId, amountStr, ts);
        PaymentCallbackRequest req = new PaymentCallbackRequest();
        req.setOrderNo(orderNo);
        req.setPayId(payId);
        req.setAmount(detail.getTotalAmount());
        req.setTs(ts);
        req.setSign(sign);
        paymentService.handlePayCallback(req);
        return ApiResponse.ok();
    }
}
