package com.rudraksha.shopsphere.returns.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateReturnRequest {

    @NotNull(message = "Order ID is required")
    private UUID orderId;

    @NotBlank(message = "Return reason is required")
    private String reason;

    private String description;

    public void validate() {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("Return reason is required");
        }
    }
}
