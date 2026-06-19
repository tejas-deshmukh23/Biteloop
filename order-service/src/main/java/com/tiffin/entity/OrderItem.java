package com.tiffin.entity;

import com.tiffin.common.constants.PrefixConstants;
import com.tiffin.common.entity.BaseEntity;
import jakarta.persistence.*;
import java.math.BigDecimal;

/**
 * Individual item within an order.
 *
 * menuItemId is a cross-service reference — plain String, no FK.
 * itemName and itemPrice are SNAPSHOTS at order placement time.
 *
 * Why snapshots?
 * Provider may change menu item name or price after order is placed.
 * The order receipt must always show what the customer actually ordered
 * and what they paid — not the current menu state.
 * This is exactly how Swiggy/Zomato work.
 *
 * orderId FK is fine — Order and OrderItem are in the same database.
 */
@Entity
@Table(
    name = "order_items",
    indexes = {
        @Index(name = "idx_order_items_order", columnList = "order_id"),
        @Index(name = "idx_order_items_menu", columnList = "menu_item_id")
    }
)
public class OrderItem extends BaseEntity {

    /**
     * FK to orders table — within same database, so FK is fine here.
     * FetchType.LAZY — don't load full order when loading an item.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    /**
     * Cross-service reference — plain String, no JPA relationship.
     * Points to MenuItem in menu-service database.
     */
    @Column(name = "menu_item_id", nullable = false, length = 40)
    private String menuItemId;

    /**
     * Snapshot of menu item name at order time.
     * Never changes even if provider renames the item later.
     */
    @Column(name = "item_name", nullable = false, length = 150)
    private String itemName;

    /**
     * Snapshot of price at order time.
     * Never changes even if provider changes price later.
     */
    @Column(name = "item_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal itemPrice;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    /**
     * itemPrice × quantity — calculated at order time.
     * Stored for quick retrieval without recalculation.
     */
    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal;

    // ── Constructors ───────────────────────────────────────────

    public OrderItem() {}

    public OrderItem(Order order, String menuItemId, String itemName,
                     BigDecimal itemPrice, Integer quantity) {
        this.order = order;
        this.menuItemId = menuItemId;
        this.itemName = itemName;
        this.itemPrice = itemPrice;
        this.quantity = quantity;
        this.subtotal = itemPrice.multiply(BigDecimal.valueOf(quantity));
    }

    // ── BaseEntity ─────────────────────────────────────────────

    @Override
    protected String getIdPrefix() {
        return PrefixConstants.ORDER_ITEM;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }

    public String getMenuItemId() { return menuItemId; }
    public void setMenuItemId(String menuItemId) { this.menuItemId = menuItemId; }

    public String getItemName() { return itemName; }
    public void setItemName(String itemName) { this.itemName = itemName; }

    public BigDecimal getItemPrice() { return itemPrice; }
    public void setItemPrice(BigDecimal itemPrice) { this.itemPrice = itemPrice; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    @Override
    public String toString() {
        return "OrderItem{" +
                "id='" + getId() + '\'' +
                ", menuItemId='" + menuItemId + '\'' +
                ", itemName='" + itemName + '\'' +
                ", itemPrice=" + itemPrice +
                ", quantity=" + quantity +
                ", subtotal=" + subtotal +
                '}';
    }
}