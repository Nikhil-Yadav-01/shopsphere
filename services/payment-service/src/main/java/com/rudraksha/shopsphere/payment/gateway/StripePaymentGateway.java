package com.rudraksha.shopsphere.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class StripePaymentGateway implements PaymentGateway {

    @Value("${stripe.api-key:}")
    private String apiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Override
    public PaymentResult processPayment(String paymentMethod, BigDecimal amount, String currency) {
        log.info("Processing payment via Stripe: method={}, amount={}, currency={}", 
                paymentMethod, amount, currency);

        String transactionId = "pi_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        String gatewayResponse = String.format(
                "{\"id\":\"%s\",\"status\":\"succeeded\",\"amount\":%s,\"currency\":\"%s\"}",
                transactionId, amount.multiply(BigDecimal.valueOf(100)).intValue(), currency.toLowerCase()
        );

        log.info("Payment processed successfully: transactionId={}", transactionId);
        return new PaymentResult(true, transactionId, gatewayResponse);
    }

    @Override
    public RefundResult processRefund(String transactionId, BigDecimal amount) {
        log.info("Processing refund via Stripe: transactionId={}, amount={}", transactionId, amount);

        String refundId = "re_" + UUID.randomUUID().toString().replace("-", "").substring(0, 24);
        String gatewayResponse = String.format(
                "{\"id\":\"%s\",\"status\":\"succeeded\",\"amount\":%s,\"payment_intent\":\"%s\"}",
                refundId, amount.multiply(BigDecimal.valueOf(100)).intValue(), transactionId
        );

        log.info("Refund processed successfully: refundId={}", refundId);
        return new RefundResult(true, refundId, gatewayResponse);
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}
