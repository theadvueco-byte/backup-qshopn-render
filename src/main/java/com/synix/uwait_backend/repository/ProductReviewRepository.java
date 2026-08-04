package com.synix.uwait_backend.repository;

import com.synix.uwait_backend.model.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Long> {

    // Product rating
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.product.id = :productId")
    Double avgRatingByProduct(Long productId);

    int countByProductId(Long productId);

    // Shop rating
    @Query("SELECT AVG(r.rating) FROM ProductReview r WHERE r.shop.id = :shopId")
    Double avgRatingByShop(UUID shopId);

    int countByShopId(UUID shopId);

    // Reviews for product
    List<ProductReview> findByProductId(Long productId);
}

