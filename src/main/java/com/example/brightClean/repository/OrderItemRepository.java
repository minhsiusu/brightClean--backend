package com.example.brightClean.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.brightClean.domain.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    // 根據訂單 ID 查詢訂單的所有商品
    List<OrderItem> findByOrderId(Long id);

    // 根據商品 ID 查詢相關的訂單商品
    List<OrderItem> findByProductId(Long productId);
}