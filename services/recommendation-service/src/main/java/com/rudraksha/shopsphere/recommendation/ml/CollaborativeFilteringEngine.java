package com.rudraksha.shopsphere.recommendation.ml;

import com.rudraksha.shopsphere.recommendation.dto.RecommendedProductItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class CollaborativeFilteringEngine {

    public List<RecommendedProductItem> getRecommendations(Long userId, Integer limit) {
        log.info("Generating CF recommendations for user {}", userId);
        
        // Mock implementation: Users like you also liked...
        List<RecommendedProductItem> recommendations = new ArrayList<>();
        
        for (int i = 0; i < limit; i++) {
            recommendations.add(RecommendedProductItem.builder()
                    .productId((long) (100 + i))
                    .productName("Recommended Product " + i)
                    .description("Popular with users similar to you")
                    .price(BigDecimal.valueOf(50 + (i * 10)))
                    .rating(4.0 + (i * 0.1))
                    .reviewCount(100 + (i * 20))
                    .relevanceScore(0.95 - (i * 0.05))
                    .reason("Users like you also liked this")
                    .build());
        }
        
        return recommendations;
    }
}
