package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.dto.CancelOrderRequest;
import com.synix.uwait_backend.dto.CreateOrderRequest;
import com.synix.uwait_backend.dto.PublicShopResponse;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.repository.CategoryRepository;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/public")
@RequiredArgsConstructor
public class PublicOrderController {

    private final OrderService orderService;
    private final ShopRepository shopRepo;
    private final CategoryRepository categoryRepo;


    /* -------- GET PRODUCTS (PUBLIC) -------- */
    @GetMapping("/{code}/products")
    public ResponseEntity<?> getProducts(
            @PathVariable String code,
            @RequestParam(required = false) Long categoryId
    ) {
        return ResponseEntity.ok(
                orderService.getPublicProducts(code, categoryId)
        );
    }

    @GetMapping("/{code}/categories")
    public List<Category> listPublic(@PathVariable String code) {
        Shop shop = shopRepo.findByPublicCode(code)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return categoryRepo.findByShopId(shop.getId());
    }


    @GetMapping("/{publicCode}/offer")
    public ResponseEntity<?> getShopOffer(@PathVariable String publicCode) {

        Shop shop = shopRepo.findByPublicCode(publicCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Optional: return only offer-related data
        Map<String, Object> response = new HashMap<>();
        response.put("offerName", shop.getShopOfferName());
        response.put("offerPercent", shop.getShopOfferPercent());
        response.put("active", shop.getOfferActive());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/{code}/qr")
    public ResponseEntity<byte[]> downloadQr(@PathVariable String code) throws IOException {

        Shop shop = shopRepo.findByPublicCode(code)
                .orElseThrow(() -> new RuntimeException("Shop not found"));


        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=shop-qr.png"
                )
                .contentType(MediaType.IMAGE_PNG)
                .body(shop.getQrCode());
    }


    @GetMapping("{code}")
    public ResponseEntity<?> getShopPublic(@PathVariable String code) {
        Shop shop = shopRepo.findByPublicCode(code)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        return ResponseEntity.ok(PublicShopResponse.from(shop));
    }

//    @GetMapping("{id}/image")
//    public ResponseEntity<byte[]> getProductImage(@PathVariable Long id) {
//
//        Product product = productRepository.findById(id).orElseThrow();
//
//        return ResponseEntity.ok()
//                .contentType(MediaType.parseMediaType(product.getImageType()))
//                .cacheControl(CacheControl.maxAge(30, TimeUnit.DAYS))
//                .body(product.getImageData());
//    }

    /* -------- PLACE ORDER -------- */
    @PostMapping("/{code}/order")
    public ResponseEntity<?> placeOrder(
            @PathVariable String code,
            @RequestBody CreateOrderRequest request,
            @RequestParam PaymentMode paymentMode
    ) throws IOException {
        Order savedOrder = orderService.placeOrder(code, request, paymentMode);

        return ResponseEntity.ok(Map.of(
                "orderId", savedOrder.getId(),
                "message", "Order placed successfully"
        ));
    }
    /* -------- GET UPI LINK -------- */
    @GetMapping("/{code}/order/{id}/pay-upi")
    public ResponseEntity<?> payViaUpi(@PathVariable Long id) {
        return ResponseEntity.ok(
                Map.of("upiLink", orderService.generateUpiLink(id))
        );
    }

    /* -------- CONFIRM PAYMENT -------- */
    @PostMapping("/{code}/order/{id}/confirm-payment")
    public ResponseEntity<?> confirmPayment(
            @PathVariable Long id,
            @RequestParam("screenshot") MultipartFile screenshot
    ) throws IOException {
        orderService.confirmPayment(id, screenshot);
        return ResponseEntity.ok(Map.of("message", "Payment confirmed"));
    }


    @PatchMapping("/{code}/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @RequestBody CancelOrderRequest request
    ) throws IOException {
        orderService.cancelOrderByCustomer(
                id,
                request.getEmail(),
                request.getReason()
        );

        return ResponseEntity.ok(Map.of(
                "message", "Order cancelled successfully"
        ));
    }


    @GetMapping("/{code}/order-status")
    public ResponseEntity<?> getOrderStatus(
            @RequestParam Long orderId,
            @RequestParam String email
    ) {
        return ResponseEntity.ok(orderService.getPublicOrderStatus(orderId, email));
    }


}
