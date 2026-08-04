package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.dto.BusinessRequest;
import com.synix.uwait_backend.model.Shop;
import com.synix.uwait_backend.model.User;
import com.synix.uwait_backend.repository.UserRepository;
import com.synix.uwait_backend.service.ShopOnboardingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/shop/onboarding")
@RequiredArgsConstructor
public class ShopOnboardingController {

    private final ShopOnboardingService service;
    private final UserRepository userRepository;


    private String email(Authentication a) { return a.getName(); }


    @PostMapping("/business-info")
    public ResponseEntity<?> business(@RequestBody BusinessRequest request, Authentication auth) {
        service.saveBusinessInfo(
                email(auth),
                request.getShopName(),
                request.getCategory(),
                request.getWhatsapp()
        );
        return ResponseEntity.ok(Map.of("message", "Business info saved successfully"));
    }

//    @PostMapping("/business-info")
//    public ResponseEntity<?> business(@RequestParam String shopName,
//                                      @RequestParam String category,
//                                      @RequestParam String whatsapp,
//                                      Authentication auth) {
//        service.saveBusinessInfo(email(auth), shopName, category, whatsapp);
//        return ResponseEntity.ok(Map.of("message", "Business info saved successfully"));
//    }

    @PostMapping("/address")
    public ResponseEntity<?> address(@RequestBody Shop shop, Authentication auth) {
        service.saveAddress(email(auth), shop);
        return ResponseEntity.ok(Map.of("message", "Address saved successfully"));
    }

    @PostMapping(value = "/branding", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> branding(@RequestParam(required = false) MultipartFile logo,
                                      @RequestParam(required = false) String shortBio,
                                      @RequestParam(required = false) String instagramId,
                                      Authentication auth) {
        service.saveBranding(email(auth), logo, shortBio, instagramId);
        return ResponseEntity.ok(Map.of("message", "Branding info saved successfully"));
    }

    @PostMapping(value = "/location-proof", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> location(
                                      @RequestParam MultipartFile shopPhoto,
                                      @RequestParam String gpsLocation,
                                      Authentication auth) {
        service.submitLocation(email(auth), shopPhoto,gpsLocation);
        return ResponseEntity.ok(Map.of("message", "Location proof submitted successfully"));
    }

    @GetMapping("/upi-link")
    public ResponseEntity<?> upi(Authentication auth) {
        String upiLink = service.getUpiLink(email(auth));
        return ResponseEntity.ok(Map.of("upiLink", upiLink));
    }


//    final onboarding stage
    @PostMapping(value = "/upi-verification", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> upiVerification(
                                             @RequestParam MultipartFile screenshot,
                                             Authentication auth) {
        service.submitUpiProof(email(auth),screenshot);

        return ResponseEntity.ok(Map.of("message", "UPI proof submitted successfully"));
    }

    // ✅ GET - Check onboarding status
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getStatus(
            Authentication auth
    ) {

        User user = userRepository.findByEmail(email(auth))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Map<String, Object> response = new HashMap<>();
        response.put("onboardingCompleted", user.isGuideTour());

        return ResponseEntity.ok(response);
    }

    // ✅ PUT - Update onboarding status
    @PutMapping("/status")
    public ResponseEntity<Map<String, Object>> updateStatus(
            Authentication auth,
            @RequestBody Map<String, Object> body
    ) {

        User user = userRepository.findByEmail(email(auth))
                .orElseThrow(() -> new RuntimeException("User not found"));

        Boolean completed = Boolean.valueOf(body.get("onboardingCompleted").toString());

        user.setGuideTour(completed);
        userRepository.save(user);

        Map<String, Object> response = new HashMap<>();
        response.put("message", "Onboarding status updated");
        response.put("onboardingCompleted", completed);

        return ResponseEntity.ok(response);
    }



}
