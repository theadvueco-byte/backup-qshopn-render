package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.model.Category;
import com.synix.uwait_backend.model.Product;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.repository.CategoryRepository;
import com.synix.uwait_backend.repository.ProductRepository;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.service.SubscriptionGuardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/owner/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepository categoryRepo;
    private final ShopRepository shopRepo;
    private final SubscriptionGuardService guard;
    private final ProductRepository productRepository;


    @PostMapping
    public Category create(@RequestBody Category c, Authentication auth) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName()).orElseThrow();

        // PLAN CHECK
        guard.checkCategoryLimit(shop);

        c.setShop(shop);
        return categoryRepo.save(c);
    }

    @PutMapping("/{id}")
    public Category update(
            @PathVariable Long id,
            @RequestBody Category updated,
            Authentication auth) {

        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Category existing = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Ownership check (VERY IMPORTANT)
        if (!existing.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        existing.setName(updated.getName());
        return categoryRepo.save(existing);
    }



    @GetMapping
    public List<Category> list(Authentication auth) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName()).orElseThrow();
        return categoryRepo.findByShopId(shop.getId());
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity<?> delete(
            @PathVariable Long id,
            Authentication auth) {

        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Category category = categoryRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // Ownership check
        if (!category.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        // Get all products linked to this category
        List<Product> products = productRepository.findByCategory_Id(id);

        // Unlink category from all products
        products.forEach(p -> p.setCategory(null));
        productRepository.saveAll(products);

        // Now delete the category
        categoryRepo.delete(category);

        return ResponseEntity.ok(
                Map.of("message", "Category deleted successfully, products unlinked")
        );
    }


}
