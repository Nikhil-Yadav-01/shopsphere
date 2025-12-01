package com.rudraksha.shopsphere.inventory.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockCheckResponse {

    private UUID productId;
    private String sku;
    private Integer availableQuantity;
    private Boolean inStock;
    private Boolean sufficientStock;
    private Integer requestedQuantity;
}
