package com.synix.uwait_backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_reviews")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* RATING */
    @Column(nullable = false)
    private Integer rating;   // 1 to 5

    /* COMMENT */
    @Column(length = 500)
    private String comment;

    /* CUSTOMER INFO */
    private String customerName;
//    private String customerPhone;

    /* RELATIONS */
    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JsonIgnore
    private Shop shop;

    private LocalDateTime createdAt = LocalDateTime.now();
}

