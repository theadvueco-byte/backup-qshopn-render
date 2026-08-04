package com.synix.uwait_backend.dto.adminanalytics;

public record RevenueByPlanResponse(
        PlanRevenue free,
        PlanRevenue retail,
        PlanRevenue growth,
        PlanRevenue business,
        Long totalRevenue
) {}



