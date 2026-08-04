package com.synix.uwait_backend.controller.supports;

import com.synix.uwait_backend.dto.ContactRequest;
import com.synix.uwait_backend.dto.SupportTicketRequest;
import com.synix.uwait_backend.model.supports.SupportTicket;
import com.synix.uwait_backend.service.EmailNotificationService;
import com.synix.uwait_backend.service.SupportTicketService;
import com.synix.uwait_backend.service.emailsend.EmailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/public/support")
@RequiredArgsConstructor
public class PublicSupportController {

    private final SupportTicketService service;
   // private final EmailService emailService;

    //smtp email service
    private final EmailNotificationService emailNotificationService;

    @PostMapping
    public ResponseEntity<?> raise(@RequestBody SupportTicketRequest req) throws IOException {
        SupportTicket ticket = service.raiseTicket(req);
        return ResponseEntity.ok(Map.of(
                "ticketId", ticket.getId(),
                "message", "Request submitted successfully"
        ));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> track(
            @PathVariable Long id,
            @RequestParam String email
    ) {
        return ResponseEntity.ok(service.getTicketForCustomer(id, email));
    }

    @PostMapping("/contact")
    public ResponseEntity<?> contact(@RequestBody ContactRequest request) {

        try {
//            emailService.sendSimpleContactEmail(
//                    request.getPhone(),
//                    request.getMessage()
//            );
//
            emailNotificationService.sendMail(
                    "theadvue@qshopn.com",
                    " Contact Request from " + request.getPhone(),
                    request.getMessage()
            );

            return ResponseEntity.ok("Sent successfully");

        } catch (Exception e) {
                e.printStackTrace(); // 👈 THIS LINE IS CRITICAL
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to send");
        }
    }

}

