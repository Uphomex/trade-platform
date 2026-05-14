package com.example.trade.inventory.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.trade.inventory.entity.TradeSku;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface TradeSkuMapper extends BaseMapper<TradeSku> {

    @Update("""
            UPDATE trade_sku
            SET reserved_stock = reserved_stock + #{qty}, version = version + 1
            WHERE id = #{skuId}
              AND (total_stock - reserved_stock) >= #{qty}
              AND version = #{version}
            """)
    int tryReserve(@Param("skuId") Long skuId, @Param("qty") int qty, @Param("version") int version);

    @Update("""
            UPDATE trade_sku
            SET reserved_stock = reserved_stock - #{qty}, version = version + 1
            WHERE id = #{skuId} AND reserved_stock >= #{qty} AND version = #{version}
            """)
    int tryRelease(@Param("skuId") Long skuId, @Param("qty") int qty, @Param("version") int version);

    /**
     * 支付成功后确认：扣减总库存并释放预占。
     */
    @Update("""
            UPDATE trade_sku
            SET total_stock = total_stock - #{qty},
                reserved_stock = reserved_stock - #{qty},
                version = version + 1
            WHERE id = #{skuId}
              AND reserved_stock >= #{qty}
              AND version = #{version}
            """)
    int tryConfirm(@Param("skuId") Long skuId, @Param("qty") int qty, @Param("version") int version);

    /**
     * 退款入库：总库存加回（订单已确认扣减过 total 的场景）。
     */
    @Update("""
            UPDATE trade_sku
            SET total_stock = total_stock + #{qty}, version = version + 1
            WHERE id = #{skuId} AND version = #{version}
            """)
    int tryRefundRestore(@Param("skuId") Long skuId, @Param("qty") int qty, @Param("version") int version);
}
