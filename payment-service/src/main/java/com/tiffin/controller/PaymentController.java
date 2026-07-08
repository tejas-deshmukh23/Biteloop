package com.tiffin.controller;

import com.tiffin.common.response.ApiResponse;
import com.tiffin.dto.InitiatePaymentRequest;
import com.tiffin.dto.InitiatePaymentResponse;
import com.tiffin.dto.PaymentResponse;
import com.tiffin.service.PaymentService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All routes under /api/payments
 *
 * Auth strategy — same as other services:
 * X-User-Id   → injected by gateway
 * X-User-Role → injected by gateway
 * X-User-Email -> injected by gateway
 *
 * Special endpoint — /api/payments/webhook:
 * Called by Razorpay directly — NO auth headers
 * Security handled via HMAC signature verification instead
 * Must be added to public-endpoints in api-gateway application.yml
 *
 * Endpoints:
 * POST /api/payments/initiate          → customer initiates payment
 * POST /api/payments/webhook           → Razorpay webhook (public)
 * GET  /api/payments/my                → customer payment history
 * GET  /api/payments/{id}              → get payment by ID
 * GET  /api/payments/order/{orderId}   → get payment by order ID
 */
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentController.class);

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // ── Initiate Payment ───────────────────────────────────────

    @PostMapping("/initiate")
    public ResponseEntity<ApiResponse<InitiatePaymentResponse>> initiatePayment(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Email") String email,
            @Valid @RequestBody InitiatePaymentRequest request) {

        if (!"CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only customers can initiate payments"));
        }

        InitiatePaymentResponse response =
                paymentService.initiatePayment(userId, request, email);

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Payment initiated", response));
    }

    // ── Razorpay Webhook ───────────────────────────────────────

    /**
     * Called by Razorpay after payment success/failure/refund.
     *
     * IMPORTANT — this endpoint:
     * 1. Must be PUBLIC — no JWT auth (Razorpay has no JWT token)
     * 2. Must receive RAW request body for signature verification
     *    (Spring must NOT parse JSON before we verify signature)
     * 3. Security is via X-Razorpay-Signature header verification
     *
     * Added to public-endpoints in api-gateway application.yml
     */
    @PostMapping("/webhook")
    public ResponseEntity<Void> handleWebhook(
            @RequestBody String payload,
            @RequestHeader("X-Razorpay-Signature") String signature) {

        log.info("Webhook received from Razorpay");

        paymentService.handleWebhook(payload, signature);

        // Razorpay expects 200 OK — any other response triggers retry
        return ResponseEntity.ok().build();
    }

    // ── Customer Queries ───────────────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<PaymentResponse>>> getMyPayments(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        if (!"CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        }

        List<PaymentResponse> payments = paymentService.getMyPayments(userId);
        return ResponseEntity.ok(ApiResponse.success(payments));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId) {

        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }

    @GetMapping("/order/{orderId}")
    public ResponseEntity<ApiResponse<PaymentResponse>> getPaymentByOrderId(
            @PathVariable String orderId,
            @RequestHeader("X-User-Id") String userId) {

        PaymentResponse payment = paymentService.getPaymentByOrderId(orderId);
        return ResponseEntity.ok(ApiResponse.success(payment));
    }
}