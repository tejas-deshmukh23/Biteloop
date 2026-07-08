package com.tiffin.service;

import com.tiffin.channel.NotificationChannel;
import com.tiffin.entity.NotificationLog;
import com.tiffin.entity.NotificationStatus;
import com.tiffin.entity.NotificationEventType;
import com.tiffin.repository.NotificationLogRepository;
import com.tiffin.template.NotificationTemplates.NotificationContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Core notification orchestrator.
 *
 * Responsibilities:
 * 1. Idempotency check — don't send duplicate notifications
 * 2. Send via each channel
 * 3. Log every attempt (success or failure) to NotificationLog
 *
 * This class knows nothing about Kafka, email, or WhatsApp.
 * It only coordinates between channels and the log.
 * Clean separation of concerns.
 */
@Service
public class NotificationService {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationService.class);

    private final NotificationLogRepository logRepository;

    public NotificationService(NotificationLogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Send a notification via the given channels and log each attempt.
     *
     * @param userId      recipient's internal user ID (for log)
     * @param eventId     idempotency key — paymentId, orderId etc.
     * @param eventType   what triggered this notification
     * @param recipient   email address or phone number
     * @param content     subject + body from NotificationTemplates
     * @param channels    list of channels to send via (EMAIL, WHATSAPP etc.)
     */
    public void send(String userId,
                     String eventId,
                     NotificationEventType eventType,
                     String recipient,
                     NotificationContent content,
                     List<NotificationChannel> channels) {

        for (NotificationChannel channel : channels) {
            sendViaChannel(userId, eventId, eventType,
                    recipient, content, channel);
        }
    }

    private void sendViaChannel(String userId,
                                 String eventId,
                                 NotificationEventType eventType,
                                 String recipient,
                                 NotificationContent content,
                                 NotificationChannel channel) {

        com.tiffin.entity.NotificationChannel channelType =
                channel.getChannelType();

        // ── Idempotency check ──────────────────────────────────
        // Check at application level first (fast path)
        // DB unique constraint is the safety net behind this
        if (logRepository.existsByEventIdAndChannel(eventId, channelType)) {
            log.warn("Duplicate notification skipped: eventId={} channel={}",
                    eventId, channelType);
            return;
        }

        // ── Send ───────────────────────────────────────────────
        boolean success = channel.send(
                recipient,
                content.subject(),
                content.body(),
                eventType
        );

        // ── Log the attempt ────────────────────────────────────
        NotificationLog notificationLog = new NotificationLog(
                userId,
                eventType,
                channelType,
                recipient,
                content.subject(),
                eventId,
                success ? NotificationStatus.SENT : NotificationStatus.FAILED
        );

        try {
            logRepository.save(notificationLog);
        } catch (DataIntegrityViolationException e) {
            // Race condition — two threads processed same event simultaneously
            // DB unique constraint caught it — this is expected and safe
            log.warn("Duplicate notification caught at DB level: " +
                    "eventId={} channel={}", eventId, channelType);
        }

        if (success) {
            log.info("Notification sent: userId={} eventType={} channel={}",
                    userId, eventType, channelType);
        } else {
            log.error("Notification failed: userId={} eventType={} channel={}",
                    userId, eventType, channelType);
        }
    }
}