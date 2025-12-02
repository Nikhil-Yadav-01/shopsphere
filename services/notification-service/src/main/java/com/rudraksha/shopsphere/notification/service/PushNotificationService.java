package com.rudraksha.shopsphere.notification.service;

import com.rudraksha.shopsphere.notification.dto.PushNotificationRequest;

import java.util.List;

public interface PushNotificationService {

    /**
     * Sends push notification to a device
     */
    void sendPushNotification(PushNotificationRequest request);

    /**
     * Sends push notification to multiple devices
     */
    void sendBulkPushNotification(List<PushNotificationRequest> requests);

    /**
     * Registers a device token for a user
     */
    void registerDeviceToken(String userId, String deviceToken);

    /**
     * Unregisters a device token
     */
    void unregisterDeviceToken(String deviceToken);
}
