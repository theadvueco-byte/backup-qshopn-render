package com.synix.uwait_backend.dto.adminanalytics;

public record PaymentModeResponse(
        Long upi,
        Long cod,
        Long card
) {}
