package com.tiffin.consumer;

import com.tiffin.common.constants.KafkaTopics;
import com.tiffin.common.events.*;
import com.tiffin.entity.NotificationEventType;
import com.tiffin.router.NotificationRouter;
import com.tiffin.service.NotificationService;
import com.tiffin.template.NotificationTemplates;
import com.tiffin.template.NotificationTemplates.NotificationContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * Consumes all 6 Kafka topics and triggers notifications.
 *
 * Each listener method follows the same pattern:
 * 1. Log the event received
 * 2. Build NotificationContent from template
 * 3. Determine recipient
 * 4. Call NotificationService.send() with appropriate channels
 *
 * Error handling strategy:
 * If an exception propagates out of a @KafkaListener method,
 * Kafka will retry the message. Since our NotificationService
 * handles idempotency, retries are safe — duplicates are skipped.
 *
 * We catch exceptions here and log them rather than letting
 * them propagate, to avoid infinite retry loops on bad data.
 */
@Component
public class NotificationEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(NotificationEventConsumer.class);

    private final NotificationService notificationService;
    private final NotificationRouter router;

    @Value("${notification.admin-email}")
    private String adminEmail;

    public NotificationEventConsumer(NotificationService notificationService,
                                     NotificationRouter router) {
        this.notificationService = notificationService;
        this.router = router;
    }

    // ── payment.success ────────────────────────────────────────

    @KafkaListener(
        topics = "payment.success",
        containerFactory = "paymentSuccessListenerFactory"
    )
    public void onPaymentSuccess(PaymentSuccessEvent event) {
        log.info("Received payment.success: paymentId={} userId={}",
                event.getPaymentId(), event.getUserId());
        try {
        	
        	// Old events published before email field was added
            // will have null email — skip them safely
            if (event.getEmail() == null) {
                log.warn("Skipping payment.success — userEmail is null: " +
                        "paymentId={}", event.getPaymentId());
                return;
            }
        	
            NotificationContent content =
                    NotificationTemplates.paymentSuccess(event);

            notificationService.send(
                    event.getUserId(),
                    event.getPaymentId(),      // eventId for idempotency
                    NotificationEventType.PAYMENT_SUCCESS,
                    event.getEmail(),       // recipient
                    content,
                    router.forPaymentSuccess()
            );
        } catch (Exception e) {
            log.error("Failed to process payment.success notification: " +
                    "paymentId={} error={}", event.getPaymentId(), e.getMessage());
        }
    }

    // ── payment.failed ─────────────────────────────────────────

    @KafkaListener(
        topics = "payment.failed",
        containerFactory = "paymentFailedListenerFactory"
    )
    public void onPaymentFailed(PaymentFailedEvent event) {
        log.info("Received payment.failed: paymentId={} userId={}",
                event.getPaymentId(), event.getUserId());
        try {
        	
        	// Old events published before email field was added
            // will have null email — skip them safely
            if (event.getEmail() == null) {
                log.warn("Skipping payment.failed — userEmail is null: " +
                        "paymentId={}", event.getPaymentId());
                return;
            }
        	
            NotificationContent content =
                    NotificationTemplates.paymentFailed(event);

            notificationService.send(
                    event.getUserId(),
                    event.getPaymentId(),
                    NotificationEventType.PAYMENT_FAILED,
                    event.getEmail(),
                    content,
                    router.forPaymentFailed()
            );
        } catch (Exception e) {
            log.error("Failed to process payment.failed notification: " +
                    "paymentId={} error={}", event.getPaymentId(), e.getMessage());
        }
    }

    // ── payment.refunded ───────────────────────────────────────

    @KafkaListener(
        topics = "payment.refunded",
        containerFactory = "paymentRefundedListenerFactory"
    )
    public void onPaymentRefunded(PaymentRefundedEvent event) {
        log.info("Received payment.refunded: orderId={} userId={}",
                event.getOrderId(), event.getUserId());
        try {
        	
        	// Old events published before email field was added
            // will have null email — skip them safely
            if (event.getEmail() == null) {
                log.warn("Skipping payment.refunded — userEmail is null: " +
                        "paymentId={}", event.getPaymentId());
                return;
            }
        	
            NotificationContent content =
                    NotificationTemplates.paymentRefunded(event);

            notificationService.send(
                    event.getUserId(),
                    event.getRazorpayRefundId(),  // eventId for idempotency
                    NotificationEventType.PAYMENT_REFUNDED,
                    event.getEmail(),
                    content,
                    router.forPaymentRefunded()
            );
        } catch (Exception e) {
            log.error("Failed to process payment.refunded notification: " +
                    "orderId={} error={}", event.getOrderId(), e.getMessage());
        }
    }

    // ── provider.registered ────────────────────────────────────

    @KafkaListener(
        topics = KafkaTopics.PROVIDER_REGISTERED,
        containerFactory = "providerRegisteredListenerFactory"
    )
    public void onProviderRegistered(ProviderRegisteredEvent event) {
        log.info("Received provider.registered: providerId={}",
                event.getProviderId());
        try {
        	
            NotificationContent content =
                    NotificationTemplates.providerRegistered(event);

            notificationService.send(
                    "ADMIN",                      // userId for log
                    event.getProviderId(),        // eventId for idempotency
                    NotificationEventType.PROVIDER_REGISTERED,
                    adminEmail,                   // always goes to admin
                    content,
                    router.forProviderRegistered()
            );
        } catch (Exception e) {
            log.error("Failed to process provider.registered notification: " +
                    "providerId={} error={}", event.getProviderId(), e.getMessage());
        }
    }

    // ── order.placed ───────────────────────────────────────────

    @KafkaListener(
        topics = KafkaTopics.ORDER_PLACED,
        containerFactory = "orderPlacedListenerFactory"
    )
    public void onOrderPlaced(OrderPlacedEvent event) {
        log.info("Received order.placed: orderId={} userId={}",
                event.getOrderId(), event.getUserId());
        try {
        	
        	// Old events published before email field was added
            // will have null email — skip them safely
            if (event.getEmail() == null) {
                log.warn("Skipping order.placed — userEmail is null: " +
                        "orderId={}", event.getOrderId());
                return;
            }
        	
            // Notify customer — order received, complete payment
            NotificationContent customerContent =
                    NotificationTemplates.orderPlacedCustomer(event);

            notificationService.send(
                    event.getUserId(),
                    event.getOrderId() + "_customer", // suffix prevents
                    NotificationEventType.ORDER_PLACED,// collision with
                    event.getEmail(),              // provider log entry
                    customerContent,
                    router.forOrderPlaced()
            );

            ///for now we will not be notifying provider as our event contains only customer event and we don't want to store event in order entity'
            
//            // Notify provider — new order waiting
//            NotificationContent providerContent =
//                    NotificationTemplates.orderPlacedProvider(event);
//
//            notificationService.send(
//                    event.getProviderId(),
//                    event.getOrderId() + "_provider", // different eventId
//                    NotificationEventType.ORDER_PLACED,
//                    event.getEmail(),
//                    providerContent,
//                    router.forOrderPlaced()
//            );

        } catch (Exception e) {
            log.error("Failed to process order.placed notification: " +
                    "orderId={} error={}", event.getOrderId(), e.getMessage());
        }
    }
    
    // ---- order.cancelled --------------------------------------------
    @KafkaListener(
    		topics = KafkaTopics.ORDER_CANCELLED,
    		containerFactory = "orderCancelledListenerFactory"
    )
    public void onOrderCancelled(OrderCancelledEvent event) {
    	 log.info("Received order.cancelled: orderId={} userId={}",
                 event.getOrderId(), event.getUserId());
    	 
    	 try {
    		 //old events published before email field was added
    		 // will have null email - skip them safely
    		 if(event.getEmail() == null) {
    			 log.warn("Skipping order.cancelled — userEmail is null: " +
                         "orderId={}", event.getOrderId());
                 return;
    		 }
    		 // Notify customer — order received, complete payment
    		 NotificationContent customerContent = NotificationTemplates.OrderCancelled(event);
    		 
    		 notificationService.send(
                     event.getUserId(),
                     event.getOrderId(), // suffix prevents
                     NotificationEventType.ORDER_CANCELLED,// collision with
                     event.getEmail(),              // provider log entry
                     customerContent,
                     router.forOrderCancelled()
             );
    	 }catch(Exception e) {
    		 log.error("Failed to process order.cancelled notification: "+ "orderId={} error={}", event.getOrderId(), e.getMessage());
    	 }
    }

    // ── order.status.updated ───────────────────────────────────

    @KafkaListener(
        topics = KafkaTopics.ORDER_STATUS_UPDATED,
        containerFactory = "orderStatusUpdatedListenerFactory"
    )
    public void onOrderStatusUpdated(OrderStatusUpdatedEvent event) {
        log.info("Received order.status.updated: orderId={} newStatus={}",
                event.getOrderId(), event.getNewStatus());
        try {
        	
        	// Old events published before email field was added
            // will have null email — skip them safely
            if (event.getEmail() == null) {
                log.warn("Skipping order.status.updated — userEmail is null: " +
                        "orderId={}", event.getOrderId());
                return;
            }
        	
            NotificationContent content =
                    NotificationTemplates.orderStatusUpdated(event);

            notificationService.send(
                    event.getUserId(),
                    // eventId includes status so each status change
                    // is a separate idempotency key
                    event.getOrderId() + "_" + event.getNewStatus(),
                    NotificationEventType.ORDER_STATUS_UPDATED,
                    event.getEmail(), //this email is of customer not providerEmail so mail will be also sent to customer
                    content,
                    router.forOrderStatusUpdated()
            );
        } catch (Exception e) {
            log.error("Failed to process order.status.updated notification: " +
                    "orderId={} error={}", event.getOrderId(), e.getMessage());
        }
    }
}