package com.tiffin.dto;

import java.math.BigDecimal;

/**
 * Returned to frontend after payment initiation.
 * Frontend uses razorpayOrderId and keyId to open
 * Razorpay payment UI (Razorpay Checkout).
 *
 * Frontend flow:
 * 1. Gets this response
 * 2. Opens Razorpay Checkout with razorpayOrderId + keyId
 * 3. Customer pays
 * 4. Razorpay sends webhook to our server
 */
public class InitiatePaymentResponse {

    private String paymentId;        // our internal pay_ id
    private String razorpayOrderId;  // order_xxxxxxxxxx from Razorpay
    private String razorpayKeyId;    // rzp_test_xxxxxxxxxx (public key for frontend)
    private BigDecimal amount;       // amount in INR
    private String currency;         // INR
    private String orderId;          // our internal ord_ id

    // ── Getters & Setters ──────────────────────────────────────

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayKeyId() { return razorpayKeyId; }
    public void setRazorpayKeyId(String razorpayKeyId) {
        this.razorpayKeyId = razorpayKeyId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getCurrency() { return currency; }
    public void setCurrency(String currency) { this.currency = currency; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }
}