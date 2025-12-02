package com.rudraksha.shopsphere.pricing.dto.response;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class PriceResponse {
    private Long id;
    private String productId;
    private BigDecimal basePrice;
    private BigDecimal sellingPrice;
    private BigDecimal discountPercentage;
    private String currency;
}
