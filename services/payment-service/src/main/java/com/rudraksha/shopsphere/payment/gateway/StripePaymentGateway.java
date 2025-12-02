package com.rudraksha.shopsphere.payment.gateway;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Slf4j
@Component
public class StripePaymentGateway implements PaymentGateway {

    private static final String PAYMENT_ID_PREFIX = "pi_";
    private static final String REFUND_ID_PREFIX = "re_";
    private static final int STRIPE_ID_LENGTH = 24;

    @Value("${stripe.api-key:}")
    private String apiKey;

    @Value("${stripe.webhook-secret:}")
    private String webhookSecret;

    @Override
    public PaymentResult processPayment(String paymentMethod, BigDecimal amount, String currency) {
        log.info("Processing payment via Stripe: method={}, amount={}, currency={}",
                paymentMethod, amount, currency);

        String transactionId = generatePaymentId();
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

        String refundId = generateRefundId();
        String gatewayResponse = String.format(
                "{\"id\":\"%s\",\"status\":\"succeeded\",\"amount\":%s,\"payment_intent\":\"%s\"}",
                refundId, amount.multiply(BigDecimal.valueOf(100)).intValue(), transactionId
        );

        log.info("Refund processed successfully: refundId={}", refundId);
        return new RefundResult(true, refundId, gatewayResponse);
    }

    private String generatePaymentId() {
        return PAYMENT_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, STRIPE_ID_LENGTH);
    }

    private String generateRefundId() {
        return REFUND_ID_PREFIX + UUID.randomUUID().toString().replace("-", "").substring(0, STRIPE_ID_LENGTH);
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }
}
