package com.tiffin.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.enums.OrderStatus;
import com.tiffin.common.events.PaymentRefundedEvent;
import com.tiffin.common.events.PaymentSuccessEvent;
import com.tiffin.service.OrderService;

@Component
public class OrderEventConsumer {
	
	private static final Logger log = LoggerFactory.getLogger(OrderEventConsumer.class);
	
	private final OrderService orderService;
	
	public OrderEventConsumer(OrderService orderService) {
		this.orderService = orderService;
	}
	
	@KafkaListener(
			topics = KafkaTopics.PAYMENT_SUCCESS,
			groupId = "order-service-group",
			containerFactory = "paymentSuccessListenerFactory"
	)
	public void handlePaymentSuccess(PaymentSuccessEvent event) {
		
		log.info("Received payment_success event : ");
		
		try {
			//as this is payment_success event means payment got succeeded and we can set orderStatus to success
			orderService.updateOrderStatusInternal(event.getOrderId(), OrderStatus.CONFIRMED);
		}catch(Exception e) {
			 log.error("Failed to process payment_success event: {}",
	                    e.getMessage());
	            // In production — implement retry/DLQ here

		}
		
	}
	
	@KafkaListener(
			topics = KafkaTopics.PAYMENT_REFUNDED,
			groupId = "order-service-group",
			containerFactory = "paymentRefundedListenerFactory"
	)
	public void handlePaymentRefunded(PaymentRefundedEvent event) {
		log.info("Received payment_refunded event : ");
		
		try {
			orderService.updateOrderStatusInternal(event.getOrderId(), OrderStatus.CANCELLED);
		}catch(Exception e) {
			log.error("Failed to process payment_refunded event: {}",
					e.getMessage());
			// In production - implement retry/DLQ here
		}
	}

}
