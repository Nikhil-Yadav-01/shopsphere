package com.rudraksha.shopsphere.notification.service;

import com.rudraksha.shopsphere.notification.dto.EmailRequest;
import com.rudraksha.shopsphere.notification.service.impl.EmailServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @InjectMocks
    private EmailServiceImpl emailService;

    @Test
    void testSendEmail_Success() {
        EmailRequest request = EmailRequest.builder()
                .to("customer@example.com")
                .subject("Order Confirmation")
                .body("<html><body>Your order confirmed</body></html>")
                .build();

        assertDoesNotThrow(() -> emailService.sendEmail(request));
    }

    @Test
    void testSendEmail_WithTemplate() {
        EmailRequest request = EmailRequest.builder()
                .to("customer@example.com")
                .subject("Order Confirmation")
                .body("Your order confirmed")
                .templateName("order-confirmation")
                .build();

        assertDoesNotThrow(() -> emailService.sendEmail(request));
    }
}
