package com.tiffin.template;

import com.tiffin.common.events.*;
import com.tiffin.common.enums.OrderStatus;

/**
 * All notification message templates in one place.
 *
 * Why a dedicated template class?
 * - Templates change frequently (marketing wants different wording)
 * - Keeping them here means zero changes to business logic
 * - Phase 2: swap this class for Thymeleaf HTML templates
 *   without touching consumers or channels
 *
 * Each method returns a NotificationContent record
 * containing subject + body — everything a channel needs to send.
 */
public class NotificationTemplates {

    // ── Result container ───────────────────────────────────────

    /**
     * Simple record holding subject + body.
     * Passed from template engine → NotificationService → channel.
     */
    public record NotificationContent(String subject, String body) {}

    // ── Payment Templates ──────────────────────────────────────

    public static NotificationContent paymentSuccess(PaymentSuccessEvent event) {
        String subject = "Payment Confirmed — Order #" + shortId(event.getOrderId());
        String body = """
                Hi there,
                
                Great news! Your payment of ₹%s has been received successfully.
                
                Order ID   : %s
                Payment ID : %s
                Status     : Confirmed ✓
                
                Your tiffin is being prepared and will be delivered soon.
                
                Thank you for choosing Biteloop!
                Team Biteloop
                """.formatted(
                event.getAmount(),
                event.getOrderId(),
                event.getPaymentId()
        );
        return new NotificationContent(subject, body);
    }

    public static NotificationContent paymentFailed(PaymentFailedEvent event) {
        String subject = "Payment Failed — Order #" + shortId(event.getOrderId());
        String body = """
                Hi there,
                
                Unfortunately your payment of ₹%s could not be processed.
                
                Order ID : %s
                Reason   : %s
                
                Your order is still saved. Please retry payment from the app.
                If the amount was deducted, it will be refunded within 5-7 business days.
                
                Need help? Reply to this email.
                Team Biteloop
                """.formatted(
                event.getAmount(),
                event.getOrderId(),
                event.getFailureReason()
        );
        return new NotificationContent(subject, body);
    }

    public static NotificationContent paymentRefunded(PaymentRefundedEvent event) {
        String subject = "Refund Initiated — Order #" + shortId(event.getOrderId());
        String body = """
                Hi there,
                
                Your refund of ₹%s has been initiated successfully.
                
                Order ID  : %s
                Refund ID : %s
                
                The amount will be credited to your original payment method
                within 5-7 business days depending on your bank.
                
                Thank you for your patience.
                Team Biteloop
                """.formatted(
                event.getRefundAmount(),
                event.getOrderId(),
                event.getRazorpayRefundId()
        );
        return new NotificationContent(subject, body);
    }

    // ── Order Templates ────────────────────────────────────────

    public static NotificationContent orderPlacedCustomer(OrderPlacedEvent event) {
        String subject = "Order Placed — #" + shortId(event.getOrderId());
        String body = """
                Hi there,
                
                Your order has been placed successfully!
                
                Order ID         : %s
                Amount           : ₹%s
                Delivery Address : %s
                
                Please complete your payment to confirm the order.
                
                Thank you for choosing Biteloop!
                Team Biteloop
                """.formatted(
                event.getOrderId(),
                event.getTotalAmount(),
                event.getDeliveryAddress()
        );
        return new NotificationContent(subject, body);
    }
    
    public static NotificationContent OrderCancelled(OrderCancelledEvent event) {
        String subject = "Order Cancelled — #" + shortId(event.getOrderId());
        String body = """
                Hi there,
                
                Your order has been cancelled successfully!
                
                Order ID         : %s
                Amount           : ₹%s
                Delivery Address : %s
                
                Thank you for choosing Biteloop!
                Team Biteloop
                """.formatted(
                event.getOrderId(),
                event.getTotalAmount(),
                event.getDeliveryAddress()
        );
        return new NotificationContent(subject, body);
    }

    public static NotificationContent orderPlacedProvider(OrderPlacedEvent event) {
        String subject = "New Order Received — #" + shortId(event.getOrderId());
        String body = """
                Hello,
                
                You have received a new order on Biteloop!
                
                Order ID : %s
                Amount   : ₹%s
                Address  : %s
                
                Please log in to your Biteloop dashboard to view full details
                and start preparing the order.
                
                Team Biteloop
                """.formatted(
                event.getOrderId(),
                event.getTotalAmount(),
                event.getDeliveryAddress()
        );
        return new NotificationContent(subject, body);
    }

