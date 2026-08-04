package com.synix.uwait_backend.service;

import com.synix.uwait_backend.config.PlanConfig;
import com.synix.uwait_backend.dto.adminanalytics.*;
import com.synix.uwait_backend.dto.admindashboard.*;
import com.synix.uwait_backend.dto.adminshop.AdminOrderListResponse;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.model.subscription.PlanType;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import com.synix.uwait_backend.model.subscription.SubscriptionStatus;
import com.synix.uwait_backend.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AdminServiceImpl implements AdminService{

    private final ShopRepository shopRepo;
    private final OrderRepository orderRepo;
    private final NotificationRepository notificationRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final PlanConfig planConfig;
    private final SubscriptionRequestRepository subscriptionRequestRepo;


    @Override
    public void sendNotificationToShop(String shopId, String title, String message) {
        Shop shop = shopRepo.findByPublicCode(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        Notification notification = Notification.create(shop, title, message, "ADMIN");
        notificationRepo.save(notification);

    }

    public AdminOrderListResponse getOrderByIdForAdmin(Long orderId) {

        Order o = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return new AdminOrderListResponse(
                o.getId(),

                // Shop + Owner
                o.getShop().getPublicCode(),
                o.getShop().getShopName(),
                o.getShop().getOwner().getFullName(),
                o.getShop().getWhatsappNumber(),

                // Customer
                o.getCustomerName(),
                o.getCustomerPhone(),

                // Order
                o.getTotalAmount(),
                o.getPaymentMode().name(),
                o.getStatus().name(),

                o.getCreatedAt(),
                o.getCompletedAt()
        );
    }


    @Override
    public void sendNotificationToAllShops(String title, String message) {
        List<Shop> shops = shopRepo.findAll();

        for (Shop shop : shops) {
            // Use the factory method instead of builder
            Notification notification = Notification.create(shop, title, message, "ADMIN");
            notificationRepo.save(notification);
        }
    }


    @Override
    public Map<String, Object> analytics() {

        return Map.of(
                "totalShops", shopRepo.count(),
                "verifiedShops", shopRepo.countByUpiVerifiedTrue(),
                "activeShops", shopRepo.countByActiveTrue(),
                "totalOrders", orderRepo.count()
        );
    }

    public List<AdminOrderListResponse> getAllOrdersForAdmin() {

        return orderRepo.findAllForAdmin()
                .stream()
                .map(o -> new AdminOrderListResponse(
                        o.getId(),

                        o.getShop().getPublicCode(),
                        o.getShop().getShopName(),

                        o.getShop().getOwner().getFullName(),
                        o.getShop().getWhatsappNumber(),

                        o.getCustomerName(),
                        o.getCustomerPhone(),

                        o.getTotalAmount(),
                        o.getPaymentMode().name(),
                        o.getStatus().name(),

                        o.getCreatedAt(),
                        o.getCompletedAt()
                ))
                .toList();
    }

      /* =====================================================
       ROW 1 — SHOP SUMMARY
       ===================================================== */

    public ShopSummaryResponse getShopSummary() {

        long totalShops = shopRepo.count();
        long subscriptionShops =
                subscriptionRepo.countDistinctByStatusIn(
                        List.of(SubscriptionStatus.ACTIVE, SubscriptionStatus.TRIAL)
                );
        long freeShops = shopRepo.countFreeShops();
        long verifiedShops = shopRepo.countByStatus(ShopStatus.APPROVE);
        long activeShops = shopRepo.countByActiveTrue();
        long inactiveShops = shopRepo.countByActiveFalse();

        return new ShopSummaryResponse(
                totalShops,
                subscriptionShops,
                freeShops,
                verifiedShops,
                activeShops,
                inactiveShops
        );
    }

    /* =====================================================
       ROW 1 — BEST SELLERS
       ===================================================== */

    public List<BestSellerShopResponse> getBestSellerShops(int limit) {

        return shopRepo.findBestSellerShops()
                .stream()
                .limit(limit)
                .map(shop -> {
                    long orders =
                            orderRepo.countByStatusAndShop(
                                    OrderStatus.COMPLETED, shop
                            );

                    BigDecimal revenue =
                            orderRepo.sumRevenueByShop(shop);

                    return new BestSellerShopResponse(
                            shop.getPublicCode(),
                            shop.getShopName(),
                            orders,
                            revenue != null ? revenue : BigDecimal.ZERO
                    );
                })
                .toList();
    }

    /* =====================================================
       ROW 2 — ORDER SUMMARY
       ===================================================== */

    public OrderSummaryResponse getOrderSummary() {

        long totalOrders = orderRepo.count();
        long completedOrders = orderRepo.countByStatus(OrderStatus.COMPLETED);
        long inProgressOrders = orderRepo.countByStatusNot(OrderStatus.COMPLETED);
        long cancelledOrders = orderRepo.countByStatus(OrderStatus.CANCELLED);

        return new OrderSummaryResponse(
                totalOrders,
                completedOrders,
                inProgressOrders,
                cancelledOrders
        );
    }


    /* =====================================================
       ROW 2 — TODAY ORDERS
       ===================================================== */

    public TodayOrderResponse getTodayOrders() {

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long totalToday = orderRepo.countByCreatedAtAfter(todayStart);

        List<TodayOrderResponse.ShopOrderCount> shopWise =
                orderRepo.countTodayOrdersByShop(todayStart)
                        .stream()
                        .map(r -> new TodayOrderResponse.ShopOrderCount(
                                (String) r[0],
                                (Long) r[1]
                        ))
                        .toList();

        List<TodayOrderResponse.PaymentModeCount> paymentWise =
                orderRepo.countTodayOrdersByPaymentMode(todayStart)
                        .stream()
                        .map(r -> new TodayOrderResponse.PaymentModeCount(
                                r[0].toString(),
                                (Long) r[1]
                        ))
                        .toList();

        return new TodayOrderResponse(
                totalToday,
                shopWise,
                paymentWise
        );
    }

    /* =====================================================
       ROW 2 — NON ACCOUNTABLE ORDERS
       ===================================================== */

    public NonAccountableOrderResponse getNonAccountableOrders() {

        long count =
                orderRepo.countByStatusIn(
                        List.of(
                                OrderStatus.CANCELLED,
                                OrderStatus.REFUNDED,
                                OrderStatus.RETURNED
                        )
                );

        return new NonAccountableOrderResponse(
                count, // cancelled
                count, // refunded
                count  // returned
        );
    }

    /* =====================================================
       ROW 3 — PENDING VERIFICATION
       ===================================================== */

    public PendingVerificationResponse getPendingVerification() {

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();

        long newToday =
                shopRepo.countByStatusAndCreatedAtAfter(
                        ShopStatus.UPI_VERIFICATION_PENDING, todayStart
                );

        long totalPending =
                shopRepo.countByStatus(ShopStatus.UPI_VERIFICATION_PENDING);

        return new PendingVerificationResponse(
                newToday,
                totalPending
        );
    }

    /* =====================================================
       ROW 3 — SUSPENDED SHOPS
       ===================================================== */

    public long getSuspendedShopCount() {
        return shopRepo.countByStatus(ShopStatus.SUSPENSE);
    }

    /* =====================================================
       ROW 3 — EXPIRING SUBSCRIPTIONS
       ===================================================== */

    public List<ExpiringSubscriptionResponse> getExpiringSubscriptions(int days) {

        LocalDate limitDate = LocalDate.now().plusDays(days);

        return subscriptionRepo.findExpiringSubscriptions(limitDate)
                .stream()
                .map(sub -> new ExpiringSubscriptionResponse(
                        sub.getShop().getPublicCode(),
                        sub.getShop().getShopName(),
                        sub.getExpiryDate()
                ))
                .toList();
    }

    /* =====================================================
       ROW 4 — FREE TIER ANALYTICS (GRAPH)
       ===================================================== */

    public List<GraphPointResponse> getFreeTierAnalytics() {

        LocalDateTime start =
                LocalDate.now().minusDays(30).atStartOfDay();

        return orderRepo.freeTierOrdersAndRevenue(start)
                .stream()
                .map(r -> new GraphPointResponse(
                        ((java.sql.Date) r[0]).toLocalDate().atStartOfDay(),
                        ((Number) r[1]).longValue(),
                        (BigDecimal) r[2]
                ))
                .toList();
    }


    /* =====================================================
       ROW 4 — SUBSCRIPTION TIER ANALYTICS (GRAPH)
       ===================================================== */

    public List<GraphPointResponse> getSubscriptionTierAnalytics() {

        LocalDateTime start =
                LocalDate.now().minusDays(30).atStartOfDay();

        return orderRepo.premiumTierOrdersAndRevenue(start)
                .stream()
                .map(r -> new GraphPointResponse(
                        ((java.sql.Date) r[0]).toLocalDate().atStartOfDay(),
                        ((Number) r[1]).longValue(),
                        (BigDecimal) r[2]
                ))
                .toList();
    }

    @Override
    public ShopSummaryAnalyticsResponse getShopSummary(String shopCode) {

        Shop shop = shopRepo.findByPublicCode(shopCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        BigDecimal totalOrders = BigDecimal.valueOf(orderRepo.countByShop(shop));

        BigDecimal completedOrders = BigDecimal.valueOf(orderRepo.countByShopAndStatus(
                shop, OrderStatus.COMPLETED
        ));

        BigDecimal cancelledOrders = BigDecimal.valueOf(orderRepo.countByShopAndStatus(
                shop, OrderStatus.CANCELLED
        ));

        BigDecimal totalRevenue = orderRepo.sumRevenueByShopAndStatus(
                shop, OrderStatus.COMPLETED
        );



        BigDecimal upiOrders = BigDecimal.valueOf(orderRepo.countByShopAndPaymentMode(
                shop, PaymentMode.UPI
        ));

        BigDecimal refundedOrders = BigDecimal.valueOf(orderRepo.countByShopAndStatus(
                shop, OrderStatus.REFUNDED
        ));

        BigDecimal codOrders = BigDecimal.valueOf(orderRepo.countByShopAndPaymentMode(
                shop, PaymentMode.COD
        ));

        return new ShopSummaryAnalyticsResponse(
                totalOrders,
                completedOrders,
                cancelledOrders,
                refundedOrders != null ? refundedOrders : BigDecimal.ZERO,
                totalRevenue != null ? totalRevenue : BigDecimal.ZERO,
                upiOrders,
                codOrders
        );
    }



    // ---------------- ROW 1 ----------------

    @Override
    public RevenueByPlanResponse getRevenueByPlan() {

        List<Object[]> results = subscriptionRequestRepo.getRevenueGroupedByPlan();

        Map<SubscriptionPlan, PlanRevenue> revenueMap = new HashMap<>();

        for (Object[] row : results) {
            SubscriptionPlan plan = (SubscriptionPlan) row[0];
            Long count = (Long) row[1];
            Double totalAmount = (Double) row[2];

            revenueMap.put(plan,
                    new PlanRevenue(totalAmount.longValue(), count));
        }

        PlanRevenue free = revenueMap.getOrDefault(SubscriptionPlan.FREE_TRIAL,
                new PlanRevenue(0L, 0L));

        PlanRevenue retail = revenueMap.getOrDefault(SubscriptionPlan.RETAIL,
                new PlanRevenue(0L, 0L));

        PlanRevenue growth = revenueMap.getOrDefault(SubscriptionPlan.GROWTH,
                new PlanRevenue(0L, 0L));

        PlanRevenue business = revenueMap.getOrDefault(SubscriptionPlan.BUSINESS,
                new PlanRevenue(0L, 0L));

        long total = free.totalRevenue()
                + retail.totalRevenue()
                + growth.totalRevenue()
                + business.totalRevenue();

        return new RevenueByPlanResponse(free, retail, growth, business, total);
    }
    // ---------------- ROW 2 ----------------

    public List<OrderTrendResponse> getOrdersTrend(int days) {

        LocalDateTime start = LocalDate.now()
                .minusDays(days)
                .atStartOfDay();

        return orderRepo.fetchOrdersTrend(start)
                .stream()
                .map(r -> new OrderTrendResponse(
                        ((Date) r[0]).toLocalDate(),   // date
                        ((Number) r[1]).longValue(),   // totalOrders
                        ((Number) r[2]).longValue(),   // freePlanOrders
                        ((Number) r[3]).longValue()    // paidPlanOrders
                ))
                .toList();
    }


    public PaymentModeResponse getPaymentModes() {

        long upi = 0, cod = 0, card = 0;

        for (Object[] r : orderRepo.fetchPaymentModeSplit()) {
            PaymentMode mode = (PaymentMode) r[0];
            long count = (Long) r[1];

            switch (mode) {
                case UPI -> upi = count;
                case COD -> cod = count;
            }
        }

        return new PaymentModeResponse(upi, cod, card);
    }

    // ---------------- SHOP VIEW ----------------

    public List<ShopOrderTrendResponse> getShopOrderTrend(String shopId, int days) {
        Shop shop = shopRepo.findByPublicCode(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        LocalDateTime start = LocalDate.now().minusDays(days).atStartOfDay();

        return orderRepo.fetchShopOrderTrend(shop.getId(), start)
                .stream()
                .map(r -> new ShopOrderTrendResponse(
                        ((Date) r[0]).toLocalDate(),
                        (Long) r[1],
                        (Long) r[2],
                        (Long) r[3]
                ))
                .toList();
    }

    public PaymentModeResponse getShopPaymentModes(String shopId) {
        Shop shop = shopRepo.findByPublicCode(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        long upi = 0, cod = 0;

        for (Object[] r : orderRepo.fetchShopPaymentSplit(shop.getId())) {
            PaymentMode mode = (PaymentMode) r[0];
            long count = (Long) r[1];

            if (mode == PaymentMode.UPI) upi = count;
            if (mode == PaymentMode.COD) cod = count;
        }

        return new PaymentModeResponse(upi, cod, 0L);
    }

    public List<OrderVsRevenueResponse> getOrdersVsRevenue(String shopId) {
        Shop shop = shopRepo.findByPublicCode(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));
        return orderRepo.fetchOrdersVsRevenue(shop.getId())
                .stream()
                .map(r -> new OrderVsRevenueResponse(
                        ((Date) r[0]).toLocalDate(),
                        ((Number) r[1]).longValue(),                // count → long
                        ((BigDecimal) r[2]).longValue()            // sum → long (or keep BigDecimal)
                ))

                .toList();
    }

}
