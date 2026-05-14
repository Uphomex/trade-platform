package com.example.trade.common.web;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcExtraConfig implements WebMvcConfigurer {

    private final PaymentCallbackRateLimiter paymentCallbackRateLimiter;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(paymentCallbackRateLimiter)
                .addPathPatterns("/api/payments/callback");
    }
}
