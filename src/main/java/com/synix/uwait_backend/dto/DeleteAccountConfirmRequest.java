package com.synix.uwait_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class DeleteAccountConfirmRequest {
    private String password; // optional
    private String otp;      // optional
}

