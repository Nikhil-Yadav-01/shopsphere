package com.rudraksha.shopsphere.pricing.service;

import com.rudraksha.shopsphere.pricing.dto.request.CreatePriceRequest;
import com.rudraksha.shopsphere.pricing.dto.response.PriceResponse;

public interface PricingService {
    PriceResponse createPrice(CreatePriceRequest request);
    PriceResponse getPriceByProductId(String productId);
    PriceResponse updatePrice(String productId, CreatePriceRequest request);
    void deletePrice(String productId);
}
