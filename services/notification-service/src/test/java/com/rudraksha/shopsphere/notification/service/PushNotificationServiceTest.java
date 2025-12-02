package com.rudraksha.shopsphere.notification.service;

import com.rudraksha.shopsphere.notification.dto.PushNotificationRequest;
import com.rudraksha.shopsphere.notification.service.impl.PushNotificationServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@ExtendWith(MockitoExtension.class)
class PushNotificationServiceTest {

    @InjectMocks
    private PushNotificationServiceImpl pushNotificationService;

    @Test
    void testSendPushNotification_Success() {
        PushNotificationRequest request = PushNotificationRequest.builder()
                .deviceToken("device-token-123")
                .title("Order Confirmation")
                .body("Your order has been confirmed")
                .build();

        assertDoesNotThrow(() -> pushNotificationService.sendPushNotification(request));
    }

    @Test
    void testSendBulkPushNotification_Success() {
        List<PushNotificationRequest> requests = Arrays.asList(
                PushNotificationRequest.builder()
                        .deviceToken("device-token-1")
                        .title("Order Status")
                        .body("Your order has been shipped")
                        .build(),
                PushNotificationRequest.builder()
                        .deviceToken("device-token-2")
                        .title("Order Status")
                        .body("Your order has been shipped")
                        .build()
        );

        assertDoesNotThrow(() -> pushNotificationService.sendBulkPushNotification(requests));
    }

    @Test
    void testRegisterDeviceToken_Success() {
        assertDoesNotThrow(() -> pushNotificationService.registerDeviceToken("user-123", "device-token-xyz"));
    }

    @Test
    void testUnregisterDeviceToken_Success() {
        assertDoesNotThrow(() -> pushNotificationService.unregisterDeviceToken("device-token-xyz"));
    }

    @Test
    void testSendPushNotification_WithCustomData() {
        Map<String, String> customData = new HashMap<>();
        customData.put("orderId", "ORD-12345");
        customData.put("link", "/orders/ORD-12345");

        PushNotificationRequest request = PushNotificationRequest.builder()
                .deviceToken("device-token-123")
                .title("Order Update")
                .body("Your order has been updated")
                .customData(customData)
                .build();

        assertDoesNotThrow(() -> pushNotificationService.sendPushNotification(request));
    }
}
