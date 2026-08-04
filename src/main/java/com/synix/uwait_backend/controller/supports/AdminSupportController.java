package com.synix.uwait_backend.controller.supports;

import com.synix.uwait_backend.dto.TicketResponseRequest;
import com.synix.uwait_backend.service.SupportTicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/support")
@RequiredArgsConstructor
public class AdminSupportController {

    private final SupportTicketService service;

    @GetMapping
    public ResponseEntity<?> list() {
        return ResponseEntity.ok(service.getAdminTickets());
    }

    @PatchMapping("/{id}/respond")
    public ResponseEntity<?> respond(
            @PathVariable Long id,
            @RequestBody TicketResponseRequest req
    ) throws IOException {
        service.respondToTicket(id, req);
        return ResponseEntity.ok(Map.of("message", "Ticket updated"));
    }
}

