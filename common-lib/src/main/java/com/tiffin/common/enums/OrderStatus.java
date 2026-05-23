package com.tiffin.common.enums;

/**
 * Order lifecycle states.
 * Used by order-service (producer) and notification-service (consumer).
 * Keeping it in common-lib avoids duplicating the enum in both services.
 *
 * State machine:
 * PENDING → CONFIRMED → PREPARING → READY → OUT_FOR_DELIVERY → DELIVERED
 *         ↘ REJECTED
 *    (any) → CANCELLED
 */
public enum OrderStatus {
    PENDING,          // order placed, waiting for provider to accept
    CONFIRMED,        // provider accepted
    PREPARING,        // kitchen is cooking
    READY,            // food is ready (triggers meal-ready notification)
    OUT_FOR_DELIVERY, // delivery person picked up
    DELIVERED,        // completed successfully
    REJECTED,         // provider rejected the order
    CANCELLED         // customer or system cancelled
}
