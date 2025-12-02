package com.rudraksha.shopsphere.fraud.service.impl;

import com.rudraksha.shopsphere.fraud.dto.FraudCheckRequest;
import com.rudraksha.shopsphere.fraud.dto.FraudScoreResponse;
import com.rudraksha.shopsphere.fraud.entity.FraudCase;
import com.rudraksha.shopsphere.fraud.repository.FraudCaseRepository;
import com.rudraksha.shopsphere.fraud.service.FraudScoringService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FraudScoringServiceImpl implements FraudScoringService {

    private final FraudCaseRepository fraudCaseRepository;

    @Override
    @Transactional(readOnly = true)
    public FraudScoreResponse checkFraud(FraudCheckRequest request) {
        Integer fraudScore = calculateFraudScore(request);
        FraudCase.RiskLevel riskLevel = determineRiskLevel(fraudScore);
        String reason = generateFraudReason(request, fraudScore);

        // Save fraud case if score is significant
        if (fraudScore >= 30) {
            FraudCase fraudCase = FraudCase.builder()
                    .userId(request.getUserId())
                    .orderId(request.getOrderId())
                    .fraudScore(fraudScore)
                    .riskLevel(riskLevel)
                    .amount(request.getAmount())
                    .shippingAddress(request.getShippingAddress())
                    .billingAddress(request.getBillingAddress())
                    .reason(reason)
                    .status(FraudCase.CaseStatus.OPEN)
                    .build();

            fraudCaseRepository.save(fraudCase);
            log.warn("Fraud case created for user {} with score {}", request.getUserId(), fraudScore);
        }

        return FraudScoreResponse.builder()
                .fraudScore(fraudScore)
                .riskLevel(riskLevel)
                .isApproved(riskLevel == FraudCase.RiskLevel.LOW)
                .reason(reason)
                .build();
    }

    @Override
    public Integer calculateFraudScore(FraudCheckRequest request) {
        Integer score = 0;

        // 1. Address Mismatch (0-20 points)
        if (request.getShippingAddress() != null && request.getBillingAddress() != null) {
            if (!request.getShippingAddress().equals(request.getBillingAddress())) {
                score += 15;
            }
        }

        // 2. Card Velocity - Check if user has multiple orders in short time (0-20 points)
        score += checkCardVelocity(request.getUserId());

        // 3. Amount Anomaly - Is order amount unusual (0-15 points)
        score += checkAmountAnomaly(request.getAmount());

        // 4. Device Fingerprinting (0-15 points)
        if (request.getDeviceId() != null) {
            score += 5; // Placeholder - in production, check device reputation
        }

        // 5. Email Verification (0-10 points)
        if (request.getEmail() == null) {
            score += 10;
        }

        // 6. IP Reputation (0-10 points)
        if (request.getIpAddress() != null) {
            score += checkIpReputation(request.getIpAddress());
        }

        // 7. User History (0-10 points)
        score += checkUserHistory(request.getUserId());

        return Math.min(score, 100); // Cap at 100
    }

    @Override
    public FraudCase.RiskLevel determineRiskLevel(Integer score) {
        if (score < 25) return FraudCase.RiskLevel.LOW;
        if (score < 50) return FraudCase.RiskLevel.MEDIUM;
        if (score < 75) return FraudCase.RiskLevel.HIGH;
        return FraudCase.RiskLevel.CRITICAL;
    }

    @Override
    public String generateFraudReason(FraudCheckRequest request, Integer score) {
        List<String> reasons = new ArrayList<>();

        if (request.getShippingAddress() != null && request.getBillingAddress() != null) {
            if (!request.getShippingAddress().equals(request.getBillingAddress())) {
                reasons.add("Shipping and billing addresses do not match");
            }
        }

        if (checkCardVelocity(request.getUserId()) > 10) {
            reasons.add("Multiple transactions in short time period");
        }

        if (checkAmountAnomaly(request.getAmount()) > 10) {
            reasons.add("Order amount is unusually high");
        }

        if (request.getEmail() == null) {
            reasons.add("Email not verified");
        }

        if (reasons.isEmpty()) {
            reasons.add("Order appears legitimate");
        }

        return String.join("; ", reasons);
    }

    private Integer checkCardVelocity(Long userId) {
        // In production, query orders within last 24 hours
        // For now, return base score
        return 5;
    }

    private Integer checkAmountAnomaly(BigDecimal amount) {
        // In production, compare to user's average order value
        // For now, flag very large orders
        if (amount.compareTo(new BigDecimal("1000")) > 0) {
            return 12;
        }
        if (amount.compareTo(new BigDecimal("500")) > 0) {
            return 8;
        }
        return 0;
    }

    private Integer checkIpReputation(String ipAddress) {
        // In production, use MaxMind or similar
        // For now, return base score
        return 3;
    }

    private Integer checkUserHistory(Long userId) {
        // In production, check for chargebacks, returns, complaints
        // For now, return base score
        return 2;
    }
}
