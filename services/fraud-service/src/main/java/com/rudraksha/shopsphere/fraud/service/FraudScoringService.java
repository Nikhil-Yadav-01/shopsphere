package com.rudraksha.shopsphere.fraud.service;

import com.rudraksha.shopsphere.fraud.dto.FraudCheckRequest;
import com.rudraksha.shopsphere.fraud.dto.FraudScoreResponse;
import com.rudraksha.shopsphere.fraud.entity.FraudCase;

public interface FraudScoringService {

    FraudScoreResponse checkFraud(FraudCheckRequest request);

    Integer calculateFraudScore(FraudCheckRequest request);

    FraudCase.RiskLevel determineRiskLevel(Integer score);

    String generateFraudReason(FraudCheckRequest request, Integer score);
}
