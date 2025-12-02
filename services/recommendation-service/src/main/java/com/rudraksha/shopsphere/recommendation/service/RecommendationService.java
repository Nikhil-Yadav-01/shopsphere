package com.rudraksha.shopsphere.recommendation.service;

import com.rudraksha.shopsphere.recommendation.dto.RecommendationResponse;

public interface RecommendationService {

    RecommendationResponse getForYouRecommendations(Long userId, Integer limit);

    RecommendationResponse getSimilarProducts(Long productId, Integer limit);

    RecommendationResponse getTrendingProducts(Integer limit);

    RecommendationResponse getPersonalizedRecommendations(Long userId, Integer limit);

    void recordUserBehavior(Long userId, Long productId, String behaviorType);
}
