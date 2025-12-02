package com.rudraksha.shopsphere.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendedProductItem {
    private Long productId;
    private String productName;
    private String description;
    private BigDecimal price;
    private Double rating;
    private Integer reviewCount;
    private String imageUrl;
    private Double relevanceScore;
    private String reason; // "Users like you also liked", "Similar to what you viewed", "Trending now"
}
