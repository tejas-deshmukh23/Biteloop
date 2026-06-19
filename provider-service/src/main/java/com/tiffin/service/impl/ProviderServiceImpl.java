package com.tiffin.service.impl;

import com.tiffin.client.UserServiceClient;
import com.tiffin.common.enums.ProviderStatus;
import com.tiffin.common.exception.TiffinException;
import com.tiffin.dto.CreateProviderRequest;
import com.tiffin.dto.ProviderResponse;
import com.tiffin.dto.UpdateProviderRequest;
import com.tiffin.entity.Provider;
import com.tiffin.repository.ProviderRepository;
import com.tiffin.service.ProviderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ProviderServiceImpl implements ProviderService {

    private static final Logger log = LoggerFactory.getLogger(ProviderServiceImpl.class);

    private final ProviderRepository providerRepository;
    
    private final UserServiceClient userServiceClient;  // ← add this

    public ProviderServiceImpl(ProviderRepository providerRepository,
    		UserServiceClient userServiceClient) {
        this.providerRepository = providerRepository;
        this.userServiceClient = userServiceClient;
    }

    // ── Register Provider ──────────────────────────────────────────────────

    @Override
    public ProviderResponse registerProvider(String ownerId,
                                             CreateProviderRequest request) {
        log.info("Registering provider for ownerId: {}", ownerId);

        // One user can only have one provider profile
        if (providerRepository.existsByOwnerId(ownerId)) {
            throw TiffinException.conflict(
                    "Provider profile already exists for this account");
        }

        Provider provider = new Provider(
                ownerId,
                request.getBusinessName(),
                request.getDescription(),
                request.getAddress()
        );

        provider.setLatitude(request.getLatitude());
        provider.setLongitude(request.getLongitude());
        provider.setDeliveryRadiusKm(request.getDeliveryRadiusKm());
        provider.setDeliveryAvailable(request.isDeliveryAvailable());

        Provider saved = providerRepository.save(provider);
        log.info("Provider registered with id: {} status: PENDING", saved.getId());
        
        // TODO: Replace with Kafka event in Milestone 8
        // Notify user-service to store providerId against this user account
        // so JWT token can include it for provider-specific operations
        userServiceClient.updateProviderId(ownerId, saved.getId());

        return mapToResponse(saved);
    }

    // ── Get Provider by OwnerId ────────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProviderResponse getProviderByOwnerId(String ownerId) {
        Provider provider = providerRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> TiffinException.notFound(
                        "Provider profile not found for this account"));
        return mapToResponse(provider);
    }

    // ── Get Provider by ProviderId ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public ProviderResponse getProviderById(String providerId) {
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> TiffinException.notFound(
                        "Provider not found: " + providerId));
        return mapToResponse(provider);
    }

    // ── Update Provider ────────────────────────────────────────────────────

    @Override
    public ProviderResponse updateProvider(String ownerId,
                                           UpdateProviderRequest request) {
        log.info("Updating provider for ownerId: {}", ownerId);

        Provider provider = providerRepository.findByOwnerId(ownerId)
                .orElseThrow(() -> TiffinException.notFound(
                        "Provider profile not found"));

        if (request.getBusinessName() != null)
            provider.setBusinessName(request.getBusinessName());
        if (request.getDescription() != null)
            provider.setDescription(request.getDescription());
        if (request.getAddress() != null)
            provider.setAddress(request.getAddress());
        if (request.getLatitude() != null)
            provider.setLatitude(request.getLatitude());
        if (request.getLongitude() != null)
            provider.setLongitude(request.getLongitude());
        if (request.getDeliveryRadiusKm() != null)
            provider.setDeliveryRadiusKm(request.getDeliveryRadiusKm());
        if (request.getDeliveryAvailable() != null)
            provider.setDeliveryAvailable(request.getDeliveryAvailable());

        return mapToResponse(providerRepository.save(provider));
    }

    // ── Get All Approved Providers ─────────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProviderResponse> getAllApprovedProviders() {
        return providerRepository
                .findByStatusAndIsActive(ProviderStatus.APPROVED, true)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Get Pending Providers (Admin) ──────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<ProviderResponse> getPendingProviders() {
        return providerRepository
                .findByStatus(ProviderStatus.PENDING)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ── Approve Provider (Admin) ───────────────────────────────────────────

    @Override
    public ProviderResponse approveProvider(String providerId) {
        log.info("Approving provider: {}", providerId);

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> TiffinException.notFound(
                        "Provider not found: " + providerId));

        if (provider.getStatus() != ProviderStatus.PENDING) {
            throw TiffinException.badRequest(
                    "Only PENDING providers can be approved");
        }

        provider.setStatus(ProviderStatus.APPROVED);
        return mapToResponse(providerRepository.save(provider));
    }

    // ── Reject Provider (Admin) ────────────────────────────────────────────

    @Override
    public ProviderResponse rejectProvider(String providerId) {
        log.info("Rejecting provider: {}", providerId);

        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> TiffinException.notFound(
                        "Provider not found: " + providerId));

        if (provider.getStatus() != ProviderStatus.PENDING) {
            throw TiffinException.badRequest(
                    "Only PENDING providers can be rejected");
        }

        provider.setStatus(ProviderStatus.REJECTED);
        return mapToResponse(providerRepository.save(provider));
    }

    // ── Private Helper ─────────────────────────────────────────────────────

    private ProviderResponse mapToResponse(Provider provider) {
        return new ProviderResponse(
                provider.getId(),
                provider.getOwnerId(),
                provider.getBusinessName(),
                provider.getDescription(),
                provider.getAddress(),
                provider.getLatitude(),
                provider.getLongitude(),
                provider.getDeliveryRadiusKm(),
                provider.isDeliveryAvailable(),
                provider.getStatus(),
                provider.isActive(),
                provider.getCreatedAt()
        );
    }
}