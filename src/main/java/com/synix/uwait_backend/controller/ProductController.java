package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.config.PlanConfig;
import com.synix.uwait_backend.dto.ProductResponse;
import com.synix.uwait_backend.model.Category;
import com.synix.uwait_backend.model.Product;
import com.synix.uwait_backend.model.ProductImage;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.model.subscription.SubscriptionFeature;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import com.synix.uwait_backend.repository.*;
import com.synix.uwait_backend.service.FeatureGuardService;
import com.synix.uwait_backend.service.FileStorageService;
import com.synix.uwait_backend.service.SubscriptionGuardService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/owner/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductRepository productRepo;
    private final CategoryRepository categoryRepo;
    private final ShopRepository shopRepo;
    private final SubscriptionGuardService guard;
    private final SubscriptionRepository subscriptionRepository;
    private final PlanConfig planConfig;
    private final OrderItemRepository orderItemRepository;
    private final FileStorageService fileStorageService;
    private final FeatureGuardService featureGuard;


    /* ---------- CREATE ---------- */
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> addProduct(
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal mrpPrice,
            @RequestParam(required = false) Integer offerPercent,
            @RequestParam(required = false) BigDecimal sellingPrice,
            @RequestParam(required = false) BigDecimal deliveryCharge,
            @RequestParam Integer stock,
            @RequestParam Long categoryId,
            @RequestParam(required = false) Boolean bestSeller,
            @RequestParam(required = false) String deliveryDetails,
            @RequestParam(required = false) String highlights,
            @RequestParam(required = false) String warrantyInfo,
            @RequestParam MultipartFile[] images,
            Authentication auth
    ) throws IOException {
        // Get shop
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // PLAN CHECK: ensure shop can add a product
        guard.checkProductLimit(shop);

        // Category
        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));

        // 🔐 PRODUCT OFFER CHECK
        if (offerPercent != null) {
            featureGuard.checkFeature(shop, SubscriptionFeature.PRODUCT_OFFERS);
        }

        // 2️⃣ 🔥 OFFER → SELLING PRICE CALCULATION (HERE)
        BigDecimal finalSellingPrice;


        if (offerPercent != null) {
            finalSellingPrice = mrpPrice.subtract(
                    mrpPrice.multiply(BigDecimal.valueOf(offerPercent))
                            .divide(BigDecimal.valueOf(100))
            );
        } else if (sellingPrice != null) {
            finalSellingPrice = sellingPrice;
        } else {
            finalSellingPrice = mrpPrice;
        }

        // Create product
        Product p = new Product();
        p.setName(name);
        p.setDescription(description);
        p.setMrpPrice(mrpPrice);
        p.setOfferPercent(offerPercent);
        p.setSellingPrice(finalSellingPrice);
        p.setDeliveryCharge(deliveryCharge);
        p.setStock(stock);
        p.setCategory(category);
        p.setShop(shop);
        p.setBestSeller(bestSeller);
        p.setDeliveryDetails(deliveryDetails);
        p.setHighlights(highlights);
        p.setWarrantyInfo(warrantyInfo);
        p.setActive(true);


        // 5️⃣ PLAN RULES: Enable payment methods based on plan
        subscriptionRepository
                .findFirstByShopOrderByExpiryDateDesc(shop)
                .ifPresent(sub -> {
                    planConfig.applyPlanConfig(sub, sub.getPlan());

                    p.setAllowUpi(sub.getAllowUpi());
                    p.setAllowCod(sub.getAllowCod());
                });

        // 🔐 IMAGE LIMIT CHECK
        int maxImages = 1;

        if (featureGuard.hasFeature(shop, SubscriptionFeature.MULTI_IMAGE_5)) {
            maxImages = 5;
        } else if (featureGuard.hasFeature(shop, SubscriptionFeature.MULTI_IMAGE_3)) {
            maxImages = 3;
        }

        if (images != null && images.length > maxImages) {
            throw new RuntimeException(
                    "Your plan allows only " + maxImages + " images per product"
            );
        }

        // 6️⃣ Save product
        productRepo.save(p);



        if (images != null && images.length > 0) {
            int index = 0;
            for (MultipartFile file : images) {
                String url = fileStorageService.storeProductImage(file, shop.getPublicCode(), p.getId(), index);
                ProductImage img = new ProductImage();
                img.setProduct(p);
                img.setImageUrl(url);
                img.setOrderIndex(index);
                p.getImages().add(img);
                index++;
            }
        }


