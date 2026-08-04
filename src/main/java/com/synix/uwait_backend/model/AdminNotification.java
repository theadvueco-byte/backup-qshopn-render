package com.synix.uwait_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "admin_notifications")
@Setter
@Getter
public class AdminNotification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String message;

    @Column(nullable = false)
    private boolean read = false;

    private LocalDateTime createdAt = LocalDateTime.now();

    // 🔥 NEW FIELDS
    @Enumerated(EnumType.STRING)
    private NotificationSourceType sourceType;  // SHOP_OWNER, CUSTOMER, SYSTEM

    @ManyToOne
    private Admin admin;   // or User with role ADMIN
}
