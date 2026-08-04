package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.Order;
import com.synix.uwait_backend.model.OrderRequest;
import com.synix.uwait_backend.model.RequestStatus;
import com.synix.uwait_backend.model.RequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderRequestRepository extends JpaRepository<OrderRequest, Long> {

    /* ================= OWNER ================= */

    // All requests for a shop
    List<OrderRequest> findByOrder_ShopId(UUID shopId);

    // Pending requests only
    List<OrderRequest> findByStatus(RequestStatus status);

    // Pending requests for a shop
    List<OrderRequest> findByOrder_ShopIdAndStatus(UUID shopId, RequestStatus status);

    /* ================= CUSTOMER ================= */

    // Requests raised by a customer for an order
    List<OrderRequest> findByOrderIdAndCustomerEmail(Long orderId, String customerEmail);

    /* ================= FILTERING ================= */

    // By type (REFUND / QUERY / CANCEL_AFTER_PAYMENT)
    List<OrderRequest> findByType(RequestType type);

    // By order
    List<OrderRequest> findByOrder(Order order);
}
