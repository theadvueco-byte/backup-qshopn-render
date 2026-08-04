package com.synix.uwait_backend.dto.ownerstatus;

public record OrderStatsResponse(
        Long totalOrders,
        Long today,
        Long yesterday,
        Long thisMonth,
        Long lastMonth
) {}


