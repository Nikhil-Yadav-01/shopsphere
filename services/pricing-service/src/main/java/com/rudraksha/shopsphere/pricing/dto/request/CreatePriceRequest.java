package com.rudraksha.shopsphere.pricing.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CreatePriceRequest {
    @NotBlank
    private String productId;

    @NotNull
    @Positive
    private BigDecimal basePrice;

    @NotNull
    @Positive
    private BigDecimal sellingPrice;

    private BigDecimal discountPercentage;

    @NotBlank
    private String currency;
}
