package com.synix.uwait_backend.dto.ownerstatus;

import java.math.BigDecimal;

public record SalesStatsResponse(
        BigDecimal totalSales,
        BigDecimal today,
        BigDecimal yesterday,
        BigDecimal thisMonth,
        BigDecimal lastMonth
) {}