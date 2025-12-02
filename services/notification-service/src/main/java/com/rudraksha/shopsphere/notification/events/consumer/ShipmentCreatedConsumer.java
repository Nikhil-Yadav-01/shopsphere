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
 * Listens to ShipmentCreated events and sends shipping notification email
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentCreatedConsumer {

    private final EmailService emailService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "shipment.created";
    private static final String GROUP_ID = "notification-service-shipment-created-group";

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeShipmentCreated(String message) {
        log.info("Received shipment created event");

        try {
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String orderId = (String) eventData.get("orderId");
            String userEmail = (String) eventData.get("userEmail");
            String trackingNumber = (String) eventData.get("trackingNumber");
            String carrier = (String) eventData.get("carrier");

            if (userEmail == null) {
                log.warn("User email not found in shipment created event for order: {}", orderId);
                return;
            }

            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", orderId);
            variables.put("trackingNumber", trackingNumber);
            variables.put("carrier", carrier);
            variables.put("estimatedDeliveryDate", eventData.get("estimatedDeliveryDate"));

            // Render template
            String htmlContent = templateService.renderEmailTemplate("shipping-notification", variables);

            // Send email
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(userEmail)
                    .subject("Your Order Has Shipped - Order " + orderId)
                    .body(htmlContent)
                    .templateName("shipping-notification")
                    .build();

            emailService.sendEmail(emailRequest);
            log.debug("Shipping notification email sent to: {} for order: {}", userEmail, orderId);
        } catch (Exception e) {
            log.error("Error processing shipment created event: {}", message, e);
            // Send to DLQ on failure
        }
    }
}
