package com.synix.uwait_backend.service;

import com.synix.uwait_backend.dto.CreateOrderItemRequest;
import com.synix.uwait_backend.dto.CreateOrderRequest;
import com.synix.uwait_backend.dto.ProductResponse;
import com.synix.uwait_backend.dto.ownerorder.OrderItemResponse;
import com.synix.uwait_backend.dto.ownerorder.OwnerOrderDetailsResponse;
import com.synix.uwait_backend.dto.ownerorder.PaymentVerificationResponse;
import com.synix.uwait_backend.model.*;
import com.synix.uwait_backend.model.subscription.Subscription;
import com.synix.uwait_backend.repository.*;
import com.synix.uwait_backend.service.emailsend.EmailService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService{

    private final OrderRepository orderRepo;
    private final ShopRepository shopRepo;
    private final ProductRepository productRepo;
    private final FileStorageService fileStorage;
    private final EmailService emailService;
    private final OrderRequestRepository orderRequestRepo;
    private final  SubscriptionGuardService subscriptionGuard;
    private final NotificationService notificationService;
    private final StockHistoryRepository stockHistoryRepo;

    //smtp email service
private final EmailNotificationService emailNotificationService;

    /* ================= PUBLIC PRODUCTS ================= */
    @Override
    public List<ProductResponse> getPublicProducts(String code, Long categoryId) {

        Shop shop = shopRepo.findByPublicCode(code)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        if (!shop.isActive()) {
            throw new RuntimeException("Shop is temporarily unavailable");
        }

        List<Product> products;

        if (categoryId != null) {
            products = productRepo
                    .findByShopAndCategoryIdAndActiveTrue(
                            shop, categoryId
                    );
        } else {
            products = productRepo
                    .findByShopAndActiveTrue(shop);
        }

        return products.stream()
                .map(p -> ProductResponse.from(p, shop))
                .toList();
    }



    @Override
    public Map<String, Object> getPublicOrderStatus(Long orderId, String email) {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getCustomerEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Email does not match order");
        }

        return Map.of(
                "orderId", order.getId(),
                "status", order.getStatus(),
                "paymentMode",order.getPaymentMode(),
                "shopName", order.getShop().getShopName(),
                "totalAmount", order.getTotalAmount(),
                "createdAt", order.getCreatedAt(),
                "lastUpdated", order.getCreatedAt()
        );
    }


    /* ================= PLACE ORDER ================= */
    @Transactional
    @Override
    public Order placeOrder(String publicCode, CreateOrderRequest request, PaymentMode paymentMode) throws IOException {

        Shop shop = shopRepo.findByPublicCode(publicCode)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // 1. Subscription checks
        Subscription sub = subscriptionGuard.validateWritable(shop);
        long monthlyOrders = orderRepo.countCurrentMonthOrders(shop.getId());
        subscriptionGuard.checkOrderLimit(sub, monthlyOrders);
//        subscriptionGuard.checkPaymentAllowed(sub, paymentMode);

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("Order must contain at least one item");
        }

        Order order = new Order();
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerEmail(request.getCustomerEmail());
        order.setDeliveryAddress(request.getDeliveryAddress());
        order.setShop(shop);
        order.setPaymentMode(paymentMode);
        order.setStatus(OrderStatus.NEW);

        BigDecimal subTotal = BigDecimal.ZERO;
        BigDecimal orderDeliveryCharge = BigDecimal.ZERO;

        List<OrderItem> orderItems = new ArrayList<>();

        for (CreateOrderItemRequest dto : request.getItems()) {

            Product product = productRepo.findById(dto.getProductId())
                    .orElseThrow(() -> new RuntimeException("Product not found: " + dto.getProductId()));

            if (!product.isActive()) {
                throw new RuntimeException(product.getName() + " is not available");
            }



            if (product.getStock() < dto.getQuantity()) {
                throw new RuntimeException("Insufficient stock for " + product.getName());
            }

            // Deduct stock
            product.setStock(product.getStock() - dto.getQuantity());
            productRepo.save(product);

            // ✅ Unit price
            BigDecimal unitPrice = product.getSellingPrice();

            if (unitPrice == null) {
                throw new RuntimeException(
                        "Selling price missing for product: " + product.getName()
                );
            }


            // ✅ Shop-wide offer
            if (Boolean.TRUE.equals(shop.getOfferActive())
                    && shop.getShopOfferPercent() != null
                    && shop.getShopOfferPercent().compareTo(BigDecimal.ZERO) > 0) {

                BigDecimal shopDiscount = unitPrice
                        .multiply(shop.getShopOfferPercent())
                        .divide(BigDecimal.valueOf(100));

                unitPrice = unitPrice.subtract(shopDiscount);
            }

            BigDecimal quantity = BigDecimal.valueOf(dto.getQuantity());

            // ✅ Product total ONLY (no delivery here)
            BigDecimal productTotal = unitPrice.multiply(quantity);
            subTotal = subTotal.add(productTotal);

            // ✅ Delivery charge logic (MAX per order)
            BigDecimal productDelivery = product.getDeliveryCharge() != null
                    ? product.getDeliveryCharge()
                    : BigDecimal.ZERO;

            orderDeliveryCharge = orderDeliveryCharge.max(productDelivery);

            // Save order item
            OrderItem item = new OrderItem();
            item.setUnitPrice(unitPrice);
            item.setProduct(product);
            item.setQuantity(dto.getQuantity());
            item.setTotalPrice(productTotal); // product-only total
            item.setOrder(order);

            orderItems.add(item);

        }

        BigDecimal orderDiscount = BigDecimal.ZERO;

