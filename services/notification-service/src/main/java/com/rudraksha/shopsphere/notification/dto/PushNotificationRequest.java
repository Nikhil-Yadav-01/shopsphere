package com.rudraksha.shopsphere.notification.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PushNotificationRequest {

    @NotBlank(message = "Device token is required")
    private String deviceToken;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Message body is required")
    private String body;

    private String templateName;

    private Map<String, Object> templateVariables;

    private Map<String, String> customData;
}
