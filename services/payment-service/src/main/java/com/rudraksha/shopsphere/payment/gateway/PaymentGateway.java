package com.rudraksha.shopsphere.payment.gateway;

import java.math.BigDecimal;

public interface PaymentGateway {

    PaymentResult processPayment(String paymentMethod, BigDecimal amount, String currency);

    RefundResult processRefund(String transactionId, BigDecimal amount);

    record PaymentResult(
            boolean success,
            String transactionId,
            String gatewayResponse
    ) {}

    record RefundResult(
            boolean success,
            String refundId,
            String gatewayResponse
    ) {}
}
