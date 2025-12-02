package com.rudraksha.shopsphere.fraud.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudCheckRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Order amount is required")
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;

    private String shippingAddress;

    private String billingAddress;

    private String email;

    private String deviceId;

    private String ipAddress;

    private Long orderId;
}
