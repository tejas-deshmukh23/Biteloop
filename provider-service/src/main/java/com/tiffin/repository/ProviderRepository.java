package com.tiffin.repository;

import com.tiffin.common.enums.ProviderStatus;
import com.tiffin.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProviderRepository extends JpaRepository<Provider, String> {

    // Check if this user already has a provider profile
    boolean existsByOwnerId(String ownerId);

    // Find provider by owner — used when provider logs in
    Optional<Provider> findByOwnerId(String ownerId);

    // Admin — get all providers by status
    List<Provider> findByStatus(ProviderStatus status);

    // Get all approved active providers — shown to customers
    List<Provider> findByStatusAndIsActive(ProviderStatus status, boolean isActive);
}