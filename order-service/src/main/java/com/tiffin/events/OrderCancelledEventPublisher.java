package com.tiffin.events;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.events.OrderCancelledEvent;

@Component
public class OrderCancelledEventPublisher {
	
	private static final Logger log = LoggerFactory.getLogger(OrderCancelledEventPublisher.class);
	
	private final KafkaTemplate<String, Object> kafkaTemplate;
	
	public OrderCancelledEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	/**
	 * Publishes ORDER_CANCELLED event if the order is cancelled
	 * initially notification-service will consume this event to send notification
	 */
	
	public void publishOrderCancelledEvent(String orderId, String userId, String providerId, BigDecimal totalAmount, String deliveryAddress, String email) {
		OrderCancelledEvent event = new OrderCancelledEvent(orderId, userId, providerId, totalAmount, deliveryAddress, email);
		kafkaTemplate.send(KafkaTopics.ORDER_CANCELLED, orderId, event);
		
		log.info("Published ORDER_CANCELLED event orderId={} userId={} providerId={} totalAmount={} deliveryAddress={} email={}", orderId, userId, providerId, totalAmount, deliveryAddress, email);
		
	}

}
