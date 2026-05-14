package com.example.trade.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderPaidMessage implements Serializable {
    private String orderNo;
    private BigDecimal payAmount;
    private long paidAtEpochMs;
}
