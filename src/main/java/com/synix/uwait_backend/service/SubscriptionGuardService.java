package com.synix.uwait_backend.service;

import com.synix.uwait_backend.model.PaymentMode;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.subscription.Subscription;


public interface SubscriptionGuardService {


    /* ---------- PRODUCT LIMIT CHECK ---------- */
    void checkProductLimit(Shop shop);

    /* ---------- CATEGORY LIMIT CHECK ---------- */
    void checkCategoryLimit(Shop shop);

    /* ---------- PAYMENT FEATURE CHECK ---------- */
    void checkPaymentFeature(Shop shop, boolean cod, boolean upi);

    /* Block all write actions */
     Subscription validateWritable(Shop shop);

    /* Order limit */
     void checkOrderLimit(Subscription sub, long orderCount);

    /* Product limit */
     void checkProductLimit(Subscription sub, long productCount);

    /* Category limit */
     void checkCategoryLimit(Subscription sub, long categoryCount);

    /*  Payment rules */
     void checkPaymentAllowed(Subscription sub, PaymentMode mode);
}

