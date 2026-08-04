package com.synix.uwait_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.synix.uwait_backend.config.BooleanToIntegerConverter;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(length = 1000)
    private String description;

    private BigDecimal mrpPrice;        // original price
    private BigDecimal sellingPrice;   // price after product offer
    private Integer offerPercent;      // optional

    @Column(name = "delivery_charge", precision = 10, scale = 2)
    private BigDecimal deliveryCharge;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(nullable = false)
    private int stockThreshold = 5;

    /* ================= FLAGS ================= */
    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean active;

    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean bestSeller;

    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean allowUpi;

    @Convert(converter = BooleanToIntegerConverter.class)
    @Column(nullable = false)
    private boolean allowCod;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ProductImage> images = new ArrayList<>();


    @Column(length = 500)
    private String deliveryDetails;

    @Column(length = 500)
    private String highlights;

    @Column(length = 300)
    private String warrantyInfo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    @JsonBackReference
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "shop_id", nullable = false)
    @JsonBackReference
    private Shop shop;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL)
    private List<ProductReview> reviews = new ArrayList<>();

    private LocalDateTime createdAt = LocalDateTime.now();
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    @Transient
    public boolean isInStock() {
        return stock != null && stock > 0;
    }

}
