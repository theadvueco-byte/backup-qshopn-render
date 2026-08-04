package com.synix.uwait_backend.controller;

import com.razorpay.RazorpayException;
import com.synix.uwait_backend.config.PlanConfig;
import com.synix.uwait_backend.dto.BillingHistoryResponse;
import com.synix.uwait_backend.dto.DeleteAccountConfirmRequest;
import com.synix.uwait_backend.dto.PaymentResponse;
import com.synix.uwait_backend.dto.ShopResponse;
import com.synix.uwait_backend.dto.ownerorder.ShopOfferRequest;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.ShopStatus;
import com.synix.uwait_backend.model.User;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import com.synix.uwait_backend.model.subscription.SubscriptionStatus;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.repository.SubscriptionRepository;
import com.synix.uwait_backend.repository.UserRepository;
import com.synix.uwait_backend.service.OwnerService;
import com.synix.uwait_backend.service.ShopOnboardingService;
import com.synix.uwait_backend.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/owner")
@RequiredArgsConstructor
public class OwnerController {

    private final OwnerService dashboardService;
    private final SubscriptionRepository subscriptionRepo;
    private final SubscriptionService subscriptionService;
    private final UserRepository userRepository;
    private final ShopRepository shopRepo;
    private final PlanConfig planConfig;
    private final ShopOnboardingService service;

    @GetMapping("/earnings")
    public Map<String, Object> earnings(Authentication auth) {
        return dashboardService.getEarnings(auth.getName());
    }

    @PatchMapping("/shop/status")
    public void toggleShop(
            @RequestBody Map<String, Boolean> body,
            Authentication auth
    ) {
        dashboardService.updateShopStatus(
                auth.getName(),
                body.get("active")
        );
    }

    @GetMapping("/shop")
    public ResponseEntity<List<ShopResponse>> getMyShops(Authentication auth) {

        String email = auth.getName();

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Shop> shops = shopRepo.findAllByOwner(owner);

        List<ShopResponse> responses = shops.stream().map(shop -> {

            ShopResponse.ShopResponseBuilder builder = ShopResponse.builder()
                    .publicCode(shop.getPublicCode())
                    .shopName(shop.getShopName())
                    .shopLogoUrl(shop.getLogoPath())
                    .category(shop.getCategory())
                    .whatsappNumber(shop.getWhatsappNumber())
                    .shortBio(shop.getShortBio())
                    .instagramId(shop.getInstagramId())
                    .status(shop.getStatus())
                    .active(shop.isActive())
                    .area(shop.getArea())
                    .city(shop.getCity())
                    .gpsLocation(shop.getGpsLocation())
                    .pincode(shop.getPincode());


            // Add active subscription (if any)
            subscriptionRepo.findFirstByShopOrderByExpiryDateDesc(shop)
                    .filter(sub -> sub.getStatus() == SubscriptionStatus.TRIAL
                            || sub.getStatus() == SubscriptionStatus.ACTIVE)
                    .ifPresent(sub -> builder
                            .subscriptionPlan(sub.getPlan().name())
                            .subscriptionStatus(sub.getStatus().name())
                            .subscriptionEndDate(sub.getExpiryDate())
                    );
            return builder.build();
        }).toList();

        return ResponseEntity.ok(responses);
    }

    @PutMapping("/shops/offer")
    public ResponseEntity<?> setShopOffer(
            @RequestBody ShopOfferRequest request,
            Authentication auth

    ) {
        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        shop.setShopOfferName(request.getOfferName());
        shop.setShopOfferPercent(request.getOfferPercent());
        shop.setOfferActive(request.getActive());

        shopRepo.save(shop);
        return ResponseEntity.ok(
                Map.of("message", "Your offer '" + request.getOfferName() + "' is now active.")
        );
    }

    @GetMapping("/shops/offer")
    public ResponseEntity<?> getShopOffer(Authentication auth) {

        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Optional: return only offer-related data
        Map<String, Object> response = new HashMap<>();
        response.put("offerName", shop.getShopOfferName());
        response.put("offerPercent", shop.getShopOfferPercent());
        response.put("active", shop.getOfferActive());

        return ResponseEntity.ok(response);
    }


