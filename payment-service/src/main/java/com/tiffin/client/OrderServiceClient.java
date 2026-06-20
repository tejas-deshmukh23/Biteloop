package com.tiffin.client;

import com.tiffin.common.enums.OrderStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.math.BigDecimal;
import java.util.Map;

/**
 * HTTP client for calling order-service internal endpoints.
 *
 * Used by payment-service to:
 * 1. Fetch order amount for verification before charging
 * 2. Update order status after payment success/failure
 *
 * TODO: Replace with Kafka events in Milestone 8
 * PAYMENT_SUCCESS event → order-service consumer updates status
 * Current approach creates temporary coupling — acceptable for MVP
 */
@Component
public class OrderServiceClient {

    private static final Logger log =
            LoggerFactory.getLogger(OrderServiceClient.class);

    private final RestTemplate restTemplate;
    private final String orderServiceUrl;

    public OrderServiceClient(
            @Value("${services.order-service.url:http://order-service:8084}")
            String orderServiceUrl) {
        this.restTemplate = new RestTemplate();
        this.orderServiceUrl = orderServiceUrl;
    }

    /**
     * Fetch order details from order-service.
     * Used to verify amount before creating Razorpay order.
     * We never trust client-sent amount — always verify from source.
     */
    public Map<String, Object> getOrderDetails(String orderId) {
        try {
            String url = orderServiceUrl +
                    "/api/orders/internal/" + orderId;
            return restTemplate.getForObject(url, Map.class);
        } catch (Exception e) {
            log.error("Failed to fetch order details for orderId={}: {}",
                    orderId, e.getMessage());
            throw new RuntimeException(
                    "Could not fetch order details: " + e.getMessage());
        }
    }

    /**
     * Update order status after payment success or failure.
     * Called from webhook handler after verifying payment.
     */
    public void updateOrderStatus(String orderId, OrderStatus status) {
        try {
            String url = UriComponentsBuilder
                    .fromHttpUrl(orderServiceUrl)
                    .path("/api/orders/internal/{orderId}/status")
                    .queryParam("status", status.name())
                    .buildAndExpand(orderId)
                    .toUriString();

            restTemplate.put(url, null);
            log.info("Updated order status: orderId={} status={}",
                    orderId, status);

        } catch (Exception e) {
            log.error("Failed to update order status: orderId={} status={}: {}",
                    orderId, status, e.getMessage());
            // Don't throw — payment is already processed
            // Order status sync failure is handled by TODO Kafka event
        }
    }
}