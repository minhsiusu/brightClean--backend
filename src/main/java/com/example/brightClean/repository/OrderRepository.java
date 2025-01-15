package com.example.brightClean.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.brightClean.domain.Order;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    // 根據用戶 ID 查詢訂單
    List<Order> findByUserId(Long userId);

    // 根據訂單狀態查詢
    List<Order> findByStatus(String status);

    // 根據用戶 ID 和訂單狀態查詢
    List<Order> findByUserIdAndStatus(Long userId, String status);

    // 查找狀態為 pending 且過期時間早於當前時間的訂單
    @Query("SELECT o FROM Order o WHERE o.status = 'unpaid' AND o.expiresAt < CURRENT_TIMESTAMP")
    List<Order> findExpiredOrders();

    // 查詢過期的訂單
    List<Order> findAllByStatusAndExpiresAtBefore(String status, LocalDateTime dateTime);

    // 查詢狀態為取消且超過一天的訂單
    @Query("SELECT o FROM Order o WHERE o.status = 'cancelled' AND o.expiresAt < :oneDayAgo")
    List<Order> findCancelledOrdersOlderThan(@Param("oneDayAgo") LocalDateTime oneDayAgo);

}
