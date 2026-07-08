package com.tiffin.channel;

import com.tiffin.entity.NotificationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * SMS notification channel — STUB implementation.
 *
 * Phase 2: integrate MSG91 or Twilio for SMS.
 * Fallback channel when WhatsApp is unavailable.
 * ~95% open rate, read within 3 minutes.
 *
 * When implementing:
 * - MSG91 preferred for Indian numbers (cheaper, better delivery)
 * - Add MSG91_API_KEY, MSG91_SENDER_ID to .env
 * - Replace this stub with actual HTTP call
 */
@Component
public class SmsChannel implements NotificationChannel {

    private static final Logger log =
            LoggerFactory.getLogger(SmsChannel.class);

    @Override
    public boolean send(String recipient, String subject,
                        String body, NotificationEventType eventType) {
        // Stub — logs intent but does not send
        log.info("[SMS STUB] Would send to={} eventType={} body={}",
                recipient, eventType, body);
        return false;
    }

    @Override
    public com.tiffin.entity.NotificationChannel getChannelType() {
        return com.tiffin.entity.NotificationChannel.SMS;
    }
}