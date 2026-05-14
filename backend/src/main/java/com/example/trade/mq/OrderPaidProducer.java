package com.example.trade.mq;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderPaidProducer {

    private final RocketMQTemplate rocketMQTemplate;

    public void sendOrderPaid(String orderNo, BigDecimal amount) {
        OrderPaidMessage msg = new OrderPaidMessage(orderNo, amount, System.currentTimeMillis());
        try {
            rocketMQTemplate.syncSend(TradeMq.TOPIC_ORDER_PAID, MessageBuilder.withPayload(msg).build());
            log.info("sent ORDER_PAID mq orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("send ORDER_PAID failed orderNo={}", orderNo, e);
            throw e;
        }
    }

    public void sendShipNotify(String orderNo, String trackingNo) {
        try {
            rocketMQTemplate.syncSend(TradeMq.TOPIC_SHIP_NOTIFY,
                    MessageBuilder.withPayload(new ShipNotifyMessage(orderNo, trackingNo)).build());
            log.info("sent SHIP_NOTIFY mq orderNo={}", orderNo);
        } catch (Exception e) {
            log.error("send SHIP_NOTIFY failed orderNo={}", orderNo, e);
        }
    }
}
