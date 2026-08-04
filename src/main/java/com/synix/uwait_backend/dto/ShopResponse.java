package com.synix.uwait_backend.dto;

import com.synix.uwait_backend.model.ShopStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.util.UUID;


@Data
@Builder
public class ShopResponse {

    private String publicCode;
    private String shopLogoUrl;


    private String shopName;
    private String category;

    private String whatsappNumber;
    private String shortBio;
    private String instagramId;


    private ShopStatus status;
    private boolean active;

    private String area;
    private String city;
    private String pincode;
    private String gpsLocation;

    private String subscriptionPlan;   // TRIAL / BASIC / PREMIUM
    private String subscriptionStatus; // ACTIVE / EXPIRED / CANCELLED
    private LocalDate subscriptionEndDate;
}
