package com.synix.uwait_backend.dto.ownerorder;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Setter
@Getter
public class ShopOfferRequest {
    private String offerName;
    private BigDecimal offerPercent;
    private Boolean active;

    // getters & setters
}

