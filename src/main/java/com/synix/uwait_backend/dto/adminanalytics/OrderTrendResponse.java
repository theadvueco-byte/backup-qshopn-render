package com.synix.uwait_backend.dto.adminanalytics;

import java.time.LocalDate;

public record OrderTrendResponse(
        LocalDate date,
        Long totalOrders,
        Long freePlanOrders,
        Long paidPlanOrders
) {}

