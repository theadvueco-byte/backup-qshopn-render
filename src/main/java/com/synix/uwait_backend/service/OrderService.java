package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.CreateOrderRequest;
import com.synix.uwait_backend.dto.ProductResponse;
import com.synix.uwait_backend.dto.ownerorder.OwnerOrderDetailsResponse;
import com.synix.uwait_backend.dto.ownerorder.PaymentVerificationResponse;
import com.synix.uwait_backend.model.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface OrderService {

    /* ================= PUBLIC PRODUCTS ================= */
     List<ProductResponse> getPublicProducts(String code, Long categoryId);


     Map<String, Object> getPublicOrderStatus(Long orderId, String email);


    /* ================ PLACE ORDER ================= */
     Order placeOrder(String publicCode, CreateOrderRequest order, PaymentMode paymentMode) throws IOException;

    /* ================= UPI LINK ================= */
    String generateUpiLink(Long orderId);

    /* ================= CONFIRM PAYMENT ================= */
    void confirmPayment(Long orderId, MultipartFile screenshot) throws IOException;

    void verifyPaymentByOwner(Long orderId) throws IOException;

    void markOutForDelivery(Long orderId) throws IOException;

    void completeOrder(Long orderId) throws IOException;

    void cancelOrderByOwner(Long orderId, String reason) throws IOException;

    void cancelOrderByCustomer(
            Long orderId,
            String email,
            String reason
    ) throws IOException;

    /* ================= REQUEST ORDER ================= */
    void raiseOrderRequest(
            Long orderId,
            String email,
            RequestType type,
            String message
    );

    void approveRefund(Long requestId, String ownerResponse) throws IOException;

    void processRequest(Long requestId, boolean approve, String responseMessage) throws IOException;

    PaymentVerificationResponse getPaymentVerificationDetails(Long id, String name);

    OwnerOrderDetailsResponse getOwnerOrderDetails(Long orderId, String name);
}
