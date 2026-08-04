package com.synix.uwait_backend.model.subscription;

import lombok.Getter;
import lombok.Setter;

@Getter
public enum SubscriptionPlan {

    FREE_TRIAL(
            false,
            true,
            false  // per-product control
    ),

    RETAIL(
            true,
            true,
            false
    ),

    GROWTH(
            true,
            true,
            true
    ),

    BUSINESS(
            true,
            true,
            true
    );

    private final boolean upiAllowed;
    private final boolean codAllowed;
    private final boolean productPaymentControl;

    SubscriptionPlan(boolean upiAllowed,
                     boolean codAllowed,
                     boolean productPaymentControl) {
        this.upiAllowed = upiAllowed;
        this.codAllowed = codAllowed;
        this.productPaymentControl = productPaymentControl;
    }


    public boolean hasProductPaymentControl() {
        return productPaymentControl;
    }
}

