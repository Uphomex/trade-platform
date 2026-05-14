package com.example.trade.payment.service;

import com.example.trade.common.error.BizException;
import com.example.trade.order.service.OrderService;
import com.example.trade.payment.dto.PaymentCallbackRequest;
import com.example.trade.payment.entity.TradePaymentRecord;
import com.example.trade.payment.mapper.TradePaymentRecordMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private static final String BIZ_PAY = "PAY";

    private final PaymentSignUtil paymentSignUtil;
    private final TradePaymentRecordMapper paymentRecordMapper;
    private final OrderService orderService;

    /**
     * 模拟支付网关回调：验签 + 幂等落库 + 推进订单。
     */
    @Transactional(rollbackFor = Exception.class)
    public void handlePayCallback(PaymentCallbackRequest req) {
        String amountStr = req.getAmount().stripTrailingZeros().toPlainString();
        if (!paymentSignUtil.verify(req.getOrderNo(), req.getPayId(), amountStr, req.getTs(), req.getSign())) {
            throw BizException.of("签名校验失败");
        }
        Instant ts = Instant.ofEpochMilli(req.getTs());
        if (Duration.between(ts, Instant.now()).abs().toMinutes() > 30) {
            throw BizException.of("请求已过期");
        }
        if (tryInsertPayRecord(BIZ_PAY, req.getOrderNo(), req.getPayId(), req.getAmount())) {
            orderService.markPaidAndPublish(req.getOrderNo(), req.getAmount());
        } else {
            log.info("duplicate pay callback ignored orderNo={} payId={}", req.getOrderNo(), req.getPayId());
        }
    }

    /**
     * @return true 表示首次插入（应继续业务）；false 表示重复回调（幂等成功）。
     */
    private boolean tryInsertPayRecord(String bizType, String orderNo, String channelId, java.math.BigDecimal amount) {
        TradePaymentRecord r = new TradePaymentRecord();
        r.setBizType(bizType);
        r.setOrderNo(orderNo);
        r.setChannelPayId(channelId);
        r.setAmount(amount);
        r.setStatus("SUCCESS");
        r.setCreatedAt(Instant.now());
        try {
            paymentRecordMapper.insert(r);
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }
}
