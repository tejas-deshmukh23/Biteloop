package com.tiffin.repository;

import com.tiffin.entity.MenuItem;
import com.tiffin.common.enums.MenuCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MenuItemRepository extends JpaRepository<MenuItem, String> {

    // All items for a provider (for provider's own dashboard — includes unavailable)
    List<MenuItem> findByProviderId(String providerId);

    // Only available items for a provider (for public listing)
    List<MenuItem> findByProviderIdAndIsAvailableTrue(String providerId);

    // Filter by veg/non-veg within a provider's available items
    List<MenuItem> findByProviderIdAndIsAvailableTrueAndIsVeg(String providerId, boolean isVeg);

    // Filter by category within a provider's available items
    List<MenuItem> findByProviderIdAndIsAvailableTrueAndCategory(String providerId, MenuCategory category);

    // Used to verify ownership before update/delete
    Optional<MenuItem> findByIdAndProviderId(String id, String providerId);
}