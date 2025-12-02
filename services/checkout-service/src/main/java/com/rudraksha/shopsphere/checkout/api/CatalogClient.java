package com.rudraksha.shopsphere.checkout.api;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.Map;

@FeignClient(name = "catalog-service", path = "/api/v1/products")
public interface CatalogClient {

    @GetMapping("/{productId}")
    ProductResponse getProduct(@PathVariable String productId);

    record ProductResponse(
            String id,
            String name,
            String description,
            BigDecimal price,
            String imageUrl,
            String category,
            Map<String, Object> attributes
    ) {
    }
}
