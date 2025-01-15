package com.example.brightClean.service.impl;

import com.example.brightClean.domain.OrderItem;
import com.example.brightClean.repository.OrderItemRepository;
import com.example.brightClean.service.OrderItemService;

import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class OrderItemServiceimpl implements OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemServiceimpl(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    // 新增訂單商品
    @Override
    public OrderItem createOrderItem(OrderItem orderItem) {
        // 確保 Order 被關聯
        if (orderItem.getOrder() == null) {
            throw new IllegalArgumentException("Order must be set for OrderItem");
        }
        return orderItemRepository.save(orderItem);
    }

    // 根據訂單 ID 查詢訂單商品
    @Override
    public List<OrderItem> getOrderItemsByOrderId(Long orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    // 刪除訂單商品
    @Override
    public void deleteOrderItem(Long orderItemId) {
        if (orderItemRepository.existsById(orderItemId)) {
            orderItemRepository.deleteById(orderItemId);
        } else {
            throw new RuntimeException("OrderItem not found with ID: " + orderItemId);
        }
    }
}