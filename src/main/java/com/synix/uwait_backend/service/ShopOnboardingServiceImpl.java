package com.synix.uwait_backend.service;

import com.synix.uwait_backend.config.QrCodeGenerator;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.repository.AdminRepository;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.repository.UserRepository;
import com.synix.uwait_backend.service.emailsend.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ShopOnboardingServiceImpl implements ShopOnboardingService{

    private final AdminNotificationService adminNotificationService;
    private final AdminRepository adminRepo;

    private final ShopRepository shopRepository;
    private final UserRepository userRepository;
    private final SubscriptionService subscriptionService;
   // private final EmailService emailService;
    private final FileStorageService fileStorageService;
    private static final String ALPHANUM = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    private static final SecureRandom RANDOM = new SecureRandom();

    //smtp email service

    private final EmailNotificationService emailNotificationService;

    private String generatePublicCode() {
        int length = 6;
        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {
            sb.append(ALPHANUM.charAt(RANDOM.nextInt(ALPHANUM.length())));
        }
        return sb.toString();
    }

    private Shop getOrCreate(String email) {
        return shopRepository.findByOwner_Email(email)
                .orElseGet(() -> {
                    Shop shop = new Shop();
                    shop.setOwnerEmail(email);
                    shop.setStatus(ShopStatus.DRAFT);
                    shop.setUpiVerified(false);
                    shop.setCreatedAt(LocalDateTime.now());
                    // ⚡ generate public code here
                    shop.setPublicCode(generatePublicCode());
                    return shopRepository.save(shop);
                });
    }

    // Save basic business info
    @Override
    public void saveBusinessInfo(String email, String name, String category, String whatsapp) {

        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Shop shop = getOrCreate(owner); // ✅ pass USER, not email

        shop.setShopName(name);
        shop.setCategory(category);
        shop.setWhatsappNumber(whatsapp);

        if (shop.getPublicCode() == null) {
            shop.setPublicCode(generatePublicCode());
        }

        shopRepository.save(shop);
    }

    private Shop getOrCreate(User owner) {
        return shopRepository.findByOwner(owner)
                .orElseGet(() -> {
                    Shop shop = new Shop();
                    shop.setOwner(owner);                 // 🔥 REQUIRED
                    shop.setOwnerEmail(owner.getEmail()); // optional
                    shop.setStatus(ShopStatus.DRAFT);
                    return shop;
                });
    }

    @Override
    public void saveAddress(String email, Shop s) {
        Shop shop = getOrCreate(email);
        shop.setFullAddress(s.getFullAddress());
        shop.setArea(s.getArea());
        shop.setCity(s.getCity());
        shop.setPincode(s.getPincode());
        shop.setUpiId(s.getUpiId());
        shopRepository.save(shop);
    }

    @Override
    @Transactional
    public void saveBranding(String email, MultipartFile logo, String bio, String insta) {
        Shop shop = getOrCreate(email);

        // 1. Ensure publicCode exists (QR MUST depend on this)
        if (shop.getPublicCode() == null || shop.getPublicCode().isEmpty()) {
            shop.setPublicCode(generatePublicCode());
        }

        if (logo != null && !logo.isEmpty()) {

            // delete old logo
            if (shop.getLogoPath() != null) {
                fileStorageService.delete(shop.getLogoPath());
            }

            String path = fileStorageService.store(logo, "logos",shop.getPublicCode());
            shop.setLogoPath(path);
        }

        shop.setShortBio(bio);
        shop.setInstagramId(insta);
        shopRepository.save(shop);
    }

    @Override
    @Transactional
    public void submitLocation(String email, MultipartFile photo, String gpsLocation) {
        Shop shop = getOrCreate(email);

        if (photo != null && !photo.isEmpty()) {
            if (shop.getShopPhotoPath() != null) {
                fileStorageService.delete(shop.getShopPhotoPath());
            }

            shop.setShopPhotoPath(
                    fileStorageService.store(photo, "shop-photos", shop.getPublicCode())
            );
        }

        shop.setGpsLocation(gpsLocation); // ⭐ SAVE STRING "lat,lng"
        shop.setStatus(ShopStatus.SUBMITTED_FOR_REVIEW);
        shopRepository.save(shop);
    }


    @Override
    public String getUpiLink(String email) {
        Shop shop = getOrCreate(email);
        return "upi://pay?pa=dharanpostmail-1@oksbi" +
                "&pn=thulasidharanp" +
                "&am=1" +
                "&cu=INR" +
                "&tn=VERIFY_SHOP_" + shop.getId();
    }

    @Override
    @Transactional
    public void submitUpiProof(String email, MultipartFile screenshot) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Shop shop = getOrCreate(email);

        if (screenshot != null && !screenshot.isEmpty()) {

            if (shop.getUpiScreenshotPath() != null) {
                fileStorageService.delete(shop.getUpiScreenshotPath());
            }

            shop.setUpiScreenshotPath(
                    fileStorageService.store(screenshot, "upi-screenshots",shop.getPublicCode())
            );
        }

        shop.setStatus(ShopStatus.UPI_VERIFICATION_PENDING);
        shopRepository.save(shop);

        user.setOnboarded(true);
        userRepository.save(user);

        List<Admin> admins = adminRepo.findAll();
        String displayName =
                shop.getPublicCode() != null && !shop.getPublicCode().isBlank()
                        ? shop.getPublicCode()
                        : shop.getShopName();

        for (Admin admin : admins) {
            adminNotificationService.createNotification(
                    admin,
                    "Complete the OP & UPI Verification Submitted",
                    "Shop '" + displayName + "' submitted UPI verification proof.",
                    NotificationSourceType.SHOP_OWNER
            );
        }
    }

    // Activate shop & generate QR code
