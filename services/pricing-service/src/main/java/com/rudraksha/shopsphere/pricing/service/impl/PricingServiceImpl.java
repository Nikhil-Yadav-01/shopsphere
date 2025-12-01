package com.rudraksha.shopsphere.pricing.service.impl;

import com.rudraksha.shopsphere.pricing.dto.request.CreatePriceRequest;
import com.rudraksha.shopsphere.pricing.dto.response.PriceResponse;
import com.rudraksha.shopsphere.pricing.entity.Price;
import com.rudraksha.shopsphere.pricing.repository.PriceRepository;
import com.rudraksha.shopsphere.pricing.service.PricingService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PricingServiceImpl implements PricingService {

    private final PriceRepository priceRepository;

    @Override
    @Transactional
    public PriceResponse createPrice(CreatePriceRequest request) {
        Price price = Price.builder()
                .productId(request.getProductId())
                .basePrice(request.getBasePrice())
                .sellingPrice(request.getSellingPrice())
                .discountPercentage(request.getDiscountPercentage())
                .currency(request.getCurrency())
                .build();
        
        Price saved = priceRepository.save(price);
        return mapToResponse(saved);
    }

    @Override
    public PriceResponse getPriceByProductId(String productId) {
        Price price = priceRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Price not found for product: " + productId));
        return mapToResponse(price);
    }

    @Override
    @Transactional
    public PriceResponse updatePrice(String productId, CreatePriceRequest request) {
        Price price = priceRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Price not found for product: " + productId));
        
        price.setBasePrice(request.getBasePrice());
        price.setSellingPrice(request.getSellingPrice());
        price.setDiscountPercentage(request.getDiscountPercentage());
        price.setCurrency(request.getCurrency());
        
        Price updated = priceRepository.save(price);
        return mapToResponse(updated);
    }

    @Override
    @Transactional
    public void deletePrice(String productId) {
        Price price = priceRepository.findByProductId(productId)
                .orElseThrow(() -> new RuntimeException("Price not found for product: " + productId));
        priceRepository.delete(price);
    }

    private PriceResponse mapToResponse(Price price) {
        return PriceResponse.builder()
                .id(price.getId())
                .productId(price.getProductId())
                .basePrice(price.getBasePrice())
                .sellingPrice(price.getSellingPrice())
                .discountPercentage(price.getDiscountPercentage())
                .currency(price.getCurrency())
                .build();
    }
}