// Free delivery above ₹500
        if (subTotal.compareTo(BigDecimal.valueOf(500)) >= 0) {
            orderDeliveryCharge = BigDecimal.ZERO;
        }

// Flat discount
//        if (subTotal.compareTo(BigDecimal.valueOf(500)) >= 0) {
//            orderDiscount = BigDecimal.valueOf(50);
//        }

        BigDecimal finalTotal = subTotal
                .add(orderDeliveryCharge)
                .subtract(orderDiscount);

        // Set totals in order
        order.setSubTotal(subTotal);
        order.setOrderDiscount(orderDiscount);
        order.setDeliveryCharge(orderDeliveryCharge);
        order.setTotalAmount(finalTotal);
        order.setItems(orderItems);

        Order savedOrder = orderRepo.save(order);

        notificationService.createNotification(
                shop,
                "New Order Received",
                "Order #" + savedOrder.getId() + " | Amount: ₹" + savedOrder.getTotalAmount()
        );

        notifyOwnerNewOrder(savedOrder);

        return savedOrder;
    }

    /* ================= UPI LINK ================= */
    @Override
    public String generateUpiLink(Long orderId) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        return "upi://pay?pa=" + order.getShop().getUpiId() +
                "&pn=" + order.getShop().getShopName() +
                "&am=" + order.getTotalAmount() +
                "&cu=INR&tn=ORDER_" + order.getId();
    }

    /* ================= CONFIRM PAYMENT ================= */
    @Override
    @Transactional
    public void confirmPayment(Long orderId, MultipartFile screenshot) throws IOException {

        if (screenshot == null || screenshot.isEmpty()) {
            throw new RuntimeException("Payment screenshot is required");
        }

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new RuntimeException("Payment already confirmed");
        }

        // 1️⃣ Store file and get URL
        String screenshotUrl = fileStorage
                .storePaymentScreenshot(screenshot, orderId);

        // 2️⃣ Update order
        order.setPaymentMode(PaymentMode.UPI);
        order.setPaymentScreenshot(screenshotUrl);
        order.setStatus(OrderStatus.PAID);

        orderRepo.save(order);

        // 3️⃣ Notifications
        notificationService.createNotification(
                order.getShop(),
                "Payment Received",
                "Payment confirmed for Order #" + order.getId()
        );

        // OWNER
//        emailService.sendOrderInfoMail(
//                order.getShop().getOwnerEmail(),
//                "Payment Confirmed - Order #" + order.getId(),
//                "Payment Confirmed",
//                order.getShop().getShopName(),
//                order,
//                "Customer payment has been successfully confirmed.",
//                null
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getShop().getOwnerEmail(),
                "Payment Confirmed - Order #" + order.getId(),
                "Payment confirmed for Order #" + order.getId() + " | Amount: ₹" + order.getTotalAmount()
        );


        // CUSTOMER
