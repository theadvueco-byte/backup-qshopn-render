package com.synix.uwait_backend.dto.adminshop;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminOrderListResponse(
        Long orderId,

        // Shop + Owner
        String shopCode,
        String shopName,
        String ownerName,
        String ownerPhone,

        // Customer
        String customerName,
        String customerPhone,

        // Order
        BigDecimal totalAmount,
        String paymentMode,
        String status,

        LocalDateTime createdAt,
        LocalDateTime completedAt
) {}

