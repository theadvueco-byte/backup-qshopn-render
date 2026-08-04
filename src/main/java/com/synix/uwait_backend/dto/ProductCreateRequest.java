package com.synix.uwait_backend.dto;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal offerPrice;
    private Integer stock;
    private Long categoryId;
    private Boolean bestSeller;
    private String deliveryDetails;
    private String highlights;
    private String warrantyInfo;
}

