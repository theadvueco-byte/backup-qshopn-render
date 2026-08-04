package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.model.Product;
import com.synix.uwait_backend.model.ProductReview;
import com.synix.uwait_backend.repository.ProductRepository;
import com.synix.uwait_backend.repository.ProductReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ProductRepository productRepo;
    private final ProductReviewRepository reviewRepo;

    @PostMapping("/{productId}")
    public ResponseEntity<?> addReview(
            @PathVariable Long productId,
            @RequestBody ProductReview review
    ) {
        Product p = productRepo.findById(productId).orElseThrow();

        review.setProduct(p);
        review.setShop(p.getShop());
        reviewRepo.save(review);
        return ResponseEntity.ok(Map.of("message","Review added"));
    }

    @GetMapping("/{productId}")
    public ResponseEntity<List<ProductReview>> getReviews(@PathVariable Long productId) {
        Product p = productRepo.findById(productId)
                .orElseThrow(() -> new RuntimeException("Product not found"));

        List<ProductReview> reviews = reviewRepo.findByProductId(productId);
        return ResponseEntity.ok(reviews);
    }
}

