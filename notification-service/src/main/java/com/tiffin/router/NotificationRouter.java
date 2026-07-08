package com.tiffin.router;

import com.tiffin.channel.EmailChannel;
import com.tiffin.channel.NotificationChannel;
import com.tiffin.channel.SmsChannel;
import com.tiffin.channel.WhatsAppChannel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Decides which channels to use for each event type.
 *
 * Why a separate router class?
 * Business rules around notification routing change frequently.
 * "Send WhatsApp for payment events, email for everything else"
 * is a business decision — it belongs here, not scattered
 * across 6 different Kafka consumers.
 *
 * When Phase 2 WhatsApp is implemented:
 * - Change this class only
 * - Zero changes to consumers, channels, or NotificationService
 */
@Component
public class NotificationRouter {

    private final EmailChannel emailChannel;
    private final WhatsAppChannel whatsAppChannel;
    private final SmsChannel smsChannel;

    public NotificationRouter(EmailChannel emailChannel,
                              WhatsAppChannel whatsAppChannel,
                              SmsChannel smsChannel) {
        this.emailChannel = emailChannel;
        this.whatsAppChannel = whatsAppChannel;
        this.smsChannel = smsChannel;
    }

    /**
     * Channels to use for payment.success events.
     * High priority — customer needs to know immediately.
     * MVP: email only. Phase 2: add WhatsApp.
     */
    public List<NotificationChannel> forPaymentSuccess() {
        return List.of(emailChannel);
    }

    /**
     * Channels to use for payment.failed events.
     * Customer needs to know to retry — time sensitive.
     * MVP: email only. Phase 2: add WhatsApp + SMS.
     */
    public List<NotificationChannel> forPaymentFailed() {
        return List.of(emailChannel);
    }

    /**
     * Channels to use for payment.refunded events.
     * Informational — email is sufficient.
     */
    public List<NotificationChannel> forPaymentRefunded() {
        return List.of(emailChannel);
    }

    /**
     * Channels to use for order.placed events.
     * Two separate calls needed:
     * - customer gets order confirmation
     * - provider gets new order alert
     * Each call uses these channels independently.
     */
    public List<NotificationChannel> forOrderPlaced() {
        return List.of(emailChannel);
    }

    /**
     * Channels to use for order.status.updated events.
     * Customer wants real-time updates.
     * Phase 2: WhatsApp is perfect here.
     */
    public List<NotificationChannel> forOrderStatusUpdated() {
        return List.of(emailChannel);
    }

    /**
     * Channels to use for provider.registered events.
     * Admin notification — email only, always.
     */
    public List<NotificationChannel> forProviderRegistered() {
        return List.of(emailChannel);
    }
}