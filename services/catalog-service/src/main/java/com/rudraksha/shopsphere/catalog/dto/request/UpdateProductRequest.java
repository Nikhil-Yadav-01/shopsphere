package com.rudraksha.shopsphere.catalog.dto.request;

import com.rudraksha.shopsphere.catalog.entity.Product.ProductStatus;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProductRequest {

    private String name;

    private String description;

    @Positive(message = "Price must be positive")
    private BigDecimal price;

    private String currency;

    private String categoryId;

    private List<String> images;

    private Map<String, Object> attributes;

    private ProductStatus status;
}
