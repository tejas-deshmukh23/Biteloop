package com.tiffin.channel;

import com.tiffin.entity.NotificationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * WhatsApp notification channel — STUB implementation.
 *
 * Phase 2: integrate MSG91 WhatsApp Business API.
 * Indian market primary channel — 98% open rate.
 *
 * When implementing:
 * - Register on MSG91 or Meta Business API directly
 * - Pre-approve message templates (required by Meta)
 * - Replace this stub with actual HTTP call to MSG91
 * - Add MSG91_API_KEY to .env and application.yml
 */
@Component
public class WhatsAppChannel implements NotificationChannel {

    private static final Logger log =
            LoggerFactory.getLogger(WhatsAppChannel.class);

    @Override
    public boolean send(String recipient, String subject,
                        String body, NotificationEventType eventType) {
        // Stub — logs intent but does not send
        log.info("[WHATSAPP STUB] Would send to={} eventType={} body={}",
                recipient, eventType, body);
        return false;
    }

    @Override
    public com.tiffin.entity.NotificationChannel getChannelType() {
        return com.tiffin.entity.NotificationChannel.WHATSAPP;
    }
}