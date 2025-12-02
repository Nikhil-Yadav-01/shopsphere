package com.rudraksha.shopsphere.notification.dto;

import lombok.Builder;

@Builder
public class EmailRequest {
    private String to;
    private String subject;
    private String body;
    private String templateName;

    public EmailRequest() {}

    public EmailRequest(String to, String subject, String body, String templateName) {
        this.to = to;
        this.subject = subject;
        this.body = body;
        this.templateName = templateName;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }
}
