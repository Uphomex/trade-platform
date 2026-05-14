package com.example.trade.payment.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@TableName("trade_payment_record")
public class TradePaymentRecord {
    @TableId(type = IdType.AUTO)
    private Long id;
    /** PAY / REFUND */
    private String bizType;
    private String orderNo;
    /** 渠道侧支付单号，幂等键之一 */
    private String channelPayId;
    private BigDecimal amount;
    private String status;
    private Instant createdAt;
}
