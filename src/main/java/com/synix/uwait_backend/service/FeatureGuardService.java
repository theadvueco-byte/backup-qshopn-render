package com.synix.uwait_backend.service;

import com.synix.uwait_backend.config.PlanFeatureConfig;
import com.synix.uwait_backend.exception.UpgradeRequiredException;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.subscription.*;
import com.synix.uwait_backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class FeatureGuardService {

    private final SubscriptionRepository subscriptionRepo;
    private final PlanFeatureConfig planFeatureConfig;

    public void checkFeature(Shop shop, SubscriptionFeature feature) {

        Subscription sub = subscriptionRepo.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (sub.getStatus() != SubscriptionStatus.ACTIVE) {
            throw new RuntimeException("Subscription inactive or expired");
        }

        Set<SubscriptionFeature> allowed =
                planFeatureConfig.featuresFor(sub.getPlan());

        if (!allowed.contains(feature)) {
            throw new UpgradeRequiredException(
                    "Upgrade required to use: " + feature.name()
            );
        }
    }

    /* Optional helper */
    public boolean hasFeature(Shop shop, SubscriptionFeature feature) {
        try {
            checkFeature(shop, feature);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
