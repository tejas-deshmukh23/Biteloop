package com.tiffin.events;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.events.PaymentFailedEvent;

@Component
public class PaymentFailedEventPublisher {
	
	private static final Logger log = LoggerFactory.getLogger(PaymentFailedEventPublisher.class);
	
	private static KafkaTemplate<String, Object> kafkaTemplate;
	
	public PaymentFailedEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
		this.kafkaTemplate = kafkaTemplate;
	}
	
	public void publishPaymentFailed(String paymentId, String orderId, String userId,String failureReason ,BigDecimal amount, String email) {
		
		PaymentFailedEvent event = new PaymentFailedEvent(paymentId, orderId, userId, failureReason, amount, email);
		
		kafkaTemplate.send(KafkaTopics.PAYMENT_FAILED, orderId, event);
		
		log.info("Published PAYMENT_FAILED event: paymentId={} orderId={} userId={} failureReason={} amount={} email={}",
    			paymentId, orderId, userId, failureReason, amount, email);
		
	}

}
