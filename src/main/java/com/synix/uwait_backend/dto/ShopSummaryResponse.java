package com.synix.uwait_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Setter
@Getter
public class ShopSummaryResponse {

    private String id;
    private String name;
    private String status;
}
