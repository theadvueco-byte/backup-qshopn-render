package com.synix.uwait_backend.dto.monthlyreport;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@AllArgsConstructor
@Getter
public class MonthlyReportDTO {

    private String shopName;
    private String shopCode;
    private String ownerName;
    private String ownerEmail;
    private String month;

    private List<OrderReportRowDTO> orders;

    private int totalOrders;
    private BigDecimal totalRevenue;
}

