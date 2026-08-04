package com.synix.uwait_backend.dto;

import com.synix.uwait_backend.model.subscription.SubscriptionFeature;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Set;

@Data
@AllArgsConstructor
public class SubscriptionFeatureResponse {
    private Set<SubscriptionFeature> features;
}

