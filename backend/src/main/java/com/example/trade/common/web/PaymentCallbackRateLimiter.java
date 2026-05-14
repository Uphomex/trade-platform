package com.example.trade.common.web;

import com.example.trade.common.error.BizException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

/**
 * 支付回调限流：同一 IP 每分钟最多 60 次（演示）。
 */
@Component
@RequiredArgsConstructor
public class PaymentCallbackRateLimiter implements HandlerInterceptor {

    private final StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String ip = request.getRemoteAddr();
        String key = "rl:paycb:" + ip;
        Long c = stringRedisTemplate.opsForValue().increment(key);
        if (c != null && c == 1L) {
            stringRedisTemplate.expire(key, Duration.ofMinutes(1));
        }
        if (c != null && c > 60) {
            throw BizException.of("请求过于频繁，请稍后再试");
        }
        return true;
    }
}
