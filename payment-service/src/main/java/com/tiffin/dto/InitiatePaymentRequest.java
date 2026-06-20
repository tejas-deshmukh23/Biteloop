package com.tiffin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Customer sends this to initiate payment for an order.
 * amount is sent by client but MUST be verified against
 * order-service before creating Razorpay order.
 * We never trust client-sent amount for actual charging.
 */
public class InitiatePaymentRequest {

    @NotBlank(message = "Order ID is required")
    private String orderId;

    // ── Getters & Setters ──────────────────────────────────────

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}