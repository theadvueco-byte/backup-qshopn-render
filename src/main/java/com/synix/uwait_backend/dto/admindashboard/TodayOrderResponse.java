package com.synix.uwait_backend.dto.admindashboard;

import java.util.List;

public record TodayOrderResponse(
        long totalOrders,
        List<ShopOrderCount> shopWise,
        List<PaymentModeCount> paymentModeWise
) {
    public record ShopOrderCount(String shopCode, long count) {}
    public record PaymentModeCount(String mode, long count) {}
}

