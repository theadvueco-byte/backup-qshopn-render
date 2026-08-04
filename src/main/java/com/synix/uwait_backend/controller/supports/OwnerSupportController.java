package com.synix.uwait_backend.controller.supports;

import com.synix.uwait_backend.dto.TicketResponseRequest;
import com.synix.uwait_backend.model.supports.OwnerSupportTicketRequest;
import com.synix.uwait_backend.model.supports.SupportTicket;
import com.synix.uwait_backend.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/owner/support")
@RequiredArgsConstructor
public class OwnerSupportController {

    private final SupportTicketService service;

    //list all tickets for a shop
    @GetMapping("/{shopId}")
    public ResponseEntity<?> list(@PathVariable String shopId) {
        return ResponseEntity.ok(service.getOwnerTickets(shopId));
    }

    //customer support ticket response by owner
    @PatchMapping("/{id}/respond")
    public ResponseEntity<?> respond(
            @PathVariable Long id,
            @RequestBody TicketResponseRequest req
    ) throws IOException {
        service.respondToTicket(id, req);
        return ResponseEntity.ok(Map.of("message", "Response sent"));
    }

    //raise ticket as owner to admin
    @PostMapping("/raise")
    public SupportTicket raiseTicket(
            Authentication authentication,
            @RequestBody OwnerSupportTicketRequest request
    ) throws IOException {
        String ownerEmail = authentication.getName();
        return service.raiseOwnerTicket(ownerEmail, request);
    }

    // Approve Refund/Cancel request
    @PatchMapping("/{id}/approve")
    public ResponseEntity<?> approveRequest(
            @PathVariable Long id,
            @RequestBody String responseMessage
    ) throws IOException {
        service.processRequest(id, true, responseMessage);
        return ResponseEntity.ok(Map.of(
                "message", "Request approved"
        ));
    }

    // Reject Refund/Cancel request
    @PatchMapping("/{id}/reject")
    public ResponseEntity<?> rejectRequest(
            @PathVariable Long id,
            @RequestParam String responseMessage
    ) throws IOException {
        service.processRequest(id, false, responseMessage);
        return ResponseEntity.ok(Map.of(
                "message", "Request rejected"
        ));
    }



}

