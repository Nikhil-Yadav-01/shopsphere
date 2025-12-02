package com.rudraksha.shopsphere.returns.dto.response;

import com.rudraksha.shopsphere.shared.models.enums.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RMAResponse {

    private UUID id;

    private UUID returnRequestId;

    private String rmaNumber;

    private ReturnStatus status;

    private String trackingNumber;

    private String shippingLabelUrl;

    private LocalDateTime expiryDate;

    private Boolean isExpired;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
