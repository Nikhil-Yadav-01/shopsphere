package com.rudraksha.shopsphere.recommendation.ml;

import com.rudraksha.shopsphere.recommendation.dto.RecommendedProductItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class SimilarProductsEngine {

    public List<RecommendedProductItem> getSimilarProducts(Long productId, Integer limit) {
        log.info("Finding similar products to product {}", productId);
        
        // Mock implementation: Similar products by attributes
        List<RecommendedProductItem> similarProducts = new ArrayList<>();
        
        for (int i = 0; i < limit; i++) {
            similarProducts.add(RecommendedProductItem.builder()
                    .productId(productId + i + 1)
                    .productName("Similar Product " + i)
                    .description("Similar category and features")
                    .price(BigDecimal.valueOf(49.99 + (i * 5)))
                    .rating(4.2 + (i * 0.05))
                    .reviewCount(95 + (i * 15))
                    .relevanceScore(0.90 - (i * 0.05))
                    .reason("Similar to what you're viewing")
                    .build());
        }
        
        return similarProducts;
    }
}
