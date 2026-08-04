package com.synix.uwait_backend.dto.adminshop;

import com.synix.uwait_backend.model.ShopStatus;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdminShopResponse {

    private String publicCode;
    private String shopName;
    private String category;
    private ShopStatus status;
    private boolean active;
    private String city;
    private String pinCode;

    // Owner info
    private String ownerName;
    private String ownerEmail;
    private LocalDateTime createdAt;
    private String whatsappNumber;

    // Subscription info
    private String subscriptionPlan;
    private String subscriptionStatus;
    private LocalDate subscriptionEndDate;
}
