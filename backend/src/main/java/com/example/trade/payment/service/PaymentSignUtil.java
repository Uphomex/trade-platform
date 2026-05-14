package com.example.trade.payment.service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class PaymentSignUtil {

    @Value("${trade.payment.callback-secret:dev-secret-change-me}")
    private String secret;

    public String sign(String orderNo, String payId, String amount, long ts) {
        String payload = orderNo + "|" + payId + "|" + amount + "|" + ts;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] raw = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(raw);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean verify(String orderNo, String payId, String amount, long ts, String signHex) {
        if (signHex == null || signHex.isBlank()) {
            return false;
        }
        String expect = sign(orderNo, payId, amount, ts);
        return constantTimeEquals(expect.toLowerCase(), signHex.trim().toLowerCase());
    }

    private static boolean constantTimeEquals(String a, String b) {
        if (a.length() != b.length()) {
            return false;
        }
        int r = 0;
        for (int i = 0; i < a.length(); i++) {
            r |= a.charAt(i) ^ b.charAt(i);
        }
        return r == 0;
    }
}
