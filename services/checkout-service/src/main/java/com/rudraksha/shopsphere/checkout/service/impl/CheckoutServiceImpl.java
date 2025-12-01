package com.rudraksha.shopsphere.checkout.service.impl;

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

    private BigDecimal calculateTotal(CheckoutRequest request) {
        return BigDecimal.valueOf(request.getItems().size() * 100);
    }
}
