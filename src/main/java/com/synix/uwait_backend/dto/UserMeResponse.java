package com.synix.uwait_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Setter
@Getter
public class UserMeResponse {
    private UUID id;
    private String fullName;
    private String email;
    private String role;
    private boolean emailVerified;
    // 🔥 Multiple shops
    private List<ShopSummaryResponse> shops;

}
