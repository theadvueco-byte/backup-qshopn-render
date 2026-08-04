package com.synix.uwait_backend.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "order_requests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* ================= RELATION ================= */
    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /* ================= CUSTOMER ================= */
    private String customerEmail;

    @Column(length = 1000)
    private String message; // reason / query

    /* ================= TYPE ================= */
    @Enumerated(EnumType.STRING)
    private RequestType type;

    /* ================= STATUS ================= */
    @Enumerated(EnumType.STRING)
    private RequestStatus status;

    /* ================= RESPONSE ================= */
    @Column(length = 1000)
    private String ownerResponse;

    private LocalDateTime createdAt = LocalDateTime.now();
}

