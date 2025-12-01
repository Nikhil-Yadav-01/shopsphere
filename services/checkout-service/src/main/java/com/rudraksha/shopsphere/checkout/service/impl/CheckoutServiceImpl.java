package com.rudraksha.shopsphere.checkout.service.impl;

import com.rudraksha.shopsphere.checkout.api.CatalogClient;
import com.rudraksha.shopsphere.checkout.dto.request.CheckoutRequest;
import com.rudraksha.shopsphere.checkout.dto.response.CheckoutResponse;
import com.rudraksha.shopsphere.checkout.saga.CheckoutSagaOrchestrator;
import com.rudraksha.shopsphere.checkout.service.CheckoutService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class CheckoutServiceImpl implements CheckoutService {

    private final CheckoutSagaOrchestrator sagaOrchestrator;
    private final CatalogClient catalogClient;

    @Override
    public CheckoutResponse processCheckout(CheckoutRequest request) {
        log.info("Processing checkout for user: {}", request.getUserId());
        
        String orderId = UUID.randomUUID().toString();
        BigDecimal totalAmount = calculateTotal(request);
        
        boolean success = sagaOrchestrator.executeCheckoutSaga(orderId, request);
        
        return CheckoutResponse.builder()
                .orderId(orderId)
                .userId(request.getUserId())
                .totalAmount(totalAmount)
                .discount(BigDecimal.ZERO)
                .finalAmount(totalAmount)
                .status(success ? "SUCCESS" : "FAILED")
                .paymentUrl(success ? "/payment/" + orderId : null)
                .build();
    }

    /**
     * Calculate total amount by fetching actual product prices from catalog service.
     * Falls back to default pricing if catalog client fails.
     */
    private BigDecimal calculateTotal(CheckoutRequest request) {
        try {
            BigDecimal total = BigDecimal.ZERO;
            
            for (var item : request.getItems()) {
                try {
                    CatalogClient.ProductResponse product = catalogClient.getProduct(item.getProductId());
                    if (product != null && product.price() != null) {
                        BigDecimal itemPrice = product.price().multiply(BigDecimal.valueOf(item.getQuantity()));
                        total = total.add(itemPrice);
                        log.debug("Added item: productId={}, price={}, quantity={}", 
                                item.getProductId(), product.price(), item.getQuantity());
                    } else {
                        log.warn("Invalid product response for productId: {}", item.getProductId());
                        total = total.add(BigDecimal.valueOf(item.getQuantity() * 100));
                    }
                } catch (Exception e) {
                    log.warn("Failed to fetch product details for productId: {}, using default pricing", 
                            item.getProductId(), e);
                    total = total.add(BigDecimal.valueOf(item.getQuantity() * 100));
                }
            }
            
            log.info("Calculated total amount: {}", total);
            return total;
        } catch (Exception e) {
            log.error("Error calculating total amount, using default pricing", e);
            return BigDecimal.valueOf(request.getItems().size() * 100);
        }
    }
}
