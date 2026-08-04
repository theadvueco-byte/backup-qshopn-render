package com.synix.uwait_backend.dto;

import com.synix.uwait_backend.model.supports.TicketTarget;
import com.synix.uwait_backend.model.supports.TicketType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SupportTicketRequest {

    private Long orderId;      // optional
    private String email;
    private String name;
    private TicketType type;
    private TicketTarget target;
    private String message;
}

