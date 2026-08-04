package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.SupportTicketRequest;
import com.synix.uwait_backend.dto.TicketResponseRequest;
import com.synix.uwait_backend.model.supports.OwnerSupportTicketRequest;
import com.synix.uwait_backend.model.supports.SupportTicket;

import java.io.IOException;
import java.util.List;

public interface SupportTicketService {

    /* ================= CUSTOMER RAISE ================= */
    SupportTicket raiseTicket(SupportTicketRequest req) throws IOException;

    /* ================= CUSTOMER TRACK ================= */
     SupportTicket getTicketForCustomer(Long id, String email);

    SupportTicket raiseOwnerTicket(String ownerEmail, OwnerSupportTicketRequest req) throws IOException;

    /* ================= OWNER VIEW ================= */
     List<SupportTicket> getOwnerTickets(String shopId);

    /* ================= OWNER / ADMIN RESPOND ================= */
     void respondToTicket(Long ticketId, TicketResponseRequest req) throws IOException;

    /* ================= ADMIN VIEW ================= */
     List<SupportTicket> getAdminTickets();

    void processRequest(Long id, boolean b, String responseMessage) throws IOException;
}

