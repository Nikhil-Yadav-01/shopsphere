package com.rudraksha.shopsphere.fraud.service;

import com.rudraksha.shopsphere.fraud.dto.FraudCheckRequest;
import com.rudraksha.shopsphere.fraud.dto.FraudScoreResponse;
import com.rudraksha.shopsphere.fraud.entity.FraudCase;
import com.rudraksha.shopsphere.fraud.repository.FraudCaseRepository;
import com.rudraksha.shopsphere.fraud.service.impl.FraudScoringServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class FraudScoringServiceImplTest {

    @Mock
    private FraudCaseRepository fraudCaseRepository;

    @InjectMocks
    private FraudScoringServiceImpl fraudScoringService;

    private FraudCheckRequest testRequest;

    @BeforeEach
    void setUp() {
        testRequest = FraudCheckRequest.builder()
                .userId(1L)
                .amount(new BigDecimal("150.00"))
                .shippingAddress("123 Main St")
                .billingAddress("123 Main St")
                .email("test@example.com")
                .deviceId("device123")
                .ipAddress("192.168.1.1")
                .orderId(100L)
                .build();
    }

    @Test
    void testCheckFraud_LowRisk() {
        FraudScoreResponse response = fraudScoringService.checkFraud(testRequest);

        assertNotNull(response);
        assertTrue(response.getIsApproved());
        assertEquals(FraudCase.RiskLevel.LOW, response.getRiskLevel());
        assertTrue(response.getFraudScore() < 25);
    }

    @Test
    void testCheckFraud_AddressMismatch() {
        testRequest.setBillingAddress("456 Other St");

        FraudScoreResponse response = fraudScoringService.checkFraud(testRequest);

        assertNotNull(response);
        assertTrue(response.getFraudScore() >= 15);
    }

    @Test
    void testCheckFraud_HighAmount() {
        testRequest.setAmount(new BigDecimal("2000.00"));

        FraudScoreResponse response = fraudScoringService.checkFraud(testRequest);

        assertNotNull(response);
        assertTrue(response.getFraudScore() > 10);
    }

    @Test
    void testCheckFraud_MissingEmail() {
        testRequest.setEmail(null);

        FraudScoreResponse response = fraudScoringService.checkFraud(testRequest);

        assertNotNull(response);
        assertTrue(response.getFraudScore() >= 10);
    }

    @Test
    void testCalculateFraudScore() {
        Integer score = fraudScoringService.calculateFraudScore(testRequest);

        assertNotNull(score);
        assertTrue(score >= 0 && score <= 100);
    }

    @Test
    void testDetermineRiskLevel_Low() {
        FraudCase.RiskLevel level = fraudScoringService.determineRiskLevel(10);
        assertEquals(FraudCase.RiskLevel.LOW, level);
    }

    @Test
    void testDetermineRiskLevel_Medium() {
        FraudCase.RiskLevel level = fraudScoringService.determineRiskLevel(40);
        assertEquals(FraudCase.RiskLevel.MEDIUM, level);
    }

    @Test
    void testDetermineRiskLevel_High() {
        FraudCase.RiskLevel level = fraudScoringService.determineRiskLevel(60);
        assertEquals(FraudCase.RiskLevel.HIGH, level);
    }

    @Test
    void testDetermineRiskLevel_Critical() {
        FraudCase.RiskLevel level = fraudScoringService.determineRiskLevel(80);
        assertEquals(FraudCase.RiskLevel.CRITICAL, level);
    }

    @Test
    void testGenerateFraudReason() {
        String reason = fraudScoringService.generateFraudReason(testRequest, 5);

        assertNotNull(reason);
        assertNotEmpty(reason);
    }

    private void assertNotEmpty(String value) {
        assertNotNull(value);
        assertTrue(value.length() > 0);
    }
}
