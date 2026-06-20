package com.tiffin.entity;

import com.tiffin.common.constants.PrefixConstants;
import com.tiffin.common.entity.BaseEntity;
import com.tiffin.common.enums.PaymentStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Maps to 'payments' table in tiffin_payments database.
 *
 * orderId — cross-service reference to order-service.
 * Plain String, no FK constraint.
 *
 * razorpayOrderId — ID returned by Razorpay when we create an order.
 * Used to match webhooks back to our payment record.
 *
 * razorpayPaymentId — ID assigned by Razorpay after payment succeeds.
 * Used for refunds.
 *
 * Why store both?
 * razorpayOrderId exists before payment (created by us)
 * razorpayPaymentId exists after payment (created by Razorpay)
 */
@Entity
@Table(
    name = "payments",
    indexes = {
        @Index(name = "idx_payments_order", columnList = "order_id"),
        @Index(name = "idx_payments_razorpay_order", columnList = "razorpay_order_id"),
        @Index(name = "idx_payments_razorpay_payment", columnList = "razorpay_payment_id"),
        @Index(name = "idx_payments_status", columnList = "status"),
        @Index(name = "idx_payments_user", columnList = "user_id")
    }
)
public class Payment extends BaseEntity {

    @Column(name = "order_id", nullable = false, length = 40)
    private String orderId;

    @Column(name = "user_id", nullable = false, length = 40)
    private String userId;

    /**
     * Amount in INR.
     * Razorpay works in paise internally (multiply by 100)
     * but we store in rupees for readability.
     */
    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    /**
     * Razorpay order ID — created when we initiate payment.
     * Format: order_xxxxxxxxxx
     * Used to identify which order a webhook belongs to.
     */
    @Column(name = "razorpay_order_id", length = 100)
    private String razorpayOrderId;

    /**
     * Razorpay payment ID — assigned after payment succeeds.
     * Format: pay_xxxxxxxxxx
     * Used for refund API calls.
     * Null until payment is captured.
     */
    @Column(name = "razorpay_payment_id", length = 100)
    private String razorpayPaymentId;

    /**
     * Razorpay refund ID — assigned after refund is initiated.
     * Format: rfnd_xxxxxxxxxx
     * Null until refund is initiated.
     */
    @Column(name = "razorpay_refund_id", length = 100)
    private String razorpayRefundId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private PaymentStatus status = PaymentStatus.PENDING;

    /**
     * Failure reason from Razorpay.
     * Populated when payment fails.
     * Helps customer understand why payment failed.
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // ── Constructors ───────────────────────────────────────────

    public Payment() {}

    public Payment(String orderId, String userId,
                   BigDecimal amount, String razorpayOrderId) {
        this.orderId = orderId;
        this.userId = userId;
        this.amount = amount;
        this.razorpayOrderId = razorpayOrderId;
        this.status = PaymentStatus.PENDING;
    }

    // ── BaseEntity ─────────────────────────────────────────────

    @Override
    protected String getIdPrefix() {
        return PrefixConstants.PAYMENT;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getRazorpayOrderId() { return razorpayOrderId; }
    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }
    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId; }

    public String getRazorpayRefundId() { return razorpayRefundId; }
    public void setRazorpayRefundId(String razorpayRefundId) {
        this.razorpayRefundId = razorpayRefundId; }

    public PaymentStatus getStatus() { return status; }
    public void setStatus(PaymentStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason; }

    @Override
    public String toString() {
        return "Payment{" +
                "id='" + getId() + '\'' +
                ", orderId='" + orderId + '\'' +
                ", userId='" + userId + '\'' +
                ", amount=" + amount +
                ", razorpayOrderId='" + razorpayOrderId + '\'' +
                ", status=" + status +
                '}';
    }
}