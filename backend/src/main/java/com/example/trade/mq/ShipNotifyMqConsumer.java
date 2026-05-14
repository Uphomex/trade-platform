package com.example.trade.mq;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.spring.annotation.RocketMQMessageListener;
import org.apache.rocketmq.spring.core.RocketMQListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RocketMQMessageListener(
        topic = TradeMq.TOPIC_SHIP_NOTIFY,
        consumerGroup = "trade-consumer-ship-notify"
)
public class ShipNotifyMqConsumer implements RocketMQListener<ShipNotifyMessage> {

    @Override
    public void onMessage(ShipNotifyMessage message) {
        log.info("consume SHIP_NOTIFY orderNo={} trackingNo={}", message.getOrderNo(), message.getTrackingNo());
    }
}
