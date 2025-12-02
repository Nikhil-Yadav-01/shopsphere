package com.rudraksha.shopsphere.fraud.controller;

import com.rudraksha.shopsphere.fraud.dto.FraudCheckRequest;
import com.rudraksha.shopsphere.fraud.dto.FraudScoreResponse;
import com.rudraksha.shopsphere.fraud.service.FraudScoringService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/fraud")
@RequiredArgsConstructor
public class FraudController {

    private final FraudScoringService fraudScoringService;

    @PostMapping("/check")
    public ResponseEntity<FraudScoreResponse> checkFraud(
            @Valid @RequestBody FraudCheckRequest request) {
        return ResponseEntity.ok(fraudScoringService.checkFraud(request));
    }

    @PostMapping("/score")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Integer> calculateScore(
            @Valid @RequestBody FraudCheckRequest request) {
        return ResponseEntity.ok(fraudScoringService.calculateFraudScore(request));
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Fraud Service is running");
    }
}
