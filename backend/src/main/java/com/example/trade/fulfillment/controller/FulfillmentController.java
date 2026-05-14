package com.example.trade.fulfillment.controller;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.trade.common.api.ApiResponse;
import com.example.trade.fulfillment.entity.TradeShipment;
import com.example.trade.fulfillment.mapper.TradeShipmentMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fulfillment")
@RequiredArgsConstructor
public class FulfillmentController {

    private final TradeShipmentMapper shipmentMapper;

    @GetMapping("/{orderNo}/shipments")
    public ApiResponse<List<TradeShipment>> list(@PathVariable String orderNo) {
        return ApiResponse.ok(shipmentMapper.selectList(
                Wrappers.<TradeShipment>lambdaQuery().eq(TradeShipment::getOrderNo, orderNo)));
    }
}
