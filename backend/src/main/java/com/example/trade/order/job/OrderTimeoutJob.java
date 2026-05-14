package com.example.trade.order.job;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.trade.order.domain.OrderStatus;
import com.example.trade.order.entity.TradeOrder;
import com.example.trade.order.mapper.TradeOrderMapper;
import com.example.trade.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderTimeoutJob {

    private final TradeOrderMapper orderMapper;
    private final OrderService orderService;

    @Value("${trade.order.pending-pay-timeout-minutes:15}")
    private long pendingPayTimeoutMinutes;

    @Scheduled(fixedDelayString = "${trade.order.timeout-scan-ms:60000}")
    public void cancelStalePendingPay() {
        Instant deadline = Instant.now().minus(pendingPayTimeoutMinutes, ChronoUnit.MINUTES);
        List<TradeOrder> stale = orderMapper.selectList(Wrappers.<TradeOrder>lambdaQuery()
                .eq(TradeOrder::getStatus, OrderStatus.PENDING_PAY)
                .lt(TradeOrder::getCreatedAt, deadline)
                .last("LIMIT 200"));
        for (TradeOrder o : stale) {
            try {
                orderService.cancelTimeout(o);
            } catch (Exception e) {
                log.warn("cancel timeout failed orderNo={}", o.getOrderNo(), e);
            }
        }
    }
}
