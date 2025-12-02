package com.rudraksha.shopsphere.recommendation.service.impl;

import com.rudraksha.shopsphere.recommendation.dto.RecommendationResponse;
import com.rudraksha.shopsphere.recommendation.dto.RecommendedProductItem;
import com.rudraksha.shopsphere.recommendation.ml.CollaborativeFilteringEngine;
import com.rudraksha.shopsphere.recommendation.ml.SimilarProductsEngine;
import com.rudraksha.shopsphere.recommendation.ml.TrendingEngine;
import com.rudraksha.shopsphere.recommendation.service.RecommendationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RecommendationServiceImpl implements RecommendationService {

    private final CollaborativeFilteringEngine cfEngine;
    private final SimilarProductsEngine similarEngine;
    private final TrendingEngine trendingEngine;

    @Override
    public RecommendationResponse getForYouRecommendations(Long userId, Integer limit) {
        log.info("Getting personalized recommendations for user {}", userId);
        
        List<RecommendedProductItem> products = cfEngine.getRecommendations(userId, limit);
        
        return RecommendationResponse.builder()
                .type("forYou")
                .products(products)
                .limit(limit)
                .message("Personalized recommendations based on your activity")
                .build();
    }

    @Override
    public RecommendationResponse getSimilarProducts(Long productId, Integer limit) {
        log.info("Getting similar products for product {}", productId);
        
        List<RecommendedProductItem> products = similarEngine.getSimilarProducts(productId, limit);
        
        return RecommendationResponse.builder()
                .type("similar")
                .products(products)
                .limit(limit)
                .message("Products similar to what you're viewing")
                .build();
    }

    @Override
    public RecommendationResponse getTrendingProducts(Integer limit) {
        log.info("Getting trending products");
        
        List<RecommendedProductItem> products = trendingEngine.getTrendingProducts(limit);
        
        return RecommendationResponse.builder()
                .type("trending")
                .products(products)
                .limit(limit)
                .message("Currently trending on ShopSphere")
                .build();
    }

    @Override
    public RecommendationResponse getPersonalizedRecommendations(Long userId, Integer limit) {
        log.info("Getting personalized recommendations for user {}", userId);
        
        // Combine multiple engines for best recommendations
        List<RecommendedProductItem> cfProducts = cfEngine.getRecommendations(userId, limit / 2);
        List<RecommendedProductItem> trendingProducts = trendingEngine.getTrendingProducts(limit / 2);
        
        List<RecommendedProductItem> combined = new java.util.ArrayList<>();
        combined.addAll(cfProducts);
        combined.addAll(trendingProducts);
        
        return RecommendationResponse.builder()
                .type("personalized")
                .products(combined)
                .limit(limit)
                .message("Personalized picks just for you")
                .build();
    }

    @Override
    public void recordUserBehavior(Long userId, Long productId, String behaviorType) {
        log.debug("Recording {} behavior: user {} on product {}", behaviorType, userId, productId);
        // In production, store this in a data warehouse or cache
        // This would be used to train recommendation models
    }
}
