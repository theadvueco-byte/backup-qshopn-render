package com.synix.uwait_backend.dto.adminshop;

import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminSubscriptionOverviewResponse {

    // Current subscription
    private String plan;
    private String status;
    private LocalDate startDate;
    private LocalDate expiryDate;

    // Latest request
    private Double amount;
    private String requestStatus;
    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;
}

