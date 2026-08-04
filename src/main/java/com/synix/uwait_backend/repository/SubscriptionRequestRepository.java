package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.SubscriptionRequest;
import com.synix.uwait_backend.model.RequestStatus;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;


import java.util.List;
import java.util.Optional;

public interface SubscriptionRequestRepository extends JpaRepository<SubscriptionRequest, Long> {

    List<SubscriptionRequest> findByStatus(RequestStatus status);

    List<SubscriptionRequest> findByShop(Shop shop);

    Optional<SubscriptionRequest> findTopByShopOrderByRequestedAtDesc(Shop shop);

    /* 🔹 Find by Razorpay order id (used in callback/webhook) */
    Optional<SubscriptionRequest> findByRazorpayOrderId(String razorpayOrderId);

    List<SubscriptionRequest> findByShopOrderByRequestedAtDesc(Shop shop);


    void deleteByShop(Shop shop);

    Optional<SubscriptionRequest> findFirstByShopOrderByRequestedAtDesc(Shop shop);

    long countByStatus(RequestStatus status);

    List<SubscriptionRequest> findAllByOrderByRequestedAtDesc();

    @Query("""
    SELECT sr.requestedPlan, COUNT(sr), COALESCE(SUM(sr.amount), 0)
    FROM SubscriptionRequest sr
    WHERE sr.status = com.synix.uwait_backend.model.RequestStatus.SUCCESS
    GROUP BY sr.requestedPlan
""")
    List<Object[]> getRevenueGroupedByPlan();
}
