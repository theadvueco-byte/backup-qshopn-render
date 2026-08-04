package com.synix.uwait_backend.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class CreateOrderRequest {
    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String deliveryAddress;
    private List<CreateOrderItemRequest> items;
}
