package com.synix.uwait_backend.dto.admindashboard;

import java.time.LocalDate;

public record ExpiringSubscriptionResponse(
        String shopCode,
        String shopName,
        LocalDate expiryDate
) {}

