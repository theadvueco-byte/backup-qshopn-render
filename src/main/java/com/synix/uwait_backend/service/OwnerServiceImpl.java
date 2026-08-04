package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.DeleteAccountConfirmRequest;
import com.synix.uwait_backend.dto.ownerearnings.*;
import com.synix.uwait_backend.dto.ownerorder.OwnerOrderSummaryResponse;
import com.synix.uwait_backend.dto.ownerstatus.*;
import com.synix.uwait_backend.exception.UpgradeRequiredException;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.model.subscription.SubscriptionFeature;
import com.synix.uwait_backend.repository.*;
import com.synix.uwait_backend.service.emailsend.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OwnerServiceImpl implements OwnerService {

    private final OrderRepository orderRepo;
    private final ShopRepository shopRepo;
    private final ProductRepository productRepo;
    private final OrderItemRepository orderItemRepo;
    private final SubscriptionRepository subscriptionRepo;
    private final CategoryRepository categoryRepo;
    private final UserRepository userRepository;
    private final EmailService emailService;
    private final EmailOtpRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final StockHistoryRepository stockHistoryRepo;
    private final FeatureGuardService featureGuard;

    //smtp email service for sending account deletion confirmation emails with otp
    private final EmailNotificationService emailNotificationService;


    @Override
    public Map<String, Object> getEarnings(String ownerEmail) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow();

        long totalOrders = orderRepo.countByShop(shop);
        long paidOrders = orderRepo.countByShopAndPaymentMode(shop, PaymentMode.UPI);
        long codOrders = orderRepo.countByShopAndPaymentMode(shop, PaymentMode.COD);
        double totalValue = orderRepo.sumTotalAmount(shop);

        return Map.of(
                "totalOrders", totalOrders,
                "paidOrders", paidOrders,
                "codOrders", codOrders,
                "totalValue", totalValue
        );
    }

    @Override
    public void updateShopStatus(String email, boolean active) {
        Shop shop = shopRepo.findByOwner_Email(email)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Only allow change if shop is APPROVED
        if (shop.getStatus() != ShopStatus.APPROVE) {
            throw new RuntimeException(
                    "Cannot change shop status. Please contact admin."
            );
        }

        shop.setActive(active);
        shopRepo.save(shop);
    }


    @Override
    public Shop getShopByIdAndOwner(String shopId, String ownerEmail) {
        return shopRepo.findByPublicCodeAndOwnerEmail(shopId, ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found or access denied"));
    }

    // 1. KPI Cards
    public EarningsKpiResponse getKpis(String ownerEmail) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow();

        BigDecimal total =
                orderRepo.sumCompletedOrdersAmount(shop);

        BigDecimal monthly =
                orderRepo.sumCompletedOrdersAmountByMonth(
                        shop,
                        LocalDate.now().getMonthValue()
                );

        BigDecimal pending =
                orderRepo.sumPendingPayments(shop);

        long totalOrders =
                orderRepo.countByShop(shop);

        BigDecimal avg =
                totalOrders > 0
                        ? total.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP)
                        : BigDecimal.ZERO;

        return new EarningsKpiResponse(
                total,
                monthly,
                pending,
                totalOrders,
                avg
        );
    }


    // 2. Revenue Trend
    public List<RevenueTrendResponse> getRevenueTrend(
            String ownerEmail,
            String dateRange
    ) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // ---- FEATURE CHECK ----
        switch (dateRange.toLowerCase()) {
            case "week" -> featureGuard.checkFeature(
                    shop, SubscriptionFeature.REVENUE_GRAPH_WEEK
            );
            case "month" -> featureGuard.checkFeature(
                    shop, SubscriptionFeature.REVENUE_GRAPH_MONTH
            );
            case "year" -> featureGuard.checkFeature(
                    shop, SubscriptionFeature.REVENUE_GRAPH_YEAR
            );
            default -> throw new RuntimeException("Invalid dateRange");
        }

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start;

        switch (dateRange.toLowerCase()) {
            case "week" -> start = end.minusDays(7);
            case "month" -> start = end.minusMonths(1);
            case "year" -> start = end.minusYears(1);
            default -> throw new RuntimeException("Invalid dateRange");
        }

        return orderRepo.findRevenueTrend(shop, start, end)
                .stream()
                .map(row -> {
                    java.sql.Date sqlDate = (java.sql.Date) row[0];
                    BigDecimal revenue = (BigDecimal) row[1];

                    return new RevenueTrendResponse(
                            sqlDate.toLocalDate(),
                            revenue
                    );
                })
                .toList();
    }



    // 3. Order Status Pie
    public List<StatusCountResponse> getOrderStatusSummary(
            String ownerEmail
    ) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return orderRepo.countOrdersByStatus(shop)
                .stream()
                .map(r -> new StatusCountResponse(
                        r[0].toString(),
                        ((Number) r[1]).intValue()
                ))
                .collect(Collectors.toList());
    }


    // 4. Payment Methods
    public List<PaymentMethodResponse> getPaymentMethods(String ownerEmail) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        boolean basicAllowed =
                featureGuard.hasFeature(shop, SubscriptionFeature.PAYMENT_PIE_BASIC);

        boolean fullAllowed =
                featureGuard.hasFeature(shop, SubscriptionFeature.PAYMENT_PIE_FULL);

        if (!basicAllowed && !fullAllowed) {
            throw new UpgradeRequiredException("Upgrade required for payment analytics");
        }

        Map<String, Integer> dbData =
                orderRepo.countPaymentMethods(shop)
                        .stream()
                        .collect(Collectors.toMap(
                                r -> r[0].toString().toUpperCase(),
                                r -> ((Number) r[1]).intValue()
                        ));

        List<String> allMethods = List.of("UPI", "COD", "CARD", "WALLET");

        return allMethods.stream()
                .map(method -> {

                    boolean allowed =
                            fullAllowed ||
                                    (basicAllowed && (method.equals("UPI") || method.equals("COD")));

                    int count = allowed ? dbData.getOrDefault(method, 0) : 0;

                    return new PaymentMethodResponse(method, count, allowed);
                })
                .toList();
    }





    // 6. Orders Table
    public List<OwnerOrderSummaryResponse> getOrders(
            String ownerEmail
    ) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return orderRepo.findByShopOrderByCreatedAtDesc(shop)
                .stream()
                .map(o -> new OwnerOrderSummaryResponse(
                        o.getId(),
                        o.getCustomerName(),
                        o.getCustomerPhone(),
                        o.getPaymentMode(),
                        o.getStatus(),
                        o.getTotalAmount(),
                        o.getCreatedAt()
                ))
                .collect(Collectors.toList());
    }



    // 7. Export CSV
    public String exportOrdersToCsv( String ownerEmail  ) {

        List<OwnerOrderSummaryResponse> orders = getOrders(ownerEmail);

        StringBuilder sb = new StringBuilder();
        sb.append("OrderID,Customer,Payment,Amount,Status,CreatedAt\n");

        orders.forEach(order -> {
            sb.append(order.getOrderId()).append(",")
                    .append(order.getCustomerName()).append(",")
                    .append(order.getPaymentMode()).append(",")
                    .append(order.getTotalAmount()).append(",")
                    .append(order.getStatus()).append(",")
                    .append(order.getCreatedAt()).append("\n");
        });

        return sb.toString();
    }

