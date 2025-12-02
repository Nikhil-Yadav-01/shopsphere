package com.rudraksha.shopsphere.notification.events.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rudraksha.shopsphere.notification.dto.EmailRequest;
import com.rudraksha.shopsphere.notification.service.EmailService;
import com.rudraksha.shopsphere.notification.service.TemplateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Listens to PaymentConfirmed events and sends payment receipt email
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class PaymentConfirmedConsumer {

    private final EmailService emailService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "payment.confirmed";
    private static final String GROUP_ID = "notification-service-payment-confirmed-group";

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumePaymentConfirmed(String message) {
        log.info("Received payment confirmed event");

        try {
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String orderId = (String) eventData.get("orderId");
            String userEmail = (String) eventData.get("userEmail");
            String transactionId = (String) eventData.get("transactionId");

            if (userEmail == null) {
                log.warn("User email not found in payment confirmed event for order: {}", orderId);
                return;
            }

            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", orderId);
            variables.put("transactionId", transactionId);
            variables.put("amount", eventData.get("amount"));
            variables.put("paymentMethod", eventData.get("paymentMethod"));
            variables.put("confirmationDate", eventData.get("confirmedAt"));

            // Render template
            String htmlContent = templateService.renderEmailTemplate("payment-receipt", variables);

            // Send email
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(userEmail)
                    .subject("Payment Receipt - Order " + orderId)
                    .body(htmlContent)
                    .templateName("payment-receipt")
                    .build();

            emailService.sendEmail(emailRequest);
            log.debug("Payment receipt email sent to: {} for order: {}", userEmail, orderId);
        } catch (Exception e) {
            log.error("Error processing payment confirmed event: {}", message, e);
            // Send to DLQ on failure
        }
    }
}
