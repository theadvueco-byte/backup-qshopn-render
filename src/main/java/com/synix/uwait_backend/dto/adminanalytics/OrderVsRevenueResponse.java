package com.synix.uwait_backend.dto.adminanalytics;

import java.time.LocalDate;

public record OrderVsRevenueResponse(
        LocalDate date,
        long completedOrders,
        long revenue
) {}

