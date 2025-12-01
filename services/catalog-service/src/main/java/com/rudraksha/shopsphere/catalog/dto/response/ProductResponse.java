package com.rudraksha.shopsphere.catalog.dto.response;

import com.rudraksha.shopsphere.catalog.entity.Product.ProductStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductResponse {

    private String id;
    private String sku;
    private String name;
    private String description;
    private BigDecimal price;
    private String currency;
    private String categoryId;
    private List<String> images;
    private Map<String, Object> attributes;
    private String sellerId;
    private ProductStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
