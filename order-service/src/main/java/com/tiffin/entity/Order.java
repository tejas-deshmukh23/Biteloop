package com.tiffin.entity;

import com.tiffin.common.constants.PrefixConstants;
import com.tiffin.common.entity.BaseEntity;
import com.tiffin.common.enums.OrderStatus;
import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Maps to 'orders' table in tiffin_orders database.
 *
 * userId and providerId are plain Strings — cross-service references.
 * No FK constraints across services.
 *
 * totalAmount is calculated and stored at order time.
 * Even if menu prices change later, this order reflects
 * what the customer actually paid.
 *
 * OrderItems are owned by this order — cascade all operations.
 */
@Entity
@Table(
    name = "orders",
    indexes = {
        @Index(name = "idx_orders_user", columnList = "user_id"),
        @Index(name = "idx_orders_provider", columnList = "provider_id"),
        @Index(name = "idx_orders_status", columnList = "status"),
        @Index(name = "idx_orders_created", columnList = "created_at")
    }
)
public class Order extends BaseEntity {

    @Column(name = "user_id", nullable = false, length = 40)
    private String userId;

    @Column(name = "provider_id", nullable = false, length = 40)
    private String providerId;
    
    @Column(name = "email", nullable = false, length = 150)
    private String email; //this will be a customer email which we will save when customers order is placed

    /**
     * Stored as VARCHAR — consistent with other enums.
     * ORDINAL would break if enum values are reordered.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    /**
     * Calculated from OrderItems at placement time.
     * Snapshot — never changes after order is placed.
     */
    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount;

    /**
     * Delivery address snapshot at order time.
     * Customer may change address later — order keeps original.
     */
    @Column(name = "delivery_address", nullable = false, length = 500)
    private String deliveryAddress;

    /**
     * Optional special instructions from customer.
     * e.g. "no spice", "extra roti"
     */
    @Column(name = "notes", length = 300)
    private String notes;

    /**
     * OrderItems owned by this order.
     * CascadeType.ALL — save/delete order cascades to items.
     * orphanRemoval — removing item from list deletes it from DB.
     * FetchType.LAZY — don't load items unless explicitly accessed.
     *
     * This FK is fine — Order and OrderItem are in the SAME service/database.
     * FK constraints are only problematic ACROSS service boundaries.
     */
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL,
               orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> items = new ArrayList<>();

    // ── Constructors ───────────────────────────────────────────

    public Order() {}

    public Order(String userId, String providerId,
                 BigDecimal totalAmount, String deliveryAddress, String notes, String email) {
        this.userId = userId;
        this.providerId = providerId;
        this.totalAmount = totalAmount;
        this.deliveryAddress = deliveryAddress;
        this.notes = notes;
        this.status = OrderStatus.PENDING;
        this.email = email;
    }

    // ── BaseEntity ─────────────────────────────────────────────

    @Override
    protected String getIdPrefix() {
        return PrefixConstants.ORDER;
    }

    // ── Getters & Setters ──────────────────────────────────────

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

    public String getDeliveryAddress() { return deliveryAddress; }
    public void setDeliveryAddress(String deliveryAddress) { this.deliveryAddress = deliveryAddress; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public List<OrderItem> getItems() { return items; }
    public void setItems(List<OrderItem> items) { this.items = items; }

    public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	@Override
    public String toString() {
        return "Order{" +
                "id='" + getId() + '\'' +
                ", userId='" + userId + '\'' +
                ", providerId='" + providerId + '\'' +
                ", status=" + status +
                ", totalAmount=" + totalAmount +
                ", createdAt=" + getCreatedAt() +
                '}';
    }
}