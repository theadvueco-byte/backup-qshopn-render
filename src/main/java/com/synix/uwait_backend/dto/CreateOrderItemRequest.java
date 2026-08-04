package com.synix.uwait_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class CreateOrderItemRequest {
    private Long productId;
    private String productName;
    private BigDecimal price;
    private int quantity;
}
