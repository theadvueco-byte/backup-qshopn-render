package com.synix.uwait_backend.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CancelOrderRequest {
    private String email;
    private String reason;
}

