package com.rudraksha.shopsphere.order.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rudraksha.shopsphere.order.dto.request.CancelOrderRequest;
import com.rudraksha.shopsphere.order.dto.request.CreateOrderRequest;
import com.rudraksha.shopsphere.order.dto.response.OrderResponse;
import com.rudraksha.shopsphere.order.service.OrderService;
import com.rudraksha.shopsphere.shared.models.enums.OrderStatus;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private OrderService orderService;

    private UUID generateUUID() {
        return UUID.randomUUID();
    }

    private OrderResponse createMockOrderResponse() {
        return OrderResponse.builder()
                .id(generateUUID())
                .orderNumber("ORD-12345")
                .userId(generateUUID())
                .status(OrderStatus.PENDING)
                .totalAmount(new BigDecimal("100.00"))
                .currency("USD")
                .build();
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCreateOrder_Success() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(generateUUID())
                .currency("USD")
                .build();

        OrderResponse response = createMockOrderResponse();

        when(orderService.createOrder(any(CreateOrderRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetOrder_Success() throws Exception {
        // Arrange
        UUID orderId = generateUUID();
        OrderResponse response = createMockOrderResponse();

        when(orderService.getOrderById(orderId)).thenReturn(response);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.orderNumber").value("ORD-12345"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testGetUserOrders_Success() throws Exception {
        // Arrange
        UUID userId = generateUUID();
        OrderResponse response = createMockOrderResponse();
        Page<OrderResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(orderService.getUserOrders(eq(userId), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders")
                        .param("userId", userId.toString())
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-12345"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testGetAllOrders_Success() throws Exception {
        // Arrange
        OrderResponse response = createMockOrderResponse();
        Page<OrderResponse> page = new PageImpl<>(List.of(response), PageRequest.of(0, 10), 1);

        when(orderService.getAllOrders(any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/v1/orders/admin/all")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].orderNumber").value("ORD-12345"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void testCancelOrder_Success() throws Exception {
        // Arrange
        UUID orderId = generateUUID();
        CancelOrderRequest request = new CancelOrderRequest();
        request.setReason("Changed my mind");

        OrderResponse response = createMockOrderResponse();
        response.setStatus(OrderStatus.CANCELLED);

        when(orderService.cancelOrder(eq(orderId), any(CancelOrderRequest.class))).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/{orderId}/cancel", orderId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void testUpdateOrderStatus_Success() throws Exception {
        // Arrange
        UUID orderId = generateUUID();
        OrderResponse response = createMockOrderResponse();
        response.setStatus(OrderStatus.CONFIRMED);

        when(orderService.updateOrderStatus(orderId, "CONFIRMED")).thenReturn(response);

        // Act & Assert
        mockMvc.perform(put("/api/v1/orders/{orderId}/status", orderId)
                        .param("status", "CONFIRMED")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void testCreateOrder_Unauthorized() throws Exception {
        // Arrange
        CreateOrderRequest request = CreateOrderRequest.builder()
                .userId(generateUUID())
                .build();

        // Act & Assert - should return 401 Unauthorized
        mockMvc.perform(post("/api/v1/orders")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}
