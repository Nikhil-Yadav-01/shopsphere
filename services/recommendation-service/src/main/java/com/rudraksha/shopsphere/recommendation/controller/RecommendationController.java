package com.rudraksha.shopsphere.recommendation.controller;

import com.rudraksha.shopsphere.recommendation.dto.RecommendationResponse;
import com.rudraksha.shopsphere.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/recommendations")
@RequiredArgsConstructor
public class RecommendationController {

    private final RecommendationService recommendationService;

    @GetMapping("/for-you")
    public ResponseEntity<RecommendationResponse> getForYouRecommendations(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(recommendationService.getForYouRecommendations(userId, limit));
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<RecommendationResponse> getSimilarProducts(
            @PathVariable Long productId,
            @RequestParam(required = false, defaultValue = "5") Integer limit) {
        return ResponseEntity.ok(recommendationService.getSimilarProducts(productId, limit));
    }

    @GetMapping("/trending")
    public ResponseEntity<RecommendationResponse> getTrendingProducts(
            @RequestParam(required = false, defaultValue = "10") Integer limit) {
        return ResponseEntity.ok(recommendationService.getTrendingProducts(limit));
    }

    @GetMapping("/personalized")
    public ResponseEntity<RecommendationResponse> getPersonalizedRecommendations(
            @RequestParam(required = false, defaultValue = "10") Integer limit,
            Authentication auth) {
        Long userId = Long.parseLong(auth.getName());
        return ResponseEntity.ok(recommendationService.getPersonalizedRecommendations(userId, limit));
    }
}