// Save again with image URL
        productRepo.save(p);

        return ResponseEntity.ok(ProductResponse.from(p, shop));

    }

    /* ---------- LIST ---------- */
    @GetMapping
    public List<ProductResponse> list(Authentication auth) {
        // 1️⃣ Get the shop of the logged-in owner
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // 2️⃣ Fetch all products for this shop
        List<Product> products = productRepo.findByShopIdAndActiveTrue(shop.getId());

        // 3️⃣ Convert to ProductResponse DTOs (includes category info)
        return products.stream()
                .map(product -> ProductResponse.from(product, shop))
                .toList();
    }


    /* ---------- UPDATE ---------- */
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProduct(
            @PathVariable Long id,
            @RequestParam String name,
            @RequestParam String description,
            @RequestParam BigDecimal mrpPrice,
            @RequestParam(required = false) Integer offerPercent,
            @RequestParam(required = false) BigDecimal sellingPrice,
            @RequestParam(required = false) BigDecimal deliveryCharge,
            @RequestParam Integer stock,
            @RequestParam Long categoryId,
            @RequestParam(required = false) Boolean bestSeller,
            @RequestParam(required = false) String deliveryDetails,
            @RequestParam(required = false) String highlights,
            @RequestParam(required = false) String warrantyInfo,
            @RequestParam(required = false) Boolean allowUpi,
            @RequestParam(required = false) Boolean allowCod,

            // 🔥 Option 2 fields
            @RequestParam(required = false) Integer replaceIndex,
            @RequestParam(required = false) MultipartFile image,

            Authentication auth
    ) throws IOException {

        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Subscription sub = subscriptionRepository
                .findFirstByShopOrderByExpiryDateDesc(shop)
                .orElseThrow(() -> new RuntimeException("No active subscription"));


        Product p = productRepo.findByIdAndShopId(id, shop.getId());

        // ---- normal updates ----
        p.setName(name);
        p.setDescription(description);
        p.setMrpPrice(mrpPrice);
        p.setOfferPercent(offerPercent);
        p.setDeliveryCharge(deliveryCharge);
        p.setStock(stock);
        p.setBestSeller(Boolean.TRUE.equals(bestSeller));
        p.setDeliveryDetails(deliveryDetails);
        p.setHighlights(highlights);
        p.setWarrantyInfo(warrantyInfo);

        SubscriptionPlan plan = sub.getPlan();


        if (!plan.hasProductPaymentControl()
                && (allowUpi != null || allowCod != null)) {
            throw new RuntimeException(
                    "Your plan does not allow per-product payment settings"
            );
        }

        if (plan.hasProductPaymentControl()) {
            // Allowed plans (NOT FREE)
            if (allowUpi != null) {
                p.setAllowUpi(allowUpi);
            }
            if (allowCod != null) {
                p.setAllowCod(allowCod);
            }
        }

        if (offerPercent != null) {
            featureGuard.checkFeature(shop, SubscriptionFeature.PRODUCT_OFFERS);
        }

        // selling price
        BigDecimal finalSellingPrice =
                offerPercent != null
                        ? mrpPrice.subtract(
                        mrpPrice.multiply(BigDecimal.valueOf(offerPercent))
                                .divide(BigDecimal.valueOf(100)))
                        : sellingPrice != null ? sellingPrice : mrpPrice;

        p.setSellingPrice(finalSellingPrice);

        Category category = categoryRepo.findById(categoryId)
                .orElseThrow(() -> new RuntimeException("Category not found"));
        p.setCategory(category);

        if (image != null) {

            int maxImages = 1;

            if (featureGuard.hasFeature(shop, SubscriptionFeature.MULTI_IMAGE_5)) {
                maxImages = 5;
            } else if (featureGuard.hasFeature(shop, SubscriptionFeature.MULTI_IMAGE_3)) {
                maxImages = 3;
            }

            // --------- REPLACE ---------
            if (replaceIndex != null) {

                ProductImage oldImage = p.getImages().stream()
                        .filter(img -> img.getOrderIndex() == replaceIndex)
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Image not found"));

                fileStorageService.delete(oldImage.getImageUrl());

                String newUrl = fileStorageService.storeProductImage(
                        image,
                        shop.getPublicCode(),
                        p.getId(),
                        replaceIndex
                );

                oldImage.setImageUrl(newUrl);
                System.out.println(newUrl);
            }

            // --------- ADD NEW ---------
            else {

                if (p.getImages().size() >= maxImages) {
                    throw new RuntimeException("Image limit reached for your plan");
                }

                int nextIndex = p.getImages().size();

                String newUrl = fileStorageService.storeProductImage(
                        image,
                        shop.getPublicCode(),
                        p.getId(),
                        nextIndex
                );

                ProductImage newImage = new ProductImage();
                newImage.setProduct(p);
                newImage.setImageUrl(newUrl);
                newImage.setOrderIndex(nextIndex);

                System.out.println(newUrl);

                p.getImages().add(newImage);
            }
        }

        productRepo.save(p);
// ✅ RETURN DTO
        return ResponseEntity.ok(ProductResponse.from(p, shop));
    }



    /* ---------- ACTIVE ON / OFF ---------- */
    @PatchMapping("/{id}/status")
    public ResponseEntity<?> toggle(
            @PathVariable Long id,
            @RequestParam Boolean active,
            Authentication auth
    ) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName()).orElseThrow();
        Product p = productRepo.findByIdAndShopId(id, shop.getId());
        p.setActive(active);
        productRepo.save(p);
        return ResponseEntity.ok(Map.of("message","Product status updated"));
    }

    /* ---------- DELETE ---------- */

    @Transactional
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {

        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Product product = productRepo.findByIdAndShopId(id, shop.getId());
        if (product == null) {
            throw new RuntimeException("Product not found");
        }

        // Check if product is in any order
        boolean hasOrders = orderItemRepository.existsByProduct_Id(product.getId());
        if (hasOrders) {
            // Soft delete: just mark as inactive
            product.setActive(false);
            productRepo.save(product);
            return ResponseEntity.ok(Map.of(
                    "message", "Product has orders, so it is marked as inactive"
            ));
        }

        // Delete reviews + product (safe)
        productRepo.delete(product);

        return ResponseEntity.ok(Map.of(
                "message", "Product and its reviews deleted successfully"
        ));
    }


}


