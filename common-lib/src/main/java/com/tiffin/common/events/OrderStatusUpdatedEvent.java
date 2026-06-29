package com.tiffin.common.events;

import com.tiffin.common.enums.OrderStatus;

/**
 * Published by order-service when provider updates order status.
 * Consumed by notification-service (Milestone 9)
 * to notify customer of status changes.
 */
public class OrderStatusUpdatedEvent {

    private String orderId;
    private String userId;
    private String providerId;
    private OrderStatus oldStatus;
    private OrderStatus newStatus;

    public OrderStatusUpdatedEvent() {}

    public OrderStatusUpdatedEvent(String orderId, String userId,
                                   String providerId, OrderStatus oldStatus,
                                   OrderStatus newStatus) {
        this.orderId = orderId;
        this.userId = userId;
        this.providerId = providerId;
        this.oldStatus = oldStatus;
        this.newStatus = newStatus;
    }

    public String getOrderId() { return orderId; }
    public void setOrderId(String orderId) { this.orderId = orderId; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public OrderStatus getOldStatus() { return oldStatus; }
    public void setOldStatus(OrderStatus oldStatus) { this.oldStatus = oldStatus; }

    public OrderStatus getNewStatus() { return newStatus; }
    public void setNewStatus(OrderStatus newStatus) { this.newStatus = newStatus; }
}