package com.rudraksha.shopsphere.shipping.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateTrackingRequest {

    @NotBlank(message = "Event code is required")
    private String eventCode;

    @NotBlank(message = "Description is required")
    private String description;

    private String location;

    private Double latitude;

    private Double longitude;
}
