package com.tiffin.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

/**
 * Full order placement request from customer.
 * userId comes from X-User-Id header — never from request body.
 * providerId must be sent by client (customer chose this provider).
 */
public class OrderRequest {

    @NotBlank(message = "Provider ID is required")
    private String providerId;

    @NotBlank(message = "Delivery address is required")
    @Size(max = 500, message = "Address too long")
    private String deliveryAddress;

    @Size(max = 300, message = "Notes too long")
    private String notes;

    @NotEmpty(message = "Order must have at least one item")
    @Valid  // triggers validation on each OrderItemRequest
    private List<OrderItemRequest> items;

    // ── Getters & Setters ──────────────────────────────────────

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<OrderItemRequest> getItems() { return items; }
    public void setItems(List<OrderItemRequest> items) { this.items = items; }
}