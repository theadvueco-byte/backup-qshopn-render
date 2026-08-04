package com.synix.uwait_backend.model.supports;

import lombok.Data;

@Data
public class OwnerSupportTicketRequest {

    private TicketType issueType;   // Subscription Not Active, Payment Issue, etc
    private String message;
}
