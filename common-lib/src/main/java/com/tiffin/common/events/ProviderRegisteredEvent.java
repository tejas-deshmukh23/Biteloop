package com.tiffin.common.events;

/**
 * Published by provider-service after provider registration.
 * Consumed by user-service to update providerId on User entity.
 *
 * Replaces: UserServiceClient REST call in provider-service
 */
public class ProviderRegisteredEvent {

    private String userId;      // usr_ id of the provider user
    private String providerId;  // prv_ id of the new provider

    public ProviderRegisteredEvent() {}

    public ProviderRegisteredEvent(String userId, String providerId) {
        this.userId = userId;
        this.providerId = providerId;
    }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getProviderId() { return providerId; }
    public void setProviderId(String providerId) { this.providerId = providerId; }
}