package com.synix.uwait_backend.dto.admindashboard;

public record ShopSummaryResponse(
        long totalShops,
        long subscriptionShops,
        long freeShops,
        long verifiedShops,
        long activeShops,
        long inactiveShops
) {}

