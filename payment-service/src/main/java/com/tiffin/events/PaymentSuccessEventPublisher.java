package com.tiffin.events;

import java.math.BigDecimal;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.events.PaymentSuccessEvent;

@Component
public class PaymentSuccessEventPublisher {
	
    private static final Logger log =
            LoggerFactory.getLogger(PaymentSuccessEventPublisher.class);
    
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentSuccessEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    /**
     * Publishes PAYMENT_SUCCESS event once we get payment success webhook from razorpay.
     * order-service consumes this to update status to CONFIRMED.
     */
    
    public void publishPaymentSuccess(String paymentId, String orderId, String userId, BigDecimal amount, String razorpayPaymentId) {
    	PaymentSuccessEvent event = new PaymentSuccessEvent(paymentId, orderId, userId, amount, razorpayPaymentId);
    	
    	kafkaTemplate.send(KafkaTopics.PAYMENT_SUCCESS, orderId, event);
    	
    	log.info("Published PAYMENT_SUCCESS event: paymentId={} orderId={} userId={} amount={} razorpayPaymentId={}",
    			paymentId, orderId, userId, amount, razorpayPaymentId);
    }


}
