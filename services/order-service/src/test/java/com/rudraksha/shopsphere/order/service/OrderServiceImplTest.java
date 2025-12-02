package com.rudraksha.shopsphere.order.service;

import com.rudraksha.shopsphere.order.dto.request.CancelOrderRequest;
import com.rudraksha.shopsphere.order.dto.request.CreateOrderRequest;
import com.rudraksha.shopsphere.order.dto.response.OrderResponse;
import com.rudraksha.shopsphere.order.entity.Order;
import com.rudraksha.shopsphere.order.exception.InvalidOrderStateException;
import com.rudraksha.shopsphere.order.exception.OrderNotFoundException;
import com.rudraksha.shopsphere.order.mapper.OrderMapper;
import com.rudraksha.shopsphere.order.repository.OrderRepository;
import com.rudraksha.shopsphere.order.service.impl.OrderServiceImpl;
import com.rudraksha.shopsphere.shared.models.enums.OrderStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    private OrderService orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(orderRepository, orderMapper);
    }

    private CreateOrderRequest createValidOrderRequest() {
        UUID userId = UUID.randomUUID();
        CreateOrderRequest.AddressRequest address = CreateOrderRequest.AddressRequest.builder()
                .fullName("John Doe")
                .phone("555-1234")
                .addressLine1("123 Main St")
                .city("New York")
                .state("NY")
                .postalCode("10001")
                .country("USA")
                .build();

        CreateOrderRequest.OrderItemRequest item = CreateOrderRequest.OrderItemRequest.builder()
                .productId(UUID.randomUUID())
                .productName("Test Product")
                .sku("SKU-001")
                .quantity(2)
                .price(new BigDecimal("50.00"))
                .build();

        return CreateOrderRequest.builder()
                .userId(userId)
                .shippingAddress(address)
                .billingAddress(address)
                .items(List.of(item))
                .currency("USD")
                .build();
    }

    @Test
    void testCreateOrder_Success() {
        // Arrange
        CreateOrderRequest request = createValidOrderRequest();
        Order mockOrder = new Order();
        mockOrder.setId(UUID.randomUUID());
        mockOrder.setOrderNumber("ORD-12345");
        mockOrder.setStatus(OrderStatus.PENDING);
        mockOrder.setItems(new ArrayList<>());

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(mockOrder.getId())
                .orderNumber("ORD-12345")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.save(any(Order.class))).thenReturn(mockOrder);
        when(orderMapper.toOrderResponse(mockOrder)).thenReturn(expectedResponse);

        // Act
        OrderResponse result = orderService.createOrder(request);

        // Assert
        assertNotNull(result);
        assertEquals("ORD-12345", result.getOrderNumber());
        assertEquals(OrderStatus.PENDING, result.getStatus());
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCreateOrder_EmptyItems_ThrowsException() {
        // Arrange
        CreateOrderRequest request = createValidOrderRequest();
        request.setItems(new ArrayList<>());

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> orderService.createOrder(request));
    }

    @Test
    void testGetOrderById_Success() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order mockOrder = Order.builder()
                .id(orderId)
                .orderNumber("ORD-12345")
                .status(OrderStatus.PENDING)
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderNumber("ORD-12345")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(mockOrder));
        when(orderMapper.toOrderResponse(mockOrder)).thenReturn(expectedResponse);

        // Act
        OrderResponse result = orderService.getOrderById(orderId);

        // Assert
        assertNotNull(result);
        assertEquals("ORD-12345", result.getOrderNumber());
        verify(orderRepository, times(1)).findById(orderId);
    }

    @Test
    void testGetOrderById_NotFound_ThrowsException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(OrderNotFoundException.class, () -> orderService.getOrderById(orderId));
    }

    @Test
    void testGetUserOrders_Success() {
        // Arrange
        UUID userId = UUID.randomUUID();
        Pageable pageable = PageRequest.of(0, 10);

        Order order = Order.builder()
                .id(UUID.randomUUID())
                .userId(userId)
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .build();

        Page<Order> ordersPage = new PageImpl<>(List.of(order), pageable, 1);
        OrderResponse response = OrderResponse.builder()
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findByUserId(userId, pageable)).thenReturn(ordersPage);
        when(orderMapper.toOrderResponse(order)).thenReturn(response);

        // Act
        Page<OrderResponse> result = orderService.getUserOrders(userId, pageable);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        verify(orderRepository, times(1)).findByUserId(userId, pageable);
    }

    @Test
    void testCancelOrder_Success() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .build();

        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("Changed my mind");

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderNumber("ORD-123")
                .status(OrderStatus.CANCELLED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(expectedResponse);

        // Act
        OrderResponse result = orderService.cancelOrder(orderId, request);

        // Assert
        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, result.getStatus());
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testCancelOrder_InvalidState_ThrowsException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123")
                .status(OrderStatus.SHIPPED)
                .build();

        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("Changed my mind");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidOrderStateException.class, () -> orderService.cancelOrder(orderId, request));
    }

    @Test
    void testUpdateOrderStatus_Success() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .build();

        OrderResponse expectedResponse = OrderResponse.builder()
                .id(orderId)
                .orderNumber("ORD-123")
                .status(OrderStatus.CONFIRMED)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toOrderResponse(any(Order.class))).thenReturn(expectedResponse);

        // Act
        OrderResponse result = orderService.updateOrderStatus(orderId, "CONFIRMED");

        // Assert
        assertNotNull(result);
        verify(orderRepository, times(1)).findById(orderId);
        verify(orderRepository, times(1)).save(any(Order.class));
    }

    @Test
    void testUpdateOrderStatus_InvalidStatus_ThrowsException() {
        // Arrange
        UUID orderId = UUID.randomUUID();
        Order order = Order.builder()
                .id(orderId)
                .orderNumber("ORD-123")
                .status(OrderStatus.PENDING)
                .build();

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        // Act & Assert
        assertThrows(InvalidOrderStateException.class, () -> orderService.updateOrderStatus(orderId, "INVALID_STATUS"));
    }
}
