package com.synix.uwait_backend.dto.admindashboard;

public record NonAccountableOrderResponse(
        long cancelled,
        long refunded,
        long returnedOrders
) {}
