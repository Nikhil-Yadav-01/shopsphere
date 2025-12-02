package com.rudraksha.shopsphere.notification.service;

import java.util.Map;

public interface TemplateService {

    /**
     * Renders an email template with variables
     */
    String renderEmailTemplate(String templateName, Map<String, Object> variables);

    /**
     * Renders an SMS template with variables
     */
    String renderSMSTemplate(String templateName, Map<String, Object> variables);

    /**
     * Renders a push notification template with variables
     */
    String renderPushTemplate(String templateName, Map<String, Object> variables);
}
