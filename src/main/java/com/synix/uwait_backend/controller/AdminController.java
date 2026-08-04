package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.dto.adminanalytics.*;
import com.synix.uwait_backend.dto.admindashboard.*;
import com.synix.uwait_backend.dto.adminshop.*;
import com.synix.uwait_backend.dto.ownerstatus.SubscriptionDashboardResponse;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.repository.*;
import com.synix.uwait_backend.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final ShopOnboardingService service;
    private final AdminService adminService;
    private final SubscriptionService subscriptionService;
    private final SubscriptionRequestRepository subscriptionRequestRepo;
    private final SubscriptionRepository subscriptionRepository;
    private final ShopRepository shopRepository;
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Activate a shop
    @PostMapping("/shop/activate/{publicCode}")
    public ResponseEntity<?> activateShop(@PathVariable String publicCode) throws IOException {

        Shop shop = shopRepository.findByPublicCode(publicCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        service.activateShop(shop);

        return ResponseEntity.ok(
                Map.of("message", "Shop activated successfully")
        );
    }

    // Get all clients
    @GetMapping("/shops")
    public ResponseEntity<List<AdminShopResponse>> getAllShopsForAdmin() {

        List<Shop> shops = shopRepository.findAll();

        List<AdminShopResponse> response = shops.stream().map(shop -> {

            User owner = shop.getOwner();

            AdminShopResponse dto = new AdminShopResponse();

            // Shop info
            dto.setPublicCode(shop.getPublicCode());
            dto.setShopName(shop.getShopName());
            dto.setCategory(shop.getCategory());
            dto.setStatus(shop.getStatus());
            dto.setActive(shop.isActive());
            dto.setCity(shop.getCity());
            dto.setPinCode(shop.getPincode());

            // Owner info
            dto.setOwnerName(owner.getFullName());
            dto.setOwnerEmail(owner.getEmail());
            dto.setCreatedAt(shop.getCreatedAt());
            dto.setWhatsappNumber(shop.getWhatsappNumber());

            // Subscription info
            subscriptionRepository.findFirstByShopOrderByExpiryDateDesc(shop)
                    .ifPresent(sub -> {
                        dto.setSubscriptionPlan(sub.getPlan().name());
                        dto.setSubscriptionStatus(sub.getStatus().name());
                        dto.setSubscriptionEndDate(sub.getExpiryDate());
                    });

            return dto;

        }).toList();

        return ResponseEntity.ok(response);
    }

    // Get full shop onboarding details
    @GetMapping("/shop/details/{code}")
    public ResponseEntity<?> getShopDetails(@PathVariable String code) {
        try {
            Map<String, Object> shopDetails = service.getFullShopOnboardingDetails(code);
            if (shopDetails == null) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Shop not found"));
            }
            return ResponseEntity.ok(shopDetails);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500)
                    .body(Map.of("error", "Something went wrong", "message", e.getMessage()));
        }
    }


    @GetMapping("/shops/{publicCode}/subscription/overview")
    public ResponseEntity<AdminSubscriptionOverviewResponse> getSubscriptionOverview( @PathVariable String publicCode) {

        Shop shop = shopRepository.findByPublicCode(publicCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Subscription subscription = subscriptionRepository
                .findFirstByShopOrderByExpiryDateDesc(shop)
                .orElseThrow(() -> new RuntimeException("No subscription found"));

        AdminSubscriptionOverviewResponse.AdminSubscriptionOverviewResponseBuilder builder =
                AdminSubscriptionOverviewResponse.builder()
                        .plan(subscription.getPlan().name())
                        .status(subscription.getStatus().name())
                        .startDate(subscription.getStartDate())
                        .expiryDate(subscription.getExpiryDate());

        subscriptionRequestRepo.findFirstByShopOrderByRequestedAtDesc(shop)
                .ifPresent(req -> builder
                        .amount(req.getAmount())
                        .requestStatus(req.getStatus().name())
                        .requestedAt(req.getRequestedAt())
                        .approvedAt(req.getApprovedAt())
                );

        return ResponseEntity.ok(builder.build());
    }

    @GetMapping("/shops/{publicCode}/limits")
    public ResponseEntity<AdminSubscriptionLimitResponse> getShopSubscriptionDashboard(
            @PathVariable String publicCode
    ) {

        Shop shop = shopRepository.findByPublicCode(publicCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Subscription sub = subscriptionRepository.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        // Orders used (current month)
        LocalDateTime monthStart =
                LocalDate.now().withDayOfMonth(1).atStartOfDay();

        long monthlyOrders =
                orderRepository.countByShopAndStatusAndCreatedAtBetween(
                        shop,
                        OrderStatus.COMPLETED,
                        monthStart,
                        LocalDateTime.now()
                );

        long totalOrders =
                orderRepository.countByShopAndStatus(
                        shop,
                        OrderStatus.COMPLETED
                );

        long usedProducts =
                productRepository.countByShopId(shop.getId());

        long usedCategories =
                categoryRepository.countByShop(shop);

        AdminSubscriptionLimitResponse response =
                new AdminSubscriptionLimitResponse(

                        // monthly orders
                        new AdminSubscriptionLimitResponse.Usage(
                                monthlyOrders,
                                sub.getMonthlyOrderLimit()
                        ),

                        // overall orders
                        new AdminSubscriptionLimitResponse.Usage(
                                totalOrders,
                                Long.MAX_VALUE // reference max
                        ),

                        // products
                        new AdminSubscriptionLimitResponse.Usage(
                                usedProducts,
                                sub.getProductLimit()
                        ),

                        // categories
                        new AdminSubscriptionLimitResponse.Usage(
                                usedCategories,
                                sub.getCategoryLimit()
                        )
                );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/shop/{shopCode}/billing/history")
    public ResponseEntity<List<AdminBillingHistoryResponse>> getShopBillingHistory(
            @PathVariable String shopCode
    ) {
        Shop shop = shopRepository.findByPublicCode(shopCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        List<SubscriptionRequest> requests =
                subscriptionRequestRepo.findByShopOrderByRequestedAtDesc(shop);

        List<AdminBillingHistoryResponse> response = requests.stream()
                .map(r -> new AdminBillingHistoryResponse(
                        r.getId(),
                        shop.getShopName(),
                        shop.getOwnerEmail(),
                        r.getRequestedPlan().name(),
                        r.getAmount(),
                        "RAZORPAY",
                        r.getStatus(),
                        r.getRequestedAt()
                ))
                .toList();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/shops/billing/history")
    public ResponseEntity<List<AdminBillingHistoryResponse>> getAllShopsBillingHistory() {

        // Fetch all subscription requests ordered by requestedAt desc
        List<SubscriptionRequest> requests = subscriptionRequestRepo.findAllByOrderByRequestedAtDesc();

        // Map each request to AdminBillingHistoryResponse
        List<AdminBillingHistoryResponse> response = requests.stream()
                .map(r -> {
                    Shop shop = r.getShop(); // Get shop from request
                    return new AdminBillingHistoryResponse(
                            r.getId(), // ADD THIS
                            shop.getShopName(),
                            shop.getOwnerEmail(),
                            r.getRequestedPlan().name(),
                            r.getAmount(),
                            "RAZORPAY", // payment mode, can be dynamic if needed
                            r.getStatus(),
                            r.getRequestedAt()
                    );
                })
                .toList();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/shops/billing/history/{id}")
    public ResponseEntity<?> deleteBillingHistory(@PathVariable Long id) {

        if (!subscriptionRequestRepo.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        subscriptionRequestRepo.deleteById(id);

        return ResponseEntity.ok("Billing history deleted successfully");
    }

    @DeleteMapping("/shops/billing/history")
    public ResponseEntity<?> deleteMultipleBillingHistory(@RequestBody List<Long> ids) {

        subscriptionRequestRepo.deleteAllById(ids);

        return ResponseEntity.ok("Selected billing history deleted successfully");
    }

    @PatchMapping("/{shopCode}/status")
    public ResponseEntity<String> changeShopStatus(
            @PathVariable String shopCode,
            @RequestParam boolean active
    ) {
        Shop shop = shopRepository.findByPublicCode(shopCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Set boolean flag
        shop.setActive(active);

        // Set enum status accordingly
        shop.setStatus(active ? ShopStatus.APPROVE : ShopStatus.SUSPENSE);

        shopRepository.save(shop);

        String statusText = active ? "ACTIVE" : "SUSPENSE";
        return ResponseEntity.ok("Shop " + shopCode + " is now " + statusText);
    }

    @GetMapping("/orders")
    public ResponseEntity<List<AdminOrderListResponse>> getAllOrders() {
        return ResponseEntity.ok(
                adminService.getAllOrdersForAdmin()
        );
    }

    @GetMapping("/orders/{orderId}")
    public ResponseEntity<AdminOrderListResponse> getOrderById(
            @PathVariable Long orderId
    ) {
        return ResponseEntity.ok(
                adminService.getOrderByIdForAdmin(orderId)
        );
    }


    @GetMapping("/analytics")
    public Map<String, Object> analytics() {
        return adminService.analytics();
    }

    // Send to one shop
    @PostMapping("/shop/{shopId}")
    public ResponseEntity<?> notifyShop(
            @PathVariable String shopId,
            @RequestParam String title,
            @RequestParam String message
    ) {
        adminService.sendNotificationToShop(shopId, title, message);
        return ResponseEntity.ok(Map.of("message", "Notification sent to shop"));
    }

    // Send to all shops
    @PostMapping("/all-shops")
    public ResponseEntity<?> notifyAllShops(
            @RequestParam String title,
            @RequestParam String message
    ) {
        adminService.sendNotificationToAllShops(title, message);
        return ResponseEntity.ok(Map.of("message", "Notification sent to all shops"));
    }

    @PatchMapping("/subscription/{shopId}/extend-trial")
    public ResponseEntity<?> extendTrial(
            @PathVariable String shopId,
            @RequestParam int days
    ) {
        subscriptionService.extendTrial(shopId, days);
        return ResponseEntity.ok(Map.of("message","Trial extended"));
    }

    @PatchMapping("/subscription/{shopId}/activate")
    public ResponseEntity<?> activate(@PathVariable String shopId) {
        subscriptionService.activate(shopId);
        return ResponseEntity.ok(Map.of("message","Shop activated"));
    }

    @GetMapping("/subscription-requests")
    public List<SubscriptionRequest> listPendingRequests() {
        return subscriptionRequestRepo.findByStatus(RequestStatus.PENDING);
    }

    @GetMapping("/subscription-requests/filter")
    public List<SubscriptionRequest> listByStatus(
            @RequestParam RequestStatus status
    ) {
        return subscriptionRequestRepo.findByStatus(status);
    }

    //dashboard endpoints
// ROW 1 — SHOPS
    @GetMapping("/shops/summary")
    public ResponseEntity<ShopSummaryResponse> shopSummary() {
        return ResponseEntity.ok(adminService.getShopSummary());
    }

    @GetMapping("/shops/best-sellers")
    public ResponseEntity<List<BestSellerShopResponse>> bestSellers(
            @RequestParam(defaultValue = "4") int limit
    ) {
        return ResponseEntity.ok(adminService.getBestSellerShops(limit));
    }

    // ROW 2 — ORDERS
    @GetMapping("/orders/summary")
    public ResponseEntity<OrderSummaryResponse> orderSummary() {
        return ResponseEntity.ok(adminService.getOrderSummary());
    }

    @GetMapping("/orders/today")
    public ResponseEntity<TodayOrderResponse> todayOrders() {
        return ResponseEntity.ok(adminService.getTodayOrders());
    }

    @GetMapping("/orders/non-accountable")
    public ResponseEntity<NonAccountableOrderResponse> nonAccountableOrders() {
        return ResponseEntity.ok(adminService.getNonAccountableOrders());
    }

    // ROW 3 — ALERTS
    @GetMapping("/shops/pending-verification")
    public ResponseEntity<PendingVerificationResponse> pendingVerification() {
        return ResponseEntity.ok(adminService.getPendingVerification());
    }

    @GetMapping("/shops/suspended")
    public ResponseEntity<Long> suspendedShops() {
        return ResponseEntity.ok(adminService.getSuspendedShopCount());
    }

    @GetMapping("/subscriptions/expiring")
    public ResponseEntity<List<ExpiringSubscriptionResponse>> expiringSubscriptions(
            @RequestParam(defaultValue = "7") int days
    ) {
        return ResponseEntity.ok(adminService.getExpiringSubscriptions(days));
    }

    // ROW 4 — GRAPHS
    @GetMapping("/analytics/free-tier")
    public ResponseEntity<List<GraphPointResponse>> freeTierAnalytics() {
        return ResponseEntity.ok(adminService.getFreeTierAnalytics());
    }

    @GetMapping("/analytics/subscription-tier")
    public ResponseEntity<List<GraphPointResponse>> subscriptionTierAnalytics() {
        return ResponseEntity.ok(adminService.getSubscriptionTierAnalytics());
    }
/////////////////////////////////

    // ROW 1 — Revenue by Plan
    @GetMapping("/revenue/by-plan")
    public RevenueByPlanResponse getRevenueByPlan() {
        return adminService.getRevenueByPlan();
    }

    // ROW 2 — Orders Trend (3 line)
    @GetMapping("/orders/trend")
    public List<OrderTrendResponse> getOrdersTrend(
            @RequestParam(defaultValue = "7") int days
    ) {
        return adminService.getOrdersTrend(days);
    }

    // ROW 2 — Payment Mode Pie
    @GetMapping("/payments/mode")
    public PaymentModeResponse getPaymentModes() {
        return adminService.getPaymentModes();
    }

    // SHOP — Summary Cards
    @GetMapping("/shop/{shopId}/summary")
    public ShopSummaryAnalyticsResponse getShopSummary(@PathVariable String shopId) {
        return adminService.getShopSummary(shopId);
    }

    // SHOP — Order Health Line Chart
    @GetMapping("/shop/{shopId}/orders/trend")
    public List<ShopOrderTrendResponse> getShopOrderTrend(
            @PathVariable String shopId,
            @RequestParam(defaultValue = "7") int days
    ) {
        return adminService.getShopOrderTrend(shopId, days);
    }

    // SHOP — COD vs UPI Pie
    @GetMapping("/shop/{shopId}/payments/mode")
    public PaymentModeResponse getShopPaymentModes(@PathVariable String shopId) {
        return adminService.getShopPaymentModes(shopId);
    }

    // SHOP — Orders vs Revenue Bar Chart
    @GetMapping("/shop/{shopId}/orders-vs-revenue")
    public List<OrderVsRevenueResponse> getOrdersVsRevenue(
            @PathVariable String shopId
    ) {
        return adminService.getOrdersVsRevenue(shopId);
    }

}
