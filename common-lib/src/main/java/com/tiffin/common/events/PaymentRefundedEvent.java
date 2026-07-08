package com.tiffin.common.events;

import java.math.BigDecimal;

/**
 * Published by payment-service after refund is processed.
 * Consumed by:
 * - order-service → updates order status to CANCELLED
 * - notification-service (Milestone 9) → notifies customer of refund
 */
public class PaymentRefundedEvent {

    private String paymentId;
    private String orderId;
    private String userId;
    private BigDecimal refundAmount;
    private String razorpayRefundId;
    private String email;

    public PaymentRefundedEvent() {}

    public PaymentRefundedEvent(String paymentId, String orderId,
                                String userId, BigDecimal refundAmount,
                                String razorpayRefundId, String email) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.refundAmount = refundAmount;
        this.razorpayRefundId = razorpayRefundId;
        this.email = email;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BigDecimal getRefundAmount() { return refundAmount; }
    public void setRefundAmount(BigDecimal refundAmount) {
        this.refundAmount = refundAmount; }

    public String getRazorpayRefundId() { return razorpayRefundId; }
    public void setRazorpayRefundId(String razorpayRefundId) {
        this.razorpayRefundId = razorpayRefundId; }

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
    
}