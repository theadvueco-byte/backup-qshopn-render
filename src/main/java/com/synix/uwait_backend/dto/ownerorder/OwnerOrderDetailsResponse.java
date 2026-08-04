package com.synix.uwait_backend.dto.ownerorder;

import com.synix.uwait_backend.model.OrderStatus;
import com.synix.uwait_backend.model.PaymentMode;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OwnerOrderDetailsResponse {
    private Long orderId;

    private String customerName;
    private String customerPhone;
    private String customerEmail;
    private String deliveryAddress;

    private PaymentMode paymentMode;
    private OrderStatus status;

    private BigDecimal subTotal;
    private BigDecimal orderDiscount;
    private BigDecimal deliveryCharge;
    private BigDecimal totalAmount;


    private String cancelledBy;
    private String cancelReason;

    private String paymentScreenshotUrl;

    private LocalDateTime createdAt;

    private List<OrderItemResponse> items;

    public OwnerOrderDetailsResponse(
            Long orderId,
            String customerName,
            String customerPhone,
            String customerEmail,
            String deliveryAddress,
            PaymentMode paymentMode,
            OrderStatus status,
            BigDecimal subTotal,
            BigDecimal orderDiscount,
            BigDecimal deliveryCharge,
            BigDecimal totalAmount,
            String paymentScreenshotUrl, // <-- add this
            String cancelledBy,
            String cancelReason,
            LocalDateTime createdAt,
            List<OrderItemResponse> items
    ) {
        this.orderId = orderId;
        this.customerName = customerName;
        this.customerPhone = customerPhone;
        this.customerEmail = customerEmail;
        this.deliveryAddress = deliveryAddress;
        this.paymentMode = paymentMode;
        this.status = status;
        this.subTotal = subTotal;
        this.orderDiscount = orderDiscount;
        this.deliveryCharge = deliveryCharge;
        this.totalAmount = totalAmount;
        this.paymentScreenshotUrl = paymentScreenshotUrl; // now correct
        this.cancelledBy = cancelledBy;
        this.cancelReason = cancelReason;
        this.createdAt = createdAt;
        this.items = items;
    }

}
