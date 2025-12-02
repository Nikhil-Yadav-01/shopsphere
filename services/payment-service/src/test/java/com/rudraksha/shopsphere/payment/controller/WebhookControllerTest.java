package com.rudraksha.shopsphere.payment.controller;

import com.rudraksha.shopsphere.payment.service.PaymentService;
import com.rudraksha.shopsphere.payment.config.SecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for WebhookController.
 * Uses full Spring Boot context to test with actual Spring Security configuration.
 * Webhook endpoint is configured to allow anonymous access in SecurityConfig.
 * Uses Apache Kafka for payment event publishing.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Import(SecurityConfig.class)
class WebhookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private PaymentService paymentService;

    @Test
    void handleStripeWebhook_Success() throws Exception {
        // Arrange
        String payload = "{\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_test123\"}}}";
        String signature = "t=1234567890,v1=valid_signature";
        doNothing().when(paymentService).handleWebhookEvent(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Stripe-Signature", signature))
                .andExpect(status().isOk());

        verify(paymentService).handleWebhookEvent(payload, signature);
    }

    @Test
    void handleStripeWebhook_MissingSignature() throws Exception {
        // Arrange
        String payload = "{\"type\":\"payment_intent.succeeded\",\"data\":{\"object\":{\"id\":\"pi_test123\"}}}";
        doNothing().when(paymentService).handleWebhookEvent(anyString(), eq(null));

        // Act & Assert - Webhook accepts requests but validates signature in service layer
        mockMvc.perform(post("/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());

        verify(paymentService).handleWebhookEvent(payload, null);
    }

    @Test
    void handleStripeWebhook_EmptyPayload() throws Exception {
        // Arrange
        String payload = "";
        String signature = "t=1234567890,v1=valid_signature";
        doNothing().when(paymentService).handleWebhookEvent(anyString(), anyString());

        // Act & Assert
        mockMvc.perform(post("/webhooks/stripe")
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload)
                .header("Stripe-Signature", signature))
                .andExpect(status().isOk());

        verify(paymentService).handleWebhookEvent(payload, signature);
    }
}
