package com.tiffin.dto;

import com.tiffin.common.enums.OrderStatus;
import jakarta.validation.constraints.NotNull;

/**
 * Provider sends this to update order status.
 * Only providers can move orders forward.
 * Only customers can cancel (and only before CONFIRMED).
 */
public class StatusUpdateRequest {

    @NotNull(message = "Status is required")
    private OrderStatus status;

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}