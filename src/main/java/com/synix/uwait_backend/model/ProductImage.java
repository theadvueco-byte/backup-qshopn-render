package com.synix.uwait_backend.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String imageUrl; // URL to disk

    private Integer orderIndex; // optional: to sort images (first image = main)

    @ManyToOne
    @JoinColumn(name = "product_id")
    private Product product;
}

