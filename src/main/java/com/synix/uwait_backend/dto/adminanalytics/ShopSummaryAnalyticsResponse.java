package com.synix.uwait_backend.dto.adminanalytics;

import java.math.BigDecimal;

public record ShopSummaryAnalyticsResponse(
        BigDecimal totalOrders,
        BigDecimal completedOrders,
        BigDecimal cancelledOrders,
        BigDecimal refundedOrders,
        BigDecimal totalRevenue,
        BigDecimal upiOrders,
        BigDecimal codOrders
) {}

