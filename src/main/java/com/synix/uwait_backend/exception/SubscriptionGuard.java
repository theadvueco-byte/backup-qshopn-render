package com.synix.uwait_backend.exception;

import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.model.subscription.SubscriptionStatus;
import com.synix.uwait_backend.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SubscriptionGuard {

    private final SubscriptionRepository subscriptionRepo;

    public void checkActive(Shop shop) {

        Subscription sub = subscriptionRepo.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        if (sub.getStatus() == SubscriptionStatus.EXPIRED ||
                sub.getStatus() == SubscriptionStatus.SUSPENDED) {

            throw new TrialExpiredException("Upgrade to continue");
        }

        if (!shop.isActive()) {
            throw new RuntimeException("Shop is suspended by admin");
        }

    }
}


