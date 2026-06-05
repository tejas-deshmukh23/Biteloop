package com.tiffin.dto;

import jakarta.validation.constraints.Size;

/**
 * Request body for PUT /api/providers/me
 * All fields optional — update only what's needed.
 */
public class UpdateProviderRequest {

    @Size(min = 2, max = 150)
    private String businessName;

    @Size(max = 500)
    private String description;

    private String address;
    private Double latitude;
    private Double longitude;
    private Double deliveryRadiusKm;
    private Boolean deliveryAvailable;

    public UpdateProviderRequest() {}

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

    public Boolean getDeliveryAvailable() { return deliveryAvailable; }
    public void setDeliveryAvailable(Boolean deliveryAvailable) { this.deliveryAvailable = deliveryAvailable; }
}