//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Payment Successful",
//                "Payment Successful",
//                order.getCustomerName(),
//                order,
//                "Thank you! Your payment has been received successfully.",
//                null
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Payment Successful - Order #" + order.getId(),
                "Your payment has been received successfully for Order #" + order.getId() + ". Thank you for your order!"
        );

    }

    public PaymentVerificationResponse getPaymentVerificationDetails(
            Long orderId,
            String ownerEmail
    ) {
        Order order = getOwnerOrder(orderId, ownerEmail);

        if (order.getPaymentMode() == PaymentMode.COD) {
            throw new RuntimeException("COD orders do not need payment verification");
        }

        if (order.getPaymentScreenshot() == null) {
            throw new RuntimeException("Payment screenshot not uploaded yet");
        }

        return new PaymentVerificationResponse(
                order.getId(),
                order.getPaymentMode().name(),
                order.getPaymentScreenshot(), // URL only
                order.getStatus()
        );
    }

    @Override
    @Transactional
    public OwnerOrderDetailsResponse getOwnerOrderDetails(Long orderId, String ownerEmail) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        List<OrderItemResponse> items = order.getItems().stream().map(i -> new OrderItemResponse(
                i.getProduct().getId(),
                i.getProduct().getName(),
                i.getUnitPrice(),
                i.getQuantity(),
                i.getTotalPrice()
        )).toList();

        return new OwnerOrderDetailsResponse(
                order.getId(),
                order.getCustomerName(),
                order.getCustomerPhone(),
                order.getCustomerEmail(),
                order.getDeliveryAddress(),
                order.getPaymentMode(),
                order.getStatus(),
                order.getSubTotal(),
                order.getOrderDiscount(),
                order.getDeliveryCharge(),
                order.getTotalAmount(),
                order.getPaymentScreenshot(),
                order.getCancelledBy() != null ? String.valueOf(order.getCancelledBy()) : null,
                order.getCancelReason(),
                order.getCreatedAt(),
                items
        );

    }


    private Order getOwnerOrder(Long orderId, String ownerEmail) {

        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (!order.getShop().getId().equals(shop.getId())) {
            throw new RuntimeException("Unauthorized access");
        }

        return order;
    }


    @Transactional
    @Override
    public void verifyPaymentByOwner(Long orderId) throws IOException {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if (order.getStatus() != OrderStatus.PAID) {
            throw new RuntimeException("Payment not received yet");
        }

        if (order.getCustomerEmail() == null || order.getCustomerEmail().isBlank()) {
            throw new RuntimeException("Customer email is required");
        }


        order.setStatus(OrderStatus.CONFIRMED);
        orderRepo.save(order);

        // Generate bill structure
        String bill = generateBill(order);

        // Send email to customer
//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Order Confirmed - #" + order.getId(),
//                "Order Confirmed",
//                order.getCustomerName(),
//                order,
//                "Your order has been successfully confirmed.",
//                null
//        );

        //smtp email service
        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Order Confirmed - #" + order.getId(),
                "Your payment has been verified for Order #" + order.getId() + ". Thank you for your order!"
        );

    }

    private String generateBill(Order order) {
        StringBuilder sb = new StringBuilder();
        sb.append("Order ID: ").append(order.getId()).append("\n");
        sb.append("Shop: ").append(order.getShop().getShopName()).append("\n");
        sb.append("Customer: ").append(order.getCustomerName()).append("\n");
        sb.append("Delivery Address: ").append(order.getDeliveryAddress()).append("\n\n");

        sb.append("Items:\n");
        for (OrderItem item : order.getItems()) {
            sb.append("- ").append(item.getProductName()).append("\n");
            sb.append("  Unit Price: ₹").append(item.getUnitPrice()).append("\n");
            sb.append("  Quantity: ").append(item.getQuantity()).append("\n");
            sb.append("  Item Total: ₹").append(item.getTotalPrice()).append("\n\n");
        }

// Order-level summary
        sb.append("Subtotal: ₹").append(order.getSubTotal()).append("\n");

        if (order.getDeliveryCharge().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("Delivery: ₹").append(order.getDeliveryCharge()).append("\n");
        } else {
            sb.append("Delivery: FREE\n");
        }

        if (order.getOrderDiscount().compareTo(BigDecimal.ZERO) > 0) {
            sb.append("Discount: -₹").append(order.getOrderDiscount()).append("\n");
        }
        sb.append("Payment Mode: ").append(order.getPaymentMode());
        sb.append("TOTAL: ₹").append(order.getTotalAmount());


        return sb.toString();
    }


    /* ================= COMPLETE ORDER ================= */
    @Transactional
    @Override
    public void markOutForDelivery(Long orderId) throws IOException {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // COD orders can be sent if order is NEW or CONFIRMED
        if (order.getPaymentMode() == PaymentMode.COD) {
            if (order.getStatus() != OrderStatus.NEW && order.getStatus() != OrderStatus.CONFIRMED) {
                throw new RuntimeException("Only new or confirmed COD orders can be sent for delivery");
            }
        } else {
            // Prepaid orders must be CONFIRMED (payment verified)
            if (order.getStatus() != OrderStatus.CONFIRMED) {
                throw new RuntimeException("Only confirmed prepaid orders can be sent for delivery");
            }
        }

        order.setStatus(OrderStatus.OUT_FOR_DELIVERY);
        orderRepo.save(order);

        // Notify customer
//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Out for Delivery - Order #" + order.getId(),
//                "Out for Delivery ",
//                order.getCustomerName(),
//                order,
//                "Your order is on the way and will reach you soon.",
//                null
//        );
//
        //smtp email service
        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Out for Delivery - Order #" + order.getId(),
                "Your order is on the way and will reach you soon."
        );

    }

    public void completeOrder(Long orderId) throws IOException {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(OrderStatus.COMPLETED);
        orderRepo.save(order);

        // Notify customer
//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Order Completed - #" + order.getId(),
//                "Order Completed ",
//                order.getCustomerName(),
//                order,
//                "Your order has been successfully completed.",
//                "Thank you for shopping with us!"
//        );

        //smtp email service
        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Order Completed - #" + order.getId(),
                "Your order has been completed. Thank you for shopping with us!"
        );

    }

    /* ================= ORDER CANCEL ================= */
    @Transactional
    @Override
    public void cancelOrderByOwner(Long orderId, String reason) throws IOException {

        // 🔐 Get logged-in owner
        String ownerEmail = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // Fetch shop of owner
        Shop shop = shopRepo.findByOwner_Email(ownerEmail)
                .orElseThrow(() -> new RuntimeException("Shop not found"));

        // Fetch order only if it belongs to this shop
        Order order = orderRepo.findByIdAndShop(orderId, shop)
                .orElseThrow(() -> new RuntimeException("Order not found for your shop"));

        // Cannot cancel completed order
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Completed orders cannot be cancelled");
        }

        // Already cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order already cancelled");
        }

        // Restore stock
        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledBy(CancelledBy.OWNER);
        order.setCancelReason(reason);

        orderRepo.save(order);

        // Notify customer
