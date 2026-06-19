package com.tiffin.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

/**
 * Represents one item in an order request.
 * Customer sends menuItemId, name, price, quantity.
 *
 * Why do we take itemName and itemPrice from the client?
 * Because menu-service lives in a different database.
 * We cannot do a DB join across services.
 * Customer's frontend already has this info from browsing the menu.
 *
 * Is this safe? Yes — totalAmount is recalculated server-side.
 * We never trust client-sent totals for payment.
 */
public class OrderItemRequest {

    @NotBlank(message = "Menu item ID is required")
    private String menuItemId;

    @NotBlank(message = "Item name is required")
    private String itemName;

    @NotNull(message = "Item price is required")
    @Min(value = 0, message = "Price cannot be negative")
    private BigDecimal itemPrice;

    @NotNull(message = "Quantity is required")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // ── Getters & Setters ──────────────────────────────────────

    public String getMenuItemId() { return menuItemId; }
    public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getItemPrice() { return itemPrice; }
    public void setItemPrice(BigDecimal itemPrice) { this.itemPrice = itemPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }
}