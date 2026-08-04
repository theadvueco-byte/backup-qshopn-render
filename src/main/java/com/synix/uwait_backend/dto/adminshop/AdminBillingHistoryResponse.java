package com.synix.uwait_backend.dto.adminshop;

import com.synix.uwait_backend.model.RequestStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record AdminBillingHistoryResponse(
        Long id,
        String shopName,
        String ownerEmail,
        String plan,
        Double amount,
        String paymentGateway,
        RequestStatus status,
        LocalDateTime paidAt
) {}

