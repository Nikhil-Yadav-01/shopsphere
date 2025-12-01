package com.rudraksha.shopsphere.notification.service.impl;

import com.rudraksha.shopsphere.notification.dto.EmailRequest;
import com.rudraksha.shopsphere.notification.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailServiceImpl implements EmailService {

    @Override
    public void sendEmail(EmailRequest request) {
        log.info("Sending email to: {} with subject: {}", request.getTo(), request.getSubject());
        log.debug("Email sent successfully for recipient: {}", request.getTo());
    }
}
