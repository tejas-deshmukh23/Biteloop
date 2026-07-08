package com.tiffin.events;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.enums.OrderStatus;
import com.tiffin.common.events.OrderPlacedEvent;
import com.tiffin.common.events.OrderStatusUpdatedEvent;

@Component
public class UpdateOrderStatusEventPublisher {
	
	private static final Logger log = LoggerFactory.getLogger(UpdateOrderStatusEventPublisher.class);
	
	private final KafkaTemplate<String, Object> kafkaTemplate;

	public UpdateOrderStatusEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	/**
	 * Publishes ORDER_PLACED event once the order is placed
	 * initially notification-service will consume this event to send notification
	 */

	public void publishUpdateOrderStatusEvent(String orderId, String userId, String providerId, OrderStatus oldStatus, OrderStatus newStatus, String email) {
		
		OrderStatusUpdatedEvent event = new OrderStatusUpdatedEvent(orderId, userId, providerId, oldStatus, newStatus, email);
		kafkaTemplate.send(KafkaTopics.ORDER_STATUS_UPDATED, orderId, event);
		
		log.info("Published ORDER_STATUS_UPDATED event orderId={} userId={} providerId={} oldStatus={} newStatus={} email={}", orderId, userId, providerId, oldStatus, newStatus, email);
		
	}
}
