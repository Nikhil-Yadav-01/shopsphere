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
 * Listens to ShipmentDelivered events and sends delivery notification email
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ShipmentDeliveredConsumer {

    private final EmailService emailService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "shipment.delivered";
    private static final String GROUP_ID = "notification-service-shipment-delivered-group";

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeShipmentDelivered(String message) {
        log.info("Received shipment delivered event");

        try {
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String orderId = (String) eventData.get("orderId");
            String userEmail = (String) eventData.get("userEmail");

            if (userEmail == null) {
                log.warn("User email not found in shipment delivered event for order: {}", orderId);
                return;
            }

            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", orderId);
            variables.put("deliveryDate", eventData.get("deliveredAt"));
            variables.put("recipientName", eventData.get("recipientName"));

            // Render template
            String htmlContent = templateService.renderEmailTemplate("delivery-notification", variables);

            // Send email
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(userEmail)
                    .subject("Your Order Has Been Delivered - Order " + orderId)
                    .body(htmlContent)
                    .templateName("delivery-notification")
                    .build();

            emailService.sendEmail(emailRequest);
            log.debug("Delivery notification email sent to: {} for order: {}", userEmail, orderId);
        } catch (Exception e) {
            log.error("Error processing shipment delivered event: {}", message, e);
            // Send to DLQ on failure
        }
    }
}
