package com.synix.uwait_backend.dto.adminanalytics;

import java.time.LocalDate;

public record ShopOrderTrendResponse(
        LocalDate date,
        Long totalOrders,
        Long completedOrders,
        Long cancelledOrders
) {}

