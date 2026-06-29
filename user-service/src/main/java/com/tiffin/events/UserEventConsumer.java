package com.tiffin.events;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.events.ProviderRegisteredEvent;
import com.tiffin.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes Kafka events for user-service.
 *
 * PROVIDER_REGISTERED → updates providerId on User entity
 * Replaces: internal REST endpoint called by provider-service
 */
@Component
public class UserEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(UserEventConsumer.class);

    private final UserService userService;

    public UserEventConsumer(UserService userService) {
        this.userService = userService;
    }

    @KafkaListener(
            topics = KafkaTopics.PROVIDER_REGISTERED,
            groupId = "user-service-group",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void handleProviderRegistered(ProviderRegisteredEvent event) {
        log.info("Received PROVIDER_REGISTERED event: userId={} providerId={}",
                event.getUserId(), event.getProviderId());

        try {
            userService.updateProviderId(event.getUserId(), event.getProviderId());
            log.info("Successfully updated providerId for userId={}",
                    event.getUserId());
        } catch (Exception e) {
            log.error("Failed to process PROVIDER_REGISTERED event: {}",
                    e.getMessage());
            // In production — implement retry/DLQ here
        }
    }
}