package com.example.trade.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PaymentCallbackRequest {
    @NotBlank
    private String orderNo;
    @NotBlank
    private String payId;
    @NotNull
    private BigDecimal amount;
    @NotNull
    private Long ts;
    @NotBlank
    private String sign;
}
