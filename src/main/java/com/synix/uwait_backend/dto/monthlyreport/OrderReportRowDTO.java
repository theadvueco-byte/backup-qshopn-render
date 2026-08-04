package com.synix.uwait_backend.dto.monthlyreport;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.LocalDate;

@AllArgsConstructor
@Getter
public class OrderReportRowDTO {

    private Long orderId;
    private LocalDate date;
    private String customer;
    private String payment;
    private String status;
    private BigDecimal amount;
}

