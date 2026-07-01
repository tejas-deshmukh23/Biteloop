package com.tiffin.events;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.events.PaymentFailedEvent;
import com.tiffin.common.events.PaymentRefundedEvent;
import com.tiffin.common.events.PaymentSuccessEvent;

@Component
public class PaymentRefundEventPublisher {
	
	private static final Logger log =
            LoggerFactory.getLogger(PaymentSuccessEventPublisher.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentRefundEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Publishes PAYMENT_REFUNDED event once we get payment REfund webhook from razorpay.
     * order-service consumes this to update status to CANCELLED.
     */
    
    public void publishPaymentRefund(String paymentId, String orderId, String userId, BigDecimal refundAmount, String razorpayRefundId) {
    	PaymentRefundedEvent event = new PaymentRefundedEvent(paymentId, orderId, userId, refundAmount, razorpayRefundId);
    	
    	kafkaTemplate.send(KafkaTopics.PAYMENT_REFUNDED, orderId, event);
    	
    	log.info("Published PAYMENT_FAILED event: paymentId={} orderId={} userId={} refundAmount={} razorpayRefundId={}",
    			paymentId, orderId, userId, refundAmount, razorpayRefundId);
    }

}
