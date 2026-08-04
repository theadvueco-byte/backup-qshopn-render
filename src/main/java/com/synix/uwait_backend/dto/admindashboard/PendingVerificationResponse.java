package com.synix.uwait_backend.dto.admindashboard;

public record PendingVerificationResponse(
        long newToday,
        long totalPending
) {}

