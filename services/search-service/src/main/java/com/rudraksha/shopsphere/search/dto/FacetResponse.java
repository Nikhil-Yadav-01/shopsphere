package com.rudraksha.shopsphere.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FacetResponse {
    private String facetName; // "category", "brand", "priceRange"
    private List<FacetBucket> buckets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class FacetBucket {
        private String key;
        private Long count;
    }
}
