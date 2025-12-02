package com.rudraksha.shopsphere.search.service;

import com.rudraksha.shopsphere.search.dto.SearchRequest;
import com.rudraksha.shopsphere.search.dto.SearchResponse;
import com.rudraksha.shopsphere.search.service.impl.SearchServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class SearchServiceImplTest {

    private SearchServiceImpl searchService;

    @BeforeEach
    void setUp() {
        searchService = new SearchServiceImpl();
    }

    @Test
    void testSearch_BasicQuery() {
        SearchRequest request = SearchRequest.builder()
                .query("laptop")
                .page(0)
                .size(20)
                .build();

        SearchResponse response = searchService.search(request);

        assertNotNull(response);
        assertTrue(response.getTotalResults() > 0);
        assertFalse(response.getResults().isEmpty());
        assertTrue(response.getExecutionTimeMs() >= 0);
    }

    @Test
    void testSearch_WithPriceFilter() {
        SearchRequest request = SearchRequest.builder()
                .query("laptop")
                .minPrice(new BigDecimal("500"))
                .maxPrice(new BigDecimal("1500"))
                .page(0)
                .size(20)
                .build();

        SearchResponse response = searchService.search(request);

        assertNotNull(response);
        response.getResults().forEach(item ->
                assertTrue(item.getPrice().compareTo(new BigDecimal("500")) >= 0 &&
                        item.getPrice().compareTo(new BigDecimal("1500")) <= 0)
        );
    }

    @Test
    void testSearch_WithCategory() {
        SearchRequest request = SearchRequest.builder()
                .query("shoes")
                .category("Electronics")
                .page(0)
                .size(20)
                .build();

        SearchResponse response = searchService.search(request);

        assertNotNull(response);
        response.getResults().forEach(item ->
                assertEquals("Electronics", item.getCategory())
        );
    }

    @Test
    void testGetSuggestions() {
        String query = "lap";

        java.util.List<String> suggestions = searchService.getSuggestions(query);

        assertNotNull(suggestions);
        assertFalse(suggestions.isEmpty());
        suggestions.forEach(s -> assertTrue(s.contains(query)));
    }

    @Test
    void testGetFacets() {
        java.util.List<Object> facets = searchService.getFacets("Electronics");

        assertNotNull(facets);
    }

    @Test
    void testRebuildIndex() {
        assertDoesNotThrow(() -> searchService.rebuildIndex());
    }

    @Test
    void testIndexProduct() {
        assertDoesNotThrow(() -> searchService.indexProduct(1L));
    }

    @Test
    void testDeleteFromIndex() {
        assertDoesNotThrow(() -> searchService.deleteFromIndex(1L));
    }

    @Test
    void testUpdateIndex() {
        assertDoesNotThrow(() -> searchService.updateIndex(1L));
    }

    @Test
    void testSearch_Pagination() {
        SearchRequest request = SearchRequest.builder()
                .query("laptop")
                .page(0)
                .size(10)
                .build();

        SearchResponse response = searchService.search(request);

        assertNotNull(response);
        assertTrue(response.getResults().size() <= 10);
    }
}
