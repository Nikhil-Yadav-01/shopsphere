package com.rudraksha.shopsphere.inventory.service;

import com.rudraksha.shopsphere.inventory.dto.request.ReserveStockRequest;
import com.rudraksha.shopsphere.inventory.dto.request.UpdateStockRequest;
import com.rudraksha.shopsphere.inventory.dto.response.InventoryResponse;
import com.rudraksha.shopsphere.inventory.dto.response.StockCheckResponse;

import java.util.List;
import java.util.UUID;

public interface InventoryService {

    InventoryResponse getInventoryByProductId(UUID productId);

    InventoryResponse updateStock(UUID productId, UpdateStockRequest request);

    InventoryResponse reserveStock(ReserveStockRequest request);

    InventoryResponse releaseStock(ReserveStockRequest request);

    List<StockCheckResponse> checkStock(List<UUID> productIds, List<Integer> quantities);

    List<InventoryResponse> getLowStockInventory();
}
