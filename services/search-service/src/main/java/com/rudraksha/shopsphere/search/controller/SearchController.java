package com.rudraksha.shopsphere.search.controller;

import com.rudraksha.shopsphere.search.dto.SearchRequest;
import com.rudraksha.shopsphere.search.dto.SearchResponse;
import com.rudraksha.shopsphere.search.service.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    @GetMapping
    public ResponseEntity<SearchResponse> search(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) java.math.BigDecimal minPrice,
            @RequestParam(required = false) java.math.BigDecimal maxPrice,
            @RequestParam(required = false, defaultValue = "relevance") String sortBy,
            @RequestParam(required = false, defaultValue = "desc") String sortOrder,
            @RequestParam(required = false, defaultValue = "0") Integer page,
            @RequestParam(required = false, defaultValue = "20") Integer size) {

        SearchRequest request = SearchRequest.builder()
                .query(q)
                .category(category)
                .minPrice(minPrice)
                .maxPrice(maxPrice)
                .sortBy(sortBy)
                .sortOrder(sortOrder)
                .page(page)
                .size(size)
                .build();

        return ResponseEntity.ok(searchService.search(request));
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(
            @RequestParam(required = false) String q) {
        return ResponseEntity.ok(searchService.getSuggestions(q));
    }

    @GetMapping("/facets")
    public ResponseEntity<List<Object>> getFacets(
            @RequestParam(required = false) String category) {
        return ResponseEntity.ok(searchService.getFacets(category));
    }

    @PostMapping("/rebuild-index")
    public ResponseEntity<String> rebuildIndex() {
        searchService.rebuildIndex();
        return ResponseEntity.ok("Index rebuild started");
    }
}
