package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.dto.ownerorder.OwnerOrderDetailsResponse;
import com.synix.uwait_backend.dto.ownerorder.OwnerOrderSummaryResponse;
import com.synix.uwait_backend.dto.ownerorder.PaymentVerificationResponse;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.repository.*;
import com.synix.uwait_backend.service.AdminNotificationService;
import com.synix.uwait_backend.service.BillingService;
import com.synix.uwait_backend.service.OrderService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/owner/orders")
@RequiredArgsConstructor
public class OwnerOrderController {

    private final OrderService orderService;
    private final OrderRepository orderRepo;
    private final ShopRepository shopRepo;
    private final ProductRepository productRepo;
    private final StockHistoryRepository stockHistoryRepo;
    private final BillingService billingService;

    private final AdminNotificationService adminNotificationService;
    private final AdminRepository adminRepo;


    @GetMapping
    public List<OwnerOrderSummaryResponse> list(Authentication auth) {


        Shop shop = shopRepo.findByOwner_Email(auth.getName())
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        return orderRepo.findOwnerOrders(shop.getId());
    }

    @GetMapping("/{orderId}")
    public OwnerOrderDetailsResponse getOrderDetails(
            @PathVariable Long orderId,
            Authentication auth
    ) {
        return orderService.getOwnerOrderDetails(orderId, auth.getName());
    }

    @GetMapping("/{id}/verify-payment")
    public ResponseEntity<PaymentVerificationResponse> getPaymentDetails(
            @PathVariable Long id,
            Authentication auth
    ) {
        return ResponseEntity.ok(
                orderService.getPaymentVerificationDetails(id, auth.getName())
        );
    }


    @PatchMapping("/{id}/verify-payment")
    public ResponseEntity<?> verifyPayment(@PathVariable Long id) throws IOException {
        orderService.verifyPaymentByOwner(id);
        return ResponseEntity.ok(Map.of(
                "message", "Payment verified and order confirmed"
        ));
    }

    @PutMapping("/{id}/complete")
    @Transactional
    public ResponseEntity<?> complete(@PathVariable Long id) {

        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.COMPLETED) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Order already completed"));
        }

//        // 1️⃣ Reduce stock
//        for (OrderItem item : order.getItems()) {
//
//            Product product = item.getProduct();
//
//            if (product.getStock() < item.getQuantity()) {
//                throw new RuntimeException(
//                        "Insufficient stock for " + product.getName()
//                );
//            }
//
//            product.setStock(product.getStock() - item.getQuantity());
//            productRepo.save(product);
//        }

        // 2️⃣ Mark order completed
        order.setStatus(OrderStatus.COMPLETED);

        orderRepo.save(order);

        // 3️⃣ Save stock snapshot
        updateStockHistory(order.getShop());

        List<Admin> admins = adminRepo.findAll();

        for (Admin admin : admins) {
            adminNotificationService.createNotification(
                    admin,
                    "Order Completed",
                    "Order #" + order.getId() + " from shop '" + order.getShop().getPublicCode() + "' has been completed.",

                    NotificationSourceType.SHOP_OWNER
            );
        }

        return ResponseEntity.ok(
                Map.of(
                        "message", "Order completed successfully",
                        "orderId", order.getId()
                )
        );
    }
    private void updateStockHistory(Shop shop) {
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


    @PatchMapping("/{id}/cancel")
    public ResponseEntity<?> cancelOrder(
            @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> body
    ) throws IOException {
        String reason = body != null ? body.get("reason") : null;
        orderService.cancelOrderByOwner(id, reason);

        return ResponseEntity.ok(Map.of(
                "message", "Order cancelled successfully"
        ));
    }

    @PatchMapping("/{id}/out-for-delivery")
    public ResponseEntity<?> markOutForDelivery(@PathVariable Long id) throws IOException {
        Order order = orderRepo.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        orderService.markOutForDelivery(id);

        billingService.sendInvoiceToCustomer(order);


        return ResponseEntity.ok(Map.of(
                "message", "Order marked as Out for Delivery"
        ));
    }
}

