package com.example.brightClean.rest;

import com.example.brightClean.service.StripeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/stripe/webhook")
public class StripeWebhookController {

    private final StripeService stripeService;

    @Autowired
    public StripeWebhookController(StripeService stripeService) {
        this.stripeService = stripeService;
    }

    @PostMapping
    public ResponseEntity<String> handleWebhook(@RequestBody String payload) {
        try {
            // 處理 Webhook Payload
            stripeService.handleStripeWebhook(payload, null);
            return ResponseEntity.ok("Webhook 處理成功");
        } catch (Exception e) {
            // 返回錯誤響應
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook 處理失敗: " + e.getMessage());
        }
    }
}