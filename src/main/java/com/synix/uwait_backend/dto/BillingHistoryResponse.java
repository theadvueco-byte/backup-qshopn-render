package com.synix.uwait_backend.dto;

import com.synix.uwait_backend.model.RequestStatus;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BillingHistoryResponse {



    private String invoice;
    private LocalDateTime date;
    private String plan;
    private Double amount;
    private String payment;
    private RequestStatus status;

    public BillingHistoryResponse(String invoice, LocalDateTime date, String plan, Double amount, String payment, RequestStatus status) {
        this.invoice = invoice;
        this.date = date;
        this.plan = plan;
        this.amount = amount;
        this.payment = payment;
        this.status = status;
    }
}

