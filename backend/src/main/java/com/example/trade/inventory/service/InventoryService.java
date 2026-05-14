package com.example.trade.inventory.service;

import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.trade.common.error.BizException;
import com.example.trade.inventory.entity.TradeSku;
import com.example.trade.inventory.mapper.TradeSkuMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final TradeSkuMapper skuMapper;

    /**
     * 预占库存：乐观锁重试，防止超卖。
     */
    @Transactional(rollbackFor = Exception.class)
    public void reserve(Long skuId, int quantity) {
        if (quantity <= 0) {
            throw BizException.of("购买数量非法");
        }
        for (int attempt = 0; attempt < 8; attempt++) {
            TradeSku sku = skuMapper.selectById(skuId);
            if (sku == null) {
                throw BizException.of("SKU 不存在");
            }
            int available = sku.getTotalStock() - sku.getReservedStock();
            if (available < quantity) {
                throw BizException.of("库存不足");
            }
            int rows = skuMapper.tryReserve(skuId, quantity, sku.getVersion());
            if (rows == 1) {
                return;
            }
            log.debug("reserve optimistic retry skuId={} attempt={}", skuId, attempt);
        }
        throw BizException.of("库存繁忙，请重试");
    }

    @Transactional(rollbackFor = Exception.class)
    public void release(Long skuId, int quantity) {
        for (int attempt = 0; attempt < 8; attempt++) {
            TradeSku sku = skuMapper.selectById(skuId);
            if (sku == null) {
                return;
            }
            if (sku.getReservedStock() < quantity) {
                log.warn("release skipped: reserved not enough skuId={} qty={}", skuId, quantity);
                return;
            }
            int rows = skuMapper.tryRelease(skuId, quantity, sku.getVersion());
            if (rows == 1) {
                return;
            }
        }
        throw BizException.of("释放库存失败，请重试");
    }

    @Transactional(rollbackFor = Exception.class)
    public void confirm(Long skuId, int quantity) {
        for (int attempt = 0; attempt < 8; attempt++) {
            TradeSku sku = skuMapper.selectById(skuId);
            if (sku == null) {
                throw BizException.of("SKU 不存在");
            }
            int rows = skuMapper.tryConfirm(skuId, quantity, sku.getVersion());
            if (rows == 1) {
                return;
            }
        }
        throw BizException.of("确认库存失败（可能已处理或预占不足）");
    }

    @Transactional(rollbackFor = Exception.class)
    public void restoreAfterRefund(Long skuId, int quantity) {
        for (int attempt = 0; attempt < 8; attempt++) {
            TradeSku sku = skuMapper.selectById(skuId);
            if (sku == null) {
                return;
            }
            int rows = skuMapper.tryRefundRestore(skuId, quantity, sku.getVersion());
            if (rows == 1) {
                return;
            }
        }
        throw BizException.of("退款入库失败，请重试");
    }

    public TradeSku getSku(Long skuId) {
        return skuMapper.selectById(skuId);
    }

    public java.util.List<TradeSku> listByProductIds(java.util.Collection<Long> productIds) {
        if (productIds.isEmpty()) {
            return java.util.List.of();
        }
        return skuMapper.selectList(Wrappers.<TradeSku>lambdaQuery().in(TradeSku::getProductId, productIds));
    }
}
