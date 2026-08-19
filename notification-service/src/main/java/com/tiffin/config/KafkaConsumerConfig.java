package com.tiffin.config;

import com.tiffin.common.events.*;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.support.serializer.JsonDeserializer;

import java.util.HashMap;
import java.util.Map;

/**
 * Explicit Kafka consumer configuration for notification-service.
 *
 * Why one factory per event type?
 * Each event class is different — JsonDeserializer needs to know
 * the exact target type (VALUE_DEFAULT_TYPE) at consumer level.
 * One generic factory would require type headers which our producers
 * don't send (ADD_TYPE_INFO_HEADERS=false).
 *
 * So we create one ConsumerFactory + one ListenerContainerFactory
 * per Kafka topic. Verbose but explicit and debuggable.
 */
@Configuration
public class KafkaConsumerConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    // ── Base config shared across all consumers ────────────────

    private Map<String, Object> baseConfig() {
        Map<String, Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, "notification-service-group");
        config.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG,
                StringDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG,
                JsonDeserializer.class);
        config.put(JsonDeserializer.TRUSTED_PACKAGES,
                "com.tiffin.common.events");
        config.put(JsonDeserializer.USE_TYPE_INFO_HEADERS, false);
        return config;
    }

    private ConcurrentKafkaListenerContainerFactory<String, Object>
            buildFactory(ConsumerFactory<String, Object> consumerFactory) {
        ConcurrentKafkaListenerContainerFactory<String, Object> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(consumerFactory);
        return factory;
    }

    // ── payment.success ────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> paymentSuccessConsumerFactory() {
        Map<String, Object> config = baseConfig();
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.tiffin.common.events.PaymentSuccessEvent");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
            paymentSuccessListenerFactory() {
        return buildFactory(paymentSuccessConsumerFactory());
    }

    // ── payment.failed ─────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> paymentFailedConsumerFactory() {
        Map<String, Object> config = baseConfig();
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.tiffin.common.events.PaymentFailedEvent");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
            paymentFailedListenerFactory() {
        return buildFactory(paymentFailedConsumerFactory());
    }

    // ── payment.refunded ───────────────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> paymentRefundedConsumerFactory() {
        Map<String, Object> config = baseConfig();
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.tiffin.common.events.PaymentRefundedEvent");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
            paymentRefundedListenerFactory() {
        return buildFactory(paymentRefundedConsumerFactory());
    }

    // ── provider.registered ────────────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> providerRegisteredConsumerFactory() {
        Map<String, Object> config = baseConfig();
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.tiffin.common.events.ProviderRegisteredEvent");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
            providerRegisteredListenerFactory() {
        return buildFactory(providerRegisteredConsumerFactory());
    }

    // ── order.placed ───────────────────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> orderPlacedConsumerFactory() {
        Map<String, Object> config = baseConfig();
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.tiffin.common.events.OrderPlacedEvent");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
            orderPlacedListenerFactory() {
        return buildFactory(orderPlacedConsumerFactory());
    }
    
    // ---- order.cancelled --------------------------------------------
    @Bean
    public ConsumerFactory<String, Object> orderCancelledConsumerFactory() {
    	Map<String, Object> config = baseConfig();
    	config.put(JsonDeserializer.VALUE_DEFAULT_TYPE, "com.tiffin.common.events.OrderCancelledEvent");
    	return new DefaultKafkaConsumerFactory<>(config);
    }
    
    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object> orderCancelledListenerFactory() {
    	return buildFactory(orderCancelledConsumerFactory());
    }

    // ── order.status.updated ───────────────────────────────────

    @Bean
    public ConsumerFactory<String, Object> orderStatusUpdatedConsumerFactory() {
        Map<String, Object> config = baseConfig();
        config.put(JsonDeserializer.VALUE_DEFAULT_TYPE,
                "com.tiffin.common.events.OrderStatusUpdatedEvent");
        return new DefaultKafkaConsumerFactory<>(config);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, Object>
            orderStatusUpdatedListenerFactory() {
        return buildFactory(orderStatusUpdatedConsumerFactory());
    }
}