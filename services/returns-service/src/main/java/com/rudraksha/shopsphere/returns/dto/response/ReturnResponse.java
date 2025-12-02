package com.rudraksha.shopsphere.returns.dto.response;

import com.rudraksha.shopsphere.shared.models.enums.ReturnStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnResponse {

    private UUID id;

    private UUID orderId;

    private UUID userId;

    private String rmaNumber;

    private ReturnStatus status;

    private String reason;

    private String description;

    private BigDecimal refundAmount;

    private BigDecimal shippingCostDeductible;

    private BigDecimal finalRefundAmount;

    private LocalDateTime requestedAt;

    private LocalDateTime approvedAt;

    private LocalDateTime rejectedAt;

    private String rejectionReason;

    private LocalDateTime returnReceivedAt;

    private LocalDateTime refundProcessedAt;

    private String refundTransactionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;
}
