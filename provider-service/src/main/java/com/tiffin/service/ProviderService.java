package com.tiffin.service;

import com.tiffin.dto.CreateProviderRequest;
import com.tiffin.dto.ProviderResponse;
import com.tiffin.dto.UpdateProviderRequest;

import java.util.List;

public interface ProviderService {

    /**
     * Register a new provider profile.
     * ownerId comes from JWT (X-User-Id header).
     * Status starts as PENDING — admin must approve.
     */
    ProviderResponse registerProvider(String ownerId, CreateProviderRequest request);

    /**
     * Get provider profile by ownerId.
     * Used by provider to see their own profile.
     */
    ProviderResponse getProviderByOwnerId(String ownerId);

    /**
     * Get provider by providerId.
     * Used by customers to view a specific provider.
     */
    ProviderResponse getProviderById(String providerId);

    /**
     * Update provider business details.
     * Only the owner can update their profile.
     */
    ProviderResponse updateProvider(String ownerId, UpdateProviderRequest request);

    /**
     * Get all APPROVED providers — shown to customers.
     */
    List<ProviderResponse> getAllApprovedProviders();

    /**
     * Admin — get all PENDING providers for approval.
     */
    List<ProviderResponse> getPendingProviders();

    /**
     * Admin — approve a provider.
     * Provider becomes visible to customers after this.
     */
    ProviderResponse approveProvider(String providerId);

    /**
     * Admin — reject a provider.
     */
    ProviderResponse rejectProvider(String providerId);
}