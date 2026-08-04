package com.synix.uwait_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {
    public String fullName;
    public String email;
    public String phone;
    public String password;
    public String confirmPassword;
}

