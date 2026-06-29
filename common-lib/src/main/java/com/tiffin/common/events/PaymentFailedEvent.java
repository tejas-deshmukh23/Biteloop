package com.tiffin.common.events;

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

    public PaymentFailedEvent() {}

    public PaymentFailedEvent(String paymentId, String orderId,
                              String userId, String failureReason) {
        this.paymentId = paymentId;
        this.orderId = orderId;
        this.userId = userId;
        this.failureReason = failureReason;
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
}