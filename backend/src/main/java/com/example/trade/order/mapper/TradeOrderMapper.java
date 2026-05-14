package com.example.trade.order.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.trade.order.entity.TradeOrder;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface TradeOrderMapper extends BaseMapper<TradeOrder> {

    @Update("UPDATE trade_order SET status = #{to}, updated_at = NOW(3) WHERE id = #{id} AND status = #{from}")
    int casUpdateStatus(@Param("id") Long id,
                        @Param("from") String from,
                        @Param("to") String to);
}
