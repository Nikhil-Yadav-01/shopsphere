package com.rudraksha.shopsphere.search.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResultItem {
    private Long productId;
    private String productName;
    private String description;
    private String category;
    private String brand;
    private BigDecimal price;
    private Double rating;
    private Integer reviewCount;
    private String imageUrl;
    private Integer stock;
}
