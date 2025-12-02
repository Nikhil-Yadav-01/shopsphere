package com.rudraksha.shopsphere.recommendation.service;

import com.rudraksha.shopsphere.recommendation.dto.RecommendationResponse;
import com.rudraksha.shopsphere.recommendation.ml.CollaborativeFilteringEngine;
import com.rudraksha.shopsphere.recommendation.ml.SimilarProductsEngine;
import com.rudraksha.shopsphere.recommendation.ml.TrendingEngine;
import com.rudraksha.shopsphere.recommendation.service.impl.RecommendationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class RecommendationServiceImplTest {

    @Mock
    private CollaborativeFilteringEngine cfEngine;

    @Mock
    private SimilarProductsEngine similarEngine;

    @Mock
    private TrendingEngine trendingEngine;

    @InjectMocks
    private RecommendationServiceImpl recommendationService;

    @BeforeEach
    void setUp() {
        // Initialize real engines for testing
        recommendationService = new RecommendationServiceImpl(
                new CollaborativeFilteringEngine(),
                new SimilarProductsEngine(),
                new TrendingEngine()
        );
    }

    @Test
    void testGetForYouRecommendations() {
        RecommendationResponse response = recommendationService.getForYouRecommendations(1L, 5);

        assertNotNull(response);
        assertEquals("forYou", response.getType());
        assertFalse(response.getProducts().isEmpty());
        assertTrue(response.getProducts().size() <= 5);
        response.getProducts().forEach(item -> {
            assertNotNull(item.getProductId());
            assertNotNull(item.getProductName());
            assertNotNull(item.getRelevanceScore());
            assertNotNull(item.getReason());
        });
    }

    @Test
    void testGetSimilarProducts() {
        RecommendationResponse response = recommendationService.getSimilarProducts(100L, 5);

        assertNotNull(response);
        assertEquals("similar", response.getType());
        assertFalse(response.getProducts().isEmpty());
        assertTrue(response.getProducts().size() <= 5);
    }

    @Test
    void testGetTrendingProducts() {
        RecommendationResponse response = recommendationService.getTrendingProducts(10);

        assertNotNull(response);
        assertEquals("trending", response.getType());
        assertFalse(response.getProducts().isEmpty());
        assertTrue(response.getProducts().size() <= 10);
        response.getProducts().forEach(item ->
            assertEquals("Trending now", item.getReason())
        );
    }

    @Test
    void testGetPersonalizedRecommendations() {
        RecommendationResponse response = recommendationService.getPersonalizedRecommendations(1L, 10);

        assertNotNull(response);
        assertEquals("personalized", response.getType());
        assertFalse(response.getProducts().isEmpty());
    }

    @Test
    void testRecordUserBehavior() {
        assertDoesNotThrow(() -> 
            recommendationService.recordUserBehavior(1L, 100L, "view")
        );
    }

    @Test
    void testRecommendationQuality() {
        RecommendationResponse response = recommendationService.getForYouRecommendations(1L, 5);

        response.getProducts().forEach(item -> {
            // Verify relevance score is between 0 and 1
            assertTrue(item.getRelevanceScore() >= 0 && item.getRelevanceScore() <= 1);
            // Verify rating is reasonable
            assertTrue(item.getRating() >= 0 && item.getRating() <= 5);
            // Verify review count is non-negative
            assertTrue(item.getReviewCount() >= 0);
        });
    }

    @Test
    void testRecommendationLimit() {
        for (int limit : List.of(1, 5, 10, 20)) {
            RecommendationResponse response = recommendationService.getForYouRecommendations(1L, limit);
            assertTrue(response.getProducts().size() <= limit);
        }
    }
}
