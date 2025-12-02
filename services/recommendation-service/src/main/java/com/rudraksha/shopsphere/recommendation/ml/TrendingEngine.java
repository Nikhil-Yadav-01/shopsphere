package com.rudraksha.shopsphere.recommendation.ml;

import com.rudraksha.shopsphere.recommendation.dto.RecommendedProductItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Component
@Slf4j
public class TrendingEngine {

    public List<RecommendedProductItem> getTrendingProducts(Integer limit) {
        log.info("Generating trending products");
        
        // Mock implementation: Most viewed, purchased, and highly-rated products
        List<RecommendedProductItem> trendingProducts = new ArrayList<>();
        
        String[] trendingNames = {
                "Apple AirPods Pro", "Sony WH-1000XM4", "Samsung Galaxy S24",
                "Nike Air Max 90", "Starbucks French Press", "DJI Air 3S",
                "Dyson V15", "GoPro Hero 13", "Sony A6700", "MacBook Pro"
        };
        
        for (int i = 0; i < Math.min(limit, trendingNames.length); i++) {
            trendingProducts.add(RecommendedProductItem.builder()
                    .productId((long) (1000 + i))
                    .productName(trendingNames[i])
                    .description("Currently trending with high demand")
                    .price(BigDecimal.valueOf(99 + (i * 150)))
                    .rating(4.5 + (i * 0.02))
                    .reviewCount(500 + (i * 100))
                    .relevanceScore(0.85 - (i * 0.02))
                    .reason("Trending now")
                    .build());
        }
        
        return trendingProducts;
    }
}
