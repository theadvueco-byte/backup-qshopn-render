package com.synix.uwait_backend.dto.adminshop;


import java.time.LocalDate;

public record AdminSubscriptionLimitResponse(
        Usage monthlyOrders,
        Usage overallOrders,
        Usage products,
        Usage categories

) {
    public record Usage(long used, long limit) {}
}
