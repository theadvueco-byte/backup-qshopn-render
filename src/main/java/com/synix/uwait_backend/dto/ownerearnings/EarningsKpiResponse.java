package com.synix.uwait_backend.dto.ownerearnings;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class EarningsKpiResponse {
    private BigDecimal totalEarnings;
    private BigDecimal monthlyEarnings;
    private BigDecimal pendingPayments;
    private long totalOrders;
    private BigDecimal avgOrderValue;
}

