package com.synix.uwait_backend.dto.ownerstatus;

import java.time.LocalDate;

public record SubscriptionDashboardResponse(
        String currentPlan,
        Usage orders,
        Usage products,
        Usage categories,
        LocalDate expiryDate,
        String status
) {
    public record Usage(Long used, Integer limit) {}
}

