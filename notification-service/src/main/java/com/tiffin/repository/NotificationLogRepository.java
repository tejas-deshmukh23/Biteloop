package com.tiffin.repository;

import com.tiffin.entity.NotificationChannel;
import com.tiffin.entity.NotificationLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationLogRepository
        extends JpaRepository<NotificationLog, String> {

    /**
     * Idempotency check — has this event already been sent
     * on this channel?
     *
     * Called before every send attempt.
     * If true → skip sending, already done.
     * Protects against Kafka retries sending duplicate notifications.
     */
    boolean existsByEventIdAndChannel(String eventId,
                                      NotificationChannel channel);
}