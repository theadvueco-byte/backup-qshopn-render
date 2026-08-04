package com.synix.uwait_backend.dto.admindashboard;

public record OrderSummaryResponse(
        long totalOrders,
        long completed,
        long inProcess,
        long cancelled
) {}

