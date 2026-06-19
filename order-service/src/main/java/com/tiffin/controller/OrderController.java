package com.tiffin.controller;

import com.tiffin.common.enums.OrderStatus;
import com.tiffin.common.response.ApiResponse;
import com.tiffin.dto.OrderRequest;
import com.tiffin.dto.OrderResponse;
import com.tiffin.dto.StatusUpdateRequest;
import com.tiffin.service.OrderService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All routes under /api/orders
 *
 * Auth strategy — same as other services:
 * X-User-Id   → injected by gateway (who is making the request)
 * X-User-Role → injected by gateway (CUSTOMER, PROVIDER, ADMIN)
 *
 * Endpoints:
 * POST   /api/orders                          → customer places order
 * GET    /api/orders/my                       → customer views their orders
 * GET    /api/orders/my?status=PENDING        → customer filters by status
 * DELETE /api/orders/{id}/cancel              → customer cancels order
 * GET    /api/orders/provider                 → provider views incoming orders
 * GET    /api/orders/provider?status=PENDING  → provider filters by status
 * PUT    /api/orders/{id}/status              → provider updates status
 * GET    /api/orders/{id}                     → get single order (any role)
 */
@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // ── Customer Endpoints ─────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<OrderResponse>> placeOrder(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody OrderRequest request) {

        if (!"CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only customers can place orders"));
        }

        OrderResponse response = orderService.placeOrder(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", response));
    }

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getMyOrders(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) OrderStatus status) {

        if (!"CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Access denied"));
        }

        List<OrderResponse> orders = status != null
                ? orderService.getMyOrdersByStatus(userId, status)
                : orderService.getMyOrders(userId);

        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @DeleteMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        if (!"CUSTOMER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only customers can cancel orders"));
        }

        OrderResponse response = orderService.cancelOrder(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", response));
    }

    // ── Provider Endpoints ─────────────────────────────────────

    @GetMapping("/provider")
    public ResponseEntity<ApiResponse<List<OrderResponse>>> getProviderOrders(
    		@RequestHeader("X-Provider-Id") String providerId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) OrderStatus status) {

        if (!"PROVIDER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only providers can access this"));
        }

        List<OrderResponse> orders = status != null
                ? orderService.getProviderOrdersByStatus(providerId, status)
                : orderService.getProviderOrders(providerId);

        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(
            @PathVariable String id,
            @RequestHeader("X-Provider-Id") String providerId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody StatusUpdateRequest request) {

        if (!"PROVIDER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only providers can update order status"));
        }

        OrderResponse response = orderService.updateOrderStatus(id, providerId, request);
        return ResponseEntity.ok(ApiResponse.success("Order status updated", response));
    }

    // ── Shared Endpoints ───────────────────────────────────────

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrderResponse>> getOrderById(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role) {

        OrderResponse response = orderService.getOrderById(id, userId, role);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}