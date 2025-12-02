package com.rudraksha.shopsphere.inventory.service.impl;

import com.rudraksha.shopsphere.inventory.dto.request.ReserveStockRequest;
import com.rudraksha.shopsphere.inventory.dto.request.UpdateStockRequest;
import com.rudraksha.shopsphere.inventory.dto.response.InventoryResponse;
import com.rudraksha.shopsphere.inventory.dto.response.StockCheckResponse;
import com.rudraksha.shopsphere.inventory.entity.Inventory;
import com.rudraksha.shopsphere.inventory.entity.StockMovement;
import com.rudraksha.shopsphere.inventory.events.producer.InventoryEventProducer;
import com.rudraksha.shopsphere.inventory.exception.InsufficientStockException;
import com.rudraksha.shopsphere.inventory.exception.InventoryNotFoundException;
import com.rudraksha.shopsphere.inventory.repository.InventoryRepository;
import com.rudraksha.shopsphere.inventory.repository.StockMovementRepository;
import com.rudraksha.shopsphere.inventory.service.InventoryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryServiceImpl implements InventoryService {

    private final InventoryRepository inventoryRepository;
    private final StockMovementRepository stockMovementRepository;
    private final InventoryEventProducer inventoryEventProducer;

    @Override
    @Transactional(readOnly = true)
    public InventoryResponse getInventoryByProductId(UUID productId) {
        Inventory inventory = inventoryRepository.findByProductId(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + productId));
        return mapToResponse(inventory);
    }

    @Override
    @Transactional
    public InventoryResponse updateStock(UUID productId, UpdateStockRequest request) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(productId)
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + productId));

        int previousQuantity = inventory.getQuantity();
        int quantityChange = request.getQuantity() - previousQuantity;

        inventory.setQuantity(request.getQuantity());

        if (request.getReorderLevel() != null) {
            inventory.setReorderLevel(request.getReorderLevel());
        }
        if (request.getReorderQuantity() != null) {
            inventory.setReorderQuantity(request.getReorderQuantity());
        }

        if (quantityChange > 0) {
            inventory.setLastRestockedAt(LocalDateTime.now());
        }

        Inventory savedInventory = inventoryRepository.save(inventory);

        if (quantityChange != 0) {
            StockMovement movement = StockMovement.builder()
                    .inventoryId(savedInventory.getId())
                    .type(quantityChange > 0 ? StockMovement.MovementType.IN : StockMovement.MovementType.OUT)
                    .quantity(Math.abs(quantityChange))
                    .reason(request.getReason() != null ? request.getReason() : "Stock update")
                    .build();
            stockMovementRepository.save(movement);
        }

        inventoryEventProducer.publishInventoryUpdated(savedInventory);

        log.info("Stock updated for product {}: {} -> {}", productId, previousQuantity, request.getQuantity());

        return mapToResponse(savedInventory);
    }

    @Override
    @Transactional
    public InventoryResponse reserveStock(ReserveStockRequest request) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(request.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + request.getProductId()));

        if (!inventory.hasAvailableStock(request.getQuantity())) {
            throw new InsufficientStockException(
                    "Insufficient stock for product: " + request.getProductId(),
                    inventory.getAvailableQuantity(),
                    request.getQuantity());
        }

        inventory.setReservedQuantity(inventory.getReservedQuantity() + request.getQuantity());
        Inventory savedInventory = inventoryRepository.save(inventory);

        StockMovement movement = StockMovement.builder()
                .inventoryId(savedInventory.getId())
                .type(StockMovement.MovementType.RESERVED)
                .quantity(request.getQuantity())
                .reason(request.getReason() != null ? request.getReason() : "Stock reserved")
                .referenceId(request.getReferenceId())
                .build();
        stockMovementRepository.save(movement);

        log.info("Stock reserved for product {}: quantity={}, referenceId={}",
                request.getProductId(), request.getQuantity(), request.getReferenceId());

        return mapToResponse(savedInventory);
    }

    @Override
    @Transactional
    public InventoryResponse releaseStock(ReserveStockRequest request) {
        Inventory inventory = inventoryRepository.findByProductIdWithLock(request.getProductId())
                .orElseThrow(() -> new InventoryNotFoundException("Inventory not found for product: " + request.getProductId()));

        int newReservedQuantity = inventory.getReservedQuantity() - request.getQuantity();
        if (newReservedQuantity < 0) {
            log.warn("Releasing more stock than reserved for product {}. Setting reserved to 0.", request.getProductId());
            newReservedQuantity = 0;
        }

        inventory.setReservedQuantity(newReservedQuantity);
        Inventory savedInventory = inventoryRepository.save(inventory);

        StockMovement movement = StockMovement.builder()
                .inventoryId(savedInventory.getId())
                .type(StockMovement.MovementType.RELEASED)
                .quantity(request.getQuantity())
                .reason(request.getReason() != null ? request.getReason() : "Stock released")
                .referenceId(request.getReferenceId())
                .build();
        stockMovementRepository.save(movement);

        log.info("Stock released for product {}: quantity={}, referenceId={}",
                request.getProductId(), request.getQuantity(), request.getReferenceId());

        return mapToResponse(savedInventory);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockCheckResponse> checkStock(List<UUID> productIds, List<Integer> quantities) {
        if (productIds.size() != quantities.size()) {
            throw new IllegalArgumentException("Product IDs and quantities lists must have the same size");
        }

        Map<UUID, Inventory> inventoryMap = inventoryRepository.findByProductIdIn(productIds)
                .stream()
                .collect(Collectors.toMap(Inventory::getProductId, Function.identity()));

        List<StockCheckResponse> responses = new ArrayList<>();

        for (int i = 0; i < productIds.size(); i++) {
            UUID productId = productIds.get(i);
            Integer requestedQuantity = quantities.get(i);
            Inventory inventory = inventoryMap.get(productId);

            if (inventory == null) {
                responses.add(StockCheckResponse.builder()
                        .productId(productId)
                        .availableQuantity(0)
                        .inStock(false)
                        .sufficientStock(false)
                        .requestedQuantity(requestedQuantity)
                        .build());
            } else {
                int availableQuantity = inventory.getAvailableQuantity();
                responses.add(StockCheckResponse.builder()
                        .productId(productId)
                        .sku(inventory.getSku())
                        .availableQuantity(availableQuantity)
                        .inStock(availableQuantity > 0)
                        .sufficientStock(availableQuantity >= requestedQuantity)
                        .requestedQuantity(requestedQuantity)
                        .build());
            }
        }

        return responses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<InventoryResponse> getLowStockInventory() {
        return inventoryRepository.findLowStockInventory()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private InventoryResponse mapToResponse(Inventory inventory) {
        return InventoryResponse.builder()
                .id(inventory.getId())
                .productId(inventory.getProductId())
                .sku(inventory.getSku())
                .quantity(inventory.getQuantity())
                .reservedQuantity(inventory.getReservedQuantity())
                .availableQuantity(inventory.getAvailableQuantity())
                .warehouseId(inventory.getWarehouseId())
                .reorderLevel(inventory.getReorderLevel())
                .reorderQuantity(inventory.getReorderQuantity())
                .needsReorder(inventory.needsReorder())
                .lastRestockedAt(inventory.getLastRestockedAt())
                .createdAt(inventory.getCreatedAt())
                .updatedAt(inventory.getUpdatedAt())
                .build();
    }
}
