package com.example.brightClean.domain;

import java.time.LocalDateTime;
import jakarta.persistence.*;
import lombok.Data;

@Data
@Entity
@Table(name = "deletedorders")
public class DeletedOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_id", nullable = false)
    private Integer orderId;

    @Column(name = "status", nullable = false)
    private String originalStatus;

    @Column(name = "deleted_at", nullable = false)
    private LocalDateTime deletedAt;
}
