package com.tiffin.repository;

import com.tiffin.common.enums.OrderStatus;
import com.tiffin.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {

    // Customer's order history — newest first
    List<Order> findByUserIdOrderByCreatedAtDesc(String userId);

    // Provider's incoming orders — newest first
    List<Order> findByProviderIdOrderByCreatedAtDesc(String providerId);

    // Provider's orders filtered by status
    // e.g. show all PENDING orders that need acceptance
    List<Order> findByProviderIdAndStatusOrderByCreatedAtDesc(
            String providerId, OrderStatus status);

    // Customer's orders filtered by status
    List<Order> findByUserIdAndStatusOrderByCreatedAtDesc(
            String userId, OrderStatus status);

    // Verify order belongs to this customer before cancel
    Optional<Order> findByIdAndUserId(String id, String userId);

    // Verify order belongs to this provider before status update
    Optional<Order> findByIdAndProviderId(String id, String providerId);
}