    public static NotificationContent orderStatusUpdated(
            OrderStatusUpdatedEvent event) {
        String subject = "Order Update — #" + shortId(event.getOrderId());
        String body = """
                Hi there,
                
                Your order status has been updated.
                
                Order ID   : %s
                Previous   : %s
                Current    : %s
                
                %s
                
                Team Biteloop
                """.formatted(
                event.getOrderId(),
                friendlyStatus(event.getOldStatus()),
                friendlyStatus(event.getNewStatus()),
                statusMessage(event.getNewStatus())
        );
        return new NotificationContent(subject, body);
    }

    // ── Provider Templates ─────────────────────────────────────

    public static NotificationContent providerRegistered(
            ProviderRegisteredEvent event) {
        String subject = "New Provider Registration — Action Required";
//        String body = """
//                Hello Admin,
//                
//                A new provider has registered on Biteloop and is awaiting approval.
//                
//                Provider ID   : %s
//                Business Name : %s
//                User ID       : %s
//                
//                Please log in to the admin dashboard to review and approve
//                or reject this provider registration.
//                
//                Team Biteloop
//                """.formatted(
//                event.getProviderId(),
//                event.getBusinessName(),
//                event.getUserId()
//        );
        
        String body = """
                Hello Admin,
                
                A new provider has registered on Biteloop and is awaiting approval.
                
                Provider ID   : %s
                User ID       : %s
                
                Please log in to the admin dashboard to review and approve
                or reject this provider registration.
                
                Team Biteloop
                """.formatted(
                event.getProviderId(),
                event.getUserId()
        );
        return new NotificationContent(subject, body);
    }

    // ── Helpers ────────────────────────────────────────────────

    /**
     * Shortens IDs for display in subjects.
     * ord_abc123def456 → abc123de
     * Keeps subjects readable without exposing full internal IDs.
     */
    private static String shortId(String id) {
        if (id == null) return "N/A";
        // Strip prefix (e.g. "ord_") and take first 8 chars
        String stripped = id.contains("_") ? id.substring(id.indexOf('_') + 1) : id;
        return stripped.length() > 8 ? stripped.substring(0, 8) : stripped;
    }

    /**
     * Human-friendly status labels for customers.
     * Customers don't need to see "OUT_FOR_DELIVERY" — "On the way!" is better.
     */
//    private static String friendlyStatus(OrderStatus status) {
//        if (status == null) return "Unknown";
//        return switch (status) {
//            case PENDING          -> "Payment Pending";
//            case CONFIRMED        -> "Confirmed";
//            case PREPARING        -> "Being Prepared";
//            case READY            -> "Ready for Pickup";
//            case OUT_FOR_DELIVERY -> "Out for Delivery";
//            case DELIVERED        -> "Delivered";
//            case CANCELLED        -> "Cancelled";
//        };
//    }

    	private static String friendlyStatus(OrderStatus status) {
    	    if (status == null) return "Unknown";
    	    return switch (status) {
    	        case PENDING          -> "Payment Pending";
    	        case CONFIRMED        -> "Confirmed";
    	        case PREPARING        -> "Being Prepared";
    	        case READY            -> "Ready for Pickup";
    	        case OUT_FOR_DELIVERY -> "Out for Delivery";
    	        case DELIVERED        -> "Delivered";
    	        case REJECTED         -> "Rejected";
    	        case CANCELLED        -> "Cancelled";
    	    };
    	}
    /**
     * Context-specific message based on new status.
     * Adds a human touch to the notification.
     */
//    private static String statusMessage(OrderStatus status) {
//        if (status == null) return "";
//        return switch (status) {
//            case CONFIRMED        -> "Your order is confirmed. Get ready for your tiffin!";
//            case PREPARING        -> "Your tiffin is being freshly prepared.";
//            case READY            -> "Your tiffin is packed and ready!";
//            case OUT_FOR_DELIVERY -> "Your tiffin is on the way. Should arrive soon!";
//            case DELIVERED        -> "Your tiffin has been delivered. Enjoy your meal!";
//            case CANCELLED        -> "Your order has been cancelled. Refund will be processed if applicable.";
//            default               -> "";
//        };
//    }
    	
	private static String statusMessage(OrderStatus status) {
	    if (status == null) return "";
	    return switch (status) {
	        case CONFIRMED        -> "Your order is confirmed. Get ready for your tiffin!";
	        case PREPARING        -> "Your tiffin is being freshly prepared.";
	        case READY            -> "Your tiffin is packed and ready!";
	        case OUT_FOR_DELIVERY -> "Your tiffin is on the way. Should arrive soon!";
	        case DELIVERED        -> "Your tiffin has been delivered. Enjoy your meal!";
	        case REJECTED         -> "Unfortunately, the provider was unable to accept your order. You will be refunded shortly.";
	        case CANCELLED        -> "Your order has been cancelled. Refund will be processed if applicable.";
	        default               -> "";
	    };
	}
}