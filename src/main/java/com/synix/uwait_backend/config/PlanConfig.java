package com.synix.uwait_backend.config;

import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import org.springframework.stereotype.Component;

@Component
public class PlanConfig {

    /* ================= PLAN AMOUNTS ================= */
    public double getPlanAmount(SubscriptionPlan plan) {
        return switch (plan) {
            case RETAIL -> 199;
            case GROWTH -> 299;
            case BUSINESS -> 1749;
            case FREE_TRIAL -> 0;
        };
    }

    /* ================= PLAN RULES ================= */
    public void applyPlanConfig(Subscription sub, SubscriptionPlan plan) {

        switch (plan) {
            case FREE_TRIAL -> {
                sub.setMonthlyOrderLimit(50);
                sub.setProductLimit(5);
                sub.setCategoryLimit(3);
                sub.setAllowUpi(true);
                sub.setAllowCod(false);
                sub.setPerProductPaymentControl(false);
            }
                 
            case RETAIL -> {
                sub.setMonthlyOrderLimit(100);
                sub.setProductLimit(50);
                sub.setCategoryLimit(15);
                sub.setAllowUpi(false);
                sub.setAllowCod(true);
                sub.setPerProductPaymentControl(false);
            }



            case GROWTH -> {
                sub.setMonthlyOrderLimit(500);
                sub.setProductLimit(100);
                sub.setCategoryLimit(25);
                sub.setAllowUpi(true);
                sub.setAllowCod(true);
                sub.setPerProductPaymentControl(true);
            }

            case BUSINESS -> {
                sub.setMonthlyOrderLimit(Integer.MAX_VALUE);
                sub.setProductLimit(Integer.MAX_VALUE);
                sub.setCategoryLimit(Integer.MAX_VALUE);
                sub.setAllowUpi(true);
                sub.setAllowCod(true);
                sub.setPerProductPaymentControl(true);
            }
        }
    }
}
