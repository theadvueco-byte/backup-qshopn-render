package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.config.PlanFeatureConfig;
import com.synix.uwait_backend.dto.SubscriptionFeatureResponse;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.User;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.repository.SubscriptionRepository;
import com.synix.uwait_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionRepository subscriptionRepo;
    private final UserRepository userRepository;
    private final ShopRepository shopRepo;
    private final PlanFeatureConfig planFeatureConfig;

    @GetMapping("/subscription/features")
    public SubscriptionFeatureResponse getAllowedFeatures(
            Authentication auth
    ) {
        String email = auth.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Shop shop = shopRepo.findByOwner_Email(owner.getEmail())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Subscription sub = subscriptionRepo.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        return new SubscriptionFeatureResponse(
                planFeatureConfig.featuresFor(sub.getPlan())
        );
    }

    @GetMapping("/public/{shopCode}/features")
    public SubscriptionFeatureResponse getAllowedFeaturesForPublicShop(
            @PathVariable String shopCode
    ) {
        Shop shop = shopRepo.findByPublicCode(shopCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Subscription sub = subscriptionRepo.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        return new SubscriptionFeatureResponse(
                planFeatureConfig.featuresFor(sub.getPlan())
        );
    }

}
