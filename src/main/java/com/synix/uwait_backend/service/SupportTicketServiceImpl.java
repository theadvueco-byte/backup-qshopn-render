package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.SupportTicketRequest;
import com.synix.uwait_backend.dto.TicketResponseRequest;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.model.supports.*;
import com.synix.uwait_backend.repository.*;
import com.synix.uwait_backend.service.emailsend.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SupportTicketServiceImpl implements SupportTicketService {

    private final SupportTicketRepository ticketRepo;
    private final OrderRepository orderRepo;
    private final ShopRepository shopRepo;
    private final EmailService emailService;
    private final ProductRepository productRepo;
    private final StockHistoryRepository stockHistoryRepo;
    private final AdminNotificationService adminNotificationService;
    private final AdminRepository adminRepo;

    //smtp email service
    private final EmailNotificationService emailNotificationService;


    /* ================= CUSTOMER RAISE ================= */
    @Override
    public SupportTicket raiseTicket(SupportTicketRequest req) throws IOException {

        Order order = null;
        Shop shop = null;

        if (req.getOrderId() != null) {
            order = orderRepo.findById(req.getOrderId())
                    .orElseThrow(() -> new RuntimeException("Order not found"));

            if (!order.getCustomerEmail().equalsIgnoreCase(req.getEmail())) {
                throw new RuntimeException("Email does not match order");
            }

            shop = order.getShop();
        }

        SupportTicket ticket = SupportTicket.builder()
                .raisedBy(TicketRaisedBy.CUSTOMER)
                .senderEmail(req.getEmail())
                .senderName(req.getName())
                .complainOrder(req.getOrderId())
                .target(req.getTarget())
                .order(order)
                .shop(shop)
                .type(req.getType())
                .message(req.getMessage())
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        ticketRepo.save(ticket);

        // Notify target
        if (req.getTarget() == TicketTarget.OWNER && shop != null) {
            String emailMessage =
                    "Hello Shop Owner,\n\n" +
                            "You have received a new customer request.\n\n" +
                            "Shop Name: " + shop.getShopName() + "\n" +
                            "Ticket Type: " + ticket.getType() + "\n" +
                            "Customer Message: " + req.getMessage() + "\n\n" +
                            "Regards,\nYour System";

            emailNotificationService.sendMail(
                    shop.getOwnerEmail(),
                    "New Customer Request",
                    emailMessage
            );

            //sendgrid email service
//            emailService.sendSupportTicketEmail(
//                    shop.getOwnerEmail(),
//                    "Shop Owner",
//                    shop.getShopName(),
//                    String.valueOf(ticket.getType()),
//                    req.getMessage(),
//                    null
//            );
            List<Admin> admins = adminRepo.findAll();

            for (Admin admin : admins) {
                adminNotificationService.createNotification(
                        admin,
                        "New Customer Request for " + shop.getShopName(),
                        "Customer '" + req.getName() + "' raised a new request for shop '" + shop.getShopName() + "'.",
                        NotificationSourceType.SHOP_OWNER
                );
            }

        }

        if (req.getTarget() == TicketTarget.ADMIN || req.getTarget() == TicketTarget.PLATFORM) {

//            emailService.sendSupportTicketEmail(
//                    "theadvue@qshopn.com",
//                    "Platform Admin",
//                    shop.getShopName(),
//                    String.valueOf(ticket.getType()),
//                    req.getMessage(),
//                    null
//            );

            //smtp email service
            String emailMessage =
                    "Hello Shop Owner,\n\n" +
                            "A new support ticket has been created.\n\n" +
                            "Shop Name: " + shop.getShopName() + "\n" +
                            "Ticket Type: " + ticket.getType() + "\n" +
                            "Customer Message: " + req.getMessage() + "\n\n" +
                            "Regards,\nQShop Support Team";

            emailNotificationService.sendMail(
                    "theadvue@qshopn.com",
                    "New Support Ticket",
                    emailMessage
            );
            List<Admin> admins = adminRepo.findAll();

            for (Admin admin : admins) {
                adminNotificationService.createNotification(
                        admin,
                        "Ticket To Admin - Customer Request for " + (shop != null ? shop.getShopName() : "General"),
                        "Customer '" + req.getName() + "' raised a new request " + (shop != null ? "for shop '" + shop.getShopName() + "'" : "to platform admin") + ".",
                        NotificationSourceType.SHOP_OWNER
                );
            }

        }

        return ticket;
    }

    /* ================= CUSTOMER TRACK ================= */
    @Override
    public SupportTicket getTicketForCustomer(Long id, String email) {

        return ticketRepo.findByIdAndSenderEmail(id, email)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));
    }

    /* ================= OWNER VIEW ================= */
    /* ================= OWNER RAISE ================= */
    @Override
    public SupportTicket raiseOwnerTicket(String ownerEmail, OwnerSupportTicketRequest req) throws IOException {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        SupportTicket ticket = SupportTicket.builder()
                .raisedBy(TicketRaisedBy.OWNER)
                .senderEmail(ownerEmail)
                .senderName(shop.getPublicCode())
                .target(TicketTarget.ADMIN)
                .shop(shop)
                .type(req.getIssueType())
                .message(req.getMessage())
                .status(TicketStatus.OPEN)
                .createdAt(LocalDateTime.now())
                .build();

        ticketRepo.save(ticket);

        // Notify Admin
        emailNotificationService.sendMail(
                "theadvue@qshopn.com",
                "Owner Support Ticket - " + req.getIssueType(),
                """
                Shop: %s
                Owner: %s
                Issue: %s

                Message:
                %s
                """.formatted(
                        shop.getOwner(),
                        shop.getOwnerEmail(),
                        req.getIssueType(),
                        req.getMessage()
                )
        );
//        emailService.sendSupportTicketEmail(
//                "admin@uwait.in",
//                "Admin",
//                shop.getShopName(),
//                String.valueOf(req.getIssueType()),
//                req.getMessage(),
//                null
//        );
        List<Admin> admins = adminRepo.findAll();

        for (Admin admin : admins) {
            adminNotificationService.createNotification(
                    admin,
                    "New Owner Support Ticket - " + req.getIssueType(),
                    "Shop: %s\nOwner: %s\nIssue: %s".formatted(
                            shop.getShopName(),
                            shop.getOwnerEmail(),
                            req.getIssueType()
                    ),
                    NotificationSourceType.SHOP_OWNER
            );
        }

        return ticket;
    }


    @Override
    public List<SupportTicket> getOwnerTickets(String shopId) {

        Shop shop = shopRepo.findByPublicCode(shopId)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return ticketRepo.findByShop(shop);
    }

    /* ================= OWNER / ADMIN RESPOND ================= */
    @Override
    public void respondToTicket(Long ticketId, TicketResponseRequest req) throws IOException {

        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        ticket.setResponse(req.getResponse());
        ticket.setStatus(req.getStatus());
        ticket.setRespondedAt(LocalDateTime.now());

        ticketRepo.save(ticket);

//        // Notify sender

//        emailService.sendSupportTicketEmail(
//                ticket.getSenderEmail(),
//                "User",
//                ticket.getShop().getShopName(),
//                String.valueOf(ticket.getType()),
//                ticket.getMessage(),
//                req.getResponse()
//        );

        //smtp email service
        String emailMessage = String.format(
                "Hello,\n\n" +
                        "Your request has been processed.\n\n" +
                        "Shop Name: %s\n" +
                        "Ticket Type: %s\n" +
                        "Your Message: %s\n" +
                        "Response: %s\n\n" +
                        "Regards,\nSupport Team",
                ticket.getShop().getShopName(),
                ticket.getType(),
                ticket.getMessage(),
                req.getResponse()
        );

        emailNotificationService.sendMail(
                ticket.getSenderEmail(),
                "Response to your request",
                emailMessage
        );
    }



    /* ================= ADMIN VIEW ================= */
    @Override
    public List<SupportTicket> getAdminTickets() {
        return ticketRepo.findAll();
    }

    @Override
    public void processRequest(Long ticketId, boolean approve, String responseMessage) throws IOException {

        SupportTicket ticket = ticketRepo.findById(ticketId)
                .orElseThrow(() -> new RuntimeException("Ticket not found"));

        if (ticket.getType() != TicketType.REFUND) {
            throw new RuntimeException("Invalid request type");
        }

        if (ticket.getStatus() != TicketStatus.OPEN) {
            throw new RuntimeException("Ticket already processed");
        }

        Order order = ticket.getOrder();

        if (order == null) {
            throw new RuntimeException("Order not linked to this ticket");
        }

        if (approve) {

            if (order.getStatus() != OrderStatus.PAID &&
                    order.getStatus() != OrderStatus.CONFIRMED &&
                    order.getStatus() != OrderStatus.COMPLETED) {
                throw new RuntimeException("Refund allowed only for paid/completed orders");
            }

            // 1️⃣ Restore stock
            restoreStock(order);

            // 2️⃣ Update stock history
            updateStockAfterRestock(order.getShop());

            // 3️⃣ Mark order as refunded
            order.setStatus(OrderStatus.REFUNDED);
            order.setCancelledBy(CancelledBy.OWNER);
            order.setCancelReason("Refund approved by owner");
            orderRepo.save(order);

            ticket.setStatus(TicketStatus.RESOLVED);

        } else {

            ticket.setStatus(TicketStatus.REJECTED);
        }

        // 4️⃣ Save ticket response
        ticket.setResponse(responseMessage);
        ticket.setRespondedAt(LocalDateTime.now());
        ticketRepo.save(ticket);

        // 5️⃣ Notify customer
//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Refund Request " + (approve ? "Approved" : "Rejected") + " - #" + order.getId(),
//                approve ? "Refund Approved" : "Refund Rejected",
//                order.getCustomerName(),
//                order,
//                "Your refund request has been " + (approve ? "approved" : "rejected") + ".",
//                responseMessage
//        );

        //smtp email service
        String emailMessage = String.format(
                "Hello %s,\n\n" +
                        "Your refund request for order #%d has been %s.\n\n" +
                        "Order Details:\n" +
                        "Shop: %s\n" +
                        "Total Amount: %.2f\n\n" +
                        "Response from Support Team:\n%s\n\n" +
                        "Regards,\nSupport Team",
                order.getCustomerName(),
                order.getId(),
                approve ? "approved" : "rejected",
                order.getShop().getShopName(),
                order.getTotalAmount(),
                responseMessage
        );
        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Refund Request " + (approve ? "Approved" : "Rejected") + " - #" + order.getId(),
                emailMessage
        );
    }

    private void restoreStock(Order order) {

        for (OrderItem item : order.getItems()) {

            Product product = productRepo.findById(
                    item.getProduct().getId()
            ).orElseThrow(() ->
                    new RuntimeException("Product not found during stock restore"));

            product.setStock(product.getStock() + item.getQuantity());
            productRepo.save(product);
        }
    }

    @Transactional
    public void updateStockAfterRestock(Shop shop) {

        LocalDate today = LocalDate.now();

        Long totalStock = productRepo.findTotalStock(shop);

        StockHistory history = stockHistoryRepo
                .findByShopAndDate(shop, today)
                .orElseGet(() -> {
                    StockHistory h = new StockHistory();
                    h.setShop(shop);
                    h.setDate(today);
                    return h;
                });

        history.setStock(totalStock);
        stockHistoryRepo.save(history);
    }

}
