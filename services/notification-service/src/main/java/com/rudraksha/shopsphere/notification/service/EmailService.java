package com.rudraksha.shopsphere.notification.service;

import com.rudraksha.shopsphere.notification.dto.EmailRequest;

public interface EmailService {
    void sendEmail(EmailRequest request);
}
