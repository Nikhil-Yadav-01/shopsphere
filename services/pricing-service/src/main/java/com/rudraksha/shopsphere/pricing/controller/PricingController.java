package com.rudraksha.shopsphere.pricing.controller;

import com.rudraksha.shopsphere.pricing.dto.request.CreatePriceRequest;
import com.rudraksha.shopsphere.pricing.dto.response.PriceResponse;
import com.rudraksha.shopsphere.pricing.service.PricingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pricing")
@RequiredArgsConstructor
public class PricingController {

    private final PricingService pricingService;

    @PostMapping
    public ResponseEntity<PriceResponse> createPrice(@Valid @RequestBody CreatePriceRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pricingService.createPrice(request));
    }

    @GetMapping("/product/{productId}")
    public ResponseEntity<PriceResponse> getPriceByProductId(@PathVariable String productId) {
        return ResponseEntity.ok(pricingService.getPriceByProductId(productId));
    }

    @PutMapping("/product/{productId}")
    public ResponseEntity<PriceResponse> updatePrice(
            @PathVariable String productId,
            @Valid @RequestBody CreatePriceRequest request) {
        return ResponseEntity.ok(pricingService.updatePrice(productId, request));
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Void> deletePrice(@PathVariable String productId) {
        pricingService.deletePrice(productId);
        return ResponseEntity.noContent().build();
    }
}
