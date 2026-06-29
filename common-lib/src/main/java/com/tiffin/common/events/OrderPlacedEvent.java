package com.tiffin.common.events;

import java.math.BigDecimal;

/**
 * Published by order-service after order is placed.
 * Consumed by notification-service (Milestone 9)
 * to send order confirmation to customer.
 */
public class OrderPlacedEvent {

    private String orderId;
    private String userId;
    private String providerId;
    private BigDecimal totalAmount;
    private String deliveryAddress;

    public OrderPlacedEvent() {}

    public OrderPlacedEvent(String orderId, String userId,
                            String providerId, BigDecimal totalAmount,
                            String deliveryAddress) {
        this.orderId = orderId;
        this.userId = userId;
        this.providerId = providerId;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }
}