package com.example.brightClean.service.impl;

import com.example.brightClean.domain.DeletedOrder;
import com.example.brightClean.domain.Order;
import com.example.brightClean.domain.OrderItem;
import com.example.brightClean.repository.DeletedOrderRepository;
import com.example.brightClean.repository.OrderItemRepository;
import com.example.brightClean.repository.OrderRepository;
import com.example.brightClean.service.OrderItemService;
import com.example.brightClean.service.OrderService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Service
public class OrderServiceimpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private DeletedOrderRepository deletedOrderRepository;

    @Transactional
    public Order createOrder(Order order) {
        // 設定訂單的默認狀態
        if (order.getStatus() == null) {
            order.setStatus("unpaid");
        }

        // 設定創建時間
        order.setCreatedAt(new java.util.Date());

        // 設置訂單的過期時間（ 15 分鐘後過期）
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(15);
        order.setExpiresAt(expiresAt); // 直接傳遞 LocalDateTime
        // 計算訂單的總價格
        double totalPrice = 0.0;
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                double itemTotal = item.getPrice() * item.getQuantity(); // 單個商品的總價
                totalPrice += itemTotal; // 累加到總價

                // 填充每個 OrderItem 的 orderId
                item.setOrder(order); // 手動設置 OrderItem 與 Order 的關聯
            }
        }
        order.setTotalPrice(totalPrice); // 設置總價格
        // 保存 Order 並生成 ID
        Order savedOrder = orderRepository.save(order);

        // 填充每個 OrderItem 的 orderId
        if (order.getOrderItems() != null) {
            for (OrderItem item : order.getOrderItems()) {
                item.setOrder(savedOrder); // 手動設置 OrderItem 的反向關聯
            }
        }

        return savedOrder;
    }

    @Override
    public void cancelExpiredOrders() {
        List<Order> expiredOrders = orderRepository.findExpiredOrders();
        for (Order order : expiredOrders) {
            order.setStatus("cancelled"); // 設置為已取消
            orderRepository.save(order); // 保存更改
        }
        System.out.println(expiredOrders.size() + " 個過期訂單已取消。");
    }

    @Transactional
    public void deleteCancelledOrders() {
        LocalDateTime oneDayAgo = LocalDateTime.now().minusDays(1);
        List<Order> cancelledOrders = orderRepository.findCancelledOrdersOlderThan(oneDayAgo);

        if (!cancelledOrders.isEmpty()) {
            // 收集所有 DeletedOrder 記錄
            List<DeletedOrder> deletedOrders = new ArrayList<>();
            for (Order order : cancelledOrders) {
                DeletedOrder deletedOrder = new DeletedOrder();
                deletedOrder.setOrderId(order.getId());
                deletedOrder.setOriginalStatus(order.getStatus());
                deletedOrder.setDeletedAt(LocalDateTime.now());
                deletedOrders.add(deletedOrder);
            }

            // 批量保存 DeletedOrder
            deletedOrderRepository.saveAll(deletedOrders);

            try {
                // 刪除過期訂單
                orderRepository.deleteAll(cancelledOrders);
                System.out.println(cancelledOrders.size() + " 個已取消的訂單已刪除。");
            } catch (Exception e) {
                System.err.println("刪除已取消訂單時發生錯誤: " + e.getMessage());
                throw e; // 視需求選擇是否回滾或繼續執行
            }
        } else {
            System.out.println("沒有需要刪除的已取消訂單。");
        }
    }

    @Override
    public List<Order> findAllOrders() {
        return orderRepository.findAll(); // 返回所有訂單
    }

    // 根據 ID 查詢訂單
    @Override
    public Optional<Order> getOrderById(Long orderId) {
        return orderRepository.findById(orderId);
    }

    // 根據用戶 ID 查詢訂單
    @Override
    public List<Order> getOrdersByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Override
    public Order updateOrderStatus(Long orderId, String status) {
        // 查詢訂單，找不到則拋出自定義異常
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found with ID: " + orderId));

        // 更新訂單狀態
        order.setStatus(status);

        // 保存更新後的訂單
        return orderRepository.save(order);
    }

    // 刪除訂單
    @Override
    public void deleteOrder(Long orderId) {
        if (orderRepository.existsById(orderId)) {
            orderRepository.deleteById(orderId);
        } else {
            throw new RuntimeException("Order not found with ID: " + orderId);
        }
    }
}