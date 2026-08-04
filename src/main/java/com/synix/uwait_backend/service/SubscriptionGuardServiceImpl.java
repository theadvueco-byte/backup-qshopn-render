package com.synix.uwait_backend.service;

import com.synix.uwait_backend.model.PaymentMode;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.model.subscription.SubscriptionStatus;
import com.synix.uwait_backend.repository.CategoryRepository;
import com.synix.uwait_backend.repository.ProductRepository;
import com.synix.uwait_backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SubscriptionGuardServiceImpl implements SubscriptionGuardService{

    private final SubscriptionRepository subscriptionRepo;
    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;

    /* ---------- PRODUCT LIMIT CHECK ---------- */
    @Override
    public void checkProductLimit(Shop shop) {
        Subscription sub = subscriptionRepo.findByShopId(shop.getId())
                .orElseThrow(() -> new RuntimeException("No active subscription"));

        long count = productRepo.countByShopId(shop.getId());

        if (count >= sub.getProductLimit()) {
            throw new RuntimeException(
                    "Product limit reached for your plan (" + sub.getPlan() + ")"
            );
        }
    }

    /* ---------- CATEGORY LIMIT CHECK ---------- */
    @Override
    public void checkCategoryLimit(Shop shop) {
        Subscription sub = subscriptionRepo.findByShopId(shop.getId())
                .orElseThrow(() -> new RuntimeException("No active subscription"));

        long count = categoryRepo.countByShopId(shop.getId());

        if (count >= sub.getCategoryLimit()) {
            throw new RuntimeException(
                    "Category limit reached for your plan (" + sub.getPlan() + ")"
            );
        }
    }

    /* ---------- PAYMENT FEATURE CHECK ---------- */
    @Override
    public void checkPaymentFeature(Shop shop, boolean cod, boolean upi) {
        Subscription sub = subscriptionRepo.findByShopId(shop.getId())
                .orElseThrow();

        if (cod && !Boolean.TRUE.equals(sub.getAllowCod())) {
            throw new RuntimeException("COD not allowed in your plan");
        }

        if (upi && !Boolean.TRUE.equals(sub.getAllowUpi())) {
            throw new RuntimeException("UPI not allowed in your plan");
        }
    }

    /* Block all write actions */
    @Override
    public Subscription validateWritable(Shop shop) {

        Subscription sub = subscriptionRepo.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (sub.getStatus() == SubscriptionStatus.EXPIRED ||
                sub.getStatus() == SubscriptionStatus.SUSPENDED) {

            throw new RuntimeException(
                    "Your subscription has expired. Please upgrade."
            );
        }
        return sub;
    }

    /* Order limit */
    @Override
    public void checkOrderLimit(Subscription sub, long orderCount) {
        if (sub.getMonthlyOrderLimit() != Integer.MAX_VALUE &&
                orderCount >= sub.getMonthlyOrderLimit()) {

            throw new RuntimeException("Monthly order limit reached");
        }
    }

    /* Product limit */
    @Override
    public void checkProductLimit(Subscription sub, long productCount) {
        if (sub.getProductLimit() != Integer.MAX_VALUE &&
                productCount >= sub.getProductLimit()) {

            throw new RuntimeException("Product limit reached");
        }
    }

    /* Category limit */
    @Override
    public void checkCategoryLimit(Subscription sub, long categoryCount) {
        if (sub.getCategoryLimit() != Integer.MAX_VALUE &&
                categoryCount >= sub.getCategoryLimit()) {

            throw new RuntimeException("Category limit reached");
        }
    }

    /*  Payment rules */
    @Override
    public void checkPaymentAllowed(Subscription sub, PaymentMode mode) {

        if (mode == PaymentMode.COD && !sub.getAllowCod()) {
            throw new RuntimeException("COD not allowed for your plan");
        }

        if (mode == PaymentMode.UPI && !sub.getAllowUpi()) {
            throw new RuntimeException("UPI not allowed for your plan");
        }
    }
}
