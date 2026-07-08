package com.tiffin.common.events;

import java.math.BigDecimal;

/**
 * Published by payment-service after payment fails.
 * Consumed by:
 * - notification-service (Milestone 9) → notifies customer of failure
 * Order stays PENDING — no order-service update needed.
 */
public class PaymentFailedEvent {

    private String paymentId;
    private String orderId;
    private String userId;
    private String failureReason;
    private BigDecimal amount;
    private String email;

    public PaymentFailedEvent() {}

    public PaymentFailedEvent(String paymentId, String orderId,
                              String userId, String failureReason, BigDecimal amount, String email) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.failureReason = failureReason;
        this.amount = amount;
        this.email = email;
    }

    public String getPaymentId() { return paymentId; }
    public void setPaymentId(String paymentId) { this.paymentId = paymentId; }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason; }

	public BigDecimal getAmount() {
		return amount;
	}
	public void setAmount(BigDecimal amount) {
		this.amount = amount;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}
}