package com.rudraksha.shopsphere.notification.service.impl;

import com.rudraksha.shopsphere.notification.service.TemplateService;
import freemarker.template.Configuration;
import freemarker.template.Template;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.StringWriter;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TemplateServiceImpl implements TemplateService {

    private static final Logger log = LoggerFactory.getLogger(TemplateServiceImpl.class);
    private final Configuration freeMarkerConfig;

    @Override
    public String renderEmailTemplate(String templateName, Map<String, Object> variables) {
        log.debug("Rendering email template: {}", templateName);

        try {
            Template template = freeMarkerConfig.getTemplate("email/" + templateName + ".ftl");
            StringWriter writer = new StringWriter();
            template.process(variables, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Failed to render email template: {}", templateName, e);
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }

    @Override
    public String renderSMSTemplate(String templateName, Map<String, Object> variables) {
        log.debug("Rendering SMS template: {}", templateName);

        try {
            Template template = freeMarkerConfig.getTemplate("sms/" + templateName + ".ftl");
            StringWriter writer = new StringWriter();
            template.process(variables, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Failed to render SMS template: {}", templateName, e);
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }

    @Override
    public String renderPushTemplate(String templateName, Map<String, Object> variables) {
        log.debug("Rendering push notification template: {}", templateName);

        try {
            Template template = freeMarkerConfig.getTemplate("push/" + templateName + ".ftl");
            StringWriter writer = new StringWriter();
            template.process(variables, writer);
            return writer.toString();
        } catch (Exception e) {
            log.error("Failed to render push template: {}", templateName, e);
            throw new RuntimeException("Failed to render template: " + templateName, e);
        }
    }
}
