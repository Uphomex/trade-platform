package com.example.trade.mq;

import com.example.trade.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.ConsumeMode;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
@RocketMQMessageListener(
        topic = TradeMq.TOPIC_ORDER_PAID,
        consumerGroup = "trade-consumer-order-paid",
        consumeMode = ConsumeMode.CONCURRENTLY
)
public class OrderPaidMqConsumer implements RocketMQListener<OrderPaidMessage> {

    private final OrderService orderService;

    @Override
    public void onMessage(OrderPaidMessage message) {
        log.info("consume ORDER_PAID orderNo={}", message.getOrderNo());
        orderService.onOrderPaidMessage(message);
    }
}
