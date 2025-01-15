package com.example.brightClean.service;

import com.example.brightClean.domain.Order;

import java.util.List;
import java.util.Optional;

public interface OrderService {

    // 創建訂單
    Order createOrder(Order order);

    // 根據 ID 查詢訂單
    Optional<Order> getOrderById(Long orderId);

    // 根據用戶 ID 查詢訂單
    List<Order> getOrdersByUserId(Long userId);

    // 更新訂單狀態
    Order updateOrderStatus(Long orderId, String status);

    // 刪除訂單
    void deleteOrder(Long orderId);

    // 過期超過15分鐘訂單 狀態為cancel
    void cancelExpiredOrders();

    // 清除過期超過1天訂單
    void deleteCancelledOrders();

    List<Order> findAllOrders();
}