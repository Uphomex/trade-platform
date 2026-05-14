package com.example.trade.fulfillment.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.trade.common.error.BizException;
import com.example.trade.fulfillment.entity.TradeShipment;
import com.example.trade.fulfillment.mapper.TradeShipmentMapper;
import com.example.trade.order.domain.OrderStatus;
import com.example.trade.order.entity.TradeOrder;
import com.example.trade.order.mapper.TradeOrderMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 物流侧 mock：生成运单号并记录，真实系统会对接承运商 API。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FulfillmentService {

    private final TradeShipmentMapper shipmentMapper;
    private final TradeOrderMapper orderMapper;

    @Transactional(rollbackFor = Exception.class)
    public TradeShipment mockShip(String orderNo) {
        TradeOrder order = orderMapper.selectOne(Wrappers.<TradeOrder>lambdaQuery().eq(TradeOrder::getOrderNo, orderNo));
        if (order == null) {
            throw BizException.of("订单不存在");
        }
        if (order.getStatus() != OrderStatus.ALLOCATING_STOCK && order.getStatus() != OrderStatus.PAID) {
            throw BizException.of("当前状态不可发货: " + order.getStatus());
        }
        TradeShipment existing = shipmentMapper.selectOne(
                Wrappers.<TradeShipment>lambdaQuery().eq(TradeShipment::getOrderNo, orderNo).last("LIMIT 1"));
        if (existing != null) {
            log.info("shipment already exists orderNo={}", orderNo);
            return existing;
        }
        TradeShipment s = new TradeShipment();
        s.setOrderNo(orderNo);
        s.setCarrier("MOCK_LOGISTICS");
        s.setTrackingNo("MOCK" + UUID.randomUUID().toString().replace("-", "").substring(0, 16).toUpperCase());
        s.setStatus("CREATED");
        shipmentMapper.insert(s);
        log.info("mock shipment created orderNo={} tracking={}", orderNo, s.getTrackingNo());
        return s;
    }
}
