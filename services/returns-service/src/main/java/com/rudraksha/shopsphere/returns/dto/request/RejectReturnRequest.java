package com.rudraksha.shopsphere.returns.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RejectReturnRequest {

    @NotBlank(message = "Rejection reason is required")
    private String reason;
}
