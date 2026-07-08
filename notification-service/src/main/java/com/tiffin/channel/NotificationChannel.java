package com.tiffin.channel;

import com.tiffin.entity.NotificationEventType;

/**
 * Strategy interface for notification delivery channels.
 *
 * Why an interface here?
 * Tomorrow when we add WhatsApp Business API or Firebase FCM,
 * we create a new class implementing this interface.
 * Zero changes to NotificationService, NotificationRouter,
 * or any Kafka consumer. Open/Closed principle in action.
 *
 * Each implementation is responsible for:
 * 1. Sending the notification via its channel
 * 2. Returning true on success, false on failure
 * 3. Never throwing exceptions — catch internally and return false
 *    so one channel failing doesn't break others
 */
public interface NotificationChannel {

    /**
     * Send a notification via this channel.
     *
     * @param recipient  email address or phone number
     * @param subject    email subject / WhatsApp template name (null for SMS)
     * @param body       message content
     * @param eventType  the event that triggered this notification
     * @return true if sent successfully, false if failed
     */
    boolean send(String recipient, String subject,
                 String body, NotificationEventType eventType);

    /**
     * Returns the channel type this implementation handles.
     * Used by NotificationRouter to identify which channel this is
     * when recording to NotificationLog.
     */
    com.tiffin.entity.NotificationChannel getChannelType();
}