//    @Override
//    public void activateShop(Long shopId) {
//        Shop shop = shopRepository.findById(shopId)
//                .orElseThrow(() -> new RuntimeException("Shop not found"));
//
//        shop.setUpiVerified(true);
//        subscriptionService.createTrialSubscription(shop);
//        shop.setStatus(ShopStatus.APPROVE);
//        shopRepository.save(shop);
//        generateQrCodeForShop(shop);
//    }

    @Override
    public void activateShop(Shop shop) throws IOException {

        // SAFETY: prevent re-activation
        if (shop.getStatus() == ShopStatus.APPROVE) {
            return;
        }
        shop.setUpiVerified(true);
        shop.setStatus(ShopStatus.APPROVE);

        subscriptionService.createTrialSubscription(shop);
        generateQrCodeForShop(shop);
        // ✅ Welcome + Trial Subscription Email smtp
        emailNotificationService.sendMail(
                shop.getOwnerEmail(),
                " Welcome! Your Shop & Trial Subscription Are Active",
                "Dear " + shop.getShopName() + ",\n\n" +
                        "Congratulations! \n\n" +
                        "Your shop \"" + shop.getShopName() + "\" has been successfully verified and activated.\n\n" +
                        "Trial Subscription Activated\n" +
                        "You have been enrolled in a FREE trial subscription, giving you full access to all premium features.\n\n" +
                        "During this trial, you can:\n" +
                        "• Accept and manage customer orders\n" +
                        "• Add and update products\n" +
                        "• Track payments and settlements\n" +
                        "• Experience all premium tools\n\n" +
                        "We recommend exploring the dashboard to make the most of your trial period.\n\n" +
                        "If you have any questions or need support, our team is always here to help.\n\n" +
                        "Welcome aboard and wish you great success! \n\n" +
                        "Best Regards,\n" +
                        "TheAdvue Qshopn Team"
        );

//        emailService.sendOwnerWelcome(
//                shop.getOwnerEmail(),
//                shop.getShopName()
//        );

        List<Admin> admins = adminRepo.findAll();

        for (Admin admin : admins) {
            adminNotificationService.createNotification(
                    admin,
                    "A Shop Has Been Activated & Trial Subscription Created",
                    "Shop '" + shop.getPublicCode() + "' has been activated and trial subscription created.",
                    NotificationSourceType.SHOP_OWNER
            );
        }

        shopRepository.save(shop);
    }

    // Generate QR code for shop
    @Override
    public void generateQrCodeForShop(Shop shop) {

        // 1. Ensure publicCode exists (QR MUST depend on this)
        if (shop.getPublicCode() == null || shop.getPublicCode().isEmpty()) {
            shop.setPublicCode(generatePublicCode());
        }

        // 2. Optional: generate slug (SEO only)
        if (shop.getSlug() == null || shop.getSlug().isEmpty()) {
            shop.setSlug(
                    shop.getShopName()
                            .toLowerCase()
                            .replaceAll("[^a-z0-9]+", "-")
                            .replaceAll("(^-|-$)", "")
            );
        }

        // 3. QR URL → ALWAYS publicCode
        String shopUrl = "https://" + shop.getPublicCode().toLowerCase() + ".qshopn.com";

        // 4. Generate QR
        byte[] qrImage = QrCodeGenerator.generateQRCode(shopUrl, 300, 300);
        shop.setQrCode(qrImage);

        // 5. Save
        shopRepository.save(shop);
    }

    @Override
    public Map<String, Object> getFullShopOnboardingDetails(String shopCode) {
        Shop shop = shopRepository.findByPublicCode(shopCode).orElse(null);
        if (shop == null) return null;

        Map<String, Object> result = new HashMap<>();
        result.put("shopId", shop.getId());
        result.put("public_code",shop.getPublicCode());
        result.put("ownerEmail", shop.getOwnerEmail());
        result.put("ownerName", shop.getOwner().getFullName());

        result.put("shopName", shop.getShopName());
        result.put("category", shop.getCategory());
        result.put("mobileNumber", shop.getWhatsappNumber());
        result.put("slug", shop.getSlug());
        result.put("fullAddress", shop.getFullAddress());
        result.put("city", shop.getCity());
        result.put("geoLocation", shop.getGpsLocation());
        result.put("createdAt", shop.getCreatedAt());
        result.put("pinCode", shop.getPincode());
        result.put("logoUrl", shop.getLogoPath());
        result.put("shortBio", shop.getShortBio());
        result.put("instagramId", shop.getInstagramId());
        result.put("shopPhotoUrl", shop.getShopPhotoPath());
        result.put("upiVerified", shop.getUpiVerified());
        result.put("upiId",shop.getUpiId());
        result.put("upiScreenshotUrl", shop.getUpiScreenshotPath());
        result.put("status", shop.getStatus());

        return result;
    }
}
