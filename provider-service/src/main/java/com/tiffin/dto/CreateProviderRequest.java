package com.tiffin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request body for POST /api/providers/register
 * ownerId comes from X-User-Id header — not from request body
 */
public class CreateProviderRequest {

    @NotBlank(message = "Business name is required")
    @Size(min = 2, max = 150, message = "Business name must be between 2 and 150 characters")
    private String businessName;

    @Size(max = 500, message = "Description cannot exceed 500 characters")
    private String description;

    @NotBlank(message = "Address is required")
    private String address;

    private Double latitude;
    private Double longitude;
    private Double deliveryRadiusKm;
    private boolean deliveryAvailable = false;

    // ── Constructors ───────────────────────────────────────────────────────
    public CreateProviderRequest() {}

    // ── Getters and Setters ────────────────────────────────────────────────
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
}