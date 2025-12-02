package com.rudraksha.shopsphere.notification.service;

import com.rudraksha.shopsphere.notification.dto.SMSRequest;
import com.rudraksha.shopsphere.notification.service.impl.SMSServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class SMSServiceTest {

    @InjectMocks
    private SMSServiceImpl smsService;

    @Test
    void testSendSMS_Success() {
        SMSRequest request = SMSRequest.builder()
                .phoneNumber("+1234567890")
                .body("Your order has been confirmed")
                .build();

        assertDoesNotThrow(() -> smsService.sendSMS(request));
    }

    @Test
    void testSendBulkSMS_Success() {
        List<SMSRequest> requests = Arrays.asList(
                SMSRequest.builder().phoneNumber("+1234567890").body("Message 1").build(),
                SMSRequest.builder().phoneNumber("+0987654321").body("Message 2").build()
        );

        assertDoesNotThrow(() -> smsService.sendBulkSMS(requests));
    }

    @Test
    void testSendSMS_WithTemplate() {
        SMSRequest request = SMSRequest.builder()
                .phoneNumber("+1234567890")
                .templateName("order-confirmation")
                .build();

        assertDoesNotThrow(() -> smsService.sendSMS(request));
    }
}
