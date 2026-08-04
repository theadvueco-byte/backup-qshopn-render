package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.adminanalytics.*;
import com.synix.uwait_backend.dto.admindashboard.*;
import com.synix.uwait_backend.dto.admindashboard.ShopSummaryResponse;
import com.synix.uwait_backend.dto.adminshop.AdminOrderListResponse;

import java.util.List;
import java.util.Map;

public interface AdminService {

    void sendNotificationToShop(String shopId, String title, String message);

    void sendNotificationToAllShops(String title, String message);

    Map<String, Object> analytics();

    List<AdminOrderListResponse> getAllOrdersForAdmin();
    AdminOrderListResponse getOrderByIdForAdmin(Long orderId);

    ShopSummaryResponse getShopSummary();

    List<BestSellerShopResponse> getBestSellerShops(int limit);

    OrderSummaryResponse getOrderSummary();

    TodayOrderResponse getTodayOrders();

    NonAccountableOrderResponse getNonAccountableOrders();

    PendingVerificationResponse getPendingVerification();
    long getSuspendedShopCount();
    List<ExpiringSubscriptionResponse> getExpiringSubscriptions(int days);
    List<GraphPointResponse> getFreeTierAnalytics();

    List<GraphPointResponse> getSubscriptionTierAnalytics();
////////////////////////
    ShopSummaryAnalyticsResponse getShopSummary(String shopCode);

    RevenueByPlanResponse getRevenueByPlan();

    List<OrderTrendResponse> getOrdersTrend(int days);

    PaymentModeResponse getPaymentModes();

    List<ShopOrderTrendResponse> getShopOrderTrend(String shopId, int days);

    PaymentModeResponse getShopPaymentModes(String shopId);

    List<OrderVsRevenueResponse> getOrdersVsRevenue(String shopId);
}

