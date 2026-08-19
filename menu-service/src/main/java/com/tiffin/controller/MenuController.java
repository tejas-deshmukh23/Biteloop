package com.tiffin.controller;

import com.tiffin.common.enums.MenuCategory;
import com.tiffin.common.response.ApiResponse;
import com.tiffin.dto.MenuItemRequest;
import com.tiffin.dto.MenuItemResponse;
import com.tiffin.service.MenuService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * All routes under /api/menu
 *
 * Auth strategy:
 *   - X-User-Id     → who is making the request (injected by gateway)
 *   - X-Provider-Id → their business ID (prv_xxx) — injected by gateway
 *                     only present when X-User-Role = PROVIDER
 *   - X-User-Role   → their role (PROVIDER, CUSTOMER, ADMIN)
 *
 * Why X-Provider-Id and not X-User-Id for provider operations?
 * Menu items belong to the provider business (prv_xxx), not the
 * user account (usr_xxx). Customers query menu by providerId —
 * so we must store and match using prv_xxx consistently.
 *
 * No Spring Security here — gateway handles token validation.
 * We trust these headers (in prod they only come from the gateway).
 *
 * Endpoints:
 *   POST   /api/menu                              → provider creates item
 *   GET    /api/menu/provider/{providerId}        → public listing (cached)
 *   GET    /api/menu/my                           → provider's own full list
 *   GET    /api/menu/provider/{providerId}/filter → filter by veg/category
 *   PUT    /api/menu/{id}                         → provider updates item
 *   PATCH  /api/menu/{id}/toggle                  → toggle availability
 *   DELETE /api/menu/{id}                         → soft delete
 */
@RestController
@RequestMapping("/api/menu")
public class MenuController {

    private final MenuService menuService;

    public MenuController(MenuService menuService) {
        this.menuService = menuService;
    }

    // ── Create ─────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ApiResponse<MenuItemResponse>> createItem(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Provider-Id") String providerId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody MenuItemRequest request) {

        if (!"PROVIDER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only providers can add menu items"));
        }

        MenuItemResponse response = menuService.createItem(providerId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Menu item created", response));
    }

    // ── Public listing (cached) ────────────────────────────────

    @GetMapping("/provider/{providerId}")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getAvailableItems(
            @PathVariable String providerId) {

        List<MenuItemResponse> items = menuService.getAvailableItems(providerId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    // ── Provider's own full listing ────────────────────────────

    @GetMapping("/my")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> getMyItems(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Provider-Id") String providerId,
            @RequestHeader("X-User-Role") String role) {

        if (!"PROVIDER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only providers can access this"));
        }

        List<MenuItemResponse> items = menuService.getAllItemsForProvider(providerId);
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    // ── Filter by veg or category (public) ────────────────────

    @GetMapping("/provider/{providerId}/filter")
    public ResponseEntity<ApiResponse<List<MenuItemResponse>>> filterItems(
            @PathVariable String providerId,
            @RequestParam(required = false) Boolean isVeg,
            @RequestParam(required = false) MenuCategory category) {

        List<MenuItemResponse> items;

        if (isVeg != null) {
            items = menuService.getByVeg(providerId, isVeg);
        } else if (category != null) {
            items = menuService.getByCategory(providerId, category);
        } else {
            items = menuService.getAvailableItems(providerId);
        }

        return ResponseEntity.ok(ApiResponse.success(items));
    }

    // ── Update ─────────────────────────────────────────────────

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<MenuItemResponse>> updateItem(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Provider-Id") String providerId,
            @RequestHeader("X-User-Role") String role,
            @Valid @RequestBody MenuItemRequest request) {

        if (!"PROVIDER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only providers can update menu items"));
        }

        MenuItemResponse response = menuService.updateItem(id, providerId, request);
        return ResponseEntity.ok(ApiResponse.success("Menu item updated", response));
    }

    // ── Toggle availability ────────────────────────────────────

    @PatchMapping("/{id}/toggle")
    public ResponseEntity<ApiResponse<MenuItemResponse>> toggleAvailability(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Provider-Id") String providerId,
            @RequestHeader("X-User-Role") String role) {

        if (!"PROVIDER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only providers can toggle availability"));
        }

        MenuItemResponse response = menuService.toggleAvailability(id, providerId);
        return ResponseEntity.ok(ApiResponse.success("Availability toggled", response));
    }

    // ── Soft Delete ────────────────────────────────────────────

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(
            @PathVariable String id,
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Provider-Id") String providerId,
            @RequestHeader("X-User-Role") String role) {

        if (!"PROVIDER".equals(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(ApiResponse.error("Only providers can delete menu items"));
        }

        menuService.deleteItem(id, providerId);
        return ResponseEntity.ok(ApiResponse.success("Menu item removed", null));
    }
}