//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Order Cancelled - #" + order.getId(),
//                "Order Cancelled",
//                order.getCustomerName(),
//                order,
//                "Your order was cancelled by the shop owner.",
//                "Reason: " + reason
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Order Cancelled - #" + order.getId(),
                "Your order was cancelled by the shop owner. Reason: " + reason
        );


        // Notify owner
//        emailService.sendOrderInfoMail(
//                order.getShop().getOwnerEmail(),
//                "Order Cancelled by Customer - #" + order.getId(),
//                "Order Cancelled",
//                order.getShop().getShopName(),
//                order,
//                "The customer has cancelled the order.",
//                "Reason: " + reason
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getShop().getOwnerEmail(),
                "Order Cancelled by Customer - #" + order.getId(),
                "The customer has cancelled the order. Reason: " + reason
        );

    }


    @Transactional
    @Override
    public void cancelOrderByCustomer(
            Long orderId,
            String email,
            String reason
    ) throws IOException {

        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Email mismatch → BLOCK
        if (!order.getCustomerEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Email does not match order details");
        }

        // Already cancelled
        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order already cancelled");
        }

        // Too late to cancel
        if (order.getStatus() == OrderStatus.COMPLETED) {
            throw new RuntimeException("Order cannot be cancelled now. Order is Completed now");
        }

        if (order.getStatus() == OrderStatus.PAID) {
            throw new RuntimeException("Order is under verification. Please raise a refund request.");
        }


        // Restore stock
        restoreStock(order);

        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledBy(CancelledBy.CUSTOMER);
        order.setCancelReason(reason);

        orderRepo.save(order);

        // OWNER EMAIL
