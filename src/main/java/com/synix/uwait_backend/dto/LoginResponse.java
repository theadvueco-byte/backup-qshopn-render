package com.synix.uwait_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LoginResponse {

    private String token;
    private String role;
    private String fullName;
    private boolean onboarding;

    public LoginResponse(String token, String role, String fullName, boolean onboarding) {
        this.token = token;
        this.role = role;
        this.fullName = fullName;
        this.onboarding = onboarding;
    }

}
