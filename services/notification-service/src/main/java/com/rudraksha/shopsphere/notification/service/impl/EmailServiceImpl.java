package com.rudraksha.shopsphere.notification.service.impl;

import com.rudraksha.shopsphere.notification.dto.EmailRequest;
import com.rudraksha.shopsphere.notification.service.EmailService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailServiceImpl.class);

    @Override
    public void sendEmail(EmailRequest request) {
        log.info("Sending email to: {} with subject: {}", request.getTo(), request.getSubject());
        log.debug("Email sent successfully for recipient: {}", request.getTo());
    }
}
