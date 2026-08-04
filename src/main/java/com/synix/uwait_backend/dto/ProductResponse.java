package com.synix.uwait_backend.dto;

import com.synix.uwait_backend.model.Product;
import com.synix.uwait_backend.model.ProductImage;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;

@Getter
@Setter
public class ProductResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal mrpPrice;
    private BigDecimal sellingPrice;
    private Integer offerPercent;
    private BigDecimal deliveryCharge;

    private int stock;
    private boolean inStock;
    private boolean bestSeller;
    private boolean active;

    private List<String> imageUrls; // gallery
    private String deliveryDetails;
    private String highlights;
    private String warrantyInfo;

    // Payment visibility (PLAN CONTROLLED)
    private boolean allowUpi;
    private boolean allowCod;
    private boolean editPaymentMode;

    // Category info
    private Long categoryId;
    private String categoryName;

    public static ProductResponse from(Product product, Shop shop) {

        SubscriptionPlan plan = shop.getSubscription().getPlan();

        ProductResponse r = new ProductResponse();

        r.setId(product.getId());
        r.setName(product.getName());
        r.setDescription(product.getDescription());
        r.setMrpPrice(product.getMrpPrice());
        r.setSellingPrice(product.getSellingPrice());
        r.setOfferPercent(product.getOfferPercent());
        r.setDeliveryCharge(product.getDeliveryCharge());

        r.setStock(product.getStock());
        r.setActive(product.isActive());
        r.setInStock(product.isInStock());
        r.setBestSeller(product.isBestSeller());

        List<ProductImage> images = product.getImages()
                .stream()
                .sorted(Comparator.comparingInt(ProductImage::getOrderIndex))
                .toList();

        r.setImageUrls(
                images.stream()
                        .map(ProductImage::getImageUrl)
                        .toList()
        );


        r.setDeliveryDetails(product.getDeliveryDetails());
        r.setHighlights(product.getHighlights());
        r.setWarrantyInfo(product.getWarrantyInfo());

        r.setAllowUpi(
                plan.hasProductPaymentControl()
                        ? product.isAllowUpi()
                        : plan.isUpiAllowed()
        );

        r.setAllowCod(
                plan.hasProductPaymentControl()
                        ? product.isAllowCod()
                        : plan.isCodAllowed()
        );

        r.setEditPaymentMode(plan.hasProductPaymentControl());


        // ✅ Category
        if (product.getCategory() != null) {
            r.setCategoryId(product.getCategory().getId());
            r.setCategoryName(product.getCategory().getName());
        }

        return r;
    }
}
