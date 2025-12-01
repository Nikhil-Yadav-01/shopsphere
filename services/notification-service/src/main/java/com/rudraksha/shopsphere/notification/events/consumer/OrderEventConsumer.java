package com.rudraksha.shopsphere.notification.events.consumer;

import com.rudraksha.shopsphere.notification.dto.EmailRequest;
import com.rudraksha.shopsphere.notification.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class OrderEventConsumer {

    private final EmailService emailService;

    public void handleOrderPlaced(String orderId, String userEmail) {
        log.info("Received order placed event for order: {}", orderId);
        
        EmailRequest emailRequest = EmailRequest.builder()
                .to(userEmail)
                .subject("Order Confirmation - " + orderId)
                .body("Your order has been placed successfully. Order ID: " + orderId)
                .build();
        
        emailService.sendEmail(emailRequest);
    }
}
