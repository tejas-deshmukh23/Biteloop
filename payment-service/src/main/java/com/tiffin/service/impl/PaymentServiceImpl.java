package com.tiffin.service.impl;

import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import com.razorpay.Refund;
import com.tiffin.client.OrderServiceClient;
import com.tiffin.common.enums.OrderStatus;
import com.tiffin.common.enums.PaymentStatus;
import com.tiffin.dto.InitiatePaymentRequest;
import com.tiffin.dto.InitiatePaymentResponse;
import com.tiffin.dto.PaymentResponse;
import com.tiffin.entity.Payment;
import com.tiffin.exception.PaymentException;
import com.tiffin.repository.PaymentRepository;
import com.tiffin.service.PaymentService;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.math.BigDecimal;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class PaymentServiceImpl implements PaymentService {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentServiceImpl.class);

    private final PaymentRepository paymentRepository;
    private final RazorpayClient razorpayClient;
    private final OrderServiceClient orderServiceClient;

    @Value("${razorpay.key-id}")
    private String razorpayKeyId;

    @Value("${razorpay.webhook-secret}")
    private String webhookSecret;

    public PaymentServiceImpl(PaymentRepository paymentRepository,
                               RazorpayClient razorpayClient,
                               OrderServiceClient orderServiceClient) {
        this.paymentRepository = paymentRepository;
        this.razorpayClient = razorpayClient;
        this.orderServiceClient = orderServiceClient;
    }

    // ── Initiate Payment ───────────────────────────────────────

    @Override
    public InitiatePaymentResponse initiatePayment(String userId,
                                                    InitiatePaymentRequest request) {
        log.info("Initiating payment for userId={} orderId={}",
                userId, request.getOrderId());

        // Step 1 — Fetch order details from order-service
        // NEVER trust client-sent amount — always verify from source
        Map<String, Object> orderDetails = orderServiceClient
                .getOrderDetails(request.getOrderId());

        // Extract amount from order response
        // orderDetails contains ApiResponse<OrderResponse> structure
        Map<String, Object> orderData = (Map<String, Object>)
                orderDetails.get("data");

        if (orderData == null) {
            throw new PaymentException("Order not found: " + request.getOrderId());
        }

        // Verify order belongs to this user
        String orderUserId = (String) orderData.get("userId");
        if (!userId.equals(orderUserId)) {
            throw new PaymentException("Order does not belong to this user");
        }

        // Verify order is in PENDING status — only PENDING orders can be paid
        String orderStatus = (String) orderData.get("status");
        if (!"PENDING".equals(orderStatus)) {
            throw new PaymentException(
                    "Order is not in PENDING status — current status: "
                    + orderStatus);
        }

        // Check if payment already exists for this order
        // Prevents duplicate payment initiation
        if (paymentRepository.findByOrderId(request.getOrderId()).isPresent()) {
            throw new PaymentException(
                    "Payment already initiated for this order");
        }

        // Extract amount — stored as number in JSON response
        BigDecimal amount = new BigDecimal(
                orderData.get("totalAmount").toString());

        // Step 2 — Create Razorpay order
        // Razorpay works in PAISE — multiply rupees by 100
        // ₹240.00 → 24000 paise
        try {
            JSONObject razorpayOrderRequest = new JSONObject();
            razorpayOrderRequest.put("amount",
                    amount.multiply(BigDecimal.valueOf(100)).intValue());
            razorpayOrderRequest.put("currency", "INR");
            razorpayOrderRequest.put("receipt", request.getOrderId());
            razorpayOrderRequest.put("payment_capture", 1); // auto capture

            Order razorpayOrder = razorpayClient.orders.create(
                    razorpayOrderRequest);

            String razorpayOrderId = razorpayOrder.get("id");
            log.info("Razorpay order created: {}", razorpayOrderId);

            // Step 3 — Save payment record in our DB
            Payment payment = new Payment(
                    request.getOrderId(),
                    userId,
                    amount,
                    razorpayOrderId
            );
            Payment saved = paymentRepository.save(payment);

            // Step 4 — Return response to frontend
            // Frontend uses razorpayOrderId + keyId to open Razorpay UI
            InitiatePaymentResponse response = new InitiatePaymentResponse();
            response.setPaymentId(saved.getId());
            response.setRazorpayOrderId(razorpayOrderId);
            response.setRazorpayKeyId(razorpayKeyId);
            response.setAmount(amount);
            response.setCurrency("INR");
            response.setOrderId(request.getOrderId());

            return response;

        } catch (RazorpayException e) {
            log.error("Razorpay order creation failed: {}", e.getMessage());
            throw new PaymentException(
                    "Failed to create payment order: " + e.getMessage());
        }
    }

    // ── Handle Webhook ─────────────────────────────────────────

    @Override
    public void handleWebhook(String payload, String razorpaySignature) {
        log.info("Received webhook from Razorpay");

        // Step 1 — Verify webhook signature
        // This is the CRITICAL security check
        // Without this, anyone can fake payment success
        if (!isValidSignature(payload, razorpaySignature)) {
            log.error("Invalid webhook signature — possible fake webhook!");
            throw new PaymentException("Invalid webhook signature");
        }

        log.info("Webhook signature verified successfully");

        // Step 2 — Parse webhook payload
        JSONObject webhookBody = new JSONObject(payload);
        String event = webhookBody.getString("event");

        log.info("Processing webhook event: {}", event);

        // Step 3 — Handle specific events
        switch (event) {
            case "payment.captured" -> handlePaymentCaptured(webhookBody);
            case "payment.failed"   -> handlePaymentFailed(webhookBody);
            case "refund.created"   -> handleRefundCreated(webhookBody);
            default -> log.warn("Unhandled webhook event: {}", event);
        }
    }

    // ── Webhook Event Handlers ─────────────────────────────────

    private void handlePaymentCaptured(JSONObject webhookBody) {
        JSONObject paymentEntity = webhookBody
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayPaymentId = paymentEntity.getString("id");
        String razorpayOrderId = paymentEntity.getString("order_id");

        log.info("Payment captured: paymentId={} orderId={}",
                razorpayPaymentId, razorpayOrderId);

        // Idempotency check — has this payment already been processed?
        // Razorpay may send same webhook multiple times for reliability
        if (paymentRepository.existsByRazorpayPaymentId(razorpayPaymentId)) {
            log.warn("Duplicate webhook — payment already processed: {}",
                    razorpayPaymentId);
            return; // ignore duplicate
        }

        // Find our payment record by Razorpay order ID
        Payment payment = paymentRepository
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new PaymentException(
                        "Payment not found for razorpayOrderId: "
                        + razorpayOrderId));

        // Update payment status
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.SUCCESS);
        paymentRepository.save(payment);

        // Update order status to CONFIRMED
        orderServiceClient.updateOrderStatus(
                payment.getOrderId(), OrderStatus.CONFIRMED);

        log.info("Payment SUCCESS processed: orderId={}", payment.getOrderId());

        // TODO: Publish PAYMENT_SUCCESS Kafka event in Milestone 8
        // notification-service will send confirmation to customer
    }

    private void handlePaymentFailed(JSONObject webhookBody) {
        JSONObject paymentEntity = webhookBody
                .getJSONObject("payload")
                .getJSONObject("payment")
                .getJSONObject("entity");

        String razorpayPaymentId = paymentEntity.getString("id");
        String razorpayOrderId = paymentEntity.getString("order_id");
        String errorDescription = paymentEntity
                .optString("error_description", "Payment failed");

        log.info("Payment failed: paymentId={} reason={}",
                razorpayPaymentId, errorDescription);

        Payment payment = paymentRepository
                .findByRazorpayOrderId(razorpayOrderId)
                .orElseThrow(() -> new PaymentException(
                        "Payment not found for razorpayOrderId: "
                        + razorpayOrderId));

        // Update payment status
        payment.setRazorpayPaymentId(razorpayPaymentId);
        payment.setStatus(PaymentStatus.FAILED);
        payment.setFailureReason(errorDescription);
        paymentRepository.save(payment);

        // Order stays in PENDING — customer can retry payment
        log.info("Payment FAILED processed: orderId={}", payment.getOrderId());

        // TODO: Publish PAYMENT_FAILED Kafka event in Milestone 8
    }

    private void handleRefundCreated(JSONObject webhookBody) {
        JSONObject refundEntity = webhookBody
                .getJSONObject("payload")
                .getJSONObject("refund")
                .getJSONObject("entity");

        String razorpayRefundId = refundEntity.getString("id");
        String razorpayPaymentId = refundEntity.getString("payment_id");

        log.info("Refund created: refundId={} paymentId={}",
                razorpayRefundId, razorpayPaymentId);

        Payment payment = paymentRepository
                .findByRazorpayPaymentId(razorpayPaymentId)
                .orElseThrow(() -> new PaymentException(
                        "Payment not found for razorpayPaymentId: "
                        + razorpayPaymentId));

        // Update payment status
        payment.setRazorpayRefundId(razorpayRefundId);
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);

        // Update order status to CANCELLED
        orderServiceClient.updateOrderStatus(
                payment.getOrderId(), OrderStatus.CANCELLED);

        log.info("Refund PROCESSED: orderId={}", payment.getOrderId());

        // TODO: Publish PAYMENT_REFUNDED Kafka event in Milestone 8
    }

    // ── Signature Verification ─────────────────────────────────

    /**
     * Verifies that the webhook actually came from Razorpay.
     *
     * How it works:
     * Razorpay computes: HMAC_SHA256(webhookBody, webhookSecret)
     * Sends result in X-Razorpay-Signature header
     *
     * We compute the same HMAC and compare.
     * If they match → webhook is genuine
     * If they don't → fake webhook, reject it
     *
     * This prevents malicious actors from faking payment success
     * by sending fake webhooks to our endpoint.
     */
    private boolean isValidSignature(String payload, String signature) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKey = new SecretKeySpec(
                    webhookSecret.getBytes(), "HmacSHA256");
            mac.init(secretKey);

            byte[] hash = mac.doFinal(payload.getBytes());

            // Convert bytes to hex string
            String computedSignature = HexFormat.of().formatHex(hash);

            // Compare our computed signature with Razorpay's signature
            return computedSignature.equals(signature);

        } catch (Exception e) {
            log.error("Signature verification failed: {}", e.getMessage());
            return false;
        }
    }

    // ── Queries ────────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentById(String paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentException(
                        "Payment not found: " + paymentId));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPaymentByOrderId(String orderId) {
        Payment payment = paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new PaymentException(
                        "Payment not found for order: " + orderId));
        return toResponse(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getMyPayments(String userId) {
        return paymentRepository
                .findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Mapper ─────────────────────────────────────────────────

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse response = new PaymentResponse();
        response.setId(payment.getId());
        response.setOrderId(payment.getOrderId());
        response.setUserId(payment.getUserId());
        response.setAmount(payment.getAmount());
        response.setStatus(payment.getStatus());
        response.setRazorpayOrderId(payment.getRazorpayOrderId());
        response.setRazorpayPaymentId(payment.getRazorpayPaymentId());
        response.setRazorpayRefundId(payment.getRazorpayRefundId());
        response.setFailureReason(payment.getFailureReason());
        response.setCreatedAt(payment.getCreatedAt());
        response.setUpdatedAt(payment.getUpdatedAt());
        return response;
    }
}