package com.synix.uwait_backend.dto.ownerearnings;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class StatusCountResponse {
    private String name;
    private int value;
}

