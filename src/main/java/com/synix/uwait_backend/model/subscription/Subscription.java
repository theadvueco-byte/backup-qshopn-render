package com.synix.uwait_backend.model.subscription;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.synix.uwait_backend.model.Shop;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Subscription {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔹 Plan name (FREE_TRIAL, RETAIL, etc.)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan plan;

    // 🔹 Trial / Active / Expired / Suspended
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status;

    private LocalDate startDate;

    private LocalDate expiryDate;

    // 🔹 Limits (plan-specific)
    private Integer monthlyOrderLimit;
    private Integer productLimit;
    private Integer categoryLimit;


    // 🔹 Payment Controls (derived from plan, stored for fast checks)
    private Boolean allowUpi;
    private Boolean allowCod;
    private Boolean perProductPaymentControl;

    @OneToOne
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonBackReference
    private Shop shop;
}
