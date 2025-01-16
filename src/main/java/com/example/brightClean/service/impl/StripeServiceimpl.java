package com.example.brightClean.service.impl;

import com.example.brightClean.domain.Order;
import com.example.brightClean.domain.OrderItem;
import com.example.brightClean.service.CartService;
import com.example.brightClean.service.OrderService;
import com.example.brightClean.service.StripeService;
import com.stripe.Stripe;
import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.checkout.Session;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import com.stripe.param.checkout.SessionCreateParams;

import io.github.cdimascio.dotenv.Dotenv;

import org.hibernate.property.access.spi.SetterMethodImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class StripeServiceimpl implements StripeService {

    // 加載 .env 文件
    private static final Dotenv dotenv = Dotenv.load();
    // Stripe Webhook 秘鑰 90 天
    // 從 .env 文件中讀取密鑰
    private static final String STRIPE_ENDPOINT_SECRET = dotenv.get("STRIPE_ENDPOINT_SECRET");
    private static final String STRIPE_API_KEY = dotenv.get("STRIPE_API_KEY");
    private final OrderService orderService;
    private CartService cartService;
    static {
        Stripe.apiKey = STRIPE_API_KEY; // Stripe
                                        // 測試金鑰
    }

    public StripeServiceimpl(OrderService orderService, CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    // 1. 創建 Stripe Checkout Session
    @Override
    public String createStripeSession(Order order) throws Exception {
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("訂單中沒有商品，無法創建 Checkout Session");
        }
        // 添加 metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("orderId", order.getId().toString());

        SessionCreateParams.Builder paramsBuilder = SessionCreateParams.builder()
                // 付款模式為 PAYMENT 代表一次性支付
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:8080/checkout/success?orderId=" + order.getId())
                .setCancelUrl("http://localhost:8080/checkout/cancel?orderId=" + order.getId())
                .putAllMetadata(metadata);
        // 構建每個商品的支付參數
        for (OrderItem item : order.getOrderItems()) {
            // 為每個商品添加支付參數。
            paramsBuilder.addLineItem(
                    SessionCreateParams.LineItem.builder()
                            // 設置商品的購買數量，轉換為 long 類型。
                            .setQuantity((long) item.getQuantity())
                            // 設置商品的價格資訊
                            .setPriceData(
                                    SessionCreateParams.LineItem.PriceData.builder()
                                            // 指定幣種(美元)
                                            .setCurrency("usd")
                                            // Stripe 的金額單位是 分，所以需要將商品單價乘以 100。
                                            .setUnitAmount((long) item.getPrice() * 100)
                                            // 定義商品資訊
                                            .setProductData(
                                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                                            .setName(item.getProductName())
                                                            .build())
                                            .build())
                            // 建立每個商品的 LineItem 並將其添加到 Session。
                            .build());
        }

        // 創建 Stripe Checkout Session
        Session session = Session.create(paramsBuilder.build());

        // 返回支付頁面的 URL
        return session.getUrl();
    }

    @Override
    public void handleStripeWebhook(String payload, String stripeSignature) throws Exception {
        try {
            // 驗證 Webhook 請求
            Event event = Event.GSON.fromJson(payload, Event.class);
            System.out.println("Session Data: " + event);
            switch (event.getType()) {
                case "checkout.session.completed":
                    System.out.println("未處理的事件類型: " + event.getType());
                    handleCheckoutSessionCompleted(event);
                    break;

                case "payment_intent.payment_failed":
                    handlePaymentFailed(event);
                    break;

                default:
                    System.out.println("未處理的事件類型: " + event.getType());
                    break;
            }
        } catch (SignatureVerificationException e) {
            throw new RuntimeException("Webhook 簽名驗證失敗: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("處理 Webhook 發生錯誤: " + e.getMessage());
        }
    }

    private void handleCheckoutSessionCompleted(Event event) throws Exception {
        // 解析事件數據
        Session session = (Session) event.getDataObjectDeserializer().getObject().orElseThrow();

        Long orderId = Long.valueOf(session.getMetadata().get("orderId")); // 獲取 metadata 中的 orderId
        System.out.println("Session Data: " + session);
        // 獲取訂單
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("訂單不存在"));

        // 更新訂單狀態
        orderService.updateOrderStatus(orderId, "paid");
        System.out.println("訂單 " + orderId + " 已支付成功");

        // 清空購物車
        if (order.getUserId() != null) {
            cartService.clearCart(order.getUserId());
            System.out.println("用戶 " + order.getUserId() + " 的購物車已清空");
        }
    }

    private void handlePaymentFailed(Event event) {
        // 處理支付失敗事件
        System.out.println("支付失敗: " + event.getId());
    }
}