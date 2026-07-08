package com.tiffin.entity;

import com.tiffin.common.constants.PrefixConstants;
import com.tiffin.common.entity.BaseEntity;
import jakarta.persistence.*;

/**
 * Records every notification attempt — success or failure.
 *
 * Why we store this:
 * - Audit trail: "was this customer notified?"
 * - Debugging: "why didn't the email arrive?"
 * - Idempotency: eventId + channel unique constraint prevents
 *   duplicate notifications when Kafka retries the same event
 *
 * One row per channel per event.
 * So a PAYMENT_SUCCESS event that sends email + WhatsApp = 2 rows.
 */
@Entity
@Table(
    name = "notification_logs",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_notification_event_channel",
            columnNames = {"event_id", "channel"}
        )
    },
    indexes = {
        @Index(name = "idx_notification_user",     columnList = "user_id"),
        @Index(name = "idx_notification_event",    columnList = "event_id"),
        @Index(name = "idx_notification_status",   columnList = "status"),
        @Index(name = "idx_notification_type",     columnList = "event_type")
    }
)
public class NotificationLog extends BaseEntity {

    /**
     * Who was this notification sent to.
     * For provider.registered → this is adminId (or "ADMIN")
     * For order.placed → this is providerId
     * For everything else → customerId
     */
    @Column(name = "user_id", nullable = false, length = 40)
    private String userId;

    /**
     * The Kafka event type that triggered this notification.
     * e.g. PAYMENT_SUCCESS, ORDER_PLACED, PROVIDER_REGISTERED
     * Stored as string — readable in DB, safe to add new types.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false, length = 50)
    private NotificationEventType eventType;

    /**
     * Which channel was used to send this notification.
     * EMAIL, WHATSAPP, SMS
     * Stored as string — same reason as above.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "channel", nullable = false, length = 20)
    private NotificationChannel channel;

    /**
     * The actual recipient address for this channel.
     * EMAIL → email address
     * WHATSAPP → phone number
     * SMS → phone number
     *
     * Storing this is important — user may change email/phone later.
     * This tells us exactly where we sent it at the time.
     */
    @Column(name = "recipient", nullable = false, length = 150)
    private String recipient;

    /**
     * Email subject line or WhatsApp template name.
     * Nullable for SMS (no subject concept).
     */
    @Column(name = "subject", length = 255)
    private String subject;

    /**
     * Idempotency key — the source event's ID.
     * e.g. paymentId for payment events, orderId for order events.
     *
     * Combined with channel in unique constraint:
     * same event + same channel = duplicate → skip sending.
     * This is what protects us from Kafka retries.
     */
    @Column(name = "event_id", nullable = false, length = 40)
    private String eventId;

    /**
     * SENT or FAILED.
     * Never assume success — always record outcome explicitly.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 10)
    private NotificationStatus status;

    /**
     * Populated only when status = FAILED.
     * Stores exception message or provider error code.
     * Critical for debugging "customer says they didn't get email".
     */
    @Column(name = "failure_reason", length = 500)
    private String failureReason;

    // ── Constructors ───────────────────────────────────────────

    public NotificationLog() {}

    public NotificationLog(String userId,
                           NotificationEventType eventType,
                           NotificationChannel channel,
                           String recipient,
                           String subject,
                           String eventId,
                           NotificationStatus status) {
        this.userId = userId;
        this.eventType = eventType;
        this.channel = channel;
        this.recipient = recipient;
        this.subject = subject;
        this.eventId = eventId;
        this.status = status;
    }

    // ── BaseEntity ─────────────────────────────────────────────

    @Override
    protected String getIdPrefix() {
        return PrefixConstants.NOTIFICATION;
    }

    // ── Getters and Setters ────────────────────────────────────

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public NotificationEventType getEventType() { return eventType; }
    public void setEventType(NotificationEventType eventType) { this.eventType = eventType; }

    public NotificationChannel getChannel() { return channel; }
    public void setChannel(NotificationChannel channel) { this.channel = channel; }

    public String getRecipient() { return recipient; }
    public void setRecipient(String recipient) { this.recipient = recipient; }

    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getEventId() { return eventId; }
    public void setEventId(String eventId) { this.eventId = eventId; }

    public NotificationStatus getStatus() { return status; }
    public void setStatus(NotificationStatus status) { this.status = status; }

    public String getFailureReason() { return failureReason; }
    public void setFailureReason(String failureReason) { this.failureReason = failureReason; }

    @Override
    public String toString() {
        return "NotificationLog{" +
                "id='" + getId() + '\'' +
                ", userId='" + userId + '\'' +
                ", eventType=" + eventType +
                ", channel=" + channel +
                ", recipient='" + recipient + '\'' +
                ", eventId='" + eventId + '\'' +
                ", status=" + status +
                '}';
    }
}