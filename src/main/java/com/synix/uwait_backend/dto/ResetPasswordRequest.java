package com.synix.uwait_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResetPasswordRequest{
    private String email;
    private String newPassword;
}

