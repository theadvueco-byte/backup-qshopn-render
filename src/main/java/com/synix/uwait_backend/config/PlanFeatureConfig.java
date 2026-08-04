package com.synix.uwait_backend.config;

import com.synix.uwait_backend.model.subscription.SubscriptionFeature;
import com.synix.uwait_backend.model.subscription.SubscriptionPlan;
import org.springframework.stereotype.Component;

import java.util.EnumSet;
import java.util.Set;

import static com.synix.uwait_backend.model.subscription.SubscriptionFeature.*;

@Component
public class PlanFeatureConfig {

    public Set<SubscriptionFeature> featuresFor(SubscriptionPlan plan) {

        return switch (plan) {

            /* ================= FREE TRIAL ================= */
            case FREE_TRIAL -> EnumSet.of(
                    // Dashboard
                    SALES_TODAY,
                    ORDERS_TODAY,
                    SUBSCRIPTION_PLAN_STATUS,
                    STOCK_ALERT_COUNT,

                    // Inventory
                    IMAGE_UPLOAD,

                    // Shop Live
                    
                    PRODUCT_FILTERS_BASIC,

                    // Support
                    SUPPORT_BASIC
            );

            /* ================= RETAIL ================= */
            case RETAIL -> EnumSet.of(
                    // Dashboard
                    SALES_TODAY,
                    SALES_MONTH,
                    ORDERS_TODAY,
                    ORDERS_MONTH,
                    SUBSCRIPTION_ORDER_USAGE,
                    REVENUE_GRAPH_WEEK,
                    REVENUE_GRAPH_MONTH,
                    ORDER_GRAPH_WEEK,
                    ORDER_GRAPH_MONTH,
                    PAYMENT_PIE_BASIC,
                    STOCK_ALERT_LIST,
                    TOP_SELLING_3,
                    ORDER_VIEW_3,

                    // Inventory
                    IMAGE_UPLOAD,
                    MULTI_IMAGE_3,
                    CATEGORY_STOCK_GRAPH,
                    SALES_STOCK_GRAPH_WEEK,

                    // Orders / Earnings
                    EXPORT_PDF,
                    ORDER_FILTERS,
                    LIVE_ORDER_TRACKING_BASIC,
                    AVG_ORDER_VALUE,
                    MONTHLY_EARNINGS,

                    // Shop Live
                    BRANDING_BASIC,
                    PRODUCT_FILTERS_BASIC,

                    // Support
                    SUPPORT_BASIC
            );

            /* ================= GROWTH ================= */
            case GROWTH -> EnumSet.of(
                    // Dashboard
                    SALES_ALL,
                    ORDERS_ALL,
                    SUBSCRIPTION_FULL_USAGE,
                    REVENUE_GRAPH_WEEK,
                    REVENUE_GRAPH_MONTH,
                    REVENUE_GRAPH_YEAR,
                    ORDER_GRAPH_WEEK,
                    ORDER_GRAPH_MONTH,
                    ORDER_GRAPH_YEAR,
                    PAYMENT_PIE_BASIC,
                    PAYMENT_PIE_FULL,
                    STOCK_ALERT_LIST,
                    TOP_SELLING_5,
                    ORDER_VIEW_5,

                    // Inventory
                    IMAGE_UPLOAD,
                    MULTI_IMAGE_5,
                    SHOP_OFFER_CARD,
                    PRODUCT_OFFERS,
                    PAYMENT_MODE_PER_PRODUCT,

                    CATEGORY_STOCK_GRAPH,
                    SALES_STOCK_GRAPH_WEEK,
                    SALES_STOCK_GRAPH_MONTH,


                    // Orders / Earnings
                    EXPORT_PDF,
                    EXPORT_EXCEL,
                    ORDER_FILTERS,
                    LIVE_ORDER_TRACKING_BASIC,
                    LIVE_ORDER_TRACKING_ADVANCED,
                    AVG_ORDER_VALUE,
                    MONTHLY_EARNINGS,

                    SHOP_SETTINGS_EDIT,
                    SHOP_BRANDING_EDIT,

                    // Shop Live
                    BRANDING_BASIC,
                    BRANDING_SOCIAL,
                    PRODUCT_FILTERS_BASIC,
                    SHOP_NAME,

                    // Support
                    SUPPORT_PRIORITY
            );

            /* ================= BUSINESS ================= */
            case BUSINESS -> EnumSet.of(
                    // Dashboard
                    SALES_ALL,
                    SALES_GROWTH_PERCENT,
                    ORDERS_ALL,
                    SUBSCRIPTION_FULL_USAGE,
                    REVENUE_GRAPH_WEEK,
                    REVENUE_GRAPH_MONTH,
                    REVENUE_GRAPH_YEAR,
                    ORDER_GRAPH_WEEK,
                    ORDER_GRAPH_MONTH,
                    ORDER_GRAPH_YEAR,
                    PAYMENT_PIE_BASIC,
                    PAYMENT_PIE_FULL,
                    STOCK_ALERT_LIST,
                    TOP_SELLING_5,
                    ORDER_VIEW_5,

                    // Inventory
                    IMAGE_UPLOAD,
                    MULTI_IMAGE_5,

                    PRODUCT_OFFERS,
                    PAYMENT_MODE_PER_PRODUCT,
                    SHOP_OFFER_CARD,

                    CATEGORY_STOCK_GRAPH,
                    SALES_STOCK_GRAPH_WEEK,
                    SALES_STOCK_GRAPH_MONTH,

                    // Orders / Earnings
                    EXPORT_PDF,
                    EXPORT_EXCEL,
                    ORDER_FILTERS,
                    LIVE_ORDER_TRACKING_BASIC,
                    LIVE_ORDER_TRACKING_ADVANCED,
                    AVG_ORDER_VALUE,
                    MONTHLY_EARNINGS,

                    SHOP_SETTINGS_EDIT,
                    SHOP_BRANDING_EDIT,

                    // Shop Live
                    BRANDING_BASIC,
                    BRANDING_SOCIAL,
                    PRODUCT_FILTERS_BASIC,
                    SHOP_NAME,

                    // Support
                    SUPPORT_PRIORITY,

                    // Automation
                    MONTHLY_REPORT_EMAIL
            );
        };
    }
}
