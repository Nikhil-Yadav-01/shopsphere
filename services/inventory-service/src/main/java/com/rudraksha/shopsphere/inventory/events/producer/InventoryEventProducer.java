package com.rudraksha.shopsphere.inventory.events.producer;

import com.rudraksha.shopsphere.inventory.entity.Inventory;
import com.rudraksha.shopsphere.shared.kafka.EventPublisher;
import com.rudraksha.shopsphere.shared.kafka.TopicConstants;
import com.rudraksha.shopsphere.shared.models.enums.EventType;
import com.rudraksha.shopsphere.shared.models.events.EventEnvelope;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final EventPublisher eventPublisher;

    public void publishInventoryUpdated(Inventory inventory) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("inventoryId", inventory.getId());
        payload.put("productId", inventory.getProductId());
        payload.put("sku", inventory.getSku());
        payload.put("quantity", inventory.getQuantity());
        payload.put("reservedQuantity", inventory.getReservedQuantity());
        payload.put("availableQuantity", inventory.getAvailableQuantity());
        payload.put("warehouseId", inventory.getWarehouseId());
        payload.put("needsReorder", inventory.needsReorder());

        EventEnvelope<Map<String, Object>> event = EventEnvelope.of(EventType.INVENTORY_UPDATED, payload);

        eventPublisher.publishAsync(
                TopicConstants.INVENTORY_EVENTS,
                inventory.getProductId().toString(),
                event
        );

        log.info("Published inventory updated event for product: {}", inventory.getProductId());
    }

    public void publishLowStockAlert(Inventory inventory) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("inventoryId", inventory.getId());
        payload.put("productId", inventory.getProductId());
        payload.put("sku", inventory.getSku());
        payload.put("availableQuantity", inventory.getAvailableQuantity());
        payload.put("reorderLevel", inventory.getReorderLevel());
        payload.put("reorderQuantity", inventory.getReorderQuantity());

        EventEnvelope<Map<String, Object>> event = EventEnvelope.of(EventType.INVENTORY_UPDATED, payload);

        eventPublisher.publishAsync(
                TopicConstants.INVENTORY_EVENTS,
                inventory.getProductId().toString(),
                event
        );

        log.info("Published low stock alert for product: {}", inventory.getProductId());
    }
}
