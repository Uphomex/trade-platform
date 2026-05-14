package com.example.trade.mq;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShipNotifyMessage implements Serializable {
    private String orderNo;
    private String trackingNo;
}