@Override
public List<TopProductResponse> getTopProducts(String ownerEmail) {

    Shop shop = shopRepo.findByOwner_Email(ownerEmail)
            .orElseThrow(() -> new RuntimeException("Shop not found"));

    boolean top3Allowed =
            featureGuard.hasFeature(shop, SubscriptionFeature.TOP_SELLING_3);

    boolean top5Allowed =
            featureGuard.hasFeature(shop, SubscriptionFeature.TOP_SELLING_5);

    if (!top3Allowed && !top5Allowed) {
        throw new UpgradeRequiredException("Upgrade required to view top products");
    }

    int limit = top5Allowed ? 5 : 3;

    List<OrderItem> items =
            orderItemRepo.findByOrderShopAndOrderStatus(
                    shop,
                    OrderStatus.COMPLETED
            );

    Map<Long, ProductStats> statsMap = new HashMap<>();

    for (OrderItem item : items) {

        Long productId = item.getProduct().getId();
        BigDecimal price = item.getProduct().getSellingPrice();
        if (price == null) continue;

        String name = item.getProduct().getName();
        long qty = item.getQuantity();

        statsMap.compute(productId, (k, v) -> {
            if (v == null) {
                return new ProductStats(
                        name,
                        price,
                        qty,
                        price.multiply(BigDecimal.valueOf(qty))
                );
            }
            v.quantitySold += qty;
            v.revenue =
                    v.revenue.add(
                            price.multiply(BigDecimal.valueOf(qty))
                    );
            return v;
        });
    }

    return statsMap.values().stream()
            .sorted(Comparator.comparing(ProductStats::getRevenue).reversed())
            .limit(limit)
            .map(p -> new TopProductResponse(
                    p.productName,
                    p.productPrice,
                    p.quantitySold,
                    p.revenue
            ))
            .collect(Collectors.toList());
}


    //low stock and out of stock alert in dashboard
    public StockReportResponse getStockReport(String ownerEmail) {

        final int LOW_STOCK_THRESHOLD = 5;

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        boolean countOnly =
                featureGuard.hasFeature(shop, SubscriptionFeature.STOCK_ALERT_COUNT);

        boolean listAllowed =
                featureGuard.hasFeature(shop, SubscriptionFeature.STOCK_ALERT_LIST);

        List<Product> products = productRepo.findByShop(shop);

        List<StockProductResponse> lowStock = new ArrayList<>();
        List<StockProductResponse> outOfStock = new ArrayList<>();

        for (Product p : products) {

            if (p.getStock() == 0) {
                outOfStock.add(
                        new StockProductResponse(
                                p.getName(),
                                p.getSellingPrice(),
                                0,
                                "OUT_OF_STOCK"
                        )
                );
            }
            else if (p.getStock() <= LOW_STOCK_THRESHOLD) {
                lowStock.add(
                        new StockProductResponse(
                                p.getName(),
                                p.getSellingPrice(),
                                p.getStock(),
                                "LOW_STOCK"
                        )
                );
            }
        }

        // 🔒 COUNT ONLY (FREE)
        if (countOnly && !listAllowed) {
            return new StockReportResponse(
                    products.size(),
                    lowStock.size(),
                    outOfStock.size(),
                    null,
                    null
            );
        }

        // 🟡 RETAIL → limit list to 5
        if (listAllowed &&
                !featureGuard.hasFeature(shop, SubscriptionFeature.STOCK_ALERT_FULL)) {

            return new StockReportResponse(
                    products.size(),
                    lowStock.size(),
                    outOfStock.size(),
                    lowStock.stream().limit(5).toList(),
                    outOfStock.stream().limit(5).toList()
            );
        }

        // 🔵 GROWTH / BUSINESS → full list
        return new StockReportResponse(
                products.size(),
                lowStock.size(),
                outOfStock.size(),
                lowStock,
                outOfStock
        );
    }



    //order chart in dashbaord
    public List<OrderChartResponse> getOrdersChart(
            String ownerEmail,
            int days
    ) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // ---- FEATURE CHECK ----
        if (days <= 7 && !featureGuard.hasFeature(shop, SubscriptionFeature.ORDER_GRAPH_WEEK)) {
            throw new UpgradeRequiredException("Upgrade required for weekly order chart");
        }

        if (days <= 30 && days > 7 &&
                !featureGuard.hasFeature(shop, SubscriptionFeature.ORDER_GRAPH_MONTH)) {
            throw new UpgradeRequiredException("Upgrade required for monthly order chart");
        }

        if (days > 30 &&
                !featureGuard.hasFeature(shop, SubscriptionFeature.ORDER_GRAPH_YEAR)) {
            throw new UpgradeRequiredException("Upgrade required for yearly order chart");
        }

        LocalDateTime end = LocalDateTime.now();
        LocalDateTime start = end.minusDays(days);

        List<Object[]> rows =
                orderRepo.findDailyOrders(shop, start, end);

        return rows.stream()
                .map(r -> {
                    LocalDate date;

                    if (r[0] instanceof LocalDate ld) {
                        date = ld;
                    } else if (r[0] instanceof java.sql.Date sd) {
                        date = sd.toLocalDate();
                    } else {
                        throw new IllegalStateException("Unsupported date type: " + r[0].getClass());
                    }

                    Long count = ((Number) r[1]).longValue();
                    return new OrderChartResponse(date, count);
                })
                .toList();
    }






    //sale card in dasboard
    public SalesStatsResponse getSalesStats(String ownerEmail) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        LocalDate today = LocalDate.now();
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime yesterdayStart = today.minusDays(1).atStartOfDay();
        LocalDateTime yesterdayEnd = todayStart.minusSeconds(1);

        LocalDateTime thisMonthStart =
                today.withDayOfMonth(1).atStartOfDay();

        LocalDateTime lastMonthStart =
                today.minusMonths(1).withDayOfMonth(1).atStartOfDay();

        LocalDateTime lastMonthEnd =
                thisMonthStart.minusSeconds(1);

        // ---- FEATURE CHECKS ----
        boolean todayAllowed =
                featureGuard.hasFeature(shop, SubscriptionFeature.SALES_TODAY);

        boolean monthAllowed =
                featureGuard.hasFeature(shop, SubscriptionFeature.SALES_MONTH);

        boolean allAllowed =
                featureGuard.hasFeature(shop, SubscriptionFeature.SALES_ALL);

        return new SalesStatsResponse(
                allAllowed ? orderRepo.sumTotalSales(shop) : null,
                todayAllowed ? orderRepo.sumSalesBetween(shop, todayStart, now) : null,
                todayAllowed ? orderRepo.sumSalesBetween(shop, yesterdayStart, yesterdayEnd) : null,
                monthAllowed ? orderRepo.sumSalesBetween(shop, thisMonthStart, now) : null,
                allAllowed ? orderRepo.sumSalesBetween(shop, lastMonthStart, lastMonthEnd) : null
        );
    }


    //order card in dashboard
    public OrderStatsResponse getOrderStats(String ownerEmail) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(23, 59, 59);

        LocalDate yesterday = today.minusDays(1);
        LocalDateTime yesterdayStart = yesterday.atStartOfDay();
        LocalDateTime yesterdayEnd = yesterday.atTime(23, 59, 59);

        LocalDateTime thisMonthStart =
                today.withDayOfMonth(1).atStartOfDay();

        LocalDateTime lastMonthStart =
                today.minusMonths(1).withDayOfMonth(1).atStartOfDay();

        LocalDateTime lastMonthEnd =
                today.withDayOfMonth(1).minusDays(1).atTime(23, 59, 59);

        boolean todayAllowed =
                featureGuard.hasFeature(shop, SubscriptionFeature.ORDERS_TODAY);

        boolean monthAllowed =
                featureGuard.hasFeature(shop, SubscriptionFeature.ORDERS_MONTH);

        boolean allAllowed =
                featureGuard.hasFeature(shop, SubscriptionFeature.ORDERS_ALL);

        return new OrderStatsResponse(
                allAllowed ? orderRepo.countByShopAndStatus(shop, OrderStatus.COMPLETED) : null,
                todayAllowed ? orderRepo.countByShopAndStatusAndCreatedAtBetween(
                        shop, OrderStatus.COMPLETED, todayStart, todayEnd
                ) : null,
                todayAllowed ? orderRepo.countByShopAndStatusAndCreatedAtBetween(
                        shop, OrderStatus.COMPLETED, yesterdayStart, yesterdayEnd
                ) : null,
                monthAllowed ? orderRepo.countByShopAndStatusAndCreatedAtBetween(
                        shop, OrderStatus.COMPLETED, thisMonthStart, todayEnd
                ) : null,
                allAllowed ? orderRepo.countByShopAndStatusAndCreatedAtBetween(
                        shop, OrderStatus.COMPLETED, lastMonthStart, lastMonthEnd
                ) : null
        );
    }


    //subscription card in dashboard in usage limits
    public SubscriptionDashboardResponse getSubscriptionDashboard(String ownerEmail) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Subscription sub = subscriptionRepo.findByShop(shop)
                .orElseThrow(() -> new RuntimeException("Subscription not found"));

        boolean canViewOrderUsage =
                featureGuard.hasFeature(shop, SubscriptionFeature.SUBSCRIPTION_ORDER_USAGE);

        boolean canViewFullUsage =
                featureGuard.hasFeature(shop, SubscriptionFeature.SUBSCRIPTION_FULL_USAGE);

        LocalDateTime monthStart =
                LocalDate.now().withDayOfMonth(1).atStartOfDay();

        Long usedOrders = null;
        Long usedProducts = null;
        Long usedCategories = null;

        // 🔓 Orders usage
        if (canViewOrderUsage || canViewFullUsage) {
            usedOrders = orderRepo.countByShopAndStatusInAndCreatedAtBetween(
                    shop,
                    List.of(
                            OrderStatus.COMPLETED,
                            OrderStatus.CANCELLED,
                            OrderStatus.REFUNDED
                    ),
                    monthStart,
                    LocalDateTime.now()
            );
        }

        // 🔓 Product & category usage
        if (canViewFullUsage) {
            usedProducts = productRepo.countByShopId(shop.getId());
            usedCategories = categoryRepo.countByShop(shop);
        }

        return new SubscriptionDashboardResponse(
                sub.getPlan().name(),

                // ✅ LIMIT ALWAYS PRESENT
                new SubscriptionDashboardResponse.Usage(
                        usedOrders,
                        sub.getMonthlyOrderLimit()
                ),

                new SubscriptionDashboardResponse.Usage(
                        usedProducts,
                        sub.getProductLimit()
                ),

                new SubscriptionDashboardResponse.Usage(
                        usedCategories,
                        sub.getCategoryLimit()
                ),

                sub.getExpiryDate(),
                sub.getStatus().name()
        );
    }



    @Override
    @Transactional
    public void requestAccountDeletion(String ownerEmail) throws IOException {

        User user = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Remove old OTPs
        otpRepository.deleteByUser(user);

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        EmailOtp emailOtp = new EmailOtp();
        emailOtp.setOtp(otp);
        emailOtp.setExpiryTime(LocalDateTime.now().plusMinutes(10));
        emailOtp.setVerified(false);
        emailOtp.setUser(user);

        otpRepository.save(emailOtp);

        emailNotificationService.sendMail(
                ownerEmail,
                "Confirm Account Deletion - Qshopn",
                "Your OTP to delete your account is: " + otp +
                        "\nThis action is irreversible."
        );
       // emailService.sendOtp(user.getEmail(), otp);
    }

    @Override
    @Transactional
    public void confirmAccountDeletion(
            String ownerEmail,
            DeleteAccountConfirmRequest req
    ) {

        User user = userRepository.findByEmail(ownerEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean verified = false;

        // 🔹 PASSWORD CHECK
        if (req.getPassword() != null) {
            if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
                throw new RuntimeException("Invalid password");
            }
            verified = true;
        }

        // 🔹 OTP CHECK
        if (!verified && req.getOtp() != null) {

            EmailOtp otp = otpRepository
                    .findTopByUserOrderByExpiryTimeDesc(user)
                    .orElseThrow(() -> new RuntimeException("OTP not found"));

            if (otp.isVerified())
                throw new RuntimeException("OTP already used");

            if (otp.getExpiryTime().isBefore(LocalDateTime.now()))
                throw new RuntimeException("OTP expired");

            if (!otp.getOtp().equals(req.getOtp()))
                throw new RuntimeException("Invalid OTP");

            otp.setVerified(true);
            verified = true;
        }

        if (!verified)
            throw new RuntimeException("Password or OTP required");

        // 🔥 DELETE FLOW (SAFE ORDER)
        otpRepository.deleteByUser(user);
        shopRepo.deleteByOwnerEmail(ownerEmail);
        userRepository.delete(user);
    }


    public List<StockTrendResponse> getStockTrend(
            String ownerEmail,
            String range
    ) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        LocalDate end = LocalDate.now();
        LocalDate start = switch (range) {
            case "month" -> end.minusMonths(1);
            case "day" -> end.minusDays(1);
            default -> end.minusDays(7);
        };

        return stockHistoryRepo.findStockTrend(shop, start, end)
                .stream()
                .map(r -> new StockTrendResponse(
                        (LocalDate) r[0],
                        ((Number) r[1]).longValue()
                ))
                .toList();
    }



    public List<CategoryInventoryResponse> getCategoryInventory(
            String ownerEmail
    ) {
        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow();

        // 🔐 FEATURE CHECK
        featureGuard.checkFeature(shop, SubscriptionFeature.CATEGORY_STOCK_GRAPH);

        return productRepo.findCategoryInventory(shop)
                .stream()
                .map(r -> new CategoryInventoryResponse(
                        (String) r[0],
                        ((Number) r[1]).longValue(),
                        ((Number) r[2]).longValue()
                ))
                .toList();
    }


    public List<SalesStockResponse> getSalesVsStock(
            String ownerEmail,
            String range
    ) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow();

        // 🔐 FEATURE CHECK BASED ON RANGE
        if ("month".equalsIgnoreCase(range)) {
            featureGuard.checkFeature(shop, SubscriptionFeature.SALES_STOCK_GRAPH_MONTH);
        } else {
            featureGuard.checkFeature(shop, SubscriptionFeature.SALES_STOCK_GRAPH_WEEK);
        }

        LocalDate end = LocalDate.now();
        LocalDate start = switch (range) {
            case "month" -> end.minusMonths(1);
            case "day" -> end.minusDays(1);
            default -> end.minusDays(7);
        };

        // ✅ SALES MAP
        Map<LocalDate, Long> salesMap =
                orderItemRepo.findDailySales(
                        shop,
                        start.atStartOfDay(),
                        end.atTime(23, 59, 59)
                ).stream().collect(Collectors.toMap(
                        r -> toLocalDate(r[0]),
                        r -> ((Number) r[1]).longValue()
                ));

        // ✅ STOCK MAP
        Map<LocalDate, Long> stockMap =
                stockHistoryRepo.findStockTrend(shop, start, end)
                        .stream().collect(Collectors.toMap(
                                r -> toLocalDate(r[0]),
                                r -> ((Number) r[1]).longValue()
                        ));

        // ✅ MERGE DATE-WISE
        List<SalesStockResponse> result = new ArrayList<>();

        for (LocalDate date = start; !date.isAfter(end); date = date.plusDays(1)) {
            result.add(new SalesStockResponse(
                    date,
                    stockMap.getOrDefault(date, 0L),
                    salesMap.getOrDefault(date, 0L)
            ));
        }

        return result;
    }

    private LocalDate toLocalDate(Object value) {
        if (value instanceof LocalDate ld) {
            return ld;
        }
        if (value instanceof java.sql.Date sd) {
            return sd.toLocalDate();
        }
        throw new IllegalArgumentException("Unsupported date type: " + value.getClass());
    }




}

class ProductStats {
    String productName;
    BigDecimal productPrice;
    long quantitySold;
    BigDecimal revenue;

    ProductStats(
            String productName,
            BigDecimal productPrice,
            long quantitySold,
            BigDecimal revenue
    ) {
        this.productName = productName;
        this.productPrice = productPrice;
        this.quantitySold = quantitySold;
        this.revenue = revenue;
    }

    BigDecimal getRevenue() {
        return revenue;
    }
}