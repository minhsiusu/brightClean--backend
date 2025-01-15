package com.example.brightClean.service.impl;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.example.brightClean.service.OrderService;

@Service
public class ScheduledTasks {

    private final OrderService orderService;

    public ScheduledTasks(OrderService orderService) {
        this.orderService = orderService;
    }

    @Scheduled(fixedRate = 60000) // 每 60 秒執行一次
    public void checkForExpiredOrders() {
        System.out.println("檢查過期訂單...");
        orderService.cancelExpiredOrders();
    }

    @Scheduled(cron = "0 0 0 * * ?") // 每天凌晨 0 點執行
    public void scheduleCancelledOrdersCleanup() {
        System.out.println("執行已取消訂單清理任務...");
        orderService.deleteCancelledOrders();
    }
}