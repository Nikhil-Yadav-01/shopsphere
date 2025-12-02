package com.rudraksha.shopsphere.search.service;

import com.rudraksha.shopsphere.search.dto.SearchRequest;
import com.rudraksha.shopsphere.search.dto.SearchResponse;

import java.util.List;

public interface SearchService {

    SearchResponse search(SearchRequest request);

    List<String> getSuggestions(String query);

    List<Object> getFacets(String category);

    void rebuildIndex();

    void indexProduct(Long productId);

    void deleteFromIndex(Long productId);

    void updateIndex(Long productId);
}
