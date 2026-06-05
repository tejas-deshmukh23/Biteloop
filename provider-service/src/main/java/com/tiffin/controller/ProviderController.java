package com.tiffin.controller;

import com.tiffin.common.exception.TiffinException;
import com.tiffin.common.response.ApiResponse;
import com.tiffin.dto.CreateProviderRequest;
import com.tiffin.dto.ProviderResponse;
import com.tiffin.dto.UpdateProviderRequest;
import com.tiffin.service.ProviderService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/providers")
public class ProviderController {

    private static final Logger log = LoggerFactory.getLogger(ProviderController.class);

    private final ProviderService providerService;

    public ProviderController(ProviderService providerService) {
        this.providerService = providerService;
    }

    // ── Register Provider ──────────────────────────────────────────────────

    /**
     * POST /api/providers/register
     * Only users with PROVIDER role can call this.
     * Creates business profile — starts as PENDING.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<ProviderResponse>> registerProvider(
            @RequestHeader("X-User-Id") String ownerId,
            @Valid @RequestBody CreateProviderRequest request) {
    	
    	if (ownerId == null || ownerId.isBlank()) {
            throw TiffinException.badRequest("User ID is required");
        }

        log.info("Provider registration request from ownerId: {}", ownerId);
        ProviderResponse response = providerService.registerProvider(ownerId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(
                        "Provider registered successfully. Awaiting admin approval.",
                        response));
    }

    // ── Get My Provider Profile ────────────────────────────────────────────

    /**
     * GET /api/providers/me
     * Provider sees their own business profile.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<ProviderResponse>> getMyProfile(
            @RequestHeader("X-User-Id") String ownerId) {

        ProviderResponse response = providerService.getProviderByOwnerId(ownerId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── Get Provider by ID ─────────────────────────────────────────────────

    /**
     * GET /api/providers/{id}
     * Public — customers view provider details.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProviderResponse>> getProviderById(
            @PathVariable String id) {

        ProviderResponse response = providerService.getProviderById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // ── Update Provider ────────────────────────────────────────────────────

    /**
     * PUT /api/providers/me
     * Provider updates their business details.
     */
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<ProviderResponse>> updateProvider(
            @RequestHeader("X-User-Id") String ownerId,
            @Valid @RequestBody UpdateProviderRequest request) {

        ProviderResponse response = providerService.updateProvider(ownerId, request);
        return ResponseEntity.ok(ApiResponse.success(
                "Provider updated successfully", response));
    }

    // ── Get All Approved Providers ─────────────────────────────────────────

    /**
     * GET /api/providers
     * Public — customers browse all approved providers.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<List<ProviderResponse>>> getAllProviders() {

        List<ProviderResponse> providers = providerService.getAllApprovedProviders();
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    // ── Admin Endpoints ────────────────────────────────────────────────────

    /**
     * GET /api/providers/admin/pending
     * Admin views all providers waiting for approval.
     */
    @GetMapping("/admin/pending")
    public ResponseEntity<ApiResponse<List<ProviderResponse>>> getPendingProviders() {

        List<ProviderResponse> providers = providerService.getPendingProviders();
        return ResponseEntity.ok(ApiResponse.success(providers));
    }

    /**
     * PUT /api/providers/admin/{id}/approve
     * Admin approves a provider.
     */
    @PutMapping("/admin/{id}/approve")
    public ResponseEntity<ApiResponse<ProviderResponse>> approveProvider(
            @PathVariable String id) {

        log.info("Admin approving provider: {}", id);
        ProviderResponse response = providerService.approveProvider(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Provider approved successfully", response));
    }

    /**
     * PUT /api/providers/admin/{id}/reject
     * Admin rejects a provider.
     */
    @PutMapping("/admin/{id}/reject")
    public ResponseEntity<ApiResponse<ProviderResponse>> rejectProvider(
            @PathVariable String id) {

        log.info("Admin rejecting provider: {}", id);
        ProviderResponse response = providerService.rejectProvider(id);
        return ResponseEntity.ok(ApiResponse.success(
                "Provider rejected", response));
    }
}