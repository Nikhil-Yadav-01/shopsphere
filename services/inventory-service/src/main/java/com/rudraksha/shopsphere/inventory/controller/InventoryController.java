package com.rudraksha.shopsphere.inventory.controller;

import com.rudraksha.shopsphere.inventory.dto.request.ReserveStockRequest;
import com.rudraksha.shopsphere.inventory.dto.request.UpdateStockRequest;
import com.rudraksha.shopsphere.inventory.dto.response.InventoryResponse;
import com.rudraksha.shopsphere.inventory.dto.response.StockCheckResponse;
import com.rudraksha.shopsphere.inventory.service.InventoryService;
import com.rudraksha.shopsphere.shared.models.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventory(@PathVariable UUID productId) {
        InventoryResponse response = inventoryService.getInventoryByProductId(productId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{productId}")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateStock(
            @PathVariable UUID productId,
            @Valid @RequestBody UpdateStockRequest request) {
        InventoryResponse response = inventoryService.updateStock(productId, request);
        return ResponseEntity.ok(ApiResponse.success("Stock updated successfully", response));
    }

    @PostMapping("/reserve")
    public ResponseEntity<ApiResponse<InventoryResponse>> reserveStock(
            @Valid @RequestBody ReserveStockRequest request) {
        InventoryResponse response = inventoryService.reserveStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock reserved successfully", response));
    }

    @PostMapping("/release")
    public ResponseEntity<ApiResponse<InventoryResponse>> releaseStock(
            @Valid @RequestBody ReserveStockRequest request) {
        InventoryResponse response = inventoryService.releaseStock(request);
        return ResponseEntity.ok(ApiResponse.success("Stock released successfully", response));
    }

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<List<StockCheckResponse>>> checkStock(
            @RequestParam List<UUID> productIds,
            @RequestParam List<Integer> quantities) {
        List<StockCheckResponse> responses = inventoryService.checkStock(productIds, quantities);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/low-stock")
    public ResponseEntity<ApiResponse<List<InventoryResponse>>> getLowStockInventory() {
        List<InventoryResponse> responses = inventoryService.getLowStockInventory();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }
}
