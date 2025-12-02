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
 * Listens to ReturnApproved events and sends return approval email
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class ReturnApprovedConsumer {

    private final EmailService emailService;
    private final TemplateService templateService;
    private final ObjectMapper objectMapper;

    private static final String TOPIC = "return.approved";
    private static final String GROUP_ID = "notification-service-return-approved-group";

    @KafkaListener(topics = TOPIC, groupId = GROUP_ID)
    public void consumeReturnApproved(String message) {
        log.info("Received return approved event");

        try {
            Map<String, Object> eventData = objectMapper.readValue(message, Map.class);
            String rmaNumber = (String) eventData.get("rmaNumber");
            String userEmail = (String) eventData.get("userEmail");
            String orderId = (String) eventData.get("orderId");

            if (userEmail == null) {
                log.warn("User email not found in return approved event for RMA: {}", rmaNumber);
                return;
            }

            // Prepare template variables
            Map<String, Object> variables = new HashMap<>();
            variables.put("rmaNumber", rmaNumber);
            variables.put("orderId", orderId);
            variables.put("refundAmount", eventData.get("refundAmount"));
            variables.put("approvalDate", eventData.get("approvedAt"));

            // Render template
            String htmlContent = templateService.renderEmailTemplate("return-approved", variables);

            // Send email
            EmailRequest emailRequest = EmailRequest.builder()
                    .to(userEmail)
                    .subject("Return Approved - RMA " + rmaNumber)
                    .body(htmlContent)
                    .templateName("return-approved")
                    .build();

            emailService.sendEmail(emailRequest);
            log.debug("Return approved email sent to: {} for RMA: {}", userEmail, rmaNumber);
        } catch (Exception e) {
            log.error("Error processing return approved event: {}", message, e);
            // Send to DLQ on failure
        }
    }
}
