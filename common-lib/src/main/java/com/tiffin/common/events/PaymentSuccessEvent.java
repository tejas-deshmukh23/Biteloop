package com.tiffin.common.events;

import java.math.BigDecimal;

/**
 * Published by payment-service after payment is captured.
 * Consumed by:
 * - order-service → updates order status to CONFIRMED
 * - notification-service (Milestone 9) → sends payment confirmation
 *
 * Replaces: OrderServiceClient REST call in payment-service
 */
public class PaymentSuccessEvent {

    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal amount;
    private String razorpayPaymentId;
    private String email;

    public PaymentSuccessEvent() {}

    public PaymentSuccessEvent(String paymentId, String orderId,
                               String userId, BigDecimal amount,
                               String razorpayPaymentId, String email) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.razorpayPaymentId = razorpayPaymentId;
        this.email = email;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId; }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}