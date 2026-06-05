package com.tiffin.dto;

import com.tiffin.common.enums.ProviderStatus;
import java.time.LocalDateTime;

/**
 * Returned for all provider responses.
 */
public class ProviderResponse {

    private String id;
    private String ownerId;
    private String businessName;
    private String description;
    private String address;
    private Double latitude;
    private Double longitude;
    private Double deliveryRadiusKm;
    private boolean deliveryAvailable;
    private ProviderStatus status;
    private boolean isActive;
    private LocalDateTime createdAt;

    public ProviderResponse() {}

    public ProviderResponse(String id, String ownerId, String businessName,
                            String description, String address, Double latitude,
                            Double longitude, Double deliveryRadiusKm,
                            boolean deliveryAvailable, ProviderStatus status,
                            boolean isActive, LocalDateTime createdAt) {
        this.id = id;
        this.ownerId = ownerId;
        this.businessName = businessName;
        this.description = description;
        this.address = address;
        this.latitude = latitude;
        this.longitude = longitude;
        this.deliveryRadiusKm = deliveryRadiusKm;
        this.deliveryAvailable = deliveryAvailable;
        this.status = status;
        this.isActive = isActive;
        this.createdAt = createdAt;
    }

    // ── Getters and Setters ────────────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

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

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}