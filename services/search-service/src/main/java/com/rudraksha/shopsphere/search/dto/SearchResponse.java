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
public class SearchResponse {
    private Long totalResults;
    private List<SearchResultItem> results;
    private List<FacetResponse> facets;
    private Long executionTimeMs;
}
