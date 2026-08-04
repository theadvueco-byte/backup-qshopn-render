package com.synix.uwait_backend.dto.ownerearnings;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentMethodResponse {
    private String name;
    private int value;
    private         boolean enabled;
}

