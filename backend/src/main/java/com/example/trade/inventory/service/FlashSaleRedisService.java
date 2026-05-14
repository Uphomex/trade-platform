package com.example.trade.inventory.service;

import com.example.trade.common.error.BizException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

/**
 * 演示：Redis + Lua 原子扣减秒杀库存（与 DB 乐观锁方案对照学习）。
 */
@Service
@RequiredArgsConstructor
public class FlashSaleRedisService {

    private final StringRedisTemplate stringRedisTemplate;

    private static final DefaultRedisScript<Long> DECR_SCRIPT = new DefaultRedisScript<>();

    static {
        DECR_SCRIPT.setResultType(Long.class);
        DECR_SCRIPT.setScriptText("""
                local v = redis.call('GET', KEYS[1])
                if v == false then return -1 end
                local n = tonumber(v)
                if n == nil then return -2 end
                if n <= 0 then return 0 end
                redis.call('DECR', KEYS[1])
                return 1
                """);
    }

    public void initStock(String flashKey, long stock) {
        stringRedisTemplate.opsForValue().set(flashKey, String.valueOf(stock));
    }

    /**
     * @return 1 成功扣减；0 已抢光；-1 key 不存在；-2 非法值
     */
    public long tryDecr(String flashKey) {
        List<String> keys = Collections.singletonList(flashKey);
        Long r = stringRedisTemplate.execute(DECR_SCRIPT, keys);
        if (r == null) {
            throw BizException.of("Redis 脚本执行失败");
        }
        return r;
    }

    public String getStock(String flashKey) {
        return stringRedisTemplate.opsForValue().get(flashKey);
    }
}
