package com.synix.uwait_backend.dto.ownerorder;

import com.synix.uwait_backend.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PaymentVerificationResponse {
    private Long orderId;
    private String paymentMode;
    private String paymentScreenshotUrl;
    private OrderStatus status;
}
