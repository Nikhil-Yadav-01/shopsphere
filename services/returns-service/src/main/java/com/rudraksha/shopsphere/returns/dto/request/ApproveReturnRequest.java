package com.rudraksha.shopsphere.returns.dto.request;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApproveReturnRequest {

    @NotNull(message = "Refund amount is required")
    private BigDecimal refundAmount;

    private BigDecimal shippingCostDeductible;
}
