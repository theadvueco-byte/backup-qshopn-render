package com.synix.uwait_backend.controller;

import com.synix.uwait_backend.dto.RefundApprovalRequest;
import com.synix.uwait_backend.model.OrderRequest;
import com.synix.uwait_backend.repository.OrderRequestRepository;
import com.synix.uwait_backend.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/owner/order-requests")
@RequiredArgsConstructor
public class TokenRequestController {
    private final OrderService orderService;
    private final OrderRequestRepository orderRequestRepo;

//    @GetMapping
//    public List<OrderRequest> getRequests() {
//        return orderRequestRepo.findAll();
//    }
//
//    @PatchMapping("{id}/approve-refund")
//    public ResponseEntity<?> approveRefund(
//            @PathVariable Long id,
//            @RequestBody RefundApprovalRequest request
//    ) throws IOException {
//        orderService.approveRefund(id, request.getResponse());
//        return ResponseEntity.ok(Map.of(
//                "message", "Refund approved and order cancelled"
//        ));
//    }
//
//    // Approve Refund/Cancel request
//    @PatchMapping("/{id}/approve")
//    public ResponseEntity<?> approveRequest(
//            @PathVariable Long id,
//            @RequestParam String responseMessage
//    ) throws IOException {
//        orderService.processRequest(id, true, responseMessage);
//        return ResponseEntity.ok(Map.of(
//                "message", "Request approved"
//        ));
//    }
//
//    // Reject Refund/Cancel request
//    @PatchMapping("/{id}/reject")
//    public ResponseEntity<?> rejectRequest(
//            @PathVariable Long id,
//            @RequestParam String responseMessage
//    ) throws IOException {
//        orderService.processRequest(id, false, responseMessage);
//        return ResponseEntity.ok(Map.of(
//                "message", "Request rejected"
//        ));
//    }


}
