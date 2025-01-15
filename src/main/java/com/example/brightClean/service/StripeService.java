package com.example.brightClean.service;

import com.example.brightClean.domain.Order;

public interface StripeService {
    // 創建 Stripe Checkout Session 並返回支付 URL
    String createStripeSession(Order order) throws Exception;

    // 驗證並處理 Stripe 的 Webhook 回調
    void handleStripeWebhook(String payload, String stripeSignature) throws Exception;
}
