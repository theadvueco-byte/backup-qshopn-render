package com.synix.uwait_backend.dto.ownerorder;

import com.synix.uwait_backend.model.OrderStatus;
import com.synix.uwait_backend.model.PaymentMode;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class OwnerOrderSummaryResponse {


    public OwnerOrderSummaryResponse(Long orderId, String customerName, String customerPhone, PaymentMode paymentMode, OrderStatus status, BigDecimal totalAmount, LocalDateTime createdAt) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.paymentMode = paymentMode;
        this.status = status;
        this.totalAmount = totalAmount;
        this.createdAt = createdAt;
    }

    private Long orderId;
    private String customerName;
    private String customerPhone;
    private PaymentMode paymentMode;
    private OrderStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;


}
