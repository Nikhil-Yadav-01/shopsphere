package com.rudraksha.shopsphere.notification.service;

import com.rudraksha.shopsphere.notification.dto.SMSRequest;

public interface SMSService {

    /**
     * Sends SMS message to a phone number
     */
    void sendSMS(SMSRequest request);

    /**
     * Sends SMS to multiple recipients
     */
    void sendBulkSMS(java.util.List<SMSRequest> requests);
}
