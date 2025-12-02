package com.rudraksha.shopsphere.notification.service.impl;

import com.rudraksha.shopsphere.notification.dto.SMSRequest;
import com.rudraksha.shopsphere.notification.service.SMSService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SMSServiceImpl implements SMSService {

    @Value("${sms.provider:twilio}")
    private String smsProvider;

    @Override
    public void sendSMS(SMSRequest request) {
        log.info("Sending SMS to: {} via provider: {}", request.getPhoneNumber(), smsProvider);

        try {
            if ("twilio".equalsIgnoreCase(smsProvider)) {
                sendViatwilio(request);
            } else if ("sns".equalsIgnoreCase(smsProvider)) {
                sendViaSNS(request);
            } else {
                log.warn("Unknown SMS provider: {}", smsProvider);
            }
            log.debug("SMS sent successfully to: {}", request.getPhoneNumber());
        } catch (Exception e) {
            log.error("Failed to send SMS to: {}", request.getPhoneNumber(), e);
            throw new RuntimeException("Failed to send SMS", e);
        }
    }

    @Override
    public void sendBulkSMS(List<SMSRequest> requests) {
        log.info("Sending bulk SMS to {} recipients", requests.size());

        requests.forEach(this::sendSMS);

        log.debug("Bulk SMS sent to {} recipients", requests.size());
    }

    private void sendViatwilio(SMSRequest request) {
        // TODO: Implement Twilio SMS integration
        // Requires: twilio-java library
        // 1. Initialize Twilio client with account SID and auth token
        // 2. Create Message with from phone, to phone, body
        // 3. Log message SID for tracking
        log.info("Sending SMS via Twilio to: {}", request.getPhoneNumber());
    }

    private void sendViaSNS(SMSRequest request) {
        // TODO: Implement AWS SNS SMS integration
        // Requires: aws-java-sdk-sns
        // 1. Initialize SNS client
        // 2. Call publish API with phone number and message
        // 3. Log message ID for tracking
        log.info("Sending SMS via AWS SNS to: {}", request.getPhoneNumber());
    }
}
