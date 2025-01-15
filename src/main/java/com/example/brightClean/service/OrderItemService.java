package com.example.brightClean.service;

import com.example.brightClean.domain.OrderItem;

import java.util.List;

public interface OrderItemService {

    // 新增訂單商品
    OrderItem createOrderItem(OrderItem orderItem);

    // 根據訂單 ID 查詢訂單商品
    List<OrderItem> getOrderItemsByOrderId(Long orderId);

    // 刪除訂單商品
    void deleteOrderItem(Long orderItemId);
}
