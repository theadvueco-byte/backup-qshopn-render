package com.synix.uwait_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class SubscriptionRequest {

    @Id
    @GeneratedValue
    private Long id;

    @ManyToOne
    @JoinColumn(name = "shop_id")
    @JsonBackReference
    private Shop shop;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionPlan requestedPlan;

    private Double amount;

    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    private String razorpayOrderId;
    private String razorpayPaymentId;

    private LocalDateTime requestedAt;
    private LocalDateTime approvedAt;

    @PrePersist
    protected void onCreate() {
        this.requestedAt = LocalDateTime.now();
    }

    @PreUpdate
    void onUpdate() {
        approvedAt = LocalDateTime.now();
    }

}
