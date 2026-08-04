package com.synix.uwait_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
@Entity
@Table(name = "order_items")
@Getter
@Setter
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // REAL product reference
    @ManyToOne(optional = false,fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Back reference
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    private Integer quantity;

    // 🔥 PRICE SNAPSHOT (VERY IMPORTANT)
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal unitPrice;


    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal totalPrice;

    // 🔹 Transient field to accept productId from JSON
    @Transient
    private Long productId;

    // Optional helpers (DO NOT persist)
    public String getProductName() {
        return product != null ? product.getName() : null;
    }


}