    @GetMapping("/shops/{publicCode}/qr")
    public ResponseEntity<byte[]> downloadQr(@PathVariable String publicCode) throws IOException {

        Shop shop = shopRepo.findByPublicCode(publicCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Activate ONLY FIRST TIME
        if (shop.getStatus() != ShopStatus.APPROVE) {
            service.activateShop(shop);
        }

        if (shop.getQrCode() == null) {
            service.generateQrCodeForShop(shop);
        }

        return ResponseEntity.ok()
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=shop-qr.png"
                )
                .contentType(MediaType.IMAGE_PNG)
                .body(shop.getQrCode());
    }



    @GetMapping("/subscription")
    public ResponseEntity<?> getSubscription(
            @RequestParam String shopId,
            Authentication auth
    ) {

        User user = userRepository.findByEmail(auth.getName())
                .orElseThrow();

        Shop shop = shopRepo.findByOwner(user)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Subscription sub = subscriptionRepo.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        return ResponseEntity.ok(Map.of(
                "plan", sub.getPlan(),
                "status", sub.getStatus(),
                "startDate",sub.getStartDate(),
                "expiryDate", sub.getExpiryDate()
        ));
    }

//    @PostMapping("/subscription/create-order")
//    public ResponseEntity<?> createOrder(
//            @RequestParam SubscriptionPlan plan,
//            Authentication auth
//    ) throws Exception {
//
//        Shop shop = shopRepo.findByOwner_Email(auth.getName())
//                .orElseThrow(() -> new RuntimeException("Shop not found"));
//
//        double amount = planConfig.getPlanAmount(plan);
//
//        Map<String, Object> response =
//                subscriptionService.createRazorpayOrder(shop, plan, amount);
//
//        return ResponseEntity.ok(response);
//    }

    @PostMapping("/subscription/pay")
    public ResponseEntity<?> createSubscriptionPayment(
            @RequestParam SubscriptionPlan plan,
            Authentication auth
    ) throws RazorpayException {

        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow();

        Double amount = planConfig.getPlanAmount(plan);

        PaymentResponse response =
                subscriptionService.createSubscriptionPaymentLink(
                        shop, plan, amount
                );

        return ResponseEntity.ok(response);
    }

    @PutMapping("/subscription/verify")
    public ResponseEntity<?> verifySubscriptionPayment(
            @RequestParam("razorpay_payment_id") String paymentId,
            @RequestParam("razorpay_payment_link_id") String orderId,
            Authentication auth
    ) throws RazorpayException {

        subscriptionService.verifyAndActivate(
                auth.getName(),
                orderId,
                paymentId
        );

        return ResponseEntity.ok(
                Map.of("message", "Subscription activated successfully")
        );
    }

    @GetMapping("/billing-history")
    public List<BillingHistoryResponse> billingHistory(
            Authentication authentication
    ) {
        String ownerEmail = authentication.getName();
        return subscriptionService.getBillingHistory(ownerEmail);
    }

//    @DeleteMapping
//    public ResponseEntity<?> deleteAccount(@RequestParam String email,Authentication authentication
//    ) {
//        Shop shop = shopRepo.findByOwner_Email(email)
//                .orElseThrow();
//        String ownerEmail = authentication.getName();
//        dashboardService.deleteOwnerAccount(ownerEmail);
//        return ResponseEntity.ok(Map.of("message", "Account deleted successfully"));
//    }

    @PostMapping("/delete/request")
    public ResponseEntity<?> requestDelete(Authentication auth) throws IOException {
        dashboardService.requestAccountDeletion(auth.getName());
        return ResponseEntity.ok("OTP sent to email");
    }

    @PostMapping("/delete/confirm")
    public ResponseEntity<?> confirmDelete(
            Authentication auth,
            @RequestBody DeleteAccountConfirmRequest req
    ) {
        dashboardService.confirmAccountDeletion(auth.getName(), req);
        return ResponseEntity.ok("Account deleted permanently");
    }



}

