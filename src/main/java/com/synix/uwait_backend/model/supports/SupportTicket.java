package com.synix.uwait_backend.model.supports;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.synix.uwait_backend.model.Order;
import com.synix.uwait_backend.model.Shop;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "support_tickets")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SupportTicket {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /* WHO RAISED */
    @Enumerated(EnumType.STRING)
    private TicketRaisedBy raisedBy; // CUSTOMER / OWNER

    private String senderEmail;
    private String senderName;
    private Long complainOrder;

    /* TARGET */
    @Enumerated(EnumType.STRING)
    private TicketTarget target; // OWNER / ADMIN / PLATFORM

    /* OPTIONAL LINKS */
    @ManyToOne
    @JsonBackReference
    private Order order;
    // nullable
    @ManyToOne
    @JsonBackReference
    private Shop shop;     // nullable

    /* CONTENT */
    @Enumerated(EnumType.STRING)
    private TicketType type; // COMPLAINT / QUERY / SUGGESTION / REFUND

    @Column(length = 1000)
    private String message;

    @Enumerated(EnumType.STRING)
    private TicketStatus status; // OPEN / IN_PROGRESS / RESOLVED / REJECTED

    /* ADMIN / OWNER RESPONSE */
    @Column(length = 1000)
    private String response;

    private LocalDateTime createdAt;
    private LocalDateTime respondedAt;
}
