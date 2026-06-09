package com.tiffin.dto;

import com.tiffin.common.enums.MenuCategory;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO — what clients see.
 * Never return the raw MenuItem entity from controllers.
 * isAvailable=false items are hidden from public listing
 * but still returned to the owning provider.
 */
public class MenuItemResponse {

    private String id;
    private String providerId;
    private String name;
    private String description;
    private BigDecimal price;
    private MenuCategory category;
    private boolean isVeg;
    private boolean isAvailable;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // ── Getters & Setters ──────────────────────────────────────

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public MenuCategory getCategory() { return category; }
    public void setCategory(MenuCategory category) { this.category = category; }

    public boolean isVeg() { return isVeg; }
    public void setVeg(boolean veg) { isVeg = veg; }

    public boolean isAvailable() { return isAvailable; }
    public void setAvailable(boolean available) { isAvailable = available; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}