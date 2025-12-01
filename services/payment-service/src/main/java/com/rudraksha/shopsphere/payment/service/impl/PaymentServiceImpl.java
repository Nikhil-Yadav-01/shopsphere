package com.rudraksha.shopsphere.payment.service.impl;

import com.rudraksha.shopsphere.payment.dto.request.CreatePaymentRequest;
import com.rudraksha.shopsphere.payment.dto.request.RefundRequest;
import com.rudraksha.shopsphere.payment.dto.response.PaymentResponse;
import com.rudraksha.shopsphere.payment.dto.response.RefundResponse;
import com.rudraksha.shopsphere.payment.entity.Payment;
import com.rudraksha.shopsphere.payment.entity.Refund;
import com.rudraksha.shopsphere.payment.events.producer.PaymentEventProducer;
import com.rudraksha.shopsphere.payment.exception.PaymentNotFoundException;
import com.rudraksha.shopsphere.payment.exception.InvalidPaymentStateException;
import com.rudraksha.shopsphere.payment.gateway.PaymentGateway;
import com.rudraksha.shopsphere.payment.repository.PaymentRepository;
import com.rudraksha.shopsphere.payment.repository.RefundRepository;
import com.rudraksha.shopsphere.payment.service.PaymentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final RefundRepository refundRepository;
    private final PaymentGateway paymentGateway;
    private final PaymentEventProducer eventProducer;

    @Override
    @Transactional
    public PaymentResponse createPayment(CreatePaymentRequest request) {
        log.info("Creating payment for orderId={}", request.getOrderId());

        Payment payment = Payment.builder()
                .orderId(request.getOrderId())
                .userId(request.getUserId())
                .amount(request.getAmount())
                .currency(request.getCurrency().toUpperCase())
                .paymentMethod(request.getPaymentMethod())
                .status(Payment.PaymentStatus.PROCESSING)
                .build();

        payment = paymentRepository.save(payment);

        PaymentGateway.PaymentResult result = paymentGateway.processPayment(
                request.getPaymentMethod(),
                request.getAmount(),
                request.getCurrency()
        );

        if (result.success()) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setTransactionId(result.transactionId());
            payment.setGatewayResponse(result.gatewayResponse());
            eventProducer.publishPaymentCompleted(payment);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            payment.setGatewayResponse(result.gatewayResponse());
            eventProducer.publishPaymentFailed(payment);
        }

        payment = paymentRepository.save(payment);
        log.info("Payment created: id={}, status={}", payment.getId(), payment.getStatus());

        return PaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public PaymentResponse getPayment(UUID paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));
        return PaymentResponse.fromEntity(payment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentResponse> getPaymentsByOrderId(UUID orderId) {
        return paymentRepository.findByOrderId(orderId).stream()
                .map(PaymentResponse::fromEntity)
                .toList();
    }

    @Override
    @Transactional
    public RefundResponse processRefund(UUID paymentId, RefundRequest request) {
        log.info("Processing refund for paymentId={}, amount={}", paymentId, request.getAmount());

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new PaymentNotFoundException("Payment not found: " + paymentId));

        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new InvalidPaymentStateException(
                    "Cannot refund payment with status: " + payment.getStatus(),
                    payment.getStatus().toString(),
                    "REFUND");
        }

        if (payment.getTransactionId() == null) {
            throw new InvalidPaymentStateException(
                    "Payment has no transaction ID for refund",
                    payment.getStatus().toString(),
                    "REFUND");
        }

        Refund refund = Refund.builder()
                .paymentId(paymentId)
                .amount(request.getAmount())
                .reason(request.getReason())
                .status(Refund.RefundStatus.PROCESSING)
                .build();

        refund = refundRepository.save(refund);

        PaymentGateway.RefundResult result = paymentGateway.processRefund(
                payment.getTransactionId(),
                request.getAmount()
        );

        if (result != null && result.success()) {
            refund.setStatus(Refund.RefundStatus.COMPLETED);
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            eventProducer.publishPaymentRefunded(payment, refund);
        } else {
            refund.setStatus(Refund.RefundStatus.FAILED);
        }

        refund = refundRepository.save(refund);
        log.info("Refund processed: id={}, status={}", refund.getId(), refund.getStatus());

        return RefundResponse.fromEntity(refund);
    }

    @Override
    @Transactional
    public void handleWebhookEvent(String payload, String signature) {
        log.info("Processing webhook event");

        try {
            if (payload == null || payload.isEmpty()) {
                log.warn("Received empty webhook payload");
                return;
            }

            String webhookSecret = getWebhookSecret();
            if (!verifyWebhookSignature(payload, signature, webhookSecret)) {
                log.warn("Invalid webhook signature, rejecting event");
                throw new InvalidPaymentStateException(
                        "Invalid webhook signature",
                        "UNAUTHORIZED",
                        "WEBHOOK_VERIFICATION");
            }

            processWebhookPayload(payload);
            log.info("Webhook event processed successfully");
        } catch (InvalidPaymentStateException e) {
            log.error("Webhook processing failed: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error processing webhook", e);
            throw new RuntimeException("Failed to process webhook event", e);
        }
    }

    /**
     * Verify Stripe webhook signature using HMAC-SHA256.
     */
    private boolean verifyWebhookSignature(String payload, String signature, String secret) {
        if (signature == null || secret == null) {
            log.warn("Missing signature or secret for webhook verification");
            return false;
        }

        try {
            String[] parts = signature.split(",");
            String timestamp = null;
            String signedContent = null;

            for (String part : parts) {
                if (part.startsWith("t=")) {
                    timestamp = part.substring(2);
                } else if (part.startsWith("v1=")) {
                    signedContent = part.substring(3);
                }
            }

            if (timestamp == null || signedContent == null) {
                log.warn("Invalid signature format");
                return false;
            }

            String toSign = timestamp + "." + payload;
            String expectedSignature = computeHmacSha256(secret, toSign);

            return signedContent.equals(expectedSignature);
        } catch (Exception e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }

    /**
     * Compute HMAC-SHA256 signature.
     */
    private String computeHmacSha256(String secret, String message) throws Exception {
        javax.crypto.Mac mac = javax.crypto.Mac.getInstance("HmacSHA256");
        javax.crypto.spec.SecretKeySpec secretKeySpec = new javax.crypto.spec.SecretKeySpec(
                secret.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                "HmacSHA256");
        mac.init(secretKeySpec);
        byte[] bytes = mac.doFinal(message.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        return javax.xml.bind.DatatypeConverter.printHexBinary(bytes).toLowerCase();
    }

    /**
     * Process the webhook payload (Stripe event).
     */
    private void processWebhookPayload(String payload) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            com.fasterxml.jackson.databind.JsonNode event = mapper.readTree(payload);

            String type = event.get("type").asText();
            com.fasterxml.jackson.databind.JsonNode data = event.get("data").get("object");

            switch (type) {
                case "payment_intent.succeeded" -> handlePaymentIntentSucceeded(data);
                case "payment_intent.payment_failed" -> handlePaymentIntentFailed(data);
                case "charge.refunded" -> handleChargeRefunded(data);
                default -> log.info("Unhandled webhook type: {}", type);
            }
        } catch (Exception e) {
            log.error("Error processing webhook payload", e);
            throw new RuntimeException("Failed to parse webhook payload", e);
        }
    }

    private void handlePaymentIntentSucceeded(com.fasterxml.jackson.databind.JsonNode data) {
        String transactionId = data.get("id").asText();
        log.info("Handling payment_intent.succeeded: transactionId={}", transactionId);
        // Note: In production, you'd look up the payment by transaction ID and update status
    }

    private void handlePaymentIntentFailed(com.fasterxml.jackson.databind.JsonNode data) {
        String transactionId = data.get("id").asText();
        String lastPaymentError = data.has("last_payment_error") 
                ? data.get("last_payment_error").get("message").asText()
                : "Unknown error";
        log.info("Handling payment_intent.payment_failed: transactionId={}, error={}", 
                transactionId, lastPaymentError);
    }

    private void handleChargeRefunded(com.fasterxml.jackson.databind.JsonNode data) {
        String chargeId = data.get("id").asText();
        log.info("Handling charge.refunded: chargeId={}", chargeId);
        // Note: In production, you'd look up the refund by payment intent and mark as completed
    }

    private String getWebhookSecret() {
        // This would normally come from the PaymentGateway
        return System.getenv("STRIPE_WEBHOOK_SECRET");
    }
}
