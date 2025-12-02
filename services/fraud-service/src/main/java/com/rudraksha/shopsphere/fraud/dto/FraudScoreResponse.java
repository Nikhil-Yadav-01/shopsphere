package com.rudraksha.shopsphere.fraud.dto;

import com.rudraksha.shopsphere.fraud.entity.FraudCase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FraudScoreResponse {
    private Integer fraudScore; // 0-100
    private FraudCase.RiskLevel riskLevel;
    private Boolean isApproved;
    private String reason;
}
