package com.tiffin.entity;

import com.tiffin.common.constants.PrefixConstants;
import com.tiffin.common.entity.BaseEntity;
import com.tiffin.common.enums.MenuCategory;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Maps to 'menu_items' table in tiffin_menus database.
 *
 * providerId is a plain String reference — no FK because
 * provider lives in a different service/database.
 *
 * Price uses BigDecimal — NEVER float/double for money.
 * isVeg is critical for Indian market filtering.
 */
@Entity
@Table(
    name = "menu_items",
    indexes = {
        @Index(name = "idx_menu_provider", columnList = "provider_id"),
        @Index(name = "idx_menu_provider_available", columnList = "provider_id, is_available"),
        @Index(name = "idx_menu_category", columnList = "category")
    }
)
public class MenuItem extends BaseEntity {

    /**
     * Cross-service reference to providers table.
     * Stored as plain String — no JPA relationship, no FK.
     */
    @Column(name = "provider_id", nullable = false, length = 40)
    private String providerId;

    @Column(name = "name", nullable = false, length = 150)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    /**
     * BigDecimal with precision 10, scale 2.
     * Handles up to ₹99,999,999.99 — more than enough.
     * NEVER use float/double for currency.
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * Stored as VARCHAR — same reason as UserRole.
     * ORDINAL would break if we reorder or insert enum values.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false, length = 20)
    private MenuCategory category;

    /**
     * Veg/Non-veg flag — critical for Indian market.
     * Customers frequently filter by this.
     */
    @Column(name = "is_veg", nullable = false)
    private boolean isVeg;

    /**
     * Soft availability toggle.
     * Provider marks item unavailable (sold out, seasonal)
     * without deleting it. History preserved.
     */
    @Column(name = "is_available", nullable = false)
    private boolean isAvailable = true;

    // ── Constructors ───────────────────────────────────────────

    public MenuItem() {}

    public MenuItem(String providerId, String name, String description,
                    BigDecimal price, MenuCategory category, boolean isVeg) {
        this.providerId = providerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.category = category;
        this.isVeg = isVeg;
        this.isAvailable = true;
    }

    // ── BaseEntity ─────────────────────────────────────────────

    @Override
    protected String getIdPrefix() {
        return PrefixConstants.MENU_ITEM;
    }

    // ── Getters & Setters ──────────────────────────────────────

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

    @Override
    public String toString() {
        return "MenuItem{" +
                "id='" + getId() + '\'' +
                ", providerId='" + providerId + '\'' +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", category=" + category +
                ", isVeg=" + isVeg +
                ", isAvailable=" + isAvailable +
                '}';
    }
}