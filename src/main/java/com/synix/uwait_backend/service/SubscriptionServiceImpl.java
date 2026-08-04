package com.synix.uwait_backend.service;

import com.razorpay.*;
import com.razorpay.Order;
import com.synix.uwait_backend.config.PlanConfig;
import com.synix.uwait_backend.dto.BillingHistoryResponse;
import com.synix.uwait_backend.dto.PaymentResponse;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import com.synix.uwait_backend.model.subscription.SubscriptionStatus;
import com.synix.uwait_backend.repository.AdminRepository;
import com.synix.uwait_backend.repository.ShopRepository;
import com.synix.uwait_backend.repository.SubscriptionRepository;
import com.synix.uwait_backend.repository.SubscriptionRequestRepository;
import com.synix.uwait_backend.service.emailsend.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class SubscriptionServiceImpl implements SubscriptionService{

    @Value("${razorpay.api.key}")
    private String apiKey;

    @Value("${razorpay.api.secret}")
    private String apiSecretKey;

    private final SubscriptionRepository subscriptionRepo;
    private final ShopRepository shopRepo;
    private final PlanConfig planConfig;
    private final NotificationService notificationService;
    private final EmailService emailService;
    private final SubscriptionRequestRepository subscriptionRequestRepo;
    private final AdminNotificationService adminNotificationService;
    private final AdminRepository adminRepo;

    private final EmailNotificationService emailNotificationService;


    /* ================= CREATE TRIAL ON SHOP ONBOARD ================= */
   @Override
    @Transactional
    public void createTrialSubscription(Shop shop) {

        if (subscriptionRepo.existsByShop(shop)) return;

        Subscription sub = Subscription.builder()
                .shop(shop)
                .plan(SubscriptionPlan.FREE_TRIAL)
                .status(SubscriptionStatus.ACTIVE)
                .startDate(LocalDate.now())
                .expiryDate(LocalDate.now().plusDays(30))
                .monthlyOrderLimit(50)
                .productLimit(20)
                .categoryLimit(2)
                .allowUpi(true)
                .allowCod(false)
                .perProductPaymentControl(false)
                .build();

        subscriptionRepo.save(sub);
    }

    @Override
    @Transactional
    public Map<String, Object> createRazorpayOrder( Shop shop, SubscriptionPlan plan, double amount ) throws Exception {

        RazorpayClient client = new RazorpayClient(apiKey, apiSecretKey);

        JSONObject options = new JSONObject();
        options.put("amount", (int) (amount * 100)); // paise
        options.put("currency", "INR");
        options.put("receipt", "sub_" + shop.getId());

        Order order = client.orders.create(options);

        SubscriptionRequest req = SubscriptionRequest.builder()
                .shop(shop)
                .requestedPlan(plan)
                .amount(amount)
                .razorpayOrderId(order.get("id"))
                .status(RequestStatus.PENDING)
                .requestedAt(LocalDateTime.now())
                .build();

        subscriptionRequestRepo.save(req);

        return Map.of(
                "orderId", order.get("id"),
                "amount", amount,
                "key", apiKey
        );
    }

    @Override
    public PaymentResponse createSubscriptionPaymentLink(
            Shop shop,
            SubscriptionPlan plan,
            Double amount
    ) throws RazorpayException {

        Double finalAmount = amount * 100; // paise

        RazorpayClient razorpay =
                new RazorpayClient(apiKey, apiSecretKey);

        JSONObject request = new JSONObject();
        request.put("amount", finalAmount);
        request.put("currency", "INR");

        JSONObject customer = new JSONObject();
        customer.put("name", shop.getShopName());
        customer.put("email", shop.getOwnerEmail());

        request.put("customer", customer);
        request.put("callback_url",
                "http://qshopn.com/owner/subscription/callback");
//        request.put("callback_url",
//                "http://localhost:5173/owner/subscription/callback");


        PaymentLink link = razorpay.paymentLink.create(request);

        SubscriptionRequest order =
                subscriptionRequestRepo.save(
                        SubscriptionRequest.builder()
                                .shop(shop)
                                .requestedPlan(plan)
                                .amount(amount)
                                .status(RequestStatus.PENDING)
                                .razorpayOrderId(link.get("id"))
                                .build()
                );

        PaymentResponse res = new PaymentResponse();
        res.setPayment_url(link.get("short_url"));
        return res;
    }

    @Override
    @Transactional
    public void verifyAndActivate(
            String ownerEmail,
            String razorpayOrderId,
            String paymentId
    ) throws RazorpayException {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow();

        SubscriptionRequest order =
                subscriptionRequestRepo
                        .findByRazorpayOrderId(razorpayOrderId)
                        .orElseThrow();

        RazorpayClient razorpay =
                new RazorpayClient(apiKey, apiSecretKey);

        Payment payment = razorpay.payments.fetch(paymentId);

        if ("captured".equals(payment.get("status"))) {

            order.setStatus(RequestStatus.SUCCESS);
            order.setRazorpayPaymentId(paymentId);

            Subscription sub =
                    subscriptionRepo.findByShop(shop).orElseThrow();

            planConfig.applyPlanConfig(sub, order.getRequestedPlan());
            sub.setPlan(order.getRequestedPlan());
            sub.setStatus(SubscriptionStatus.ACTIVE);
            sub.setStartDate(LocalDate.now());
            sub.setExpiryDate(
                    calculateExpiry(order.getRequestedPlan())
            );


            subscriptionRepo.save(sub);
            subscriptionRequestRepo.save(order);

            notificationService.createNotification(
                    shop,
                    "Subscription Activated",
                    "Your " + order.getRequestedPlan() + " plan is active"
            );

            List<Admin> admins = adminRepo.findAll();

            for (Admin admin : admins) {
                adminNotificationService.createNotification(
                        admin,
                        "Subscription Activated",
                        "Shop '" + shop.getPublicCode() + "' activated " + order.getRequestedPlan() + " plan.",
                        NotificationSourceType.SHOP_OWNER
                );
            }

            return;
        }

        order.setStatus(RequestStatus.FAILED);
        List<Admin> admins = adminRepo.findAll();

        for (Admin admin : admins) {
            adminNotificationService.createNotification(
                    admin,
                    "Subscription Failed",
                    "Shop '" + shop.getPublicCode() + "' failed to activate " + order.getRequestedPlan() + " plan.",
                    NotificationSourceType.SHOP_OWNER
            );
        }
        subscriptionRequestRepo.save(order);

        throw new RuntimeException("Payment verification failed");
    }


    /* ================= ADMIN OVERRIDE ================= */
    @Override
    @Transactional
    public void extendTrial(String shopCode, int days) {

        Shop shop = shopRepo.findByPublicCode(shopCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Subscription sub = subscriptionRepo.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        sub.setExpiryDate(sub.getExpiryDate().plusDays(days));
        sub.setStatus(SubscriptionStatus.TRIAL);

        subscriptionRepo.save(sub);
    }

    @Override
    @Transactional
    public void activate(String shopCode) {

        Shop shop = shopRepo.findByPublicCode(shopCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Subscription sub = subscriptionRepo.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        sub.setStatus(SubscriptionStatus.ACTIVE);
        subscriptionRepo.save(sub);
    }

    @Override
    public List<BillingHistoryResponse> getBillingHistory(String ownerEmail) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        List<SubscriptionRequest> requests =
                subscriptionRequestRepo.findByShopOrderByRequestedAtDesc(shop);

        return requests.stream()
                .map(r -> new BillingHistoryResponse(
                        generateInvoiceNo(r),
                        r.getRequestedAt(),
                        r.getRequestedPlan().name(),
                        r.getAmount(),
                        "RAZORPAY",
                        r.getStatus()
                ))
                .toList();
    }



    private String generateInvoiceNo(SubscriptionRequest req) {

        int year = req.getRequestedAt().getYear();
        long id = req.getId();

        return "INV-" + year + "-" + String.format("%04d", id);
    }


    /* ================= TRIAL REMINDERS ================= */
    @Scheduled(cron = "0 0 9 * * ?")
    public void handleTrialReminders() throws IOException {

        List<Subscription> trials =
                subscriptionRepo.findByStatus(SubscriptionStatus.TRIAL);

        for (Subscription sub : trials) {

            long daysLeft = ChronoUnit.DAYS.between(
                    LocalDate.now(),
                    sub.getExpiryDate()
            );

            if (daysLeft == 7) {
//                emailService.sendSubscriptionAlert(
//                        sub.getShop().getOwnerEmail(),
//                        sub.getShop().getOwner().getFullName(),
//                        sub.getShop().getShopName(),
//                        7
//                );

                //smtp email service
                emailNotificationService.sendMail(
                        sub.getShop().getOwnerEmail(),
                        "Trial ending soon",
                        "Your trial ends in 7 days"
                );

            }

            if (daysLeft == 2) {
                notificationService.createNotification(
                        sub.getShop(),
                        "Trial ending soon",
                        "Your trial ends in 2 days"
                );
//                emailService.sendSubscriptionAlert(
//                        sub.getShop().getOwnerEmail(),
//                        sub.getShop().getOwner().getFullName(),
//                        sub.getShop().getShopName(),
//                        2
//                );

                //smtp email service
                emailNotificationService.sendMail(
                        sub.getShop().getOwnerEmail(),
                        "Trial ending soon",
                        "Your trial ends in 2 days"
                );
            }

            if (daysLeft < 0) {
                sub.setStatus(SubscriptionStatus.EXPIRED);
//                emailService.sendSubscriptionAlert(
//                        sub.getShop().getOwnerEmail(),
//                        sub.getShop().getOwner().getFullName(),
//                        sub.getShop().getShopName(),
//                        0
//                );

                //smtp email service
                emailNotificationService.sendMail(
                        sub.getShop().getOwnerEmail(),
                        "Trial Expired",
                        "Your trial has expired. Please subscribe to continue using our services."
                );

                subscriptionRepo.save(sub);
                List<Admin> admins = adminRepo.findAll();

                for (Admin admin : admins) {
                    adminNotificationService.createNotification(
                            admin,
                            "Plan Expired",
                            "Shop '" + sub.getShop().getPublicCode() + "' trial has expired.",
                            NotificationSourceType.SHOP_OWNER
                    );
                }
            }
        }
    }

    /* ================= EXPIRY CALC ================= */
    private LocalDate calculateExpiry(SubscriptionPlan plan) {

        return switch (plan) {
            case RETAIL -> LocalDate.now().plusMonths(1);
            case GROWTH -> LocalDate.now().plusMonths(6);
            case BUSINESS -> LocalDate.now().plusYears(1);
            default -> LocalDate.now().plusDays(30);
        };
    }



    private String HmacSHA256(String data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey =
                    new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            mac.init(secretKey);
            byte[] raw = mac.doFinal(data.getBytes());
            return Base64.getEncoder().encodeToString(raw);
        } catch (Exception e) {
            throw new RuntimeException("Signature error");
        }
    }
}
