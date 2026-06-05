package com.tiffin.entity;

import com.tiffin.common.constants.PrefixConstants;
import com.tiffin.common.entity.BaseEntity;
import com.tiffin.common.enums.ProviderStatus;
import jakarta.persistence.*;

/**
 * Maps to 'providers' table in tiffin_providers database.
 *
 * A Provider is a mess owner or restaurant that serves food.
 * It is ALWAYS linked to a User account (role = PROVIDER)
 * via ownerId — but no FK constraint since different DB.
 *
 * Status flow:
 * PENDING → APPROVED → visible to customers
 * PENDING → REJECTED → cannot operate
 * APPROVED → SUSPENDED → temporarily hidden
 */
@Entity
@Table(
    name = "providers",
    indexes = {
        @Index(name = "idx_providers_owner",  columnList = "owner_id"),
        @Index(name = "idx_providers_status", columnList = "status"),
        @Index(name = "idx_providers_active", columnList = "is_active")
    }
)
public class Provider extends BaseEntity {

    /**
     * References User.id from user-service.
     * Plain String — no FK constraint across databases.
     * Validated implicitly via JWT (gateway confirms user exists).
     */
    @Column(name = "owner_id", nullable = false, updatable = false)
    private String ownerId;

    @Column(name = "business_name", nullable = false, length = 150)
    private String businessName;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "address", nullable = false, length = 255)
    private String address;

    /**
     * Latitude and longitude for location-based discovery.
     * Used to calculate distance from customer.
     * Future: "find mess within 2km of me"
     */
    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    /**
     * How far this provider can deliver in kilometers.
     * Example: 2.5 means they deliver within 2.5km radius.
     * null means pickup/dine-in only.
     */
    @Column(name = "delivery_radius_km")
    private Double deliveryRadiusKm;

    /**
     * Whether this provider offers home delivery.
     * false = customer must come to the mess.
     */
    @Column(name = "delivery_available", nullable = false)
    private boolean deliveryAvailable = false;

    /**
     * Admin approval status.
     * New providers start as PENDING — not visible to customers
     * until admin approves.
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProviderStatus status = ProviderStatus.PENDING;

    /**
     * Soft delete — same pattern as User entity.
     */
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true;

    // ── Constructors ───────────────────────────────────────────────────────

    public Provider() {}

    public Provider(String ownerId, String businessName,
                    String description, String address) {
        this.ownerId = ownerId;
        this.businessName = businessName;
        this.description = description;
        this.address = address;
        this.status = ProviderStatus.PENDING;
        this.isActive = true;
        this.deliveryAvailable = false;
    }

    // ── ID Prefix ──────────────────────────────────────────────────────────

    @Override
    protected String getIdPrefix() {
        return PrefixConstants.PROVIDER;  // "prv_"
    }

    // ── Getters and Setters ────────────────────────────────────────────────

    public String getOwnerId() { return ownerId; }
    public void setOwnerId(String ownerId) { this.ownerId = ownerId; }

    public String getBusinessName() { return businessName; }
    public void setBusinessName(String businessName) { this.businessName = businessName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public Double getDeliveryRadiusKm() { return deliveryRadiusKm; }
    public void setDeliveryRadiusKm(Double deliveryRadiusKm) { this.deliveryRadiusKm = deliveryRadiusKm; }

    public boolean isDeliveryAvailable() { return deliveryAvailable; }
    public void setDeliveryAvailable(boolean deliveryAvailable) { this.deliveryAvailable = deliveryAvailable; }

    public ProviderStatus getStatus() { return status; }
    public void setStatus(ProviderStatus status) { this.status = status; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { isActive = active; }

    @Override
    public String toString() {
        return "Provider{" +
                "id='" + getId() + '\'' +
                ", ownerId='" + ownerId + '\'' +
                ", businessName='" + businessName + '\'' +
                ", status=" + status +
                ", deliveryAvailable=" + deliveryAvailable +
                '}';
    }
}