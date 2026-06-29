package com.tiffin.events;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.events.ProviderRegisteredEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

/**
 * Publishes provider-related Kafka events.
 * Replaces UserServiceClient REST call.
 */
@Component
public class ProviderEventPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(ProviderEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public ProviderEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    /**
     * Publishes PROVIDER_REGISTERED event after provider registration.
     * user-service consumes this to update providerId on User entity.
     */
    public void publishProviderRegistered(String userId, String providerId) {
        ProviderRegisteredEvent event =
                new ProviderRegisteredEvent(userId, providerId);

        kafkaTemplate.send(KafkaTopics.PROVIDER_REGISTERED, userId, event);

        log.info("Published PROVIDER_REGISTERED event: userId={} providerId={}",
                userId, providerId);
    }
}