package com.example.trade.order.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.trade.common.error.BizException;
import com.example.trade.fulfillment.entity.TradeShipment;
import com.example.trade.fulfillment.service.FulfillmentService;
import com.example.trade.inventory.entity.TradeSku;
import com.example.trade.inventory.service.InventoryService;
import com.example.trade.mq.OrderPaidMessage;
import com.example.trade.mq.OrderPaidProducer;
import com.example.trade.order.domain.OrderStatus;
import com.example.trade.order.dto.CreateOrderRequest;
import com.example.trade.order.dto.OrderDetailResponse;
import com.example.trade.order.dto.OrderSummaryResponse;
import com.example.trade.order.entity.TradeOrder;
import com.example.trade.order.entity.TradeOrderItem;
import com.example.trade.order.mapper.TradeOrderItemMapper;
import com.example.trade.order.mapper.TradeOrderMapper;
import com.example.trade.payment.entity.TradePaymentRecord;
import com.example.trade.payment.mapper.TradePaymentRecordMapper;
import com.example.trade.product.entity.TradeProduct;
import com.example.trade.product.mapper.TradeProductMapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final TradeOrderMapper orderMapper;
    private final TradeOrderItemMapper orderItemMapper;
    private final TradeProductMapper productMapper;
    private final InventoryService inventoryService;
    private final OrderPaidProducer orderPaidProducer;
    private final FulfillmentService fulfillmentService;
    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;
    private final TradePaymentRecordMapper paymentRecordMapper;

    @Value("${trade.cache.order-detail-ttl-seconds:60}")
    private long orderDetailTtlSeconds;

    private String orderCacheKey(String orderNo) {
        return "order:detail:" + orderNo;
    }

    /**
     * 下单：预占库存 + 写订单。Seata AT 演示：跨多表写参与同一全局事务（见 undo_log）。
     */
    @GlobalTransactional(name = "tx-create-order", rollbackFor = Exception.class)
    @Transactional(rollbackFor = Exception.class)
    public TradeOrder createOrder(CreateOrderRequest req) {
        BigDecimal total = BigDecimal.ZERO;
        for (CreateOrderRequest.Line line : req.getItems()) {
            TradeSku sku = inventoryService.getSku(line.getSkuId());
            if (sku == null) {
                throw BizException.of("SKU 不存在: " + line.getSkuId());
            }
            TradeProduct product = productMapper.selectById(sku.getProductId());
            if (product == null) {
                throw BizException.of("商品不存在");
            }
            BigDecimal lineAmount = product.getPrice().multiply(BigDecimal.valueOf(line.getQuantity()));
            total = total.add(lineAmount);
        }

        String orderNo = newOrderNo();
        TradeOrder order = new TradeOrder();
        order.setOrderNo(orderNo);
        order.setUserId(req.getUserId());
        order.setStatus(OrderStatus.PENDING_PAY);
        order.setTotalAmount(total);
        order.setRemark(req.getRemark() == null ? "" : req.getRemark());
        order.setCreatedAt(Instant.now());
        order.setUpdatedAt(Instant.now());
        orderMapper.insert(order);

        for (CreateOrderRequest.Line line : req.getItems()) {
            inventoryService.reserve(line.getSkuId(), line.getQuantity());
            TradeSku sku = inventoryService.getSku(line.getSkuId());
            TradeProduct product = productMapper.selectById(sku.getProductId());
            TradeOrderItem item = new TradeOrderItem();
            item.setOrderId(order.getId());
            item.setSkuId(line.getSkuId());
            item.setProductName(product.getName());
            item.setQuantity(line.getQuantity());
            item.setUnitPrice(product.getPrice());
            orderItemMapper.insert(item);
        }
        stringRedisTemplate.delete(orderCacheKey(orderNo));
        log.info("order created orderNo={} total={}", orderNo, total);
        return order;
    }

    public OrderDetailResponse getOrderDetail(String orderNo) {
        String key = orderCacheKey(orderNo);
        String cachedJson = stringRedisTemplate.opsForValue().get(key);
        if (cachedJson != null) {
            try {
                return objectMapper.readValue(cachedJson, OrderDetailResponse.class);
            } catch (JsonProcessingException e) {
                log.warn("order cache corrupt, ignore. orderNo={}", orderNo, e);
            }
        }
        TradeOrder order = orderMapper.selectOne(Wrappers.<TradeOrder>lambdaQuery().eq(TradeOrder::getOrderNo, orderNo));
        if (order == null) {
            throw BizException.of("订单不存在");
        }
        List<TradeOrderItem> items = orderItemMapper.selectList(
                Wrappers.<TradeOrderItem>lambdaQuery().eq(TradeOrderItem::getOrderId, order.getId()));
        OrderDetailResponse dto = OrderDetailResponse.builder()
                .orderNo(order.getOrderNo())
                .userId(order.getUserId())
                .status(order.getStatus())
                .totalAmount(order.getTotalAmount())
                .remark(order.getRemark())
                .createdAt(order.getCreatedAt())
                .items(items.stream()
                        .map(i -> OrderDetailResponse.Item.builder()
                                .skuId(i.getSkuId())
                                .productName(i.getProductName())
                                .quantity(i.getQuantity())
                                .unitPrice(i.getUnitPrice())
                                .build())
                        .collect(Collectors.toList()))
                .build();
        try {
            stringRedisTemplate.opsForValue().set(key, objectMapper.writeValueAsString(dto),
                    java.time.Duration.ofSeconds(orderDetailTtlSeconds));
        } catch (JsonProcessingException e) {
            log.warn("order cache write fail orderNo={}", orderNo, e);
        }
        return dto;
    }

    public void evictOrderCache(String orderNo) {
        stringRedisTemplate.delete(orderCacheKey(orderNo));
    }

    public Page<OrderSummaryResponse> pageOrders(long page, long size, OrderStatus status) {
        Page<TradeOrder> p = new Page<>(page, size);
        var q = Wrappers.<TradeOrder>lambdaQuery().orderByDesc(TradeOrder::getCreatedAt);
        if (status != null) {
            q.eq(TradeOrder::getStatus, status);
        }
        orderMapper.selectPage(p, q);
        Page<OrderSummaryResponse> out = new Page<>(p.getCurrent(), p.getSize(), p.getTotal());
        out.setRecords(p.getRecords().stream()
                .map(o -> OrderSummaryResponse.builder()
                        .orderNo(o.getOrderNo())
                        .status(o.getStatus())
                        .totalAmount(o.getTotalAmount())
                        .createdAt(o.getCreatedAt())
                        .build())
                .collect(Collectors.toList()));
        return out;
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelByUser(String orderNo, long userId) {
        TradeOrder order = mustGet(orderNo);
        if (order.getUserId() != userId) {
            throw BizException.of("无权操作该订单");
        }
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw BizException.of("仅待支付订单可取消");
        }
        int rows = casStatus(order.getId(), OrderStatus.PENDING_PAY, OrderStatus.CANCELLED);
        if (rows != 1) {
            throw BizException.of("取消失败，状态已变更");
        }
        releaseLines(order.getId());
        evictOrderCache(orderNo);
    }

    @Transactional(rollbackFor = Exception.class)
    public void cancelTimeout(TradeOrder order) {
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            return;
        }
        int rows = casStatus(order.getId(), OrderStatus.PENDING_PAY, OrderStatus.CANCELLED);
        if (rows == 1) {
            releaseLines(order.getId());
            evictOrderCache(order.getOrderNo());
            log.info("order timeout cancelled orderNo={}", order.getOrderNo());
        }
    }

    private void releaseLines(Long orderId) {
        List<TradeOrderItem> items = orderItemMapper.selectList(
                Wrappers.<TradeOrderItem>lambdaQuery().eq(TradeOrderItem::getOrderId, orderId));
        for (TradeOrderItem item : items) {
            inventoryService.release(item.getSkuId(), item.getQuantity());
        }
    }

    /**
     * 支付成功后由 PaymentService 调用：仅切状态 + 发 MQ（异步扣减确认与发货）。
     */
    @Transactional(rollbackFor = Exception.class)
    public void markPaidAndPublish(String orderNo, BigDecimal paidAmount) {
        TradeOrder order = mustGet(orderNo);
        if (order.getStatus() == OrderStatus.PAID
                || order.getStatus() == OrderStatus.ALLOCATING_STOCK
                || order.getStatus() == OrderStatus.SHIPPED
                || order.getStatus() == OrderStatus.COMPLETED) {
            return;
        }
        if (order.getStatus() != OrderStatus.PENDING_PAY) {
            throw BizException.of("订单状态不可支付: " + order.getStatus());
        }
        if (order.getTotalAmount().compareTo(paidAmount) != 0) {
            throw BizException.of("支付金额与订单不一致");
        }
        int rows = casStatus(order.getId(), OrderStatus.PENDING_PAY, OrderStatus.PAID);
        if (rows != 1) {
            return;
        }
        evictOrderCache(orderNo);
        orderPaidProducer.sendOrderPaid(orderNo, paidAmount);
    }

    @Transactional(rollbackFor = Exception.class)
    public void applyRefund(String orderNo, long userId, String refundNo) {
        if (paymentRecordMapper.selectCount(Wrappers.<TradePaymentRecord>lambdaQuery()
                .eq(TradePaymentRecord::getBizType, "REFUND")
                .eq(TradePaymentRecord::getChannelPayId, refundNo)) > 0) {
            return;
        }
        TradeOrder order = mustGet(orderNo);
        if (order.getUserId() != userId) {
            throw BizException.of("无权操作该订单");
        }
        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw BizException.of("仅已完成订单可发起售后退款（演示规则）");
        }
        int rows = casStatus(order.getId(), OrderStatus.COMPLETED, OrderStatus.REFUNDING);
        if (rows != 1) {
            throw BizException.of("退款申请失败，状态已变更");
        }
        List<TradeOrderItem> items = orderItemMapper.selectList(
                Wrappers.<TradeOrderItem>lambdaQuery().eq(TradeOrderItem::getOrderId, order.getId()));
        for (TradeOrderItem item : items) {
            inventoryService.restoreAfterRefund(item.getSkuId(), item.getQuantity());
        }
        int rows2 = casStatus(order.getId(), OrderStatus.REFUNDING, OrderStatus.REFUNDED);
        if (rows2 != 1) {
            TradeOrder again = orderMapper.selectById(order.getId());
            if (again != null && again.getStatus() == OrderStatus.REFUNDED) {
                return;
            }
            throw BizException.of("退款完成态更新失败");
        }
        TradePaymentRecord pr = new TradePaymentRecord();
        pr.setBizType("REFUND");
        pr.setOrderNo(orderNo);
        pr.setChannelPayId(refundNo);
        pr.setAmount(order.getTotalAmount());
        pr.setStatus("SUCCESS");
        pr.setCreatedAt(Instant.now());
        try {
            paymentRecordMapper.insert(pr);
        } catch (DuplicateKeyException e) {
            log.info("refund duplicate channel id refundNo={}", refundNo);
        }
        evictOrderCache(orderNo);
        log.info("refund done orderNo={} refundNo={}", orderNo, refundNo);
    }

    private TradeOrder mustGet(String orderNo) {
        TradeOrder order = orderMapper.selectOne(Wrappers.<TradeOrder>lambdaQuery().eq(TradeOrder::getOrderNo, orderNo));
        if (order == null) {
            throw BizException.of("订单不存在");
        }
        return order;
    }

    private int casStatus(long orderId, OrderStatus from, OrderStatus to) {
        return orderMapper.update(null, Wrappers.<TradeOrder>lambdaUpdate()
                .set(TradeOrder::getStatus, to)
                .set(TradeOrder::getUpdatedAt, Instant.now())
                .eq(TradeOrder::getId, orderId)
                .eq(TradeOrder::getStatus, from));
    }

    private static String newOrderNo() {
        return "TP" + System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(1000, 9999);
    }

    /** MQ 消费者：确认库存 + mock 发货 + 完结（幂等按状态推进）。 */
    @Transactional(rollbackFor = Exception.class)
    public void onOrderPaidMessage(OrderPaidMessage msg) {
        TradeOrder order = orderMapper.selectOne(Wrappers.<TradeOrder>lambdaQuery()
                .eq(TradeOrder::getOrderNo, msg.getOrderNo()));
        if (order == null) {
            return;
        }
        if (order.getStatus() == OrderStatus.COMPLETED) {
            return;
        }
        if (order.getStatus() == OrderStatus.PAID) {
            int r = casStatus(order.getId(), OrderStatus.PAID, OrderStatus.ALLOCATING_STOCK);
            if (r != 1) {
                return;
            }
            List<TradeOrderItem> items = orderItemMapper.selectList(
                    Wrappers.<TradeOrderItem>lambdaQuery().eq(TradeOrderItem::getOrderId, order.getId()));
            for (TradeOrderItem item : items) {
                inventoryService.confirm(item.getSkuId(), item.getQuantity());
            }
            evictOrderCache(order.getOrderNo());
            order = orderMapper.selectById(order.getId());
        }
        if (order.getStatus() == OrderStatus.ALLOCATING_STOCK) {
            TradeShipment ship = fulfillmentService.mockShip(order.getOrderNo());
            orderPaidProducer.sendShipNotify(order.getOrderNo(), ship.getTrackingNo());
            int r2 = casStatus(order.getId(), OrderStatus.ALLOCATING_STOCK, OrderStatus.SHIPPED);
            if (r2 == 1) {
                evictOrderCache(order.getOrderNo());
            }
            order = orderMapper.selectById(order.getId());
        }
        if (order.getStatus() == OrderStatus.SHIPPED) {
            int r3 = casStatus(order.getId(), OrderStatus.SHIPPED, OrderStatus.COMPLETED);
            if (r3 == 1) {
                evictOrderCache(order.getOrderNo());
                log.info("order flow completed orderNo={}", order.getOrderNo());
            }
        }
    }
}
