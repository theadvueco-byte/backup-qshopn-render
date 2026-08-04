package com.synix.uwait_backend.dto.admindashboard;

import java.math.BigDecimal;

public record BestSellerShopResponse(
        String shopCode,
        String shopName,
        long totalOrders,
        BigDecimal totalRevenue
) {}