//        emailService.sendOrderInfoMail(
//                order.getShop().getOwnerEmail(),
//                "Order Cancelled by Customer - #" + order.getId(),
//                "Order Cancelled",
//                order.getShop().getShopName(),
//                order,
//                "The customer has cancelled the order.",
//                "Reason: " + reason
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getShop().getOwnerEmail(),
                "Order Cancelled by Customer - #" + order.getId(),
                "Customer cancelled the order. Reason: " + reason
        );



        // CUSTOMER EMAIL

//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Order Cancelled by Customer - #" + order.getId(),
//                "Order Cancelled",
//                order.getShop().getShopName(),
//                order,
//                "Order Cancelled Successfully",
//                null
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Order Cancelled Successfully - #" + order.getId(),
                "Your order has been cancelled successfully."
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


    /* ================= REQUEST ORDER ================= */
    @Transactional
    @Override
    public void raiseOrderRequest(
            Long orderId,
            String email,
            RequestType type,
            String message
    ) {
        Order order = orderRepo.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        // Email verification
        if (order.getCustomerEmail() == null ||
                !order.getCustomerEmail().equalsIgnoreCase(email)) {
            throw new RuntimeException("Email does not match order");
        }

        // Business rules per request type
        switch (type) {

            case REFUND -> {
                if (order.getPaymentMode() != PaymentMode.UPI) {
                    throw new RuntimeException("Refund allowed only for prepaid orders");
                }
                if (order.getStatus() != OrderStatus.PAID) {
                    throw new RuntimeException("Refund can be requested only after payment");
                }
            }

            case CANCEL_REQUEST -> {
                if (order.getStatus() == OrderStatus.COMPLETED) {
                    throw new RuntimeException("Order cannot be cancelled now");
                }
            }

            case QUERY -> {
                // No restriction
            }
        }

        // Save request
        OrderRequest request = OrderRequest.builder()
                .order(order)
                .customerEmail(email)
                .type(type)
                .message(message)
                .status(RequestStatus.PENDING)
                .build();

        orderRequestRepo.save(request);

        // Notify owner
//        emailService.sendMail(
//                order.getShop().getOwnerEmail(),
//                "New Order Request - Order #" + order.getId(),
//                "Type: " + type + "\nMessage: " + message
//        );
    }

    @Transactional
    @Override
    public void approveRefund(Long requestId, String ownerResponse) throws IOException {

        OrderRequest request = orderRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        if (request.getType() != RequestType.REFUND) {
            throw new RuntimeException("Invalid request type");
        }

        Order order = request.getOrder();

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new RuntimeException("Order already cancelled");
        }

        if (order.getStatus() != OrderStatus.PAID &&
                order.getStatus() != OrderStatus.CONFIRMED &&
                order.getStatus() != OrderStatus.COMPLETED) {
            throw new RuntimeException("Refund allowed only for paid/completed orders");
        }

        // 1️⃣ Restore stock
        restoreStock(order);

        // 2️⃣ Update stock history AFTER restock
        updateStockAfterRestock(order.getShop());

        // 3️⃣ Cancel order
        order.setStatus(OrderStatus.CANCELLED);
        order.setCancelledBy(CancelledBy.OWNER);
        order.setCancelReason("Refund approved by owner");
        orderRepo.save(order);

        // 4️⃣ Update request
        request.setStatus(RequestStatus.SUCCESS);
        request.setOwnerResponse(ownerResponse);
        orderRequestRepo.save(request);

        // 5️⃣ Notify customer

//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Refund Approved - Order #" + order.getId(),
//                "Refund Approved",
//                order.getCustomerName(),
//                order,
//                "Your refund request has been approved.",
//                ownerResponse
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Refund Approved - Order #" + order.getId(),
                "Your refund request has been approved for Order #" + order.getId() + "."
        );


        // 6️⃣ Notify owner (optional)

