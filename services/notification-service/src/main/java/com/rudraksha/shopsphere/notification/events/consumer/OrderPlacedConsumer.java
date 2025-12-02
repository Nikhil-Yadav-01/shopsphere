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
 * Listens to OrderPlaced events and sends order confirmation email
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OrderPlacedConsumer {

    private final EmailService emailService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "order.placed";
    private static final String GROUP_ID = "notification-service-order-placed-group";

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeOrderPlaced(String message) {
        log.info("Received order placed event");

        try {
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String orderId = (String) eventData.get("id");
            String userEmail = (String) eventData.get("userEmail");

            if (userEmail == null) {
                log.warn("User email not found in order placed event for order: {}", orderId);
                return;
            }

            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("orderId", orderId);
            variables.put("orderDate", eventData.get("createdAt"));
            variables.put("totalAmount", eventData.get("totalAmount"));

            // Render template
            String htmlContent = templateService.renderEmailTemplate("order-confirmation", variables);

            // Send email
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(userEmail)
                    .subject("Order Confirmation - " + orderId)
                    .body(htmlContent)
                    .templateName("order-confirmation")
                    .build();

            emailService.sendEmail(emailRequest);
            log.debug("Order confirmation email sent to: {} for order: {}", userEmail, orderId);
        } catch (Exception e) {
            log.error("Error processing order placed event: {}", message, e);
            // Send to DLQ on failure
        }
    }
}
