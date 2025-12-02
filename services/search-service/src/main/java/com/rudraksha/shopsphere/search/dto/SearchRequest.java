package com.rudraksha.shopsphere.search.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SearchRequest {
    private String query;
    private String category;
    private BigDecimal minPrice;
    private BigDecimal maxPrice;
    private String sortBy; // relevance, price, rating, newest
    private String sortOrder; // asc, desc
    private Integer page = 0;
    private Integer size = 20;
    private String brand;
}
