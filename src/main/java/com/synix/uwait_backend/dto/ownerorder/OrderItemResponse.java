package com.synix.uwait_backend.dto.ownerorder;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderItemResponse {
    public OrderItemResponse(Long productId, String productName, BigDecimal unitPrice, Integer quantity, BigDecimal totalPrice) {
        this.productId = productId;
        this.productName = productName;
        this.unitPrice = unitPrice;
        this.quantity = quantity;
        this.totalPrice = totalPrice;
        this.productImageUrl = productImageUrl;
    }

    private Long productId;
    private String productName;

    private BigDecimal unitPrice;
    private Integer quantity;
    private BigDecimal totalPrice;

    private String productImageUrl;


}
