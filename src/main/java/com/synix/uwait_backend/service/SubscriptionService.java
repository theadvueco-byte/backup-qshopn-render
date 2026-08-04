package com.synix.uwait_backend.service;


import com.razorpay.RazorpayException;
import com.synix.uwait_backend.dto.BillingHistoryResponse;
import com.synix.uwait_backend.dto.PaymentResponse;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public interface SubscriptionService {

    /* ================= CREATE TRIAL ON SHOP ONBOARD ================= */
    void createTrialSubscription(Shop shop);


    Map<String, Object> createRazorpayOrder(
            Shop shop,
            SubscriptionPlan plan,
            double amount
    ) throws Exception;

    PaymentResponse createSubscriptionPaymentLink(
            Shop shop,
            SubscriptionPlan plan,
            Double amount
    ) throws RazorpayException;


    void verifyAndActivate(
            String ownerEmail,
            String razorpayOrderId,
            String paymentId
    ) throws RazorpayException;


    /* ================= ADMIN OVERRIDE ================= */
    void extendTrial(String shopCode, int days);


    void activate(String shopCode);


   void handleTrialReminders() throws IOException;

   List<BillingHistoryResponse> getBillingHistory(String ownerEmail);

}
