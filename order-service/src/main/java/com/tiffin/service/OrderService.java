package com.tiffin.service;

import com.tiffin.common.enums.OrderStatus;
import com.tiffin.dto.OrderRequest;
import com.tiffin.dto.OrderResponse;
import com.tiffin.dto.StatusUpdateRequest;

import java.util.List;

public interface OrderService {

    // Customer places a new order
    OrderResponse placeOrder(String userId, OrderRequest request);

    // Customer views their own orders
    List<OrderResponse> getMyOrders(String userId);

    // Customer views orders by status
    List<OrderResponse> getMyOrdersByStatus(String userId, OrderStatus status);

    // Customer cancels order (only if PENDING)
    OrderResponse cancelOrder(String orderId, String userId);

    // Provider views incoming orders
    List<OrderResponse> getProviderOrders(String providerId);

    // Provider views orders by status
    List<OrderResponse> getProviderOrdersByStatus(String providerId, OrderStatus status);

    // Provider updates order status
    OrderResponse updateOrderStatus(String orderId, String providerId,
                                    StatusUpdateRequest request);

    // Get single order by id
    OrderResponse getOrderById(String orderId, String userId, String role);
    
    OrderResponse getOrderInternal(String orderId);
    
    void updateOrderStatusInternal(String orderId, OrderStatus status);
}