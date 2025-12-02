package com.rudraksha.shopsphere.recommendation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RecommendationResponse {
    private String type; // forYou, similar, trending, personalized
    private List<RecommendedProductItem> products;
    private Integer limit;
    private String message;
}
