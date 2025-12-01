package com.rudraksha.shopsphere.cart.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "inventory-service", path = "/api/v1/inventory")
public interface InventoryClient {

    @GetMapping("/{productId}/availability")
    StockResponse checkStock(@PathVariable String productId, @RequestParam Integer quantity);

    record StockResponse(
        String productId,
        Integer availableQuantity,
        Boolean inStock,
        String warehouseId
    ) {}
}
