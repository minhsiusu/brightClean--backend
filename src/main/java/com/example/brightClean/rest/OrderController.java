package com.example.brightClean.rest;

import com.example.brightClean.domain.Order;
import com.example.brightClean.domain.OrderItem;
import com.example.brightClean.service.CartService;
import com.example.brightClean.service.OrderService;
import com.example.brightClean.service.StripeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.Map;

@RestController
@RequestMapping("/orders") // 基本路徑
public class OrderController {

    private final OrderService orderService;
    private final StripeService stripeService;
    @Autowired
    private CartService cartService;

    @Autowired
    public OrderController(OrderService orderService, StripeService stripeService) {
        this.orderService = orderService;
        this.stripeService = stripeService;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Order order) {
        Map<String, Object> response = new HashMap<>();
        try {
            // 驗證支付方式是否提供
            if (order.getPaymentMethod() == null || order.getPaymentMethod().isEmpty()) {
                throw new IllegalArgumentException("支付方式未提供");
            }

            // 創建訂單
            Order createdOrder = orderService.createOrder(order);

            // 判空檢查
            if (createdOrder == null) {
                throw new Exception("訂單創建失敗，返回空數據");
            }

            // 返回成功響應
            response.put("success", true);
            response.put("data", createdOrder);
            response.put("message", "訂單創建成功");
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // 處理非法參數錯誤
            response.put("success", false);
            response.put("error", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);

        } catch (Exception e) {
            // 處理其他未知錯誤
            response.put("success", false);
            response.put("error", "系統錯誤，請稍後再試");
            response.put("details", e.getMessage());
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    // 根據 ID 查詢訂單
    @GetMapping("/{orderId}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long orderId) {
        Optional<Order> order = orderService.getOrderById(orderId);
        return order.map(ResponseEntity::ok)
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    // 根據用戶 ID 查詢訂單
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUserId(@PathVariable Long userId) {
        List<Order> orders = orderService.getOrdersByUserId(userId);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    @PostMapping("/update_status")
    public ResponseEntity<String> updateOrderStatus(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String status = payload.get("status").toString();

        try {
            // 更新訂單狀態
            orderService.updateOrderStatus(orderId, status);

            // 清空購物車
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "訂單不存在"));
            if (order.getUserId() != null) {
                cartService.clearCart(order.getUserId());
            }

            return ResponseEntity.ok("訂單狀態已更新，購物車已清空");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("無法更新訂單狀態");
        }
    }

    // 刪除訂單
    @DeleteMapping("/{orderId}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Long orderId) {
        try {
            orderService.deleteOrder(orderId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/checkout")
    public ResponseEntity<Map<String, String>> createStripeCheckoutSession(@RequestBody Map<String, Object> payload) {

        if (!payload.containsKey("id") || payload.get("id") == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "訂單 ID 未提供");
        }
        Long orderId = Long.valueOf(payload.get("id").toString());

        // 使用 Optional.orElseThrow 正確處理
        Order order = orderService.getOrderById(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "訂單不存在"));
        try {
            // 調用 StripeService 生成 URL
            String sessionUrl = stripeService.createStripeSession(order);

            // 返回 Stripe URL
            Map<String, String> response = new HashMap<>();
            response.put("checkoutUrl", sessionUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            // 記錄錯誤，返回錯誤響應
            e.printStackTrace();
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "無法創建結帳會話");
        }
    }

    @PostMapping("/checkout_success")
    public ResponseEntity<String> handleCheckoutSuccess(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());

        try {
            // 更新訂單狀態
            orderService.updateOrderStatus(orderId, "paid");

            // 清空購物車
            Order order = orderService.getOrderById(orderId)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "訂單不存在"));
            if (order.getUserId() != null) {
                cartService.clearCart(order.getUserId());
            }

            return ResponseEntity.ok("訂單更新成功");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("無法處理結帳成功邏輯");
        }
    }

    @PostMapping("/admin/update_order")
    public ResponseEntity<String> adminUpdateOrder(@RequestBody Map<String, Object> payload) {
        Long orderId = Long.valueOf(payload.get("orderId").toString());
        String status = payload.get("status").toString();

        try {
            orderService.updateOrderStatus(orderId, status);
            return ResponseEntity.ok("訂單狀態已更新");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("更新訂單狀態失敗");
        }
    }
}
