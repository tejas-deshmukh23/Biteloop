package com.tiffin.dto;

import com.tiffin.common.enums.MenuCategory;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;

/**
 * Used for both create and update menu item requests.
 * Provider sends this — providerId comes from X-User-Id header, not request body.
 * That way a provider can never create items for another provider.
 */
public class MenuItemRequest {

    @NotBlank(message = "Name is required")
    @Size(max = 150, message = "Name must not exceed 150 characters")
    private String name;

    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    @Digits(integer = 8, fraction = 2, message = "Price format invalid")
    private BigDecimal price;

    @NotNull(message = "Category is required")
    private MenuCategory category;

    @NotNull(message = "isVeg flag is required")
    private Boolean isVeg;

    // ── Getters & Setters ──────────────────────────────────────

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public MenuCategory getCategory() { return category; }
    public void setCategory(MenuCategory category) { this.category = category; }

    public Boolean getIsVeg() { return isVeg; }
    public void setIsVeg(Boolean isVeg) { this.isVeg = isVeg; }
}