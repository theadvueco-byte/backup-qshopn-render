package com.synix.uwait_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ContactRequest {
    private String phone;
    private String message;

    // getters & setters
}

