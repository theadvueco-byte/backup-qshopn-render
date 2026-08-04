package com.synix.uwait_backend.model.subscription;

import lombok.Getter;

@Getter
public enum SubscriptionFeature {

    /* ================= DASHBOARD ================= */

    // SALES
    SALES_TODAY,
    SALES_MONTH,
    SALES_ALL,
    SALES_GROWTH_PERCENT,

    // ORDERS
    ORDERS_TODAY,
    ORDERS_MONTH,
    ORDERS_ALL,

    // SUBSCRIPTION
    SUBSCRIPTION_PLAN_STATUS,
    SUBSCRIPTION_ORDER_USAGE,
    SUBSCRIPTION_FULL_USAGE,

    // GRAPHS
    REVENUE_GRAPH_WEEK,
    REVENUE_GRAPH_MONTH,
    REVENUE_GRAPH_YEAR,

    ORDER_GRAPH_WEEK,
    ORDER_GRAPH_MONTH,
    ORDER_GRAPH_YEAR,

    // PAYMENT
    PAYMENT_PIE_BASIC,
    PAYMENT_PIE_FULL,

    // STOCK
    STOCK_ALERT_COUNT,
    STOCK_ALERT_LIST,

    // TOP SELLING
    TOP_SELLING_3,
    TOP_SELLING_5,

    // ORDER VIEW
    ORDER_VIEW_3,
    ORDER_VIEW_5,

    /* ================= INVENTORY ================= */


    IMAGE_UPLOAD,
    MULTI_IMAGE_3,
    MULTI_IMAGE_5,

    PRODUCT_OFFERS,
    PAYMENT_MODE_PER_PRODUCT,

    CATEGORY_STOCK_GRAPH,
    SALES_STOCK_GRAPH_WEEK,
    SALES_STOCK_GRAPH_MONTH,

    SHOP_OFFER_CARD,

    /* ================= ORDERS / EARNINGS ================= */

    EXPORT_PDF,
    EXPORT_EXCEL,
    ORDER_FILTERS,
    LIVE_ORDER_TRACKING_BASIC,
    LIVE_ORDER_TRACKING_ADVANCED,
    AVG_ORDER_VALUE,
    MONTHLY_EARNINGS,

    /* ================= SHOP LIVE ================= */

    BRANDING_BASIC,
    SHOP_NAME,
    BRANDING_SOCIAL,
    PRODUCT_FILTERS_BASIC,

    /* ================= SUPPORT ================= */

    SUPPORT_BASIC,
    SUPPORT_PRIORITY,

    /* ================= AUTOMATION ================= */

    SHOP_SETTINGS_EDIT,
    SHOP_BRANDING_EDIT,

    MONTHLY_REPORT_EMAIL, STOCK_ALERT_FULL;

    /* ================= INTERNAL ================= */


}
