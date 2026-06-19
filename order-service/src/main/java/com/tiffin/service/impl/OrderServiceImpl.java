package com.tiffin.service.impl;

import com.tiffin.common.enums.OrderStatus;
import com.tiffin.dto.*;
import com.tiffin.entity.Order;
import com.tiffin.entity.OrderItem;
import com.tiffin.exception.OrderNotFoundException;
import com.tiffin.exception.OrderStatusException;
import com.tiffin.repository.OrderRepository;
import com.tiffin.service.OrderService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;

    public OrderServiceImpl(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    // ── Place Order ────────────────────────────────────────────

    @Override
    public OrderResponse placeOrder(String userId, OrderRequest request) {

        // Calculate total from items — never trust client total
        BigDecimal totalAmount = request.getItems().stream()
                .map(item -> item.getItemPrice()
                        .multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Create order
        Order order = new Order(
                userId,
                request.getProviderId(),
                totalAmount,
                request.getDeliveryAddress(),
                request.getNotes()
        );

        // Create order items and link to order
        List<OrderItem> items = request.getItems().stream()
                .map(itemRequest -> new OrderItem(
                        order,
                        itemRequest.getMenuItemId(),
                        itemRequest.getItemName(),
                        // TODO: SECURITY — itemPrice trusted from client.
                        // Fix before payment-service (Milestone 6):
                        // call menu-service API to verify actual price.
                        itemRequest.getItemPrice(),
                        itemRequest.getQuantity()
                ))
                .collect(Collectors.toList());

        order.setItems(items);

        Order saved = orderRepository.save(order);

        // TODO: Publish ORDER_PLACED Kafka event for notification-service
        // Will implement when Kafka events milestone begins

        return toResponse(saved);
    }

    // ── Customer Queries ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrders(String userId) {
        return orderRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getMyOrdersByStatus(String userId, OrderStatus status) {
        return orderRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(userId, status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Cancel Order ───────────────────────────────────────────

    @Override
    public OrderResponse cancelOrder(String orderId, String userId) {
        Order order = orderRepository.findByIdAndUserId(orderId, userId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found or does not belong to you"));

        // Can only cancel if still PENDING
        // Once provider CONFIRMED, customer cannot cancel
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new OrderStatusException(
                    "Order cannot be cancelled — current status: "
                    + order.getStatus());
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order updated = orderRepository.save(order);

        // TODO: Publish ORDER_CANCELLED Kafka event

        return toResponse(updated);
    }

    // ── Provider Queries ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getProviderOrders(String providerId) {
        return orderRepository.findByProviderIdOrderByCreatedAtDesc(providerId)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> getProviderOrdersByStatus(String providerId,
                                                          OrderStatus status) {
        return orderRepository
                .findByProviderIdAndStatusOrderByCreatedAtDesc(providerId, status)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    // ── Provider Status Update ─────────────────────────────────

    @Override
    public OrderResponse updateOrderStatus(String orderId, String providerId,
                                           StatusUpdateRequest request) {
        Order order = orderRepository.findByIdAndProviderId(orderId, providerId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found or does not belong to your business"));

        validateStatusTransition(order.getStatus(), request.getStatus());

        order.setStatus(request.getStatus());
        Order updated = orderRepository.save(order);

        // TODO: Publish ORDER_STATUS_UPDATED Kafka event

        return toResponse(updated);
    }

    // ── Get Single Order ───────────────────────────────────────

    @Override
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(String orderId, String userId, String role) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new OrderNotFoundException(
                        "Order not found"));

        // ADMIN can see any order
        // CUSTOMER can only see their own
        // PROVIDER can only see orders for their business
        if ("ADMIN".equals(role)) {
            return toResponse(order);
        }

        if ("CUSTOMER".equals(role) && !order.getUserId().equals(userId)) {
            throw new OrderNotFoundException("Order not found");
        }

        if ("PROVIDER".equals(role) && !order.getProviderId().equals(userId)) {
            throw new OrderNotFoundException("Order not found");
        }

        return toResponse(order);
    }

    // ── Status Transition Validator ────────────────────────────

    /**
     * Enforces valid status transitions.
     * Provider cannot skip steps or go backwards.
     *
     * Valid transitions:
     * PENDING    → CONFIRMED, REJECTED
     * CONFIRMED  → PREPARING
     * PREPARING  → READY
     * READY      → OUT_FOR_DELIVERY
     * OUT_FOR_DELIVERY → DELIVERED
     */
    private void validateStatusTransition(OrderStatus current, OrderStatus next) {

        boolean valid = switch (current) {
            case PENDING -> next == OrderStatus.CONFIRMED
                         || next == OrderStatus.REJECTED;
            case CONFIRMED -> next == OrderStatus.PREPARING;
            case PREPARING -> next == OrderStatus.READY;
            case READY -> next == OrderStatus.OUT_FOR_DELIVERY;
            case OUT_FOR_DELIVERY -> next == OrderStatus.DELIVERED;
            default -> false; // DELIVERED, REJECTED, CANCELLED are terminal
        };

        if (!valid) {
            throw new OrderStatusException(
                    "Invalid status transition: " + current + " → " + next);
        }
    }

    // ── Mapper ─────────────────────────────────────────────────

    private OrderResponse toResponse(Order order) {
        OrderResponse response = new OrderResponse();
        response.setId(order.getId());
        response.setUserId(order.getUserId());
        response.setProviderId(order.getProviderId());
        response.setStatus(order.getStatus());
        response.setTotalAmount(order.getTotalAmount());
        response.setDeliveryAddress(order.getDeliveryAddress());
        response.setNotes(order.getNotes());
        response.setCreatedAt(order.getCreatedAt());
        response.setUpdatedAt(order.getUpdatedAt());

        // Map order items
        if (order.getItems() != null) {
            response.setItems(order.getItems().stream()
                    .map(this::toItemResponse)
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private OrderItemResponse toItemResponse(OrderItem item) {
        OrderItemResponse response = new OrderItemResponse();
        response.setId(item.getId());
        response.setMenuItemId(item.getMenuItemId());
        response.setItemName(item.getItemName());
        response.setItemPrice(item.getItemPrice());
        response.setQuantity(item.getQuantity());
        response.setSubtotal(item.getSubtotal());
        return response;
    }
}