//        emailService.sendOrderInfoMail(
//                order.getShop().getOwnerEmail(),
//                "Refund Approved - Order #" + order.getId(),
//                "Refund Approved",
//                order.getShop().getShopName(),
//                order,
//                "Refund has been processed successfully.",
//                null
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getShop().getOwnerEmail(),
                "Refund Approved - Order #" + order.getId(),
                "Refund has been processed successfully for Order #" + order.getId() + "."
        );

    }


    @Transactional
    @Override
    public void processRequest(Long requestId, boolean approve, String responseMessage) throws IOException {

        OrderRequest request = orderRequestRepo.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        if (request.getStatus() != RequestStatus.PENDING) {
            throw new RuntimeException("Request already processed");
        }

        // Update request status
        request.setStatus(approve ? RequestStatus.APPROVED : RequestStatus.REJECTED);
        orderRequestRepo.save(request);

        Order order = request.getOrder();

        // If approved and type is REFUND → cancel order & restore stock
        if (approve && request.getType() == RequestType.REFUND) {
            cancelOrderByOwner(order.getId(), "Refund approved by owner");
        }

        // Send email to customer

//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Order Request " + (approve ? "Approved" : "Rejected") + " - #" + order.getId(),
//                approve ? "Order Approved" : "Order Rejected",
//                order.getCustomerName(),
//                order,
//                "Your order request has been " + (approve ? "approved" : "rejected") + ".",
//                responseMessage
//        );
        //smtp email service
        String emailMessage = String.format(
                "Hello,\n\n" +
                        "Your order request has been %s.\n\n" +
                        "Order ID: #%d\n" +
                        "Response: %s\n\n" +
                        "Regards,\nTeam",
                approve ? "approved" : "rejected",
                order.getId(),
                responseMessage
        );

        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Order Request " + (approve ? "Approved" : "Rejected") + " - #" + order.getId(),
                emailMessage
        );

    }

    /* ================= EMAIL ================= */
    private void notifyOwnerNewOrder(Order order) throws IOException {

//        emailService.sendOrderInfoMail(
//                order.getShop().getOwnerEmail(),
//                "New Order Request - #" + order.getId(),
//                "New Customer Request",
//                order.getShop().getShopName(),
//                order,
//                "A new request has been submitted by the customer.",
//                null
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getShop().getOwnerEmail(),
                "New Order Received - #" + order.getId(),
                "New order received from " + order.getCustomerName() + " | Amount: ₹" + order.getTotalAmount()
        );

//        emailService.sendOrderInfoMail(
//                order.getCustomerEmail(),
//                "Order Confirmed - #" + order.getId(),
//                "Thank You for Your Order",
//                order.getCustomerName(),
//                order,
//                "Your order has been placed successfully with "
//                        + order.getShop().getShopName()
//                        + ". Our team will start processing it shortly.",
//                "For any queries, contact to raise a ticket"
//        );
        //smtp email service
        emailNotificationService.sendMail(
                order.getCustomerEmail(),
                "Order Confirmed - #" + order.getId(),
                "Your order has been placed successfully with " + order.getShop().getShopName() + ". Our team will start processing it shortly."
        );


    }

    private String buildRefundApprovedMail(Order order, String response) {

        return """
        Your refund request has been approved 

        Order ID: %d
        Shop: %s
        Amount: ₹%.2f
        Payment Mode: %s

        Owner Message:
        %s

        Refund will be processed shortly.
        Thank you for your patience.
        """.formatted(
                order.getId(),
                order.getShop().getShopName(),
                order.getTotalAmount(),
                order.getPaymentMode(),
                response != null ? response : "No additional message"
        );
    }

    private String buildCancelMail(Order order, String reason) {

        return """
            Your order has been cancelled.

            Order ID: %d
            Shop: %s
            Amount: ₹%.2f

            Reason: %s

            If payment was completed, refund will be initiated shortly.
            """.formatted(
                order.getId(),
                order.getShop().getShopName(),
                order.getTotalAmount(),
                reason != null ? reason : "Not specified"
        );
    }


}
