package com.rudraksha.shopsphere.inventory.events.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rudraksha.shopsphere.inventory.dto.request.ReserveStockRequest;
import com.rudraksha.shopsphere.inventory.service.InventoryService;
import com.rudraksha.shopsphere.shared.kafka.TopicConstants;
import com.rudraksha.shopsphere.shared.models.events.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderEventConsumer {

    private final InventoryService inventoryService;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = TopicConstants.ORDER_EVENTS, groupId = "inventory-service-group")
    public void handleOrderEvent(EventEnvelope<Map<String, Object>> event) {
        log.info("Received order event: type={}, eventId={}", event.getEventType(), event.getEventId());

        try {
            switch (event.getEventType()) {
                case ORDER_PLACED -> handleOrderPlaced(event.getPayload());
                case ORDER_CANCELLED -> handleOrderCancelled(event.getPayload());
                case ORDER_CONFIRMED -> handleOrderConfirmed(event.getPayload());
                default -> log.debug("Ignoring event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("Failed to process order event: {}", event.getEventId(), e);
        }
    }

    private void handleOrderPlaced(Map<String, Object> payload) {
        UUID orderId = UUID.fromString((String) payload.get("orderId"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        if (items == null || items.isEmpty()) {
            log.warn("Order {} has no items to reserve", orderId);
            return;
        }

        for (Map<String, Object> item : items) {
            UUID productId = UUID.fromString((String) item.get("productId"));
            Integer quantity = (Integer) item.get("quantity");

            try {
                ReserveStockRequest request = ReserveStockRequest.builder()
                        .productId(productId)
                        .quantity(quantity)
                        .referenceId(orderId)
                        .reason("Order placed: " + orderId)
                        .build();

                inventoryService.reserveStock(request);
                log.info("Reserved stock for order {}: productId={}, quantity={}", orderId, productId, quantity);
            } catch (Exception e) {
                log.error("Failed to reserve stock for order {}: productId={}", orderId, productId, e);
            }
        }
    }

    private void handleOrderCancelled(Map<String, Object> payload) {
        UUID orderId = UUID.fromString((String) payload.get("orderId"));
        List<Map<String, Object>> items = (List<Map<String, Object>>) payload.get("items");

        if (items == null || items.isEmpty()) {
            log.warn("Cancelled order {} has no items to release", orderId);
            return;
        }

        for (Map<String, Object> item : items) {
            UUID productId = UUID.fromString((String) item.get("productId"));
            Integer quantity = (Integer) item.get("quantity");

            try {
                ReserveStockRequest request = ReserveStockRequest.builder()
                        .productId(productId)
                        .quantity(quantity)
                        .referenceId(orderId)
                        .reason("Order cancelled: " + orderId)
                        .build();

                inventoryService.releaseStock(request);
                log.info("Released stock for cancelled order {}: productId={}, quantity={}", orderId, productId, quantity);
            } catch (Exception e) {
                log.error("Failed to release stock for cancelled order {}: productId={}", orderId, productId, e);
            }
        }
    }

    private void handleOrderConfirmed(Map<String, Object> payload) {
        UUID orderId = UUID.fromString((String) payload.get("orderId"));
        log.info("Order confirmed: {}. Reserved stock will be deducted upon shipping.", orderId);
    }
}
