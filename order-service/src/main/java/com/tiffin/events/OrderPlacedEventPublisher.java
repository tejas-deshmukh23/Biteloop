package com.tiffin.events;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.events.OrderPlacedEvent;

@Component
public class OrderPlacedEventPublisher {
	
	private static final Logger log = LoggerFactory.getLogger(OrderPlacedEventPublisher.class);
	
	private final KafkaTemplate<String, Object> kafkaTemplate;

	public OrderPlacedEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	/**
	 * Publishes ORDER_PLACED event once the order is placed
	 * initially notification-service will consume this event to send notification
	 */

	public void publishOrderPlacedEvent(String orderId, String userId, String providerId, BigDecimal totalAmount, String deliveryAddress, String email) {
		
		OrderPlacedEvent event = new OrderPlacedEvent(orderId, userId, providerId, totalAmount, deliveryAddress, email);
		kafkaTemplate.send(KafkaTopics.ORDER_PLACED, orderId, event);
		
		log.info("Published ORDER_PLACED event orderId={} userId={} providerId={} totalAmount={} deliveryAddress={} email={}", orderId, userId, providerId, totalAmount, deliveryAddress, email);
		
	}
}
