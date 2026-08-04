package com.synix.uwait_backend.dto;

import com.synix.uwait_backend.model.supports.TicketStatus;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TicketResponseRequest {
    private String response;
    private TicketStatus status;
}
