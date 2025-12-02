package com.rudraksha.shopsphere.order.service;

import com.rudraksha.shopsphere.order.dto.request.CancelOrderRequest;
import com.rudraksha.shopsphere.order.dto.request.CreateOrderRequest;
import com.rudraksha.shopsphere.order.dto.response.OrderResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface OrderService {

    /**
     * Creates a new order with the given request
     */
    OrderResponse createOrder(CreateOrderRequest request);

    /**
     * Retrieves an order by ID
     */
    OrderResponse getOrderById(UUID orderId);

    /**
     * Lists all orders for a user with pagination
     */
    Page<OrderResponse> getUserOrders(UUID userId, Pageable pageable);

    /**
     * Lists all orders (admin view) with pagination
     */
    Page<OrderResponse> getAllOrders(Pageable pageable);

    /**
     * Cancels an order
     */
    OrderResponse cancelOrder(UUID orderId, CancelOrderRequest request);

    /**
     * Updates order status (admin only)
     */
    OrderResponse updateOrderStatus(UUID orderId, String